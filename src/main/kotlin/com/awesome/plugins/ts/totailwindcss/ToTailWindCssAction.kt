package com.awesome.plugins.ts.totailwindcss

import com.awesome.common.BaseAnAction
import com.awesome.plugins.ts.totailwindcss.TailWindHelper.replaceRegex
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor

/**
 * 将复制到的css代码转换成 tailwindcss代码
 **/
class ToTailWindCssAction : BaseAnAction() {
    override fun fileType(): ArrayList<String> = arrayListOf("vue", "css")

    override fun actionPerformed(e: AnActionEvent) {
        val editor: Editor? = e.getData(CommonDataKeys.EDITOR)
        editor?.apply {
            WriteCommandAction.runWriteCommandAction(editor.project) {
                val selectText = selectionModel.selectedText ?: ""
                val lines = selectText.split("\n")
                val builder = StringBuilder()
                for (index in 0 until lines.size) {
                    val line = lines[index]
                    if (line.trim().isEmpty()) continue
                    if (!line.contains(":")) {
                        builder.append("$line ")
                    }
                    val results = line.split(":")
                    val key = results.first().trim()
                    val value = results.last().trim().replace(";", "")
                    val prop = convertTailWindCss(key, value)
                    if (prop.trim().isEmpty()) continue
                    if (index == lines.size - 1) {
                        builder.append(prop)
                    } else {
                        builder.append("$prop ")
                    }
                }
                document.replaceString(
                    selectionModel.selectionStart,
                    selectionModel.selectionEnd,
                    builder.toString().trim()
                )
            }
        }
    }

    /**
     * 将css的样式，转换成tailwindcss的样式
     **/
    private fun convertTailWindCss(key: String, value: String): String {
        return when (key) {
            "width" -> "w-[$value]"
            "max-width" -> "max-w-[$value]"
            "height" -> "h-[$value]"
            "max-height" -> "max-h-[$value]"

            "background" -> "bg-[$value]"
            "color" -> "text-[$value]"
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

            "display" -> value
            "justify-content" -> TailWindHelper.justifyContent(value)
            "align-items" -> TailWindHelper.alignItems(value)
            "align-self" -> TailWindHelper.alignSelf(value)
            "align-content" -> TailWindHelper.alignContent(value)
            "flex-grow" -> TailWindHelper.flexGrow(value)
            "flex-shrink" -> TailWindHelper.flexShrink(value)
            "overflow" -> "$key-$value"
            //"position"->TailWindHelper.opacity(key,value)
            else -> ""
        }
    }

}
