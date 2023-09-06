package com.awesome.plugins.assetgenerate

import com.awesome.plugins.assetgenerate.generator.FlutterAssetGenerator
import com.awesome.plugins.assetgenerate.generator.WebAssetGenerator
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
                    FlutterAssetGenerator(mDirectory, ignoreDirs, targetDir).generate()
                } else if (isWeb(fileContent, mDirectory)) {
                    WebAssetGenerator(mDirectory, ignoreDirs, targetDir).generate()
                } else {

                }
            }
        }
    }

    fun isWeb(fileContent: String, mDirectory: PsiDirectory): Boolean {
        return fileContent.contains("\"web\"") || File(mDirectory.project.basePath, "package.json").exists()
    }


}
