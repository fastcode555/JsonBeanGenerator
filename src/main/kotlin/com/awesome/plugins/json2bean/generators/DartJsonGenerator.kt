package com.awesome.plugins.json2bean.generators

import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import org.apache.http.util.TextUtils
import toCamel
import toUpperCamel

//用于生成Dart对象的生成器
class DartJsonGenerator(
    content: String,
    private val fileName: String,
    private val extendsClass: String,
    private val implementClass: String,
    private val sqliteSupport: Boolean,
    private val primaryKey: String,
) :
    BaseGenerator(
        content
    ) {
    val classNames = ArrayList<String>()

    override fun toString(): String {
        val classes = ArrayList<java.lang.StringBuilder>()
        val classBuilder = parseJson(json, fileName.toUpperCamel(), classes)
        classes.forEach { classBuilder.append("\n").append(it) }
        if (sqliteSupport) {
            classBuilder.insert(0, "import 'package:json2dart_db/json2dart_db.dart';\n")
        }
        classBuilder.insert(0, "import 'package:json2dart_safe/json2dart.dart';\nimport 'dart:convert';\n")
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
        builder.append(generateClassHeader(uniqueClassName, enableToBean))
        for ((key, element) in parseObj!!.innerMap) {
            if (element is JSONObject) {
                builder.append("\t${key.toUpperCamel()}? ${key.toCamel()};\n")
                construtorMethod.append("this.${key.toCamel()},")
                toJsonMethod.append("\t\t\t'$key':${key.toCamel()}?.toJson(),\n")
                fromJsonMethod.append("\t\t${key.toCamel()} = json.asBean('$key',(v)=>${key.toUpperCamel()}.fromJson(v));\n")
                classes.add(parseJson(element, key.toUpperCamel(), classes, false))
            } else if (element is JSONArray) {
                if (element.isNotEmpty()) { //简单类型 List<String>.from(json['operations'])
                    val result = element[0]
                    construtorMethod.append("this.${key.toCamel()},")
                    if (result is String || result is Int || result is Double || result is Boolean || result is Float) {
                        builder.append("\tList<${getType(result)}>? ${key.toCamel()};\n")
                        toJsonMethod.append("\t\t\t'$key':${key.toCamel()},\n")
                        fromJsonMethod.append("\t\t${key.toCamel()} = json.asList<${getType(result)}>('$key');\n")
                    } else {//对象类型
                        builder.append("\tList<${key.toUpperCamel()}>? ${key.toCamel()};\n")
                        toJsonMethod.append("\t\t\t'$key':${key.toCamel()}?.map((v)=>v.toJson()).toList(),\n")
                        fromJsonMethod.append("\t\t${key.toCamel()} = json.asList<${key.toUpperCamel()}>('$key',(v)=>${key.toUpperCamel()}.fromJson(v));\n")
                        classes.add(parseJson(result, key.toUpperCamel(), classes, false))
                    }
                } else {//不明类型
                    construtorMethod.append("this.${key.toCamel()},")
                    builder.append("\tList<${key.toUpperCamel()}>? ${key.toCamel()};\n")
                    toJsonMethod.append("\t\t\t'$key':${key.toCamel()}?.map((v)=>v.toJson()).toList(),\n")
                    fromJsonMethod.append("\t\t${key.toCamel()} = json.asList<${key.toUpperCamel()}>('$key',(v)=>${key.toUpperCamel()}.fromJson(v));\n")
                    classes.add(parseJson(JSONObject(), key.toUpperCamel(), classes, false))
                }
            } else {
                construtorMethod.append("this.${key.toCamel()},")
                builder.append("\t${getType(element)}? ${key.toCamel()};\n")
                toJsonMethod.append("\t\t\t'$key':${key.toCamel()},\n")
                fromJsonMethod.append("\t\t${key.toCamel()} = json.${getParseType(element)}('$key');\n")
            }
        }
        //如果这个key不存在，则需要主动添加这个key
        if (enableToBean) {
            addThePrimaryKey(builder, fromJsonMethod, toJsonMethod, construtorMethod)
        }

        if (construtorMethod.isNotEmpty()) {
            construtorMethod.insert(0, "\n\t${uniqueClassName}({")
            construtorMethod.append("});\n")
            builder.append(construtorMethod.toString())
        }
        val isToJsonNotEmpty = toJsonMethod.isNotEmpty()

        if (isToJsonNotEmpty) {
            if (sqliteSupport && enableToBean) {
                toJsonMethod.insert(
                    0,
                    "\n\t@override\n\tMap<String, dynamic> toJson() => {\n"
                )
            } else {
                toJsonMethod.insert(0, "\n\tMap<String, dynamic> toJson() => {\n")
            }

        } else {
            toJsonMethod.insert(0, "\n\tMap<String, dynamic> toJson() => {\n")
        }
        builder.append(toJsonMethod).append("\t\t};\n")

        builder.append(fromJsonMethod.append("\t}\n"))
        if (enableToBean) {
            builder.append("\n\tstatic $uniqueClassName toBean(Map json) => ${uniqueClassName}.fromJson(json);\n")
            if (sqliteSupport) {
                val dataPrimaryKey = primaryKey.toCamel()
                builder.append("\n\t@override\n\tMap<String, dynamic> primaryKeyAndValue() => {\"${primaryKey}\": $dataPrimaryKey};\n\n")
                builder.append("  @override\n  int get hashCode => $dataPrimaryKey?.hashCode ?? super.hashCode;\n\n")
                builder.append("  @override\n  bool operator ==(Object other) {\n    if (other is $uniqueClassName && $dataPrimaryKey != null) {\n      return other.$dataPrimaryKey == $dataPrimaryKey;\n    }\n    return super == other;\n  }\n")
            }
        }
        builder.append("\n  @override\n  String toString() => jsonEncode(toJson());\n")
        builder.append("}")
        return builder
    }

    private fun addThePrimaryKey(
        builder: StringBuilder,
        fromJsonMethod: StringBuilder,
        toJsonMethod: StringBuilder,
        construtorMethod: StringBuilder
    ) {
        if (!TextUtils.isEmpty(primaryKey) && !builder.contains(" ${primaryKey.toCamel()};")) {
            builder.append("\tint? ${primaryKey.toCamel()};\n")
            construtorMethod.append("this.${primaryKey.toCamel()},")
            fromJsonMethod.append("\t\t${primaryKey.toCamel()} = json.asInt('$primaryKey');\n")
            toJsonMethod.append("\t\t\t'$primaryKey':${primaryKey.toCamel()}\n")
        }
    }

    private fun generateClassHeader(className: String, enableToBean: Boolean): String {
        var finalImplementClass = implementClass
        if (sqliteSupport && enableToBean) {
            finalImplementClass = "BaseDbModel"
        }
        val extends = if (extendsClass.isNotEmpty()) " extends $extendsClass" else ""
        val implements =
            if (finalImplementClass.isNotEmpty()) " with $finalImplementClass" else ""
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