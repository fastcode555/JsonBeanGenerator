package com.awesome.plugins.ts.tomapfield

import com.awesome.common.BaseAnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Editor

/**
 * 将json转换为TypeScript可识别的字典型变量
 **/
class ToMapFieldAction : BaseAnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val editor: Editor? = e.getData(CommonDataKeys.EDITOR)
        val dialog = TopMapFieldDialog(editor)
        dialog.showDialog()
    }

    override fun fileType(): ArrayList<String> = arrayListOf("vue", "ts")
}
