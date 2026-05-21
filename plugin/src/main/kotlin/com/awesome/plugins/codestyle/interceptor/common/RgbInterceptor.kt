package com.awesome.plugins.codestyle.interceptor.common

import com.awesome.plugins.codestyle.base.BaseInterceptor
import com.awesome.plugins.stringassociate.processor.StringHelper
import com.awesome.utils.regex
import com.awesome.utils.regexOne
import com.intellij.openapi.editor.Editor

/**
 * 将Rgb颜色转换成 #开始的颜色值
 **/
class RgbInterceptor(private val editor: Editor) : BaseInterceptor(editor) {
    override fun process(content: String): String {
        var result = content
        content.regex("rgb(a){0,1}\\(.*?\\)") {
            result = result.replace(it, StringHelper.toHexColor(it))
        }
        var text = result
        text.regex("var\\(.*?\\)") {
            val color = it.regexOne("(?<=, )\\#[0-9a-fA-F]+") ?: ""
            result = result.replace(it, color)
        }
        return result
    }
}
