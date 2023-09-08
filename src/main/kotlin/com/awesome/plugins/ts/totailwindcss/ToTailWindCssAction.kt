package com.awesome.plugins.ts.totailwindcss

import com.awesome.common.BaseAnAction
import com.awesome.plugins.ts.totailwindcss.processor.ColorProcessor
import com.awesome.plugins.ts.totailwindcss.processor.ModifyProcessor
import com.awesome.plugins.ts.totailwindcss.processor.TailConfigProcessor
import com.awesome.plugins.ts.totailwindcss.processor.TailWindProcessor
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor

/**
 * 将复制到的css代码转换成 tailwindcss代码
 **/
class ToTailWindCssAction : BaseAnAction() {

    override fun fileType(): ArrayList<String> = arrayListOf("vue", "css", "tsx", "vue", "js", "ts", "jsx")

    override fun actionPerformed(e: AnActionEvent) {
        val editor: Editor? = e.getData(CommonDataKeys.EDITOR)
        editor?.apply {
            val processors = listOf(
                TailWindProcessor(this),
                ModifyProcessor(this),
                TailConfigProcessor(this),
                ColorProcessor(this),
            )

            val selectText = selectionModel.selectedText
            if (!selectText.isNullOrEmpty()) {
                WriteCommandAction.runWriteCommandAction(editor.project) {
                    var content = ""
                    processors.forEach { content = it.process(content) }
                    //替换掉css的代码
                    if (content.isNotEmpty()) {
                        content = "@apply $content"
                    }
                    document.replaceString(selectionModel.selectionStart, selectionModel.selectionEnd, content)
                }
            } else {
                val dialog = TailWindCssDialog(this, processors)
                dialog.showDialog()

            }
        }
    }


}
