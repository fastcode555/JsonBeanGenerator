package com.awesome.plugins.codestyle.interceptor

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.NlsSafe
import com.intellij.psi.PsiFile

class StrategyManager(private val psiFile: PsiFile, val editor: Editor) {
    fun process(selectText: @NlsSafe String): @NlsSafe String {
        val type = psiFile.virtualFile.extension
        if (type == "dart") {
            return FlutterProcessor(editor, psiFile).process(selectText)
        }
        return TailWindProcessor(editor).process(selectText);
    }

}
