package com.awesome.generators

import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import org.apache.http.util.TextUtils
import toCamel
import toUpperCamel

//用于生成Dart对象的生成器
class PythonJsonGenerator(
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
        classes.forEach { classBuilder.append("\n\n").append(it) }
        classBuilder.insert(0, "import json\n\n\n")
        return classBuilder.toString().trim()
    }

    private fun parseJson(
        obj: Any?,
        className: String,
        classes: ArrayList<java.lang.StringBuilder>,
    ): java.lang.StringBuilder {

        val uniqueClassName = generateUniqueClassName(className)

        val builder = StringBuilder()
        val fromJsonMethod = StringBuilder()

        var parseObj: JSONObject? = null
        if (obj is JSONObject) {
            parseObj = obj
        } else if (obj is JSONArray) {
            parseObj = obj[0] as JSONObject
        }
        builder.append(generateClassHeader(uniqueClassName))
        for ((key, element) in parseObj!!.innerMap) {
            if (element is JSONObject) {
                fromJsonMethod.append("\t\tself.${key.toCamel()} = ${key.toUpperCamel()}(_dict['$key']) if _dict.__contains__('$key') else None\n")
                classes.add(parseJson(element, key.toUpperCamel(), classes))
            } else if (element is JSONArray) {
                if (element.isNotEmpty()) { //简单类型 List<String>.from(json['operations'])
                    val result = element[0]
                    if (result is String || result is Int || result is Double || result is Boolean || result is Float) {
                        fromJsonMethod.append("\t\tself.${key.toCamel()} = []\n")
                        fromJsonMethod.append("\t\tif _dict.__contains__('$key') and isinstance(_dict['$key'], list):\n")
                        fromJsonMethod.append("\t\t\tfor element in _dict['$key']:\n")
                        fromJsonMethod.append("\t\t\t\tself.${key.toCamel()}.append(element)\n")
                    } else {//对象类型
                        fromJsonMethod.append("\t\tself.${key.toCamel()} = []\n")
                        fromJsonMethod.append("\t\tif _dict.__contains__('$key') and isinstance(_dict['$key'], list):\n")
                        fromJsonMethod.append("\t\t\tfor element in _dict['$key']:\n")
                        fromJsonMethod.append("\t\t\t\tself.${key.toCamel()}.append(${key.toUpperCamel()}(element))\n")
                        classes.add(parseJson(result, key.toUpperCamel(), classes))
                    }
                } else {//不明类型
                    fromJsonMethod.append("\t\tself.${key.toCamel()} = []\n")
                    fromJsonMethod.append("\t\tif _dict.__contains__('$key') and isinstance(_dict['$key'], list):\n")
                    fromJsonMethod.append("\t\t\tfor element in _dict['$key']:\n")
                    fromJsonMethod.append("\t\t\t\tself.${key.toCamel()}.append(${key.toUpperCamel()}(element))\n")
                    classes.add(parseJson(JSONObject(), key.toUpperCamel(), classes))
                }
            } else {
                fromJsonMethod.append("\t\tself.${key.toCamel()} = _dict['$key'] if _dict.__contains__('$key') else None\n")
            }
        }
        fromJsonMethod.insert(
            0,
            "\tdef __init__(self, *args):\n\t\tif len(args) == 0:\n\t\t\treturn\n\t\t_dict = json.loads(args[0]) if isinstance(args[0], str) else args[0]\n"
        )
        builder.append(fromJsonMethod)
        return builder
    }


    private fun generateClassHeader(className: String): String {
        val implements =
            if (implementClass.isNotEmpty()) " implements $implementClass" else ""
        return "class $className$implements(${if (extendsClass.isNotEmpty()) extendsClass else "object"}):\n"
    }

    private fun generateUniqueClassName(className: String): String {
        return if (classNames.contains(className)) {
            generateUniqueClassName("${className}x")
        } else {
            classNames.add(className)
            className
        }

    }
}