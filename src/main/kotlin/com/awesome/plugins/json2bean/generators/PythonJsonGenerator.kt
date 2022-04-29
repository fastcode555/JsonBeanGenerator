package com.awesome.plugins.json2bean.generators

import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
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

    override fun toString(): String {
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
        val fromJsonTurpleMethod = StringBuilder()
        val toJsonMethod = StringBuilder()
        val toJsonHeaderMethod = StringBuilder()

        var parseObj: JSONObject? = null
        if (obj is JSONObject) {
            parseObj = obj
        } else if (obj is JSONArray) {
            parseObj = obj[0] as JSONObject
        }
        builder.append(generateClassHeader(uniqueClassName))
        var count = 1
        for ((key, element) in parseObj!!.innerMap) {
            if (element is JSONObject) {
                fromJsonMethod.append("\t\t\tself.${key.toCamel()} = ${key.toUpperCamel()}(_dict['$key']) if _dict.__contains__('$key') else None\n")
                fromJsonTurpleMethod.append("\t\t\tself.${key.toCamel()} = ${key.toUpperCamel()}(json.loads(_dict[$count])) if len(_dict) > $count and isinstance(_dict[$count], str) and len(_dict[$count]) > 0 else None\n")
                toJsonMethod.append("\t\t\t'${key}': self.${key.toCamel()}.toJson() if self.${key.toCamel()} is not None else None,\n")
                classes.add(parseJson(element, key.toUpperCamel(), classes))
            } else if (element is JSONArray) {
                if (element.isNotEmpty()) { //简单类型 List<String>.from(json['operations'])
                    val result = element[0]
                    if (result is String || result is Int || result is Double || result is Boolean || result is Float) {
                        fromJsonMethod.append("\t\t\tself.${key.toCamel()} = []\n")
                        fromJsonMethod.append("\t\t\tif _dict.__contains__('$key') and isinstance(_dict['$key'], list):\n")
                        fromJsonMethod.append("\t\t\t\tfor element in _dict['$key']:\n")
                        fromJsonMethod.append("\t\t\t\t\tself.${key.toCamel()}.append(element)\n")
                        fromJsonTurpleMethod.append("\t\t\tself.${key.toCamel()} = json.loads(_dict[$count]) if len(_dict) > ${count} else None\n")
                    } else {//对象类型
                        fromJson(
                            fromJsonMethod,
                            fromJsonTurpleMethod,
                            toJsonMethod,
                            toJsonHeaderMethod,
                            key,
                            classes,
                            result,
                            count
                        )
                    }
                } else {//不明类型
                    fromJson(
                        fromJsonMethod,
                        fromJsonTurpleMethod,
                        toJsonMethod,
                        toJsonHeaderMethod,
                        key,
                        classes,
                        JSONObject(),
                        count
                    )
                }
            } else {
                fromJsonMethod.append("\t\t\tself.${key.toCamel()} = _dict['$key'] if _dict.__contains__('$key') else None\n")
                fromJsonTurpleMethod.append("\t\t\tself.${key.toCamel()} = _dict[$count] if len(_dict) > ${count} else None\n")
                toJsonMethod.append("\t\t\t'${key}': self.${key.toCamel()},\n")
            }
            count += 1
        }
        fromJsonTurpleMethod.insert(
            0,
            "\tdef __init__(self, *args):\n\t\tif len(args) == 0:\n\t\t\treturn\n\t\t_dict = json.loads(args[0]) if isinstance(args[0], str) else args[0]\n\t\tif isinstance(_dict, tuple):\n\t\t\tself.id = _dict[0] if len(_dict) > 0 else None\n"
        )
        builder.append(fromJsonTurpleMethod)
        builder.append("\t\telse:\n")
        builder.append(fromJsonMethod)

        toJsonMethod.insert(0, "\n\tdef toJson(self):\n${toJsonHeaderMethod.toString()}\t\treturn {\n")
        toJsonMethod.append("\t\t}\n")
        builder.append(toJsonMethod)
        return builder
    }


    private fun fromJson(
        fromJsonMethod: StringBuilder,
        fromJsonTurpleMethod: StringBuilder,
        toJsonMethod: StringBuilder,
        toJsonHeaderMethod: StringBuilder,
        key: String?,
        classes: ArrayList<java.lang.StringBuilder>,
        result: Any?,
        count: Int
    ) {
        fromJsonMethod.append("\t\t\tself.${key.toCamel()} = []\n")
        fromJsonMethod.append("\t\t\tif _dict.__contains__('$key') and isinstance(_dict['$key'], list):\n")
        fromJsonMethod.append("\t\t\t\tfor element in _dict['$key']:\n")
        fromJsonMethod.append("\t\t\t\t\tself.${key.toCamel()}.append(${key.toUpperCamel()}(element))\n")

        fromJsonTurpleMethod.append("\t\t\tself.${key.toCamel()} = []\n")
        fromJsonTurpleMethod.append("\t\t\tif len(_dict) > ${count} and _dict[$count] is not None and isinstance(_dict[$count], str):\n")
        fromJsonTurpleMethod.append("\t\t\t\tfor element in json.loads(_dict[$count]):\n")
        fromJsonTurpleMethod.append("\t\t\t\t\tself.${key.toCamel()}.append(${key.toUpperCamel()}(element))\n")


        toJsonHeaderMethod.append("\t\t_${key.toCamel()} = []\n")
        toJsonHeaderMethod.append("\t\tfor element in self.${key.toCamel()}:\n")
        toJsonHeaderMethod.append("\t\t\t_${key.toCamel()}.append(element.toJson())\n")
        toJsonMethod.append("\t\t\t'${key}': _${key.toCamel()},\n")
        classes.add(parseJson(result, key.toUpperCamel(), classes))
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