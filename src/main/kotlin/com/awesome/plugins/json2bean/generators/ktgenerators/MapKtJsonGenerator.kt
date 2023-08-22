package com.awesome.plugins.json2bean.generators.ktgenerators

import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.awesome.plugins.json2bean.generators.BaseGenerator
import com.intellij.psi.PsiDirectory
import toUpperCamel
import java.io.File
import java.math.BigDecimal

/**
 * 用于直接生成Map对象
 **/
class MapKtJsonGenerator(
    content: String,
    private val fileName: String,
    private val extendsClass: String,
    private val implementClass: String,
    private val psiDir: PsiDirectory
) :
    BaseGenerator(
        content
    ) {
    val classNames = ArrayList<String>()

    override fun toString(): String {
        val classes = HashMap<String, java.lang.StringBuilder>()
        val classBuilder = parseJson(json, fileName.toUpperCamel(), classes)
        classes.forEach { (key, builder) ->
            classBuilder.append("\n\n").append(builder)
        }
        return classBuilder.toString().trim()
    }

    /**
     * 生成对应的文件
     **/
    fun generate() {
        val classes = HashMap<String, java.lang.StringBuilder>()
        val classBuilder = parseJson(json, fileName.toUpperCamel(), classes)
        val file = File(psiDir.virtualFile.path, "${fileName.toUpperCamel()}.kt")
        file.writeText(classBuilder.toString())
        classes.forEach { (key, builder) ->
            val file = File(psiDir.virtualFile.path, "${key}.kt")
            file.writeText(builder.toString())
        }
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
            parseObj = obj[0] as JSONObject
        }
        builder.append(generateClassHeader(uniqueClassName))
        for ((key, element) in parseObj!!.innerMap) {
            if (element is JSONObject) {
                builder.append("${key.prefix()}${key.toUpperCamel()}?,\n")
                classes[key.toUpperCamel()] = parseJson(element, key.toUpperCamel(), classes)
            } else if (element is JSONArray) {
                if (element.isNotEmpty()) {
                    val result = element[0]
                    if (result is String || result is Int || result is Double || result is Boolean || result is Float) {
                        builder.append("${key.prefix()}List<${getType(result)}>?,\n")
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
        return "    val ${this}: "
    }

    private fun generateClassHeader(className: String): String {
        var finalImplementClass = implementClass
        val extends = if (extendsClass.isNotEmpty()) " extends $extendsClass" else ""
        val implements =
            if (finalImplementClass.isNotEmpty()) " with $finalImplementClass" else ""
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
        if (element is String) {
            return "String"
        } else if (element is Int) {
            return "Int"
        } else if (element is Double || element is BigDecimal) {
            return "Double"
        } else if (element is Float) {
            return "Float"
        } else if (element is Boolean) {
            return "Boolean"
        } else {
            return "String"
        }
    }
}
