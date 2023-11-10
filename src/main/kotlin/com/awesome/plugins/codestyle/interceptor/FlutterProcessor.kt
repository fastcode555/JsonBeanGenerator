package com.awesome.plugins.codestyle.interceptor

import com.awesome.common.PluginProps
import com.awesome.plugins.codestyle.base.BaseProcessor
import com.awesome.plugins.codestyle.interceptor.flutter.FlutterInterceptor
import com.awesome.utils.PropertiesHelper
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile
import java.util.Properties

/**
 * Tailwind处理器的代码，处理器中会有多个拦截器
 **/
class FlutterProcessor(private val editor: Editor, private val psiFile: PsiFile) : BaseProcessor(editor) {

    val properties = PropertiesHelper(psiFile)

    init {
        val isChain = "true" == properties.getProperty(PluginProps.flutterChain)
        interceptors.add(FlutterInterceptor(editor, isChain))
    }

    override fun process(text: String): String {
        var content = text
        interceptors.forEach { content = it.process(content) }
        return content
    }
}
