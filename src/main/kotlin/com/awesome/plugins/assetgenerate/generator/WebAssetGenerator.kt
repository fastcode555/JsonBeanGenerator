package com.awesome.plugins.assetgenerate.generator

import clearSymbol
import com.intellij.psi.PsiDirectory
import toCamel
import java.io.File
import java.lang.StringBuilder

class WebAssetGenerator(
    private val mDirectory: PsiDirectory,
    private val ignoreDirs: String?,
    private val targetDir: String?
) :
    BaseAssetGenerator(mDirectory, ignoreDirs, targetDir) {
    override fun generate() {
        val builder = StringBuilder()
        val arraylists = arrayListOf<String>()
        generateAssetDartFile(mDirectory, builder, mDirectory.virtualFile.path, arraylists)
        mDirectory.parent?.virtualFile?.path?.let {
            val newTargetDir =
                if (targetDir != null && targetDir.trim().isNotEmpty()) targetDir else "${File.separator}res"
            val genDartFilePath = "${it}$newTargetDir${File.separator}r.ts".replace("//", "/")
            val file = File(genDartFilePath)
            //生成相关的dart.文件
            if (!file.exists()) {
                if (!file.parentFile.exists()) {
                    file.parentFile.mkdirs()
                }
                file.createNewFile()
            }
            builder.append("// Resource\n")
            builder.append("export const R = { ")
            builder.append(arraylists.joinToString(", "))
            builder.append(" }")
            file.writeText(builder.toString().trim())
        }
    }

    ///生成所在工程的资源文件索引
    private fun generateAssetDartFile(
        mDirectory: PsiDirectory,
        builder: StringBuilder,
        rootPath: String?,
        arraylists: ArrayList<String>,
    ) {
        if (isContainFile(mDirectory)) {
            if (ignoreDirs?.contains(mDirectory.name) == true) {
                return
            }
            builder.append("\n// ------------------------ ${mDirectory.name} ------------------------\n")
        }
        mDirectory.files.map {
            var fileName = it.virtualFile.nameWithoutExtension.clearSymbol().toCamel().newName(arraylists)
            arraylists.add(fileName)
            val path = it.virtualFile.path.replace(it.project.basePath!!, "").replace("/src/", "/")
            builder.append("import $fileName from '@$path'\n")
        }
        mDirectory.subdirectories.map { generateAssetDartFile(it, builder, rootPath, arraylists) }
    }

    private fun String.newName(arraylists: List<String>): String {
        if (arraylists.contains(this)) {
            val number = this.last().toString().toIntOrNull()
            if (number == null) {
                return "${this}2".newName(arraylists)
            } else {
                return "${this}${number + 1}".newName(arraylists)
            }
        }
        return this
    }

    //判断文件夹下是否有文件
    private fun isContainFile(mDirectory: PsiDirectory): Boolean {
        return mDirectory.files.isNotEmpty()
    }
}
