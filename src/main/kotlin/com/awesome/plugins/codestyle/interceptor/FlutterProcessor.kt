package com.awesome.plugins.codestyle.interceptor

import com.awesome.common.PluginProps
import com.awesome.plugins.codestyle.base.BaseProcessor
import com.awesome.plugins.codestyle.interceptor.flutter.FlutterInterceptor
import com.awesome.utils.*
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile

/**
 * Tailwind处理器的代码，处理器中会有多个拦截器
 **/
class FlutterProcessor(private val editor: Editor, private val psiFile: PsiFile) : BaseProcessor(editor) {

    //获取配置文件
    private val properties = PropertiesHelper(psiFile)

    //存储颜色值还有对应的在代码中的名字
    private val colorMap = hashMapOf<String, String>()

    private var needThemeMode = false

    init {
        var file = PsiFileUtils.getFileByName(psiFile, "colours.dart")
        file?.text?.regex(RegexText.colorConstRegex) {
            val key = it.regexOne(RegexText.colorConstValueRegex)?.lowercase()
            val value = it.regexOne(RegexText.colorConstNameRegex)
            println("$key,$value")
            needThemeMode = true
            if (key != null && value != null) {
                colorMap.put(key, value)
            }
        }
        file = PsiFileUtils.getFileByName(psiFile, "colours_dark.dart")
        val file2 = PsiFileUtils.getFileByName(psiFile, "colours_light.dart")
        if (file != null || file2 != null) {
            val content = "${file?.text}\n\n${file2?.text}"
            content.regex(RegexText.colorConstRegex2) {
                val key = it.regexOne(RegexText.colorConstValueRegex)?.lowercase()
                val value = it.regexOne(RegexText.colorConstNameRegex2)
                println("$key,$value")
                needThemeMode = false
                if (key != null && value != null) {
                    colorMap.put(key, value)
                }
            }
        }

        val isChain = "true" == properties.getProperty(PluginProps.flutterChain)
        interceptors.add(FlutterInterceptor(editor, isChain, colorMap, needThemeMode))
    }

    override fun process(text: String): String {
        var content = text
        interceptors.forEach { content = it.process(content) }
        return content
    }
}
