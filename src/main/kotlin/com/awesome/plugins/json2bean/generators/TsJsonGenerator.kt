package com.awesome.plugins.json2bean.generators

import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import toUpperCamel

/**
 * 用于生成TypeScript对象
 **/
class TsJsonGenerator(
    content: String,
    private val fileName: String,
    private val extendsClass: String,
    private val implementClass: String,
) :
    BaseGenerator(
        content
    ) {
    val classNames = ArrayList<String>()

    override fun toString(): String {
        val classes = ArrayList<java.lang.StringBuilder>()
        val classBuilder = parseJson(json, fileName.toUpperCamel(), classes)
        classes.forEach {
            classBuilder.append("\n\n").append(it)
        }
        return classBuilder.toString().trim()
    }

    private fun parseJson(
        obj: Any?,
        className: String,
        classes: ArrayList<java.lang.StringBuilder>,
    ): java.lang.StringBuilder {

        val uniqueClassName = generateUniqueClassName(className)

        val builder = StringBuilder()

        var parseObj: JSONObject? = null
        if (obj is JSONObject) {
            parseObj = obj
        } else if (obj is JSONArray) {
            parseObj = obj[0] as JSONObject
        }
        builder.append(generateClassHeader(uniqueClassName))
        for ((key, element) in parseObj!!.innerMap) {
            if (element is JSONObject) {
                builder.append("  ${key}?: ${key.toUpperCamel()}\n")
                classes.add(parseJson(element, key.toUpperCamel(), classes))
            } else if (element is JSONArray) {
                if (element.isNotEmpty()) {
                    val result = element[0]
                    if (result is String || result is Int || result is Double || result is Boolean || result is Float) {
                        builder.append("  ${key}?: ${getType(result)}[]\n")
                    } else {//对象类型
                        builder.append("  ${key}?: ${key.toUpperCamel()}[]\n")
                        classes.add(parseJson(result, key.toUpperCamel(), classes))
                    }
                } else {//不明类型
                    builder.append("  ${key}?: ${key.toUpperCamel()}[]\n")
                    classes.add(parseJson(JSONObject(), key.toUpperCamel(), classes))
                }
            } else {
                builder.append("  ${key}?: ${getType(element)}\n")
            }
        }
        builder.append("}")
        return builder
    }

    private fun generateClassHeader(className: String): String {
        var finalImplementClass = implementClass
        val extends = if (extendsClass.isNotEmpty()) " extends $extendsClass" else ""
        val implements =
            if (finalImplementClass.isNotEmpty()) " with $finalImplementClass" else ""
        return "export interface $className$extends$implements {\n"
    }

    private fun generateUniqueClassName(className: String): String {
        return if (classNames.contains(className)) {
            generateUniqueClassName("${className}x")
        } else {
            classNames.add(className)
            className
        }
    }

    private fun getType(element: Any): String {
        return when (element) {
            (element is String) -> "string"
            (element is Int || element is Double || element is Float) -> "number"
            (element is Boolean) -> "boolean"
            else -> "string"
        }
    }
}
