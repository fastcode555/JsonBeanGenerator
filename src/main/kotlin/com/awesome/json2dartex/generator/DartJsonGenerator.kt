package com.awesome.json2dartex.generator

import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import toCamel
import toUpperCamel

//用于生成Dart对象的生成器
class DartJsonGenerator(var content: String, var fileName: String) : BaseGenerator(content, fileName) {

    override fun toJson(): String {
        val json = if (content.startsWith("{")) JSONObject.parseObject(content) else JSONArray.parse(content)
        val classes = ArrayList<java.lang.StringBuilder>()
        val classBuilder = parseJson(json, fileName.toUpperCamel(), classes)
        classes.forEach { classBuilder.append("\n").append(it) }
        return classBuilder.toString()
    }

}

fun parseJson(
    obj: Any?,
    className: String,
    classes: ArrayList<java.lang.StringBuilder>,
    enableToBean: Boolean = true
): java.lang.StringBuilder {
    val builder = StringBuilder()
    val toJsonMethod = StringBuilder("Map<String, dynamic> toJson() {\n Map<String, dynamic> map = Map();\n")
    val fromJsonMethod = StringBuilder("${className}.fromJson(Map<String, dynamic> json) {\n")

    var parseObj: JSONObject? = null
    if (obj is JSONObject) {
        parseObj = obj
    } else if (obj is JSONArray) {
        parseObj = obj[0] as JSONObject
    }
    builder.append("class $className{")
    for ((key, element) in parseObj!!.innerMap) {
        if (element is JSONObject) {
            builder.append("${key.toUpperCamel()} ${key.toCamel()};\n")
            toJsonMethod.append("if (this.${key.toCamel()} != null) map['$key'] = this.${key.toCamel()}.toJson();\n")
            fromJsonMethod.append("this.${key.toCamel()}=json['$key']!=null?${key.toUpperCamel()}.fromJson(json['$key']):null;\n")
            classes.add(parseJson(element, key.toUpperCamel(), classes, false))
        } else if (element is JSONArray) {
            if (element.isNotEmpty()) { //简单类型 List<String>.from(json['operations'])
                val result = element[0]
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
                builder.append("List<${key.toUpperCamel()}> ${key.toCamel()};\n")
                toJsonMethod.append("if (this.${key.toCamel()} != null) map['$key'] = this.${key.toCamel()}.map((v)=>v.toJson()).toList();\n")
                fromJsonMethod.append("this.${key.toCamel()}=json.asList<${key.toUpperCamel()}>('$key',(v)=>${key.toUpperCamel()}.fromJson(v));\n")
                classes.add(parseJson(JSONObject(), key.toUpperCamel(), classes, false))
            }
        } else {
            builder.append("${getType(element)} ${key.toCamel()};\n")
            toJsonMethod.append("if (this.${key.toCamel()} != null) map['$key'] = this.${key.toCamel()};\n")
            fromJsonMethod.append("this.${key.toCamel()}=json.${getParseType(element)}('$key');\n")
        }
    }
    builder.append(toJsonMethod.append("return map;\n}\n"))
    builder.append(fromJsonMethod.append("}\n"))
    if (enableToBean) {
        builder.append("static $className toBean(Map<String, dynamic> json) => ${className}.fromJson(json);\n")
    }
    builder.append("}")
    return builder
}

fun getParseType(element: Any): String {
    if (element is String) {
        return "asString"
    } else if (element is Int || element is Double || element is Float) {
        return "asNum"
    } else if (element is Boolean) {
        return "asBool"
    } else {
        return "asString"
    }
}

fun getType(element: Any): String {
    if (element is String) {
        return "String"
    } else if (element is Int || element is Double || element is Float) {
        return "num"
    } else if (element is Boolean) {
        return "bool"
    } else {
        return "String"
    }
}