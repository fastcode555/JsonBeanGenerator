package com.awesome.plugins.assetgenerate.generator

import clearSymbol
import com.awesome.utils.basePath
import com.awesome.utils.moduleName
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import toCamel
import java.io.File
import java.lang.StringBuilder

/***
 * 生成flutter 工程的资源索引
 **/
private val FONT_TYPES: Array<String> = arrayOf("ttf", "otf", "woff", "eot")

class FlutterAssetGenerator(
    private val mDirectory: PsiDirectory,
    private val ignoreDirs: String?,
    private val targetDir: String?
) :
    BaseAssetGenerator(mDirectory, ignoreDirs, targetDir) {

    // 存储已定义的资源映射关系
    private val existingAssetMap = mutableMapOf<String, String>()

    override fun generate() {
        // 读取现有的资源定义
        loadExistingAssets()
        
        val builder = StringBuilder()
        val basePath = mDirectory.basePath()
        val isChildProject = basePath != mDirectory.project.basePath
        builder.append("class R {\n")

        val dirName = mDirectory.virtualFile.name
        val rootVariableName = "_${dirName.clearSymbol().toCamel()}"
        if (isChildProject) {
            builder.append("  static const String $rootVariableName = 'packages/${mDirectory.moduleName()}/$dirName';\n")
        } else {
            builder.append("  static const String $rootVariableName = '$dirName';\n")
        }
        generateAssetDartFile(
            mDirectory,
            builder,
            mDirectory.virtualFile.path,
        )
        builder.append("}")

        mDirectory.parent?.virtualFile?.path?.let {
            val newTargetDir =
                if (!targetDir.isNullOrEmpty()) targetDir else "${File.separator}lib${File.separator}res"
            val genDartFilePath = "${it}$newTargetDir${File.separator}r.dart".replace("//", "/")
            val file = File(genDartFilePath)
            //生成相关的dart.文件
            if (!file.exists()) {
                if (!file.parentFile.exists()) {
                    file.parentFile.mkdirs()
                }
                file.createNewFile()
            }
            file.writeText(builder.toString().trim())
        }
    }

    ///生成所在工程的资源文件索引
    private fun generateAssetDartFile(
        mDirectory: PsiDirectory,
        builder: StringBuilder,
        rootPath: String?,
    ) {

        val dirVariableName = "_${mDirectory.name.clearSymbol().toCamel()}"
        val isRoot = mDirectory.virtualFile.path.replace(rootPath!!, "").trim().isEmpty()
        val parentVariableName = "\$_${mDirectory.parent?.name.clearSymbol().toCamel()}"
        val currentDirVariableName = "\$_${mDirectory.name.clearSymbol().toCamel()}"

        if (isContainFile(mDirectory)) {
            if (ignoreDirs?.contains(mDirectory.name) == true) {
                return
            }
            builder.append("\n  ///------------------------ ${mDirectory.name} ------------------------\n")
            if (!isRoot) {
                builder.append("  static const String $dirVariableName = '${parentVariableName}${File.separator}${mDirectory.name}';\n")
            }

        }

        mDirectory.files.map {
            if (isFont(it)) {
                // 字体文件使用已存在的名称或生成新的名称
                val fontName = it.virtualFile.nameWithoutExtension
                val variableName = existingAssetMap[fontName] 
                    ?: fontName.clearSymbol().toCamel()
                
                builder.append(
                    "  static const String $variableName = '$fontName';\n"
                )
            } else {
                val assetName = it.virtualFile.name
                val assetPath = "$currentDirVariableName${File.separator}$assetName"
                
                // 使用已存在的名称或生成新的名称
                val variableName = existingAssetMap[assetPath] 
                    ?: it.virtualFile.nameWithoutExtension.clearSymbol().toCamel()
                
                builder.append(
                    "  static const String $variableName = '$assetPath';\n"
                )
            }
        }
        mDirectory.subdirectories.map {
            generateAssetDartFile(it, builder, rootPath)
        }
    }

    private fun isFont(it: PsiFile): Boolean {
        return FONT_TYPES.any { type ->
            it.virtualFile.name.endsWith(type, ignoreCase = true)
        }
    }

    //判断文件夹下是否有文件
    private fun isContainFile(mDirectory: PsiDirectory): Boolean {
        return mDirectory.files.isNotEmpty() || mDirectory.subdirectories.isNotEmpty()
    }

    private fun loadExistingAssets() {
        val rDartFile = File(getRDartFilePath())
        if (!rDartFile.exists()) return

        // 匹配资源定义语句
        val pattern = "static const String ([a-zA-Z0-9_]+) = '(.*?)'"
        val regex = Regex(pattern)
        
        rDartFile.readText().lines().forEach { line ->
            regex.find(line)?.let { result ->
                val name = result.groupValues[1]
                val path = result.groupValues[2]
                
                // 如果值不包含路径分隔符,说明可能是字体文件
                if (!path.contains(File.separator)) {
                    existingAssetMap[path] = name
                }
                existingAssetMap[path] = name
            }
        }
    }

    private fun getRDartFilePath(): String {
        val newTargetDir = if (!targetDir.isNullOrEmpty()) {
            targetDir 
        } else {
            "${File.separator}lib${File.separator}res"
        }
        return "${mDirectory.parent?.virtualFile?.path}$newTargetDir${File.separator}r.dart"
    }
}
