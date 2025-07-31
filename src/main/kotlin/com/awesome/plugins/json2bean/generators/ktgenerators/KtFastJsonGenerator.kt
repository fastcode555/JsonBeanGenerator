package com.awesome.plugins.json2bean.generators.ktgenerators

import com.alibaba.fastjson2.JSONArray
import com.alibaba.fastjson2.JSONObject
import com.awesome.plugins.json2bean.generators.BaseGenerator
import com.intellij.psi.PsiDirectory
import mergeKeys
import toCamel
import toUpperCamel
import java.io.File
import java.math.BigDecimal

/**
 * 使用Gson解析生成的对象
 **/
class KtFastJsonGenerator(
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
        classes.forEach { (_, builder) ->
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
        write2File(classes.keys, fileName.toUpperCamel(), classBuilder)
        //生成对应文件值
        classes.forEach { (key, builder) ->
            write2File(classes.keys, key, builder)
        }
    }

    private fun write2File(keys: MutableSet<String>, key: String, builder: java.lang.StringBuilder) {
        val headerBuilder = StringBuilder()
        headerBuilder.append("package ${psiDir.virtualFile.nameWithoutExtension}\n\n")
        keys.forEach {
            if ((builder.contains(" $it") || builder.contains("<$it")) && !builder.contains("class $it")) {
                headerBuilder.append("import $it\n")
            }
        }
        headerBuilder.append("import com.alibaba.fastjson2.annotation.JSONField\n\n")
        val file = File(psiDir.virtualFile.path, "${key}.kt")
        builder.insert(0, headerBuilder)
        file.writeText(builder.toString())
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
        builder.append(generateClassHeader(uniqueClassName))
        for ((key, element) in parseObj!!) {
            if (element is JSONObject) {
                builder.append("${key.prefix()}${key.toUpperCamel()}?,\n")
                classes[key.toUpperCamel()] = parseJson(element, key.toUpperCamel(), classes)
            } else if (element is JSONArray) {
                if (element.isNotEmpty()) {
                    val result = element.mergeKeys()
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
        return "    @JSONField(name = \"$this\") val ${this.toCamel()}: "
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
        if (element is String) return "String"
        if (element is Int) return "Int"
        if (element is Double || element is BigDecimal) return "Double"
        if (element is Float) return "Float"
        if (element is Boolean) return "Boolean"
        return "String"
    }
}
