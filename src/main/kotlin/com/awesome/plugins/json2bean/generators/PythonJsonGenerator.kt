package com.awesome.plugins.json2bean.generators

import com.alibaba.fastjson2.JSONArray
import com.alibaba.fastjson2.JSONObject
import mergeKeys
import toLowerUnderScore
import toUpperCamel

/**
 * 用于生成对应的Python对象
 **/
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
        return classBuilder.toString().trim().replace("\t", "  ")
    }

    private fun parseJson(
        obj: Any?,
        className: String,
        classes: ArrayList<java.lang.StringBuilder>,
    ): java.lang.StringBuilder {

        val uniqueClassName = generateUniqueClassName(className)

        val builder = StringBuilder()
        val initHeaderMethod = StringBuilder("\tdef __init__(self,")
        val initMethod = StringBuilder()
        val fromJsonMethod = StringBuilder()
        val fromJsonTurpleMethod = StringBuilder()
        val toJsonMethod = StringBuilder()
        val toJsonHeaderMethod = StringBuilder()

        var parseObj: JSONObject? = null
        if (obj is JSONObject) {
            parseObj = obj
        } else if (obj is JSONArray) {
            parseObj = obj.mergeKeys() as JSONObject
        }
        builder.append(generateClassHeader(uniqueClassName))
        var count = 0
        for ((key, element) in parseObj!!) {
            initMethod.append("\t\tself.${key.toLowerUnderScore()} = ${key.toLowerUnderScore()}\n")
            if (element is JSONObject) {
                initHeaderMethod.append("${key.toLowerUnderScore()} = None,")
                fromJsonMethod.append("\t\t\t\t${key.toLowerUnderScore()} = ${key.toUpperCamel()}.fromJson(_dict['$key']) if _dict.__contains__('$key') else None,\n")
                fromJsonTurpleMethod.append("\t\t\t\t${key.toLowerUnderScore()} = ${key.toUpperCamel()}.fromJson(json.loads(_dict[$count])) if len(_dict) > $count and isinstance(_dict[$count], str) and len(_dict[$count]) > 0 else None,\n")
                toJsonMethod.append("\t\t\t'${key}': self.${key.toLowerUnderScore()}.toJson() if self.${key.toLowerUnderScore()} is not None else None,\n")
                classes.add(parseJson(element, key.toUpperCamel(), classes))
            } else if (element is JSONArray) {
                if (element.isNotEmpty()) { //简单类型 List<String>.from(json['operations'])
                    val result = element.mergeKeys()
                    if (result is String || result is Int || result is Double || result is Boolean || result is Float) {
                        initHeaderMethod.append("${key.toLowerUnderScore()}: List[${getParseType(result)}] = None,")
                        fromJsonMethod.append("\t\t\t\t${key.toLowerUnderScore()} = [element for element in _dict['$key'] if element],\n")

                        fromJsonTurpleMethod.append("\t\t\t\t${key.toLowerUnderScore()} = json.loads(_dict[$count]) if len(_dict) > $count else None,\n")
                    } else {//对象类型
                        initHeaderMethod.append("${key.toLowerUnderScore()}: list = None,")
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
                    initHeaderMethod.append("${key.toLowerUnderScore()} = None,")
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
                initHeaderMethod.append("${key.toLowerUnderScore()}: ${getParseType(element)} = None,")
                fromJsonMethod.append("\t\t\t\t${key.toLowerUnderScore()} = _dict.get('$key'),\n")
                fromJsonTurpleMethod.append("\t\t\t\t${key.toLowerUnderScore()} = _dict[$count] if len(_dict) > $count else None,\n")
                toJsonMethod.append("\t\t\t'${key}': self.${key.toLowerUnderScore()},\n")
            }
            count += 1
        }
        fromJsonTurpleMethod.insert(
            0,
            "\t@classmethod\n\tdef fromJson(cls, *args):\n\t\tif len(args) == 0:\n\t\t\treturn\n\t\t_dict = json.loads(args[0]) if isinstance(args[0], str) else args[0]\n\t\tif isinstance(_dict, tuple):\n\t\t\treturn cls(\n"
        )

        builder.append(initHeaderMethod.substring(0, initHeaderMethod.length - 1)).append("):\n")
        builder.append(initMethod).append("\n")
        builder.append(fromJsonTurpleMethod).append("\t\t\t)\n")
        builder.append("\t\telse:\n")
        builder.append("\t\t\treturn cls(\n").append(fromJsonMethod).append("\t\t\t)\n")

        toJsonMethod.insert(0, "\n\tdef toJson(self):\n${toJsonHeaderMethod.toString()}\t\treturn {\n")
        toJsonMethod.append("\t\t}\n")
        builder.append(toJsonMethod)
        builder.append("\n\tdef toString(self):\n\t\treturn json.dumps(self.toJson(), indent=2, ensure_ascii=False)")
        return builder
    }

    private fun getParseType(result: Any): String {
        if (result is String) {
            return "str"
        } else if (result is Int) {
            return "int"
        } else if (result is Boolean) {
            return "bool"
        } else if (result is Double || result is Float) {
            return "float"
        } else if (result is JSONArray || result is List<*>) {
            return "list"
        }
        return "str"
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
        fromJsonMethod.append("\t\t\t\t${key.toLowerUnderScore()} = [${key.toUpperCamel()}.fromJson(element) for element in _dict.get('$key',[]) if element],\n")
        fromJsonTurpleMethod.append("\t\t\t\t${key.toLowerUnderScore()} = [${key.toUpperCamel()}.fromJson(element) for element in json.loads(_dict[$count]) if element]\n\t\t\t\t if len(_dict) > ${count} and _dict[$count] and isinstance(_dict[$count], str) else [],\n")
        toJsonMethod.append("\t\t\t'${key}': [element.toJson() for element in self.${key.toLowerUnderScore()} if element],\n")
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
