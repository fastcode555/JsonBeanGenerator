package com.awesome.plugins.language

import com.awesome.LanguageResDialog
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.apache.http.util.TextUtils


//https://www.w3schools.com/tags/ref_language_codes.asp
class LanguageResAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val mDirectory = e.getData<PsiElement>(CommonDataKeys.PSI_ELEMENT)
        if (mDirectory != null && mDirectory is PsiDirectory) {
            val mDialog = LanguageResDialog(mDirectory, "")
            mDialog.showDialog()
        } else {
            val editor: Editor? = e.getData(CommonDataKeys.EDITOR)
            val psiFile: PsiFile? = e.getData(CommonDataKeys.PSI_FILE)
            val selectionModel = editor?.selectionModel
            var value = selectionModel?.selectedText
            if (TextUtils.isEmpty(selectionModel?.selectedText)) {
                return
            }
            if (value!!.startsWith("'") || value.startsWith("\"")) {
                value = value.substring(1, value.length)
            }
            if (value.endsWith("'") || value.endsWith("\"")) {
                value = value.substring(0, value.length - 1)
            }
            val mDialog = LanguageResDialog(psiFile!!, value)
            mDialog.showDialog()
        }
    }
}