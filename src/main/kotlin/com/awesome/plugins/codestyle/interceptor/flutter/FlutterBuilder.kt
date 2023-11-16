package com.awesome.plugins.codestyle.interceptor.flutter

import com.awesome.plugins.codestyle.interceptor.flutter.widgetsbuilder.ContainerBuilder
import com.awesome.plugins.codestyle.interceptor.flutter.widgetsbuilder.TextBuilder
import com.awesome.utils.RegexText
import kotlin.math.roundToInt

class FlutterBuilder(val colorMap: HashMap<String, String>, private val needThemeMode: Boolean) {
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
            "background" -> background = value
            "border-radius" -> borderRadius = value
            "opacity" -> opacity = RegexText.getNum(value)
            "border" -> border = value
            "box-shadow" -> boxShadow = value
            "line-height" -> lineHeight = RegexText.getNum(value)
        }
    }

    fun build(isChain: Boolean): String {
        if (fontFamily != null) {
            return TextBuilder(this, isChain).build()
        }
        return ContainerBuilder(this, isChain).build()
    }

    fun crTail() = if (needThemeMode) ".cr" else ""

    /**
     * 颜色的属性
     **/
    fun colorProp(color: String?): String {
        if (color != null) {
            val colorName = colorMap[color]
            if (colorName != null) {
                return "color: Colours.$colorName${crTail()},"
            } else {
                return "color: const Color(${color}),"
            }
        }
        return ""
    }

    fun getColorName(color: String?): String {
        if (color != null) {
            val colorName = colorMap[color]
            if (colorName != null) {
                return "Colours.$colorName${crTail()}"
            } else {
                return "const Color(${color})"
            }
        }
        return ""
    }

    /**
     *链式编程的颜色属性
     **/
    fun colorChainProp(color: String?): String {
        if (color != null) {
            val colorName = colorMap[color]
            return if (colorName != null) ".$colorName" else ".Color(${color})"
        }
        return ""
    }
}
