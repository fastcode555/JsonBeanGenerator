package com.awesome.plugins.assetgenerate

import com.awesome.common.PluginProps
import com.awesome.plugins.assetgenerate.generator.FlutterAssetGenerator
import com.awesome.plugins.assetgenerate.generator.WebAssetGenerator
import com.awesome.utils.PropertiesHelper
import com.awesome.utils.runWriteCmd
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import java.io.File

class AssetGenerateAction : AnAction() {
    override fun update(e: AnActionEvent) {
        super.update(e)
        val mDirectory = e.getData<PsiElement>(CommonDataKeys.PSI_ELEMENT)
        e.presentation.isEnabledAndVisible = mDirectory != null && mDirectory is PsiDirectory
    }

    override fun actionPerformed(e: AnActionEvent) {
        val mDirectory = e.getData<PsiElement>(CommonDataKeys.PSI_ELEMENT)
        val properties = mDirectory?.let { PropertiesHelper(it) }
        val ignoreDirs = properties?.getProperty(PluginProps.assetsIgnoreDirs)
        val targetDir = properties?.getProperty(PluginProps.generateAssetDirs)
        if (mDirectory != null && mDirectory is PsiDirectory) {
            mDirectory.runWriteCmd {
                val fileContent = File(mDirectory.project.projectFile?.path).readText()
                if (fileContent.contains("\"io.flutter\"")) {
                    FlutterAssetGenerator(mDirectory, ignoreDirs, targetDir).generate()
                } else if (isWeb(fileContent, mDirectory)) {
                    WebAssetGenerator(mDirectory, ignoreDirs, targetDir).generate()
                } else {
                    FlutterAssetGenerator(mDirectory, ignoreDirs, targetDir).generate()
                }
            }
        }
    }

    fun isWeb(fileContent: String, mDirectory: PsiDirectory): Boolean {
        return fileContent.contains("\"web\"") || File(mDirectory.project.basePath, "package.json").exists()
    }
}
