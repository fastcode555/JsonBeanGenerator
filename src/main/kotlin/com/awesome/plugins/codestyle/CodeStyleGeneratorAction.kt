package com.awesome.plugins.codestyle

import com.awesome.common.BaseAnAction
import com.awesome.plugins.codestyle.interceptor.TailWindProcessor
import com.awesome.utils.runWriteCmd
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Editor

/**
 * 将复制到的css代码转换成 tailwindcss代码
 **/
class CodeStyleGeneratorAction : BaseAnAction() {

    override fun fileType(): ArrayList<String> = arrayListOf("vue", "css", "tsx", "vue", "js", "ts", "jsx")

    override fun actionPerformed(e: AnActionEvent) {
        val editor: Editor? = e.getData(CommonDataKeys.EDITOR)
        editor?.apply {
            val selectText = selectionModel.selectedText
            if (!selectText.isNullOrEmpty()) {
                editor.runWriteCmd {
                    val content = TailWindProcessor(editor).process(selectText)
                    editor.apply {
                        document.replaceString(selectionModel.selectionStart, selectionModel.selectionEnd, content)
                    }
                }
            } else {
                val dialog = CodeStyleGeneratorDialog(this)
                dialog.showDialog()
            }
        }
    }


}
