package com.awesome.plugins.assetgenerate

import clearSymbol
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.jetbrains.annotations.NotNull
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
                builder.append("class R {\n")
                generateAssetDartFile(
                    mDirectory,
                    builder,
                    mDirectory.parentDirectory?.virtualFile?.path
                )
                builder.append("}")
                mDirectory.parent?.virtualFile?.path?.let {
                    val genDartFilePath = "${it}${File.separator}lib${File.separator}gen${File.separator}r.dart"
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
    private fun generateAssetDartFile(mDirectory: PsiDirectory, builder: StringBuilder, rootPath: @NotNull String?) {
        if (isContainFile(mDirectory)) {
            builder.append("\t///------------------------ ${mDirectory.name} ------------------------\n")
        }

        mDirectory.files.map {
            val assetName = it.virtualFile.path.replace("${rootPath!!}/", "")
            if (isFont(it)) {
                builder.append(
                    "\tstatic const String ${
                        it.virtualFile.nameWithoutExtension.clearSymbol().toCamel()
                    } = '${it.virtualFile.nameWithoutExtension}';\n"
                )
            } else {
                builder.append(
                    "\tstatic const String ${
                        it.virtualFile.nameWithoutExtension.clearSymbol().toCamel()
                    } = '$assetName';\n"
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