package com.awesome.plugins.codestyle.interceptor.flutter

import com.awesome.plugins.codestyle.interceptor.tailwind.TailWindHelper.replaceRegex
import com.awesome.plugins.codestyle.base.BaseInterceptor
import com.awesome.plugins.codestyle.interceptor.tailwind.props.HeightInterceptor
import com.awesome.plugins.codestyle.interceptor.tailwind.props.WidthInterceptor
import com.intellij.openapi.editor.Editor

/**
 * 处理Css 代码，转换成 TailWindCss
 **/
class FlutterInterceptor(private val editor: Editor, private val isChain: Boolean) : BaseInterceptor(editor) {
    override fun process(content: String): String {
        return convertTailCss(content)
    }

    /**
     * 处理选中的文本，并将css转换为tailwindcss
     **/
    private fun convertTailCss(selectedText: String): String {
        val lines = selectedText.split("\n")
        val flutterBuilder = FlutterBuilder()
        for (index in lines.indices) {
            val line = lines[index]
            if (line.trim().isEmpty()) continue
            val results = line.split(":")
            val key = results.first().trim()
            val value = results.last().trim().replace(";", "").replace("!important", "").trim()
            flutterBuilder.parseProp(key, value)
        }
        if (isChain) {
            return flutterBuilder.buildChain()
        }
        return flutterBuilder.build()
    }
}
