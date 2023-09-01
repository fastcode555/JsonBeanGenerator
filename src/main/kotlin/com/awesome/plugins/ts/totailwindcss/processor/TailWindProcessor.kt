package com.awesome.plugins.ts.totailwindcss.processor

import com.awesome.plugins.ts.totailwindcss.TailWindHelper
import com.awesome.plugins.ts.totailwindcss.TailWindHelper.replaceRegex
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.SelectionModel

class TailWindProcessor(private val editor: Editor) : BaseProcessor(editor) {
    override fun process(content: String): String = convertTailCss(editor.selectionModel)

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
        return builder.toString()
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
            "box-shadow" -> "shadow-"
            "cursor" -> "$key-$value"
            else -> "$key"
        }
    }
}
