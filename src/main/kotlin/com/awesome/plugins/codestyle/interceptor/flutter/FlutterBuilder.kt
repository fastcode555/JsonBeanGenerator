package com.awesome.plugins.codestyle.interceptor.flutter

import com.awesome.utils.RegexText
import kotlin.math.roundToInt

class FlutterBuilder(private val colorMap: HashMap<String, String>, private val needThemeMode: Boolean) {
    var width: Double? = null
    var height: Double? = null
    var fontFamily: String? = null
    var fontWeight: Int? = null
    var fontSize: Double? = null
    var lineHeight: Double? = null
    var color: String? = null
    var background: String? = null
    var opacity: Double? = null
    var boxShadow: String? = null
    var borderRadius: String? = null
    var border: String? = null

    /**
     *将css的样式，转换成flutter的属性
     **/
    fun parseProp(key: String, value: String) {
        when (key) {
            "width" -> width = RegexText.getNum(value)
            "height" -> height = RegexText.getNum(value)
            "font-family" -> fontFamily = value
            "font-weight" -> fontWeight = RegexText.getNum(value)?.roundToInt()
            "font-size" -> fontSize = RegexText.getNum(value)
            "color" -> color = FlutterHelper.getColor(value)
            "background" -> background = FlutterHelper.background(value)
            "border-radius" -> borderRadius = FlutterHelper.borderRadius(value)
            "opacity" -> opacity = RegexText.getNum(value)
            "border" -> "8"
            "box-shadow" -> "8"
            "line-height" -> lineHeight = RegexText.getNum(value)
            else -> "auto"
        }
    }

    fun build(): String {
        if (fontFamily != null) {
            return buildNormalText()
        }
        return ""
    }

    fun buildChain(): String {
        if (fontFamily != null) {
            return buildChainText()
        }
        return ""
    }

    private fun buildNormalText(): String {
        val builder = StringBuilder("Text('Text'*50,")
        val maxLine = maxLine()
        if (maxLine > 1) {
            builder.append("maxLines: ${maxLine},")
        }
        builder.append("style: ${buildNormalTextStyle()},")
        builder.append("),")
        return builder.toString()
    }

    private fun buildChainText(): String {
        val builder = StringBuilder("Text('Text'*50,")
        val maxLine = maxLine()
        if (maxLine > 1) {
            builder.append("maxLines: ${maxLine},")
        }
        builder.append("style: ${buildChainTextStyle()}),")
        return builder.toString()
    }

    private fun buildNormalTextStyle(): String {
        val builder = StringBuilder("TextStyle(")
        if (fontSize != 14.0 && fontSize != null) {
            builder.append("fontSize: $fontSize.sp,")
        }
        if (fontWeight != 400 && fontWeight != null) {
            builder.append("fontWeight: FontWeight.w$fontWeight,")
        }
        if (color != null) {
            val colorName = colorMap[color]
            if (colorName != null) {
                builder.append("color: Colours.$colorName${if (needThemeMode) ".cr" else ""},")
            } else {
                builder.append("color: const Color($color),")
            }
        }
        return builder.append("overflow: TextOverflow.ellipsis)").toString()
    }

    private fun buildChainTextStyle(): String {
        val builder = StringBuilder("ts")
        if (fontSize != 14.0 && fontSize != null) {
            builder.append(".f${fontSize!!.toInt()}")
        }
        if (fontWeight != 400 && fontWeight != null) {
            builder.append(if (fontWeight == 700) ".bold" else ".w$fontWeight")
        }
        if (color != null) {
            val colorName = colorMap[color]
            if (colorName != null) {
                builder.append(".$colorName")
            } else {
                builder.append(".Color($color)")
            }
        }
        return builder.append(".ellipsis").append(".mk").toString()
    }

    private fun maxLine(): Int {
        if (lineHeight != null) {
            val line = ((height ?: 0).toDouble() / (lineHeight ?: 0).toDouble()).toInt()
            return if (line <= 0) 1 else line
        }
        return 1
    }

}
