package com.awesome.plugins.language

import com.awesome.LanguageResDialog
import com.awesome.common.BaseAnAction
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import org.apache.http.util.TextUtils

//https://www.w3schools.com/tags/ref_language_codes.asp
class LanguageResAction : BaseAnAction() {
    override fun fileType(): ArrayList<String> = arrayListOf("dart")
    override fun actionPerformed(e: AnActionEvent) {
        val mDirectory = e.getData(CommonDataKeys.PSI_ELEMENT)
        if (mDirectory != null && mDirectory is PsiDirectory) {
            val mDialog = LanguageResDialog(mDirectory, "", null)
            mDialog.showDialog()
        } else {
            val editor: Editor? = e.getData(CommonDataKeys.EDITOR)
            val psiFile: PsiFile? = e.getData(CommonDataKeys.PSI_FILE)
            val selectionModel = editor?.selectionModel
            var value = selectionModel?.selectedText
            if (TextUtils.isEmpty(selectionModel?.selectedText)) {
                val mDialog = LanguageObfuscateDialog(editor, psiFile)
                mDialog.showDialog()
                return
            }
            if (value!!.startsWith("'") || value.startsWith("\"")) {
                value = value.substring(1, value.length)
            }
            if (value.endsWith("'") || value.endsWith("\"")) {
                value = value.substring(0, value.length - 1)
            }
            val mDialog = LanguageResDialog(psiFile!!, value, selectionModel)
            mDialog.showDialog()
        }
    }
}
