package com.awesome.plugins.codestyle.interceptor.flutter

import com.awesome.plugins.codestyle.base.BaseInterceptor
import com.awesome.utils.regex
import com.intellij.openapi.editor.Editor

class FlutterColorInterceptor(private val editor: Editor) : BaseInterceptor(editor) {

    override fun process(text: String): String {
        var content = text
        text.regex("\\#[0-9a-fA-F]+") {
            content = content.replace(it, getColorName(it.lowercase()))
        }
        return content
    }

    private fun getColorName(color: String): String {
        return when (color) {
            "#00000000" -> "transparent"
            "#000000" -> "black"
            "#000" -> "black"
            "#dd000000" -> "black87"
            "#8a000000" -> "black54"
            "#73000000" -> "black45"
            "#61000000" -> "black38"
            "#42000000" -> "black26"
            "#1f000000" -> "black12"
            "#ffffff" -> "white"
            "#fff" -> "white"
            "#b3ffffff" -> "white70"
            "#b2ffffff" -> "white70"
            "#99ffffff" -> "white60"
            "#8affffff" -> "white54"
            "#62ffffff" -> "white38"
            "#4dffffff" -> "white30"
            "#3dffffff" -> "white24"
            "#1fffffff" -> "white12"
            "#1affffff" -> "white10"
            else -> color
        }
    }
}
