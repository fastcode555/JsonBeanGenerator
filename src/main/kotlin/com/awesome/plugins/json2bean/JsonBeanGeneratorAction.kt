package com.awesome.plugins.json2bean

import com.awesome.JsonBeanDialog
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement

class JsonBeanGeneratorAction : AnAction() {

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    override fun update(e: AnActionEvent) {
        super.update(e)
        val mDirectory = e.getData<PsiElement>(CommonDataKeys.PSI_ELEMENT)
        e.presentation.isEnabledAndVisible = mDirectory != null && mDirectory is PsiDirectory
    }

    override fun actionPerformed(e: AnActionEvent) {
        val mDirectory = e.getData<PsiElement>(CommonDataKeys.PSI_ELEMENT)
        if (mDirectory != null && mDirectory is PsiDirectory) {
            val mDialog = JsonBeanDialog(mDirectory)
            mDialog.showDialog()
        }
    }
}
