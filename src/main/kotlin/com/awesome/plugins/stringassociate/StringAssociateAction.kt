package com.awesome.plugins.stringassociate

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Editor

/**
 * 字符串联想，给出相应的提示
 **/
class StringAssociateAction : AnAction() {

    override fun update(e: AnActionEvent) {
        super.update(e)
        val editor: Editor? = e.getData(CommonDataKeys.EDITOR)
        val isNullOrEmpty = editor?.selectionModel?.selectedText.isNullOrEmpty()
        e.presentation.setEnabledAndVisible(!isNullOrEmpty)
    }

    override fun actionPerformed(e: AnActionEvent) {
        val editor: Editor? = e.getData(CommonDataKeys.EDITOR)
        editor?.apply {
            val dialog = AssociateDialog(editor)
            dialog.showDialog()
        }
    }
}
