package com.awesome.utils

object RegexText {

    /**
     * 匹配有变量的代码，形似:
     *   Condition? condition;
     *要求有空格符开头，这样是为了筛选出更多的精准的Bean模型
     **/
    const val variableRegex = "^([ ]*)([a-zA-Z\\<\\>]*)\\? [a-zA-Z]*;\$"
    const val variableConstStringRegex = "static const String .*? = '.*?';"
}
