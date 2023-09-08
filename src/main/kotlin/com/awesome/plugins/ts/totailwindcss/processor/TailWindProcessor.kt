package com.awesome.plugins.ts.totailwindcss.processor

import com.awesome.plugins.ts.totailwindcss.TailWindHelper
import com.awesome.plugins.ts.totailwindcss.TailWindHelper.replaceRegex
import com.awesome.plugins.ts.totailwindcss.processor.props.HeightProcessor
import com.awesome.plugins.ts.totailwindcss.processor.props.WidthProcessor
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.SelectionModel

/**
 * 处理Css 代码，转换成 TailWindCss
 **/
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
            val value = results.last().trim().replace(";", "").replace("!important", "").trim()
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
        return builder.toString().replace("-DEFAULT", "").replace("  ", " ")
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
            "background" -> TailWindHelper.color("bg", value)
            "background-attachment" -> "bg-$value"
            "background-clip" -> TailWindHelper.backgroundClip(value)
            "background-repeat" -> TailWindHelper.backgroundRepeat(value)
            "background-origin" -> TailWindHelper.backgroundOrigin(value)
            "background-blend-mode" -> TailWindHelper.bgBlendMode(value)
            "background-position" -> TailWindHelper.bgPosition(value)
            "mix-blend-mode" -> TailWindHelper.mixBlendMode(value)
            "color" -> TailWindHelper.color("text", value)
            "opacity" -> TailWindHelper.opacity(key, value)

            "border" -> TailWindHelper.border(key, value)
            "border-bottom" -> TailWindHelper.border("border-b", value)
            "border-top" -> TailWindHelper.border("border-t", value)
            "border-left" -> TailWindHelper.border("border-l", value)
            "border-right" -> TailWindHelper.border("border-r", value)

            "border-radius" -> TailWindHelper.borderRadius(value)
                .replaceRegex("rounded-[a-z\\-]{0,4}\\[0[rem]*\\]")

            "border-style" -> "border-$value"
            "border-bottom-style" -> "border-b-$value"
            "border-left-style" -> "border-l-$value"
            "border-top-style" -> "border-t-$value"
            "border-right-style" -> "border-r-$value"

            "border-width" -> TailWindHelper.borderWidth("border", value)
            "border-bottom-width" -> TailWindHelper.borderWidth("border-b", value)
            "border-top-width" -> TailWindHelper.borderWidth("border-t", value)
            "border-left-width" -> TailWindHelper.borderWidth("border-l", value)
            "border-right-width" -> TailWindHelper.borderWidth("border-r", value)

            "border-color" -> TailWindHelper.color("border", value)
            "border-bottom-color" -> TailWindHelper.color("border-b", value)
            "border-top-color" -> TailWindHelper.color("border-t", value)
            "border-left-color" -> TailWindHelper.color("border-l", value)
            "border-right-color" -> TailWindHelper.color("border-r", value)

            "font-weight" -> TailWindHelper.fontWeight(value)
            "font-size" -> TailWindHelper.fontSize(value)
            "line-height" -> TailWindHelper.lineHeight(value)
            "letter-spacing" -> TailWindHelper.letterSpacing(value)
            "word-spacing" -> TailWindHelper.letterSpacing(value)
            "gap" -> TailWindHelper.spacing(key, value)
            "column-gap" -> TailWindHelper.spacing("gap-x", value)
            "row-gap" -> TailWindHelper.spacing("gap-y", value)
            "font-family" -> TailWindHelper.fontFamily(value)
            "text-align" -> "text-$value"
            "text-overflow" -> "text-$value"
            "text-wrap" -> "text-$value"
            "text-transform" -> TailWindHelper.textTransform(value)
            "-webkit-background-clip" -> TailWindHelper.backgroundClip(value)
            "font-style" -> TailWindHelper.fontStyle(value)
            "text-decoration" -> TailWindHelper.textDecorationLine(value)
            "text-decoration-style" -> TailWindHelper.textDecorationStyle(value)
            "text-decoration-line" -> TailWindHelper.textDecorationLine(value)
            "text-decoration-thickness" -> TailWindHelper.textDecorationThickness(value)
            "text-underline-offset" -> TailWindHelper.textUnderLineOffset(value)
            "--tw-text-opacity" -> TailWindHelper.opacity("text-opacity", value)

            "margin" -> TailWindHelper.margin(value)
            "margin-top" -> TailWindHelper.spacing("mt", value)
            "margin-bottom" -> TailWindHelper.spacing("mb", value)
            "margin-left" -> TailWindHelper.spacing("ml", value)
            "margin-right" -> TailWindHelper.spacing("mr", value)

            "padding" -> TailWindHelper.padding(value)
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
            "box-shadow" -> TailWindHelper.boxShadow(value)

            "display" -> value
            "position" -> value
            "justify-content" -> TailWindHelper.justifyContent(value)
            "justify-self" -> "$key-$value"
            "justify-items" -> "$key-$value"
            "place-self" -> "$key-$value"
            "align-items" -> TailWindHelper.alignItems(value)
            "align-self" -> TailWindHelper.alignSelf(value)
            "align-content" -> TailWindHelper.alignContent(value)
            "flex-grow" -> TailWindHelper.flexGrow(value)
            "flex-shrink" -> TailWindHelper.flexShrink(key, value)
            "shrink" -> TailWindHelper.flexShrink(key, value)
            "overflow" -> "$key-$value"
            "overflow-x" -> "$key-$value"
            "overflow-y" -> "$key-$value"
            "overscroll-behavior" -> "overscroll-$value"
            "overscroll-behavior-x" -> "overscroll-x-$value"
            "overscroll-behavior-y" -> "overscroll-y-$value"
            "scroll-behavior" -> "scroll-$value"

            "cursor" -> "$key-$value"
            "rotate" -> TailWindHelper.rotate(key, value)
            "order" -> TailWindHelper.order(value)
            "aspect-ratio" -> TailWindHelper.aspectRatio(value)
            "background-size" -> "bg-$value"
            "background-color" -> TailWindHelper.color("bg", value)
            "filter" -> TailWindHelper.filter(key, value)
            "backdrop-filter" -> TailWindHelper.filter(key, value)
            "clear" -> "clear-$value"
            "outline-width" -> TailWindHelper.borderWidth("outline", value)
            "outline-offset" -> "outline-offset-[$value]"
            "outline-style" -> "outline-$value"

            "scale" -> TailWindHelper.scale("scale", value)
            "scale-x" -> TailWindHelper.scale("scale-x", value)
            "scale-y" -> TailWindHelper.scale("scale-y", value)
            "stroke-width" -> TailWindHelper.strokeWidth(value)
            "z-index" -> TailWindHelper.zIndex(value)

            "vertical-align" -> TailWindHelper.verticalAlign(value)
            "visibility" -> value
            "transform" -> TailWindHelper.transform(key, value)

            "object-fit" -> "object-$value"
            "object-position" -> "object-${value.replace(" ", "-")}"
            "flex-direction" -> TailWindHelper.flexDirection(value)
            "flex-wrap" -> TailWindHelper.flexWrap(value)
            "grid-auto-flow" -> TailWindHelper.gridAutoFlow(value)
            "grid-column-start" -> "col-start-$value"
            "grid-column-end" -> "col-end-$value"
            "grid-row-start" -> "row-start-$value"
            "grid-row-end" -> "row-end-$value"
            "pointer-events" -> "$key-$value"

            "break-before" -> TailWindHelper.breakBefore(value)
            "break-inside" -> TailWindHelper.breakInside(value)
            "break-after" -> TailWindHelper.breakAfter(value)
            "table-layout" -> "table-$value"
            "caption-side" -> "caption-$value"
            "border-collapse" -> "border-$value"
            "user-select" -> "select-$value"
            "scroll-snap-stop" -> "snap-$value"
            "resize" -> TailWindHelper.resize(value)
            "list-style-position" -> "list-$value"
            "scroll-snap-align" -> TailWindHelper.scrollSnapAlign(value)
            "place-content" -> TailWindHelper.placeContent(value)
            "place-items" -> "$key-$value"
            "hyphens" -> "$key-$value"
            "white-space" -> "whitespace-$value"
            "word-break" -> TailWindHelper.wordBreak(value)
            "overflow-wrap" -> TailWindHelper.overflowWrap(value)
            "box-decoration-break" -> "box-decoration-$value"
            "isolation" -> TailWindHelper.isolation(value)
            "columns" -> TailWindHelper.columns(key, value)
            "box-sizing" -> TailWindHelper.boxSizing(value)
            else -> "$key"
        }
    }
}
