package com.awesome.plugins.codestyle.interceptor.tailwind

import com.awesome.plugins.codestyle.base.BaseInterceptor
import com.intellij.openapi.editor.Editor

/**
 * 修正TailWindCss中存在的代码问题
 **/
class ModifyInterceptor(private val editor: Editor) : BaseInterceptor(editor) {
    override fun process(text: String): String {
        var content = text

        content = text.replace(Regex("((?<= )|^)[p|m]{1}[l|r|t|b|x|y]{0,1}-\\[0(rem|px|dp)*\\]"), "")
        val maps = mapOf(
            Pair("left-[0]", "left-0"),
            Pair("right-[0]", "right-0"),
            Pair("top-[0]", "top-0"),
            Pair("bottom-[0]", "bottom-0"),
        )
        maps.forEach { (key, value) ->
            content = content.replace(key, value)
        }
        return content.replace("  ", " ").trim()
    }
}
