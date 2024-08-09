package com.awesome.plugins.random

import ImageUtil
import com.awesome.plugins.codestyle.CodeStyleGeneratorDialog
import com.awesome.plugins.codestyle.interceptor.StrategyManager
import com.awesome.utils.runWriteCmd
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile

class RandomStringAction : AnAction() {

    override fun update(e: AnActionEvent) {
        super.update(e)
        val editor: Editor? = e.getData(CommonDataKeys.EDITOR)
        val text = editor?.selectionModel?.selectedText
        try {
            e.presentation.isEnabledAndVisible = text!!.isEmpty()
        } catch (e: Exception) {
            println(e)
        }
    }

    override fun actionPerformed(e: AnActionEvent) {
        val editor: Editor? = e.getData(CommonDataKeys.EDITOR)
        val psiFile: PsiFile? = e.getData(CommonDataKeys.PSI_FILE)
        editor?.apply {
            val strategy = StrategyManager(psiFile!!, editor)
            val selectText = selectionModel.selectedText
            if (selectText.isNullOrEmpty()) {
                editor.runWriteCmd {
                    document.insertString(selectionModel.selectionStart, ImageUtil.getRandomImage())
                }
            } else {
                val dialog = CodeStyleGeneratorDialog(strategy)

                dialog.showDialog()
            }
        }
    }
}
