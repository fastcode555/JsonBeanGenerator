package com.awesome.plugins.codestyle.interceptor.flutter

import com.awesome.plugins.codestyle.base.BaseInterceptor
import com.intellij.openapi.editor.Editor

/**
 * 处理Css 代码，转换成 TailWindCss
 **/
class FlutterInterceptor(
    private val editor: Editor,
    private val isChain: Boolean,
    private val colorMap: HashMap<String, String>,
    private val needThemeMode: Boolean
) : BaseInterceptor(editor) {
    override fun process(content: String) = convertTailCss(content)

    /**
     * 处理选中的文本，并将css转换为tailwindcss
     **/
    private fun convertTailCss(selectedText: String): String {
        val lines = selectedText.split("\n")
        val flutterBuilder = FlutterBuilder(colorMap, needThemeMode)
        for (index in lines.indices) {
            val line = lines[index]
            if (line.trim().isEmpty()) continue
            val results = line.split(":")
            val key = results.first().trim()
            val value = results.last().trim().replace(";", "").replace("!important", "").trim()
            flutterBuilder.parseProp(key, value)
        }
        return flutterBuilder.build(isChain)
    }
}
