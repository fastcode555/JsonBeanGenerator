package com.awesome.plugins.codestyle.interceptor.flutter

import com.awesome.utils.RegexText
import com.awesome.utils.regex
import com.awesome.utils.regexAll
import com.awesome.utils.regexOne
import java.math.BigDecimal
import java.math.RoundingMode

object FlutterHelper {
    fun background(value: String, flutter: FlutterBuilder): String? {
        if (value.isEmpty()) return null
        if (value.startsWith("#")) {
            return getColor(value)
        }
        //linear-gradient(90deg, #FDE068 0%, #FED55B 35%, #F6C35B 100%)
        if (value.startsWith("linear-gradient")) {
            val deg = value.regexOne(RegexText.degConstValueRegex)?.toIntOrNull()
            val colors = value.regexAll(RegexText.colorConstValue)
            val ints = value.regexAll(RegexText.colorConstOpacity)

            val builder = StringBuilder("LinearGradient(")
            if (deg == 90) {
                builder.append("begin: Alignment.topLeft,\n")
                builder.append("end: Alignment.bottomRight,\n")
            }
            builder.append("colors:[")
            for (color in colors) {
                val colorValue = getColor(color)
                val colorName = flutter.colorMap[colorValue]
                if (colorName != null) {
                    builder.append("Colours.$colorName${flutter.crTail()},\n")
                } else {
                    builder.append("const Color($colorValue),\n")
                }
            }
            builder.append("],")
            val lists = arrayListOf<String>()
            for (num in ints) {
                var value = num.toDoubleOrNull()
                if (value != null) {
                    value /= 100
                    val valueText = BigDecimal(value).setScale(2, RoundingMode.HALF_EVEN).toString()
                    lists.add(valueText)
                }
            }
            builder.append("stops: [${lists.joinToString(",")}]")
            return builder.append(",)").toString()
        }

        return null
    }

    fun borderRadius(value: String?): String? {
        if (value == null) return null
        val results = value.split(" ").clearEmpty()
        if (results.isSame()) {
            return "BorderRadius.circular(${results[0]}.r)"
        }
        if (results.size == 4) {
            if (results[0] == results[1] && results[2] == results[3]) {
                return "BorderRadius.vertical(top: Radius.circular(${results[0]}.r),bottom: Radius.circular(${results[2]}.r))"
            }
            if (results[0] == results[3] && results[1] == results[2]) {
                return "BorderRadius.horizontal(left: Radius.circular(${results[0]}.r),right: Radius.circular(${results[1]}.r))"
            }
            if (results[0] == results[1] && results[2] != results[3]) {
                return "BorderRadius.only(topLeft:Radius.circular(${results[0]}.r),topRight: Radius.circular(${results[0]}.r), bottomRight: Radius.circular(${results[2]}.r), bottomLeft: Radius.circular(${results[3]}.r))"
            }
            if (results[0] != results[1] && results[2] == results[3]) {
                return "BorderRadius.only(topLeft: Radius.circular(${results[0]}.r),topRight: Radius.circular(${results[1]}.r), bottomRight: Radius.circular(${results[2]}.r), bottomLeft: Radius.circular(${results[2]}.r))"
            }
            if (results[0] == results[3] && results[1] != results[2]) {
                return "BorderRadius.only(topLeft: Radius.circular(${results[0]}.r),topRight: Radius.circular(${results[1]}.r), bottomRight: Radius.circular(${results[2]}.r), bottomLeft: Radius.circular(${results[0]}.r))"
            }
            if (results[0] != results[3] && results[1] == results[2]) {
                return "BorderRadius.only(topLeft: Radius.circular(${results[0]}.r),topRight: Radius.circular(${results[1]}.r), bottomRight: Radius.circular(${results[1]}.r), bottomLeft: Radius.circular(${results[3]}.r))"
            }
            return "BorderRadius.only(topLeft: Radius.circular(${results[0]}.r),topRight: Radius.circular(${results[1]}.r), bottomRight: Radius.circular(${results[2]}.r), bottomLeft: Radius.circular(${results[3]}.r))"
        }
        if (results.size == 3) {
            if (results[0] == results[1]) {
                return "BorderRadius.only(topLeft: Radius.circular(${results[0]}.r),topRight: Radius.circular(${results[0]}.r), bottomRight: Radius.circular(${results[1]}.r), bottomLeft: Radius.circular(${results[2]}.r))"
            }
            if (results[0] == results[2]) {
                return "BorderRadius.horizontal(left: Radius.circular(${results[0]}.r),right: Radius.circular(${results[1]}.r))"
            }
        }
        if (results.size == 2) {
            return "BorderRadius.only(topLeft: Radius.circular(${results[0]}.r),topRight: Radius.circular(${results[1]}.r), bottomRight: Radius.circular(${results[0]}.r), bottomLeft: Radius.circular(${results[1]}.r))"
        }
        return "BorderRadius.circular(${results[0]}.r)"
    }

    fun borderRadiusChain(value: String?): String? {
        if (value == null) return null
        val results = value.split(" ").clearEmpty()
        if (results.isSame()) {
            return ".r${results[0].toDouble().toInt()}"
        }
        if (results.size == 4) {
            if (results[0] == results[1] && results[2] == results[3]) {
                return ".rv(${results[0]}.r,${results[2]}.r)"
            }
            if (results[0] == results[3] && results[1] == results[2]) {
                return ".rh(${results[0]}.r,${results[1]}.r)"
            }
            if (results[0] == results[1] && results[2] != results[3]) {
                return ".rOnly(${results[0]}.r,${results[0]}.r,${results[3]}.r,${results[2]}.r)"
            }
            if (results[0] != results[1] && results[2] == results[3]) {
                return ".rOnly(${results[0]}.r,${results[1]}.r,${results[2]}.r,${results[2]}.r)"
            }
            if (results[0] == results[3] && results[1] != results[2]) {
                return ".rOnly(${results[0]}.r,${results[1]}.r,${results[0]}.r,${results[2]}.r)"
            }
            if (results[0] != results[3] && results[1] == results[2]) {
                return ".rOnly(${results[0]}.r,${results[1]}.r,${results[3]}.r,${results[1]}.r)"
            }
            return ".rOnly(${results[0]}.r,${results[1]}.r,${results[3]}.r,${results[2]}.r)"
        }
        if (results.size == 3) {
            if (results[0] == results[1]) {
                return ".rOnly(${results[0]}.r,${results[0]}.r,${results[2]}.r,${results[1]}.r)"
            }
            if (results[0] == results[2]) {
                return ".rv(${results[0]}.r,${results[1]}.r)"
            }
        }
        if (results.size == 2) {
            return ".rOnly(${results[0]}.r,${results[1]}.r,${results[1]}.r,${results[0]}.r)"
        }
        return ".r${results[0].toDouble().toInt()}"
    }

    fun getColor(value: String): String {
        val color = value.replace("#", "").lowercase()
        if (color.length == 6) {
            return "0xff$color"
        } else if (color.length == 8) {
            return "0x$color"
        }
        return color
    }

    /**
     * 清掉数组中是空的数据
     **/
    private fun List<String>.clearEmpty(): ArrayList<String> {
        val results = arrayListOf<String>()
        for (text in this) {
            if (text.trim().isEmpty()) continue
            RegexText.getNum(text.trim())?.apply { results.add("$this") }
        }
        return results
    }

    /**
     * 判断数组中的数据是否相等，忽略空字符串的问题
     **/
    private fun List<String>.isSame(): Boolean {
        if (this.isEmpty()) return true
        var isSame = true
        var string = this[0].trim()
        for (text in this) {
            isSame = (isSame && (string == text.trim()))
            if (!isSame) return isSame
        }
        return isSame
    }

    /**
     * 移除无用的属性
     **/
    fun removeUselessRadius(text: String): String {
        var content = text
        text.regex("[\\, ]*[a-z]*\\:[ ]*Radius\\.circular\\(0\\.0\\.r\\)[\\,]*") {
            content = content.replace(it, "")
        }
        return content
    }

    fun removeUselessChainRadius(text: String): String {
        var content = text
        text.regex(",[ 0.r]+(?=\\))") { content = content.replace(it, "") }
        return content
    }

    /**
     * 解析border的属性值
     **/
    fun border(border: String?, style: FlutterBuilder): String? {
        if (border == null) return null
        val results = border.trim().split(" ")
        if (results.size == 3) {
            val borderWidth = RegexText.getNum(results[0]) ?: 0.0
            val borderType = results[1].trim()
            var color = results[2]
            if (color.startsWith("#")) {
                color = style.getColorName(getColor(color))
            } else {
                color = "Colors.$color"
            }
            val builder = StringBuilder("Border.all(")
            if (borderWidth > 1.0) {
                builder.append("width: $borderWidth.r,")
            }
            builder.append("color: $color,")
            if (borderType != "solid") {
                builder.append("style: BorderStyle.$borderType,")
            }
            builder.append(")")
            return builder.toString()
        }
        return null
    }

    /**
     * 设置border的属性值
     **/
    fun borderChain(border: String?, style: FlutterBuilder): String? {
        if (border == null) return null
        val results = border.trim().split(" ")
        if (results.size == 3) {
            val borderWidth = RegexText.getNum(results[0]) ?: 0.0
            var color = results[2]
            if (color.startsWith("#")) {
                color = style.getColorName(getColor(color))
            } else {
                color = "Colors.$color"
            }
            val builder = StringBuilder(".border($color")
            if (borderWidth > 1.0) {
                builder.append(",$borderWidth.r)")
            } else {
                builder.append(")")
            }
            return builder.toString()
        }
        return null
    }
}
