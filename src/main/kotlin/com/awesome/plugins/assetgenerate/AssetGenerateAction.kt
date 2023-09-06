package com.awesome.plugins.assetgenerate

import com.awesome.plugins.assetgenerate.generator.DartAssetGenerator
import com.awesome.utils.PropertiesHelper
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import java.io.File

class AssetGenerateAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val mDirectory = e.getData<PsiElement>(CommonDataKeys.PSI_ELEMENT)
        val properties = mDirectory?.let { PropertiesHelper(it) }
        val ignoreDirs = properties?.getProperty("plugin.assetsIgnoreDirs")
        val targetDir = properties?.getProperty("plugin.generateAssetDirs")
        if (mDirectory != null && mDirectory is PsiDirectory) {
            WriteCommandAction.runWriteCommandAction(mDirectory.project) {
                val fileContent = File(mDirectory.project.projectFile?.path).readText()
                if (fileContent.contains("\"io.flutter\"")) {
                    DartAssetGenerator(mDirectory, ignoreDirs, targetDir).generate()
                } else if (fileContent.contains("\"web\"")) {
                    DartAssetGenerator(mDirectory, ignoreDirs, targetDir).generate()
                }
            }
        }
    }


}
