package com.awesome.plugins.stringassociate

import com.awesome.plugins.stringassociate.processor.StringHelper
import com.awesome.utils.replaceSelect
import com.awesome.utils.runWriteCmd
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Editor

/**
 * 字符串联想，给出相应的提示
 **/
val colorRegex = Regex("rgb(a){0,1}\\(.*?\\)")

class StringAssociateAction : AnAction() {

    override fun update(e: AnActionEvent) {
        super.update(e)
        val editor: Editor? = e.getData(CommonDataKeys.EDITOR)
        val isNullOrEmpty = editor?.selectionModel?.selectedText.isNullOrEmpty()
        e.presentation.setEnabledAndVisible(!isNullOrEmpty)
    }

    override fun actionPerformed(e: AnActionEvent) {
        val editor: Editor? = e.getData(CommonDataKeys.EDITOR)
        val content = editor?.selectionModel?.selectedText ?: ""

        editor?.runWriteCmd {
            //匹配是否是字符串
            if (colorRegex.matches(content)) {
                editor.replaceSelect(StringHelper.toHexColor(content))
            } else {
                val dialog = AssociateDialog(editor)
                dialog.showDialog()
            }
        }

    }
}
