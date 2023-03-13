package com.awesome.plugins.assetgenerate

import clearSymbol
import com.awesome.utils.basePath
import com.awesome.utils.moduleName
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import toCamel
import java.io.File
import java.lang.StringBuilder

/***
 * 生成flutter 工程的资源索引
 **/
private val FONT_TYPES: Array<String> = arrayOf("ttf", "otf", "woff", "eot")

class AssetGenerateAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val mDirectory = e.getData<PsiElement>(CommonDataKeys.PSI_ELEMENT)
        if (mDirectory != null && mDirectory is PsiDirectory) {
            WriteCommandAction.runWriteCommandAction(mDirectory.project) {
                val builder = StringBuilder()
                val basePath = mDirectory.basePath()
                val isChildProject = basePath != mDirectory.project.basePath
                builder.append("class R {\n")

                val dirName = mDirectory.virtualFile.name
                val rootVariableName = "_${dirName.clearSymbol().toCamel()}"
                if (isChildProject) {
                    builder.append("  static const String $rootVariableName = \"packages/${mDirectory.moduleName()}/$dirName\";\n")
                } else {
                    builder.append("  static const String $rootVariableName = \"$dirName\";\n")
                }
                generateAssetDartFile(
                    mDirectory,
                    builder,
                    mDirectory.virtualFile.path,
                )
                builder.append("}")
                mDirectory.parent?.virtualFile?.path?.let {
                    val genDartFilePath = "${it}${File.separator}lib${File.separator}res${File.separator}r.dart"
                    val file = File(genDartFilePath)
                    //生成相关的dart.文件
                    if (!file.exists()) {
                        if (!file.parentFile.exists()) {
                            file.parentFile.mkdirs()
                        }
                        file.createNewFile()
                    }
                    file.writeText(builder.toString())
                }

            }
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
            builder.append("\n\t///------------------------ ${mDirectory.name} ------------------------\n")
            if (!isRoot) {
                builder.append("\tstatic const String $dirVariableName = '${parentVariableName}${File.separator}${mDirectory.name}';\n")
            }

        }

        mDirectory.files.map {
            val assetName = it.virtualFile.name
            if (isFont(it)) {
                builder.append(
                    "\tstatic const String ${
                        it.virtualFile.nameWithoutExtension.clearSymbol().toCamel()
                    } = '$currentDirVariableName${File.separator}${it.virtualFile.nameWithoutExtension}';\n"
                )
            } else {
                builder.append(
                    "\tstatic const String ${
                        it.virtualFile.nameWithoutExtension.clearSymbol().toCamel()
                    } = '$currentDirVariableName${File.separator}$assetName';\n"
                )
            }
        }
        mDirectory.subdirectories.map {
            generateAssetDartFile(it, builder, rootPath)
        }
    }

    private fun isFont(it: PsiFile): Boolean {
        for (type in FONT_TYPES) {
            if (it.virtualFile.name.endsWith(type)) {
                return true;
            }
        }
        return false
    }

    //判断文件夹下是否有文件
    private fun isContainFile(mDirectory: PsiDirectory): Boolean {
        return mDirectory.files.isNotEmpty()
    }
}