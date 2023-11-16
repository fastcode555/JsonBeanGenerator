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
    const val colorConstRegex2 = "Colours\\..*?\\: Color\\(.*?\\)"

    //static const Color black = Color(0xff000000);匹配出black这个名字
    const val colorConstNameRegex = "(?<=static const Color ).*?(?= = Color\\(.*?\\);)"
    const val colorConstNameRegex2 = "(?<=Colours\\.).*?(?=\\:)"

    //匹配出Color(0xff000000)中的0xff000000
    const val colorConstValueRegex = "(?<=Color\\().*?(?=\\))"

    /**
     * 匹配出偏转的角度
     **/
    const val degConstValueRegex = "[0-9]+(?=deg)"

    /**
     * 匹配出颜色值
     **/
    const val colorConstValue = "(?<=\\#)[A-Fa-f0-9]+"

    /**
     *匹配出属性值，数字
     **/
    const val colorConstOpacity = "[0-9.]+(?=%)"
    //linear-gradient(90deg, #FDE068 0%, #FED55B 35%, #F6C35B 100%)
}
