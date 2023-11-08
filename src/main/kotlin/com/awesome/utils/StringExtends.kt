package com.awesome.utils

import org.apache.http.util.TextUtils
import toCamel
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.regex.Matcher
import java.util.regex.Pattern

///统一正则匹配的过程
inline fun String.regex(regex: String, method: (String) -> Unit) {
    val pattern = Pattern.compile(regex)
    val matcher = pattern.matcher(this)
    while (matcher.find()) {
        method(matcher.group())
    }
}

fun String.regexOne(regex: String): String? {
    val pattern = Pattern.compile(regex)
    val matcher = pattern.matcher(this)
    if (matcher.find()) {
        return matcher.group()
    }
    return null
}

fun String.md5(): String {
    return Md5Util.encode(this)
}

fun String.forceTrim(): String {
    return this.replace(" ", "").replace("\n", "").trim()
}

fun String.matchStartRegex(content: String, method: (String) -> Unit) {
    val index = this.indexOf(content)
    if (index >= 0) {
        var tail = this.substring(index, this.length)
        val result = tail.matchTail()
        method(result.trim())
        val nextIndex = tail.indexOf(result)
        tail = tail.substring(nextIndex + result.length, tail.length)
        tail.matchStartRegex(content, method)
    }
}

inline fun String.matchOneRegex(content: String, method: (String) -> Unit) {
    val index = this.indexOf(content)
    if (index >= 0) {
        var tail = this.substring(index, this.length)
        val result = tail.matchTail()
        method(result)
    }
}

fun String.matchRegexOne(content: String): String? {
    val index = this.indexOf(content)
    if (index >= 0) {
        var tail = this.substring(index, this.length)
        return tail.matchTail()
    }
    return null
}

///获取符号闭环的代码
fun String.matchTail(): String {
    var result = this
    var header = ""
    var tail = result

    val braces = ArrayList<String>()
    var left = tail.indexOf("(")
    var bracesLeft = tail.indexOf("[")
    var startIndex = Math.min(left, bracesLeft)
    startIndex = if (startIndex < 0) Math.max(left, bracesLeft) else startIndex
    var symbol = ""
    try {
        if (startIndex < 0) {
            return ""
        }
        symbol = tail.get(startIndex).toString()
    } catch (e: Exception) {
        print(e)
    }
    braces.add(symbol)

    header = tail.substring(0, startIndex + 1)
    tail = tail.substring(startIndex + 1, tail.length)
    while (braces.isNotEmpty()) {
        val minIndex = tail.minIndex()
        if (minIndex < 0) break
        val symbol = tail.get(minIndex).toString()
        val lastSymbol = braces[braces.size - 1]
        if (symbol == ")" && lastSymbol == "(") {
            braces.removeLast()
        } else if (symbol == "]" && lastSymbol == "[") {
            braces.removeLast()
        } else {
            braces.add(symbol)
        }
        header += tail.substring(0, minIndex + 1)
        tail = tail.substring(minIndex + 1, tail.length)
    }
    return header
}

private fun String.minIndex(): Int {
    val left = this.indexOf("(")
    val bracesLeft = this.indexOf("[")
    val right = this.indexOf(")")
    val bracesRight = this.indexOf("]")
    var resultLeft = Math.min(left, bracesLeft)
    var resultRight = Math.min(right, bracesRight)
    resultLeft = if (resultLeft < 0) Math.max(left, bracesLeft) else resultLeft
    resultRight = if (resultRight < 0) Math.max(right, bracesRight) else resultRight
    var result = Math.min(resultLeft, resultRight)
    result = if (result < 0) Math.max(resultLeft, resultRight) else result
    return result
}

fun String.isNumber(): Boolean {
    val pattern: Pattern = Pattern.compile("-?[0-9]+\\.?[0-9]*")
    val isNum: Matcher = pattern.matcher(this)
    return isNum.matches()
}

fun String.styleName(): String {
    var tail = ""
    var header = ""
    this.regex("(?<=\\:).*?(?=\\n)") {
        var value = it.trim().replace(",", "")
        if (value.isNumber()) {
            if (value.contains(".")) {
                value = value.replace(".", "")
            }
            tail = "${tail}_$value"
        } else {
            value = it.trim()
            if (value.contains(".")) {
                value = value.split(".")[1]
            }
            value = value.replace(")", "").replace("[", "").replace("]", "").replace(",", "_").replace("(", "")
                .replace(" ", "")
            if (TextUtils.isEmpty(header)) {
                header = value
            } else {
                header = "${header}_$value"
            }
        }

    }
    return "${header.toCamel()}$tail"
}

fun String.toStringFixed(): Double? {
    val format = DecimalFormat("0.##")
    //未保留小数的舍弃规则，RoundingMode.FLOOR表示直接舍弃。
    format.roundingMode = RoundingMode.FLOOR
    return format.format(this.toDouble())?.toDouble()
}

fun String.showCount(text: String, count: Int = 0): Int {
    val index = this.indexOf(text)
    var total = count
    if (index > 0) {
        total += 1
    } else {
        return total
    }
    val tail = this.substring(index, this.length)
    total = tail.showCount(text, total)
    return total
}
