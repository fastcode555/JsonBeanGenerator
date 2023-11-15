package com.awesome.plugins.codestyle.interceptor.flutter

import com.awesome.utils.RegexText
import com.awesome.utils.regex

object FlutterHelper {
    fun background(value: String): String {
        if (value.startsWith("#")) {
            return getColor(value)
        }
        return ""
    }

    fun borderRadius(value: String?): String? {
        if (value == null) return null
        val results = value.split(" ").clearEmpty()
        if (results.isSame()) {
            return "BorderRadius.circular(${results[0]}.r),"
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
        return "BorderRadius.circular(${results[0]}.r),"
    }

    fun borderRadiusChain(value: String?): String? {
        if (value == null) return null
        val results = value.split(" ").clearEmpty()
        if (results.isSame()) {
            return ".r${results[0]}"
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
        return ".r${results[0]}"
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


}
