package com.awesome.plugins.codestyle.interceptor

import com.awesome.plugins.codestyle.base.BaseProcessor
import com.awesome.plugins.codestyle.interceptor.tailwind.ColorInterceptor
import com.awesome.plugins.codestyle.interceptor.tailwind.ModifyInterceptor
import com.awesome.plugins.codestyle.interceptor.tailwind.TailConfigInterceptor
import com.awesome.plugins.codestyle.interceptor.tailwind.TailWindInterceptor
import com.intellij.openapi.editor.Editor

/**
 * Tailwind处理器的代码，处理器中会有多个拦截器
 **/
class TailWindProcessor(private val editor: Editor) : BaseProcessor(editor) {

    init {
        interceptors.add(TailWindInterceptor(editor))
        interceptors.add(ModifyInterceptor(editor))
        interceptors.add(TailConfigInterceptor(editor))
        interceptors.add(ColorInterceptor(editor))
    }

    override fun process(text: String): String {
        var content = text
        interceptors.forEach { content = it.process(content) }
        //替换掉css的代码
        if (content.isNotEmpty()) {
            content = "@apply $content"
        }
        return content
    }
}
