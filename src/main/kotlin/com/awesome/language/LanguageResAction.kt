package com.awesome.language

import com.awesome.LanguageResDialog
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
//https://www.w3schools.com/tags/ref_language_codes.asp
class LanguageResAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val mDirectory = e.getData<PsiElement>(CommonDataKeys.PSI_ELEMENT)
        if (mDirectory != null && mDirectory is PsiDirectory) {
            val mDialog = LanguageResDialog()
            mDialog.showDialog()
        }
    }
}