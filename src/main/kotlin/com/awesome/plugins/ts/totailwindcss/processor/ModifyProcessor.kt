package com.awesome.plugins.ts.totailwindcss.processor

import com.intellij.openapi.editor.Editor

class ModifyProcessor(private val editor: Editor) : BaseProcessor(editor) {
    override fun process(text: String): String {
        var content = text
        val maps = mapOf(
            Pair(" left-[0] ", " left-0 "),
            Pair(" right-[0] ", " right-0 "),
            Pair(" top-[0] ", " top-0 "),
            Pair(" bottom-[0] ", " bottom-0 "),
        )
        maps.forEach { (key, value) ->
            content = content.replace(key, value)
        }
        content = content.replace(Regex("[p|m]{1}[l|r|t|b|x|y]{0,1}-\\[0(rem|px|dp)*\\]"), "")
        return content.replace("  ", " ").trim()
    }
}
