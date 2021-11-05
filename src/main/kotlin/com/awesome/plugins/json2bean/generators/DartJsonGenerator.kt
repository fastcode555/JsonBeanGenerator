package com.awesome.plugins.json2bean.generators

import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import toCamel
import toUpperCamel

//用于生成Dart对象的生成器
class DartJsonGenerator(
    content: String,
    val fileName: String,
    val extendsClass: String,
    val implementClass: String
) :
    BaseGenerator(
        content
    ) {
    val classNames = ArrayList<String>()

    override fun toJson(): String {
        val classes = ArrayList<java.lang.StringBuilder>()
        val classBuilder = parseJson(json, fileName.toUpperCamel(), classes)
        classes.forEach { classBuilder.append("\n").append(it) }
        classBuilder.insert(0, "import 'package:json2dart_safe/json2dart.dart';\n")
        return classBuilder.toString()
    }

    private fun parseJson(
        obj: Any?,
        className: String,
        classes: ArrayList<java.lang.StringBuilder>,
        enableToBean: Boolean = true
    ): java.lang.StringBuilder {

        val uniqueClassName = generateUniqueClassName(className)

        val builder = StringBuilder()
        val fromJsonMethod = StringBuilder("\n\t${uniqueClassName}.fromJson(Map json) {\n")
        val construtorMethod = StringBuilder()
        var toJsonMethod = StringBuilder()

        var parseObj: JSONObject? = null
        if (obj is JSONObject) {
            parseObj = obj
        } else if (obj is JSONArray) {
            parseObj = obj[0] as JSONObject
        }
        builder.append(generateClassHeader(uniqueClassName))
        for ((key, element) in parseObj!!.innerMap) {
            if (element is JSONObject) {
                builder.append("\t${key.toUpperCamel()}? ${key.toCamel()};\n")
                construtorMethod.append("this.${key.toCamel()},")
                toJsonMethod.append("\t\t\t..put('$key',this.${key.toCamel()}?.toJson())\n")
                fromJsonMethod.append("\t\tthis.${key.toCamel()}=json.asBean('$key',(v)=>${key.toUpperCamel()}.fromJson(v));\n")
                classes.add(parseJson(element, key.toUpperCamel(), classes, false))
            } else if (element is JSONArray) {
                if (element.isNotEmpty()) { //简单类型 List<String>.from(json['operations'])
                    val result = element[0]
                    construtorMethod.append("this.${key.toCamel()},")
                    if (result is String || result is Int || result is Double || result is Boolean || result is Float) {
                        builder.append("\tList<${getType(result)}>? ${key.toCamel()};\n")
                        toJsonMethod.append("\t\t\t..put('$key',this.${key.toCamel()})\n")
                        fromJsonMethod.append("\tthis.${key.toCamel()}=json.asList<${getType(result)}>('$key',null);\n")
                    } else {//对象类型
                        builder.append("\tList<${key.toUpperCamel()}>? ${key.toCamel()};\n")
                        toJsonMethod.append("\t\t\t..put('$key', this.${key.toCamel()}?.map((v)=>v.toJson()).toList())\n")
                        fromJsonMethod.append("\t\tthis.${key.toCamel()}=json.asList<${key.toUpperCamel()}>('$key',(v)=>${key.toUpperCamel()}.fromJson(v));\n")
                        classes.add(parseJson(result, key.toUpperCamel(), classes, false))
                    }
                } else {//不明类型
                    construtorMethod.append("this.${key.toCamel()},")
                    builder.append("\tList<${key.toUpperCamel()}>? ${key.toCamel()};\n")
                    toJsonMethod.append("\t\t\t..put('$key',this.${key.toCamel()}?.map((v)=>v.toJson()).toList())\n")
                    fromJsonMethod.append("\t\tthis.${key.toCamel()}=json.asList<${key.toUpperCamel()}>('$key',(v)=>${key.toUpperCamel()}.fromJson(v));\n")
                    classes.add(parseJson(JSONObject(), key.toUpperCamel(), classes, false))
                }
            } else {
                construtorMethod.append("this.${key.toCamel()},")
                builder.append("\t${getType(element)}? ${key.toCamel()};\n")
                toJsonMethod.append("\t\t\t..put('$key',this.${key.toCamel()})\n")
                fromJsonMethod.append("\t\tthis.${key.toCamel()}=json.${getParseType(element)}('$key');\n")
            }
        }
        if (construtorMethod.isNotEmpty()) {
            construtorMethod.insert(0, "\n\t${uniqueClassName}({")
            construtorMethod.append("});\n")
            builder.append(construtorMethod.toString())
        }
        val isToJsonNotEmpty = toJsonMethod.isNotEmpty()

        if (isToJsonNotEmpty) {
            val index = toJsonMethod.lastIndexOf("\n")
            toJsonMethod.delete(index, index + 1)
            toJsonMethod.append(";\n")
            toJsonMethod.insert(
                0,
                "\n\tMap<String, dynamic> toJson() {\n\t\treturn Map<String, dynamic>()\n"
            )

        } else {
            toJsonMethod.insert(
                0,
                "\n\tMap<String, dynamic> toJson() {\n\t\treturn Map<String, dynamic>();\n"
            )
        }
        builder.append(toJsonMethod.append("\t}\n"))

        builder.append(fromJsonMethod.append("\t}\n"))
        if (enableToBean) {
            builder.append("\n\tstatic $uniqueClassName toBean(Map json) => ${uniqueClassName}.fromJson(json);\n")
        }
        builder.append("}")
        return builder
    }

    private fun generateClassHeader(className: String): String {
        val extends = if (extendsClass.isNotEmpty()) " extends $extendsClass" else ""
        val implements =
            if (implementClass.isNotEmpty()) " implements $implementClass" else ""
        return "\nclass $className$extends$implements{\n"
    }

    private fun generateUniqueClassName(className: String): String {
        return if (classNames.contains(className)) {
            generateUniqueClassName("${className}x")
        } else {
            classNames.add(className)
            className
        }

    }

    private fun getParseType(element: Any): String {
        if (element is String) {
            return "asString"
        } else if (element is Int) {
            return "asInt"
        } else if (element is Double || element is Float) {
            return "asDouble"
        } else if (element is Boolean) {
            return "asBool"
        } else {
            return "asString"
        }
    }

    private fun getType(element: Any): String {
        if (element is String) {
            return "String"
        } else if (element is Int) {
            return "int"
        } else if (element is Double || element is Float) {
            return "double"
        } else if (element is Boolean) {
            return "bool"
        } else {
            return "String"
        }
    }
}