package com.awesome.core.generators.kt

import com.alibaba.fastjson2.JSONArray
import com.alibaba.fastjson2.JSONObject
import com.awesome.core.generators.BaseGenerator
import com.awesome.core.util.mergeKeys
import com.awesome.core.util.toCamel
import com.awesome.core.util.toUpperCamel
import java.math.BigDecimal

/**
 * 使用FastJson解析生成的对象
 **/
class KtFastJsonGenerator(
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
        val classes = HashMap<String, java.lang.StringBuilder>()
        val classBuilder = parseJson(json, fileName.toUpperCamel(), classes)
        classes.forEach { (_, builder) ->
            classBuilder.append("\n\n").append(builder)
        }
        return classBuilder.toString().trim()
    }

    private fun parseJson(
        obj: Any?,
        className: String,
        classes: HashMap<String, java.lang.StringBuilder>,
    ): java.lang.StringBuilder {

        val uniqueClassName = generateUniqueClassName(className)

        val builder = StringBuilder()

        var parseObj: JSONObject? = null
        if (obj is JSONObject) {
            parseObj = obj
        } else if (obj is JSONArray) {
            parseObj = obj.mergeKeys() as JSONObject
        }
        if (parseObj!!.isEmpty()) {
            builder.append(generateClassHeader(uniqueClassName, isEmpty = true))
            return builder
        }
        builder.append(generateClassHeader(uniqueClassName))
        for ((key, element) in parseObj) {
            if (element is JSONObject) {
                builder.append("${key.prefix()}${key.toUpperCamel()}?,\n")
                classes[key.toUpperCamel()] = parseJson(element, key.toUpperCamel(), classes)
            } else if (element is JSONArray) {
                if (element.isNotEmpty()) {
                    val result = element.mergeKeys()
                    if (result is String || result is Int || result is Double || result is Boolean || result is Float) {
                        builder.append("${key.prefix()}List<${getType(result)}>?,\n")
                    } else if (result is JSONArray) {
                        // 二维数组：与 DartJsonGenerator 对齐
                        val item = result.mergeKeys()
                        if (item is String || item is Int || item is Double || item is Boolean || item is Float) {
                            builder.append("${key.prefix()}List<List<${getType(item)}>>?,\n")
                        } else {
                            builder.append("${key.prefix()}List<List<${key.toUpperCamel()}>>?,\n")
                            classes[key.toUpperCamel()] = parseJson(item, key.toUpperCamel(), classes)
                        }
                    } else {//对象类型
                        builder.append("${key.prefix()}List<${key.toUpperCamel()}>?,\n")
                        classes[key.toUpperCamel()] = parseJson(result, key.toUpperCamel(), classes)
                    }
                } else {//不明类型
                    builder.append("${key.prefix()}List<${key.toUpperCamel()}>?,\n")
                    classes[key.toUpperCamel()] = parseJson(JSONObject(), key.toUpperCamel(), classes)
                }
            } else {
                builder.append("${key.prefix()}${getType(element)}?,\n")
            }
        }
        builder.append(")")
        return builder
    }

    private fun String.prefix(): String {
        return "    @JSONField(name = \"$this\") val ${this.toCamel()}: "
    }

    private fun generateClassHeader(className: String, isEmpty: Boolean = false): String {
        var finalImplementClass = implementClass
        val extends = if (extendsClass.isNotEmpty()) " extends $extendsClass" else ""
        val implements =
            if (finalImplementClass.isNotEmpty()) " with $finalImplementClass" else ""
        // 空字段的类不能用 data class（Kotlin 要求至少一个主构造参数）
        if (isEmpty) return "class $className$extends$implements\n"
        return "data class $className$extends$implements (\n"
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
        if (element is String) return "String"
        if (element is Int) return "Int"
        if (element is Long) return "Long"
        if (element is Double || element is BigDecimal) return "Double"
        if (element is Float) return "Float"
        if (element is Boolean) return "Boolean"
        return "String"
    }
}
