package com.awesome.plugins.codestyle

import com.awesome.common.BaseAnAction
import com.awesome.plugins.codestyle.interceptor.StrategyManager
import com.awesome.utils.runWriteCmd
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile

/**
 * 将复制到的css代码转换成 tailwindcss代码
 **/
class CodeStyleGeneratorAction : BaseAnAction() {

    override fun fileType(): ArrayList<String> = arrayListOf("vue", "css", "tsx", "vue", "js", "ts", "jsx", "dart")

    override fun actionPerformed(e: AnActionEvent) {
        val editor: Editor? = e.getData(CommonDataKeys.EDITOR)
        val psiFile: PsiFile? = e.getData(CommonDataKeys.PSI_FILE)
        editor?.apply {
            val strategy = StrategyManager(psiFile!!, editor)
            val selectText = selectionModel.selectedText
            if (!selectText.isNullOrEmpty()) {
                editor.runWriteCmd {
                    val content = strategy.process(selectText)
                    document.replaceString(selectionModel.selectionStart, selectionModel.selectionEnd, content)
                }
            } else {
                val dialog = CodeStyleGeneratorDialog(strategy)

                dialog.showDialog()
            }
        }
    }


}
