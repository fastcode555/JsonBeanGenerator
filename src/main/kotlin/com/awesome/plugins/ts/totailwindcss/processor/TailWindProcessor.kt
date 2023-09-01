package com.awesome.plugins.ts.totailwindcss.processor

import com.awesome.plugins.ts.totailwindcss.TailWindHelper
import com.awesome.plugins.ts.totailwindcss.TailWindHelper.replaceRegex
import com.awesome.plugins.ts.totailwindcss.processor.props.HeightProcessor
import com.awesome.plugins.ts.totailwindcss.processor.props.WidthProcessor
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
        return builder.toString().replace("-DEFAULT", "")
    }

    /**
     * 将css的样式，转换成tailwindcss的样式
     **/
    private fun convertTailProp(key: String, value: String): String {
        return when (key) {
            "width" -> WidthProcessor().process(key, value)
            "max-width" -> WidthProcessor("max-w").process(key, value)
            "min-width" -> WidthProcessor("min-w").process(key, value)
            "height" -> HeightProcessor().process(key, value)
            "max-height" -> HeightProcessor("max-h").process(key, value)
            "min-height" -> HeightProcessor("min-h").process(key, value)
            "background" -> "bg-[$value]"
            "background-blend-mode" -> TailWindHelper.bgBlendMode(key, value)
            "mix-blend-mode" -> TailWindHelper.mixBlendMode(key, value)
            "color" -> TailWindHelper.color(key, value)
            "opacity" -> TailWindHelper.opacity(key, value)

            "border" -> TailWindHelper.border(key, value)
            "border-width" -> TailWindHelper.borderWidth("border", value)
            "border-color" -> "border-[$value]"
            "border-radius" -> TailWindHelper.borderRadius(key, value).replaceRegex("rounded.*?\\[0rem\\]")

            "font-weight" -> TailWindHelper.fontWeight(key, value)
            "font-size" -> TailWindHelper.fontSize(key, value)
            "line-height" -> TailWindHelper.lineHeight(key, value)
            "letter-spacing" -> TailWindHelper.letterSpacing(key, value)
            "font-family" -> TailWindHelper.fontFamily(key, value)
            "text-transform" -> value
            "text-align" -> "text-$value"
            "text-transform" -> TailWindHelper.textTransform(key, value)
            "-webkit-background-clip" -> TailWindHelper.backgroundClip(key, value)
            "font-style" -> TailWindHelper.fontStyle(key, value)
            "text-decoration-style" -> TailWindHelper.textDecorationStyle(key, value)
            "text-decoration-line" -> TailWindHelper.textDecorationLine(key, value)
            "text-decoration-thickness" -> TailWindHelper.textDecorationThickness(key, value)
            "text-underline-offset" -> TailWindHelper.textUnderLineOffset(key, value)
            "--tw-text-opacity" -> TailWindHelper.opacity("text-opacity", value)

            "margin" -> TailWindHelper.margin(key, value)
            "margin-top" -> TailWindHelper.spacing("mt", value)
            "margin-bottom" -> TailWindHelper.spacing("mb", value)
            "margin-left" -> TailWindHelper.spacing("ml", value)
            "margin-right" -> TailWindHelper.spacing("mr", value)

            "padding" -> TailWindHelper.padding(key, value)
            "padding-top" -> TailWindHelper.spacing("pt", value)
            "padding-bottom" -> TailWindHelper.spacing("pb", value)
            "padding-left" -> TailWindHelper.spacing("pl", value)
            "padding-right" -> TailWindHelper.spacing("pr", value)
            "left" -> TailWindHelper.spacing("left", value)
            "right" -> TailWindHelper.spacing("right", value)
            "top" -> TailWindHelper.spacing("top", value)
            "bottom" -> TailWindHelper.spacing("bottom", value)
            "border-spacing" -> TailWindHelper.spacing("border-spacing", value)
            "flex-basis" -> TailWindHelper.spacing("basis", value, key)
            "inset" -> TailWindHelper.spacing("inset", value)
            "scroll-margin" -> TailWindHelper.spacing("scroll-m", value)
            "scroll-padding" -> TailWindHelper.spacing("scroll-p", value)
            "text-indent" -> TailWindHelper.spacing("indent", value, key)
            "box-shadow" -> TailWindHelper.boxShadow(key, value)

            "display" -> value
            "position" -> value
            "justify-content" -> TailWindHelper.justifyContent(value)
            "align-items" -> TailWindHelper.alignItems(value)
            "align-self" -> TailWindHelper.alignSelf(value)
            "align-content" -> TailWindHelper.alignContent(value)
            "flex-grow" -> TailWindHelper.flexGrow(value)
            "flex-shrink" -> TailWindHelper.flexShrink(value)
            "overflow" -> "$key-$value"
            "overflow-x" -> "$key-$value"
            "overflow-y" -> "$key-$value"
            "overscroll-behavior" -> "overscroll-$value"
            "overscroll-behavior-x" -> "overscroll-x-$value"
            "overscroll-behavior-y" -> "overscroll-y-$value"

            "box-shadow" -> "shadow-"
            "cursor" -> "$key-$value"
            "rotate" -> TailWindHelper.rotate(key, value)
            "order" -> TailWindHelper.order(key, value)
            "aspect-ratio" -> TailWindHelper.aspectRatio(key, value)
            "background-size" -> "bg-$value"
            "filter" -> TailWindHelper.filter(key, value)
            "clear" -> "clear-$value"
            "outline-width" -> TailWindHelper.borderWidth("outline", value)

            "scale" -> TailWindHelper.scale("scale", value)
            "scale-x" -> TailWindHelper.scale("scale-x", value)
            "scale-y" -> TailWindHelper.scale("scale-y", value)
            "stroke-width" -> TailWindHelper.strokeWidth(key, value)
            "z-index" -> TailWindHelper.zIndex(key, value)
            else -> "$key"
        }
    }
}
