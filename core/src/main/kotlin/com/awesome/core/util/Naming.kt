package com.awesome.core.util

import com.google.common.base.CaseFormat

fun String?.toCamel(): String {
    var result = this.clearSymbol()?.trim()
    if (this == result && !result!!.contains("_")) {
        val firstWord = result.substring(0, 1)
        result = firstWord.lowercase() + result.substring(1, result.length)
        return if (KEYS.contains(result)) "${result}x" else result
    }
    result = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, result)
    return if (KEYS.contains(result)) "${result}x" else result!!
}

fun String.firstUpperCamel(): String {
    val header = this.substring(0, 1).uppercase()
    val tail = this.substring(1, this.length)
    return "$header$tail"
}

fun String?.toUpperCamel(): String {
    if (this.isNullOrEmpty()) return ""
    if (this.contains("_") || this.contains(" ")) {
        val result = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, this.clearSymbol())
        return if (UPPER_KEYS.contains(result)) "${result}x" else result
    }
    val result = this.clearSymbol()?.firstUpperCamel()
    return if (UPPER_KEYS.contains(result)) "${result}x" else "$result"
}

fun String?.toLowerUnderScore(): String {
    if (this.isNullOrEmpty()) return ""
    return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, this.clearSymbol().toCamel())
}

fun String?.toUpperUnderScore(): String {
    if (this.isNullOrEmpty()) return ""
    return CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, this.clearSymbol().toCamel())
}

fun String?.clearSymbol(): String? {
    if (this.isNullOrEmpty()) return this
    var finalKey: String = this
    Regex(REGEX_SYMBOL).findAll(this).forEach { match ->
        val it = match.value
        if (it.trim().isEmpty()) return@forEach
        finalKey = finalKey.replace(it, "_")
    }
    return finalKey.replace("\n", "").replace("\\", "").removeStartSymbol()
}

fun String.removeStartSymbol(): String {
    var value = this
    if (this.startsWith("_")) {
        value = value.substring(1, value.length)
        return value.removeStartSymbol()
    }
    return this
}
