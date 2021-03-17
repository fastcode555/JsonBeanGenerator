package com.awesome.generators

import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import toCamel
import toUpperCamel
import java.lang.StringBuilder

class PythonBeanGenerator(
    var content: String,
    val fileName: String,
    val extendsClass: String,
    val implementClass: String
) :
    BaseGenerator(
        content, fileName, extendsClass, implementClass
    ) {

    override fun toJson(): String {
        val classes = ArrayList<java.lang.StringBuilder>()
        val classBuilder = parseJson(json, fileName.toUpperCamel(), classes)
        classes.forEach { classBuilder.append("\n").append(it) }
        classBuilder.insert(0, "import 'package:awesome_core/utils/json_parse_utils.dart';\n")
        return classBuilder.toString()
    }

    private fun parseJson(
        obj: Any?,
        className: String,
        classes: ArrayList<java.lang.StringBuilder>,
        enableToBean: Boolean = true
    ): java.lang.StringBuilder {
        val builder = StringBuilder()
        val toJsonMethod = StringBuilder("Map<String, dynamic> toJson() {\n Map<String, dynamic> map = Map();\n")
        val fromJsonMethod = StringBuilder("${className}.fromJson(Map<String, dynamic> json) {\n")
        val construtorMethod = StringBuilder("${className}(\n")

        var parseObj: JSONObject? = null
        if (obj is JSONObject) {
            parseObj = obj
        } else if (obj is JSONArray) {
            parseObj = obj[0] as JSONObject
        }
        builder.append(generateClassHeader(className))
        for ((key, element) in parseObj!!.innerMap) {
            if (element is JSONObject) {
                builder.append("${key.toUpperCamel()} ${key.toCamel()};\n")
                construtorMethod.append("this.${key.toCamel()},")
                toJsonMethod.append("if (this.${key.toCamel()} != null) map['$key'] = this.${key.toCamel()}.toJson();\n")
                fromJsonMethod.append("this.${key.toCamel()}=json.asBean('$key',(v)=>${key.toUpperCamel()}.fromJson(v));\n")
                classes.add(parseJson(element, key.toUpperCamel(), classes, false))
            } else if (element is JSONArray) {
                if (element.isNotEmpty()) { //简单类型 List<String>.from(json['operations'])
                    val result = element[0]
                    construtorMethod.append("this.${key.toCamel()},")
                    if (result is String || result is Int || result is Double || result is Boolean || result is Float) {
                        builder.append("List<${getType(result)}> ${key.toCamel()};\n")
                        toJsonMethod.append("if (this.${key.toCamel()} != null) map['$key'] = this.${key.toCamel()};\n")
                        fromJsonMethod.append("this.${key.toCamel()}=json.asList<${getType(result)}>('$key',null);\n")
                    } else {//对象类型
                        builder.append("List<${key.toUpperCamel()}> ${key.toCamel()};\n")
                        toJsonMethod.append("if (this.${key.toCamel()} != null) map['$key'] = this.${key.toCamel()}.map((v)=>v.toJson()).toList();\n")
                        fromJsonMethod.append("this.${key.toCamel()}=json.asList<${key.toUpperCamel()}>('$key',(v)=>${key.toUpperCamel()}.fromJson(v));\n")
                        classes.add(parseJson(result, key.toUpperCamel(), classes, false))
                    }
                } else {//不明类型
                    construtorMethod.append("this.${key.toCamel()},")
                    builder.append("List<${key.toUpperCamel()}> ${key.toCamel()};\n")
                    toJsonMethod.append("if (this.${key.toCamel()} != null) map['$key'] = this.${key.toCamel()}.map((v)=>v.toJson()).toList();\n")
                    fromJsonMethod.append("this.${key.toCamel()}=json.asList<${key.toUpperCamel()}>('$key',(v)=>${key.toUpperCamel()}.fromJson(v));\n")
                    classes.add(parseJson(JSONObject(), key.toUpperCamel(), classes, false))
                }
            } else {
                construtorMethod.append("this.${key.toCamel()},")
                builder.append("${getType(element)} ${key.toCamel()};\n")
                toJsonMethod.append("if (this.${key.toCamel()} != null) map['$key'] = this.${key.toCamel()};\n")
                fromJsonMethod.append("this.${key.toCamel()}=json.${getParseType(element)}('$key');\n")
            }
        }
        builder.append(construtorMethod.append(");"))
        builder.append(toJsonMethod.append("return map;\n}\n"))
        builder.append(fromJsonMethod.append("}\n"))
        if (enableToBean) {
            builder.append("static $className toBean(Map<String, dynamic> json) => ${className}.fromJson(json);\n")
        }
        builder.append("}")
        return builder
    }

    private fun generateClassHeader(className: String): String {
        val extends = if (extendsClass != null && extendsClass.isNotEmpty()) " extends $extendsClass" else ""
        val implements =
            if (implementClass != null && implementClass.isNotEmpty()) " implements $implementClass" else ""
        return "class $className$extends$implements{"
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