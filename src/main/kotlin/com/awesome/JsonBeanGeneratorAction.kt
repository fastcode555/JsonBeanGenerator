package com.awesome

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement

class JsonBeanGeneratorAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val mDirectory = e.getData<PsiElement>(CommonDataKeys.PSI_ELEMENT)
        if (mDirectory != null && mDirectory is PsiDirectory) {
            WriteCommandAction.runWriteCommandAction(mDirectory.project){
                val mDialog = JsonBeanDialog(mDirectory)
                mDialog.showDialog()
            }
        }
    }

}