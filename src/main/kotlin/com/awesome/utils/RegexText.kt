package com.awesome.utils

object RegexText {
    fun getNum(value: String): Double? = value.regexOne(number)?.toDoubleOrNull()

    /**
     * 匹配有变量的代码，形似:
     *   Condition? condition;
     *要求有空格符开头，这样是为了筛选出更多的精准的Bean模型
     **/
    const val number = "[0-9.]+"
    const val variableRegex = "^([ ]*)([a-zA-Z\\<\\>]*)\\? [a-zA-Z]*;\$"
    const val variableConstStringRegex = "static const String .*? = '.*?';"
    const val variableConstNameRegex = "(?<=static const String ).*?(?= = '.*?';)"
    const val colorConstRegex = "static const Color .*? = Color\\(.*?\\);"
    const val colorConstNameRegex = "(?<=static const Color ).*?(?= = Color\\(.*?\\);)"
}
