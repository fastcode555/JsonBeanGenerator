package com.awesome.plugins.codestyle.interceptor.tailwind

import com.awesome.plugins.codestyle.base.BaseInterceptor
import com.awesome.utils.regex
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import java.io.File

/**
 * 读取TailWindConfig文件
 **/
class TailConfigInterceptor(private val editor: Editor) : BaseInterceptor(editor) {
    private val props: HashMap<String, String> = hashMapOf()

    init {
        readTailWindConfig(editor.project)
    }

    /**
     * 读取TailWindConfig的配置文件，获取里面自定义配置的属性
     **/
    private fun readTailWindConfig(project: Project?) {
        //读取tailwind.config.js文件
        val tailwindConfigFile = File("${project?.basePath}", "tailwind.config.js")
        if (tailwindConfigFile.exists()) {
            val text = tailwindConfigFile.readText()
            text.regex("[a-zA-Z0-9_]{1,30}:[ ]*.*?'(?=,)") {
                val results = it.split(":")
                val key = results.first().trim()
                var value = results.last().trim()
                value = value.substring(1, value.length - 1)
                props[value] = key
            }
        }
    }

    override fun process(text: String): String {
        var content = text
        props.forEach { (key, value) ->
            val newKey = "[$key]"
            if (content.contains(newKey)) {
                content = content.replace(newKey, value)
            }
        }
        return content
    }
}
