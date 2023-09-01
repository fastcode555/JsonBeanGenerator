package com.awesome.plugins.ts.totailwindcss

import com.awesome.common.BaseAnAction
import com.awesome.plugins.ts.totailwindcss.TailWindHelper.replaceRegex
import com.awesome.utils.regex
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.SelectionModel
import com.intellij.openapi.project.Project
import java.io.File

/**
 * 将复制到的css代码转换成 tailwindcss代码
 **/
class ToTailWindCssAction : BaseAnAction() {

    private val props: HashMap<String, String> = hashMapOf()
    override fun fileType(): ArrayList<String> = arrayListOf("vue", "css")

    override fun actionPerformed(e: AnActionEvent) {
        val editor: Editor? = e.getData(CommonDataKeys.EDITOR)
        editor?.apply {
            WriteCommandAction.runWriteCommandAction(editor.project) {
                readTailWindConfig(editor.project)
                //替换掉css的代码
                document.replaceString(
                    selectionModel.selectionStart,
                    selectionModel.selectionEnd,
                    convertTailCss(selectionModel).formatProps()
                )
            }
        }
    }

    /**
     * 格式化属性
     **/
    private fun String.formatProps(): String {
        var content = this
        props.forEach { (key, value) ->
            val newKey = "[$key]"
            if (content.contains(newKey)) {
                content = content.replace(newKey, value)
            }
        }
        return content
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

    private fun String.modify(): String {
        val maps = mapOf(
            Pair(" left-[0] ", " left-0 "),
            Pair(" right-[0] ", " right-0 "),
            Pair(" top-[0] ", " top-0 "),
            Pair(" bottom-[0] ", " bottom-0 "),
        )
        var content = this
        maps.forEach { (key, value) ->
            content = content.replace(key, value)
        }
        return content.trim()
    }

    /**
     * 处理选中的文本，并将css转换为tailwindcss
     **/
    private fun convertTailCss(selectionModel: SelectionModel): String {
        val selectText = selectionModel.selectedText ?: ""
        val lines = selectText.split("\n")
        val builder = StringBuilder()
        for (index in lines.indices) {
            val line = lines[index]
            if (line.trim().isEmpty()) continue
            if (!line.contains(":")) {
                builder.append("$line ")
            }
            val results = line.split(":")
            val key = results.first().trim()
            val value = results.last().trim().replace(";", "")
            val prop = convertTailProp(key, value)
            if (prop.trim().isEmpty()) continue
            if (index == lines.size - 1) {
                if (!builder.contains(" $prop ")) {
                    builder.append(prop)
                }
            } else {
                if (!builder.contains(" $prop ")) {
                    builder.append("$prop ")
                }
            }
        }
        builder.insert(0, "@apply ")
        return builder.toString().modify()
    }

    /**
     * 将css的样式，转换成tailwindcss的样式
     **/
    private fun convertTailProp(key: String, value: String): String {
        return when (key) {
            "width" -> "w-[$value]"
            "max-width" -> "max-w-[$value]"
            "height" -> "h-[$value]"
            "max-height" -> "max-h-[$value]"

            "background" -> "bg-[$value]"
            "color" -> TailWindHelper.color(key, value)
            "opacity" -> TailWindHelper.opacity(key, value)

            "border" -> TailWindHelper.border(key, value)
            "border-width" -> "border-[$value]"
            "border-color" -> "border-[$value]"
            "border-radius" -> TailWindHelper.borderRadius(key, value).replaceRegex("rounded.*?\\[0rem\\]")

            "font-weight" -> TailWindHelper.fontWeight(key, value)
            "font-size" -> "text-[$value]"
            "line-height" -> "leading-[$value]"
            "letter-spacing" -> "tracking-[$value]"
            "font-family" -> "font-[$value]"
            "text-transform" -> value
            "text-align" -> "text-$value"

            "margin" -> TailWindHelper.margin(key, value)
            "margin-top" -> "mt-[$value]"
            "margin-bottom" -> "mb-[$value]"
            "margin-left" -> "ml-[$value]"
            "margin-right" -> "mr-[$value]"

            "padding" -> TailWindHelper.padding(key, value)
            "padding-top" -> "pt-[$value]"
            "padding-bottom" -> "pb-[$value]"
            "padding-left" -> "pl-[$value]"
            "padding-right" -> "pr-[$value]"
            "left" -> "left-[$value]"
            "right" -> "right-[$value]"
            "top" -> "top-[$value]"
            "bottom" -> "bottom-[$value]"

            "display" -> value
            "position" -> value
            "justify-content" -> TailWindHelper.justifyContent(value)
            "align-items" -> TailWindHelper.alignItems(value)
            "align-self" -> TailWindHelper.alignSelf(value)
            "align-content" -> TailWindHelper.alignContent(value)
            "flex-grow" -> TailWindHelper.flexGrow(value)
            "flex-shrink" -> TailWindHelper.flexShrink(value)
            "overflow" -> "$key-$value"

            "cursor" -> "$key-$value"
            //"position"->TailWindHelper.opacity(key,value)
            else -> "$key"
        }
    }

}
