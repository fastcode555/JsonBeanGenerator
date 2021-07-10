package com.awesome

import com.awesome.utils.NotifyUtils
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import org.jetbrains.annotations.NotNull
import java.io.File
import java.lang.StringBuilder

/***
 * 生成flutter 工程的资源索引
 **/
class AssetGenerateAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val mDirectory = e.getData<PsiElement>(CommonDataKeys.PSI_ELEMENT)
        if (mDirectory != null && mDirectory is PsiDirectory) {
            WriteCommandAction.runWriteCommandAction(mDirectory.project) {
                val builder = StringBuilder()
                builder.append("class R {\n")
                generateAssetDartFile(
                    File(mDirectory.virtualFile.path),
                    builder,
                    mDirectory?.parentDirectory?.virtualFile?.path
                )
                builder.append("}")
                mDirectory.parent?.virtualFile?.path?.let {
                    val genDartFilePath = "${it}/lib/gen/r.dart"
                    val file = File(genDartFilePath)
                    //生成相关的dart.文件
                    if (!file.exists()) {
                        if (!file.parentFile.exists()) {
                            file.parentFile.mkdirs()
                        }
                        file.createNewFile()
                    }
                    file.writeText(builder.toString())
                    NotifyUtils.showInfo(mDirectory.project, "代码执行完成")
                }

            }
        }
    }

    ///生成所在工程的资源文件索引
    private fun generateAssetDartFile(mDirectory: File, builder: StringBuilder, rootPath: @NotNull String?) {
        if (isContainFile(mDirectory)) {
            builder.append("\t///------------------------ ${mDirectory.name} ------------------------\n")
        }

        mDirectory.listFiles().map {
            if (it.isDirectory) {
                generateAssetDartFile(it, builder, rootPath)
            } else {
                val assetName = it.absolutePath.replace("${rootPath!!}/", "")
                builder.append("\tstatic const String ${it.nameWithoutExtension} = '$assetName';\n")
            }
        }
    }

    //判断文件夹下是否有文件
    private fun isContainFile(mDirectory: File): Boolean {
        for (file in mDirectory.listFiles()) {
            if (file.isFile) {
                return true
            }
        }
        return false
    }
}