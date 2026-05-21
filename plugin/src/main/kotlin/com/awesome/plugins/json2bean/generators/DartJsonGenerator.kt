package com.awesome.plugins.json2bean.generators

import com.alibaba.fastjson2.JSONArray
import com.alibaba.fastjson2.JSONObject
import mergeKeys
import toCamel
import toUpperCamel
import java.math.BigDecimal

/**
 * 用于生成对应的dart对象
 **/
class DartJsonGenerator(
    content: String,
    private val fileName: String,
    private val extendsClass: String,
    private val implementClass: String,
    private val sqliteSupport: Boolean,
    private val primaryKey: String,
    private val needClone: Boolean,
    private val splitGFile: Boolean = false,
) :
    BaseGenerator(
        content
    ) {
    val classNames = ArrayList<String>()

    /**
     * 双文件生成结果。partContent / partFileName 在 splitGFile=false 时为 null。
     */
    data class Output(
        val mainContent: String,
        val partContent: String?,
        val mainFileName: String,
        val partFileName: String?,
    )

    /**
     * 拆分模式下，部件方法体内引用字段需要加 "i." 前缀；非拆分模式留空。
     */
    private val selfRef: String = if (splitGFile) "i." else ""

    /**
     * Part 文件的累积内容（仅在 splitGFile=true 时填充）。
     */
    private val partBuilder = StringBuilder()

    fun generate(): Output {
        val classes = ArrayList<java.lang.StringBuilder>()
        val classBuilder = parseJson(json, fileName.toUpperCamel(), classes, sqliteSupport)
        classes.forEach { classBuilder.append("\n").append(it) }

        // 头部 import
        if (sqliteSupport) {
            classBuilder.insert(0, "import 'package:json2dart_db/json2dart_db.dart';\n")
        }
        classBuilder.insert(0, "import 'dart:convert';\n\nimport 'package:json2dart_safe/json2dart.dart';\n")

        val mainFileName = "$fileName.dart"
        if (!splitGFile) {
            return Output(
                mainContent = classBuilder.toString(),
                partContent = null,
                mainFileName = mainFileName,
                partFileName = null,
            )
        }

        // 拆分模式：在 import 之后、首个 class 之前插入 part 指令。
        val classMarker = "\nclass "
        val classStart = classBuilder.indexOf(classMarker)
        val partDirective = "\npart '$fileName.g.dart';\n"
        if (classStart >= 0) {
            classBuilder.insert(classStart, partDirective)
        } else {
            classBuilder.append(partDirective)
        }

        val partFileName = "$fileName.g.dart"
        val partFull = StringBuilder()
        partFull.append("part of '$fileName.dart';\n")
        partFull.append(partBuilder)

        return Output(
            mainContent = classBuilder.toString(),
            partContent = partFull.toString(),
            mainFileName = mainFileName,
            partFileName = partFileName,
        )
    }

    override fun toString(): String {
        val out = generate()
        if (out.partContent == null) return out.mainContent
        return buildString {
            append("// ===== ${out.mainFileName} =====\n")
            append(out.mainContent)
            append("\n\n// ===== ${out.partFileName} =====\n")
            append(out.partContent)
        }
    }

    private fun parseJson(
        obj: Any?,
        className: String,
        classes: ArrayList<java.lang.StringBuilder>,
        sqliteEnable: Boolean = false,
    ): java.lang.StringBuilder {

        val uniqueClassName = generateUniqueClassName(className)

        val builder = StringBuilder()
        val fromJsonMethod = StringBuilder()
        val cloneMethod = StringBuilder()
        val requiredConstructorMethod = StringBuilder()
        val constructorMethod = StringBuilder()
        val toJsonMethod = StringBuilder()

        var parseObj: JSONObject? = null
        if (obj is JSONObject) {
            parseObj = obj
        } else if (obj is JSONArray) {
            parseObj = obj.mergeKeys() as JSONObject
        }
        builder.append(generateClassHeader(uniqueClassName, sqliteEnable))
        for ((key, element) in parseObj!!) {
            val camel = key.toCamel()
            val upper = key.toUpperCamel()
            if (element is JSONObject) {
                builder.append("  $upper? $camel;\n")
                constructorMethod.append("    this.$camel,\n")
                toJsonMethod.append("        '$key': $selfRef$camel?.toJson(),\n")
                fromJsonMethod.append("      $camel: json.asBean('$key', $upper.fromJson),\n")
                cloneMethod.append("        $camel: $selfRef$camel?.clone(),\n")
                classes.add(parseJson(element, upper, classes))
                continue
            }
            if (element is JSONArray) {
                if (element.isNotEmpty()) { //简单类型 List<String>.from(json['operations'])
                    val result = element.mergeKeys()
                    if (result is String || result is Int || result is Double || result is Boolean || result is Float) {
                        requiredConstructorMethod.append("    required this.$camel,\n")
                        builder.append("  List<${getType(result)}>? $camel;\n")
                        toJsonMethod.append("        '$key': $selfRef$camel,\n")
                        fromJsonMethod.append("      $camel: json.asList<${getType(result)}>('$key'),\n")
                        cloneMethod.append("        $camel: List<${getType(result)}>.from($selfRef$camel??[]),\n")
                        continue
                    }
                    if (result is JSONArray) {
                        //二维数组类型
                        val item = result.mergeKeys()
                        if (item is String || item is Int || item is Double || item is Boolean || item is Float) {
                            requiredConstructorMethod.append("    required this.$camel,\n")
                            val listType = "${getType(item)}"
                            builder.append("  List<List<${listType}>>? $camel;\n")
                            toJsonMethod.append("        '$key': $selfRef$camel,\n")
                            fromJsonMethod.append("      $camel: json.asArray2d<${listType}>('$key'),\n")
                            cloneMethod.append(
                                "        $camel: $selfRef$camel?.map((e) => List<${getType(item)}>.from(e)).toList(),\n"
                            )
                        } else {
                            constructorMethod.append("    this.$camel,\n")
                            val listType = upper
                            builder.append("  List<List<$listType>>? $camel;\n")
                            toJsonMethod.append("        '$key': $selfRef$camel?.map((v) => v.map((e) => e.toJson()).toList()).toList(),\n")
                            fromJsonMethod.append("      $camel: json.asArray2d<${listType}>('$key', $upper.fromJson),\n")
                            classes.add(parseJson(item, upper, classes))
                            cloneMethod.append("        $camel: $selfRef$camel?.map((v) => v.map((e) => e.clone()).toList()).toList(),\n")
                        }
                        continue
                    }
                    //对象类型
                    constructorMethod.append("    this.$camel,\n")
                    builder.append("  List<$upper>? $camel;\n")
                    toJsonMethod.append("        '$key': $selfRef$camel?.map((v) => v.toJson()).toList(),\n")
                    fromJsonMethod.append("      $camel: json.asList<$upper>('$key', $upper.fromJson),\n")
                    classes.add(parseJson(result, upper, classes))
                    cloneMethod.append("        $camel: $selfRef$camel?.map((v) => v.clone()).toList(),\n")
                } else {//不明类型
                    constructorMethod.append("    this.$camel,\n")
                    builder.append("  List<$upper>? $camel;\n")
                    toJsonMethod.append("        '$key': $selfRef$camel?.map((v) => v.toJson()).toList(),\n")
                    fromJsonMethod.append("      $camel: json.asList<$upper>('$key', $upper.fromJson),\n")
                    classes.add(parseJson(JSONObject(), upper, classes))
                    cloneMethod.append("        $camel: $selfRef$camel?.map((v) => v.clone()).toList(),\n")
                }
                continue
            }
            requiredConstructorMethod.append("    required this.$camel,\n")
            builder.append("  ${getType(element, true)} $camel;\n")
            toJsonMethod.append("        '$key': $selfRef$camel,\n")
            fromJsonMethod.append("      $camel: json.${getParseType(element)}('$key'),\n")
            cloneMethod.append("        $camel: $selfRef$camel,\n")
        }

        requiredConstructorMethod.append(constructorMethod)
        val isNeed2AddPrimayKey = !requiredConstructorMethod.contains(primaryKey)
        if (sqliteEnable && isNeed2AddPrimayKey) {
            builder.append("  int? ${primaryKey.toCamel()};\n")
        }

        builder.append(construtorMethod(requiredConstructorMethod, uniqueClassName, sqliteEnable))

        // clone — 拆分模式下，主类放瘦委托，方法体进 partBuilder
        appendCloneSection(builder, cloneMethod, uniqueClassName, sqliteEnable)

        // toJson — 拆分模式下，主类放瘦委托，方法体进 partBuilder
        appendToJsonSection(builder, toJsonMethod, uniqueClassName, sqliteEnable, isNeed2AddPrimayKey)

        // fromJson — 拆分模式下，主类只保留 factory 委托，方法体进 partBuilder
        appendFromJsonSection(builder, fromJsonMethod, uniqueClassName, sqliteEnable, isNeed2AddPrimayKey)

        // sqlite 类绑定成员永远留在主类（part 文件无法添加类成员）
        if (sqliteEnable) {
            val dataPrimaryKey = primaryKey.toCamel()
            builder.append("\n @override\n Map<String, dynamic> primaryKeyAndValue() => {\"${primaryKey}\": $dataPrimaryKey};\n\n")
            builder.append("  @override\n  int get hashCode => $dataPrimaryKey?.hashCode ?? super.hashCode;\n\n")
            builder.append("  @override\n  bool operator ==(Object other) {\n    if (other is $uniqueClassName && $dataPrimaryKey != null) {\n      return other.$dataPrimaryKey == $dataPrimaryKey;\n    }\n    return super == other;\n  }\n")
        }
        builder.append("\n  @override\n  String toString() => jsonEncode(toJson());\n")
        builder.append("}")
        return builder
    }

    /**
     * 输出 clone 段：
     * - 拆分关闭：行为与改造前一致（方法体内联主类）。
     * - 拆分开启：主类只放 `Foo clone() => _$FooClone(this);`，方法体走 partBuilder。
     */
    private fun appendCloneSection(
        builder: StringBuilder,
        cloneBody: StringBuilder,
        uniqueClassName: String,
        sqliteEnable: Boolean,
    ) {
        if (!needClone) return

        // sqlite 主键字段补齐（参照原行 206-208 的位置和判定）
        if (sqliteEnable && !cloneBody.contains(primaryKey.toCamel())) {
            cloneBody.append("        ${primaryKey.toCamel()}: $selfRef${primaryKey.toCamel()},\n")
        }

        if (!splitGFile) {
            // 与改造前一致：方法体内联
            if (cloneBody.isNotEmpty()) {
                cloneBody.insert(0, "\n $uniqueClassName clone() => $uniqueClassName(\n")
                cloneBody.append("      );\n\n")
            } else {
                cloneBody.append("\n $uniqueClassName clone() => $uniqueClassName();\n\n")
            }
            builder.append(cloneBody)
            return
        }

        // 拆分模式：主类瘦委托
        builder.append("\n $uniqueClassName clone() => _\$${uniqueClassName}Clone(this);\n\n")

        // part 文件:顶层函数
        if (cloneBody.isNotEmpty()) {
            partBuilder.append("\n$uniqueClassName _\$${uniqueClassName}Clone($uniqueClassName i) => $uniqueClassName(\n")
            partBuilder.append(cloneBody)
            partBuilder.append("    );\n")
        } else {
            partBuilder.append("\n$uniqueClassName _\$${uniqueClassName}Clone($uniqueClassName i) => $uniqueClassName();\n")
        }
    }

    /**
     * 输出 toJson 段。
     */
    private fun appendToJsonSection(
        builder: StringBuilder,
        toJsonBody: StringBuilder,
        uniqueClassName: String,
        sqliteEnable: Boolean,
        isNeed2AddPrimayKey: Boolean,
    ) {
        val isBodyNotEmpty = toJsonBody.isNotEmpty()
        val needsOverride = sqliteEnable && isNeed2AddPrimayKey

        if (!splitGFile) {
            // 与改造前一致：方法体内联
            if (isBodyNotEmpty) {
                if (needsOverride) {
                    toJsonBody.insert(0, "\n  @override\n  Map<String, dynamic> toJson() => {\n")
                    toJsonBody.append("        '$primaryKey': ${primaryKey.toCamel()}\n")
                } else {
                    toJsonBody.insert(0, "\n  Map<String, dynamic> toJson() => {\n")
                }
                builder.append(toJsonBody).append("      };\n")
            } else {
                toJsonBody.insert(0, "\n  Map<String, dynamic> toJson() => {};\n")
                builder.append(toJsonBody)
            }
            return
        }

        // 拆分模式：主类瘦委托
        val overridePrefix = if (needsOverride) "  @override\n" else ""
        builder.append("\n${overridePrefix}  Map<String, dynamic> toJson() => _\$${uniqueClassName}ToJson(this);\n")

        // part 文件：顶层函数，体内引用通过 selfRef ("i.") 取字段值
        if (isBodyNotEmpty) {
            partBuilder.append("\nMap<String, dynamic> _\$${uniqueClassName}ToJson($uniqueClassName i) => {\n")
            partBuilder.append(toJsonBody)
            if (needsOverride) {
                partBuilder.append("        '$primaryKey': i.${primaryKey.toCamel()}\n")
            }
            partBuilder.append("    };\n")
        } else {
            partBuilder.append("\nMap<String, dynamic> _\$${uniqueClassName}ToJson($uniqueClassName i) => {};\n")
        }
    }

    /**
     * 输出 fromJson 段。
     */
    private fun appendFromJsonSection(
        builder: StringBuilder,
        fromJsonBody: StringBuilder,
        uniqueClassName: String,
        sqliteEnable: Boolean,
        isNeed2AddPrimayKey: Boolean,
    ) {
        if (sqliteEnable && isNeed2AddPrimayKey) {
            fromJsonBody.append("     ${primaryKey.toCamel()} : json.asInt('$primaryKey'),\n")
        }

        if (!splitGFile) {
            // 与改造前一致：factory 内联完整方法体
            if (fromJsonBody.isNotEmpty()) {
                fromJsonBody.insert(
                    0,
                    "\n  factory ${uniqueClassName}.fromJson(Map json) {\n    return ${uniqueClassName}(\n"
                )
                builder.append(fromJsonBody.append("    );\n  }\n"))
            } else {
                fromJsonBody.append("\n  factory ${uniqueClassName}.fromJson(Map json) {\n    return ${uniqueClassName}();\n  }\n")
                builder.append(fromJsonBody)
            }
            return
        }

        // 拆分模式：主类瘦 factory 委托
        builder.append("\n  factory ${uniqueClassName}.fromJson(Map json) => _\$${uniqueClassName}FromJson(json);\n")

        // part 文件：顶层 factory 函数
        if (fromJsonBody.isNotEmpty()) {
            partBuilder.append("\n$uniqueClassName _\$${uniqueClassName}FromJson(Map json) => $uniqueClassName(\n")
            partBuilder.append(fromJsonBody)
            partBuilder.append("    );\n")
        } else {
            partBuilder.append("\n$uniqueClassName _\$${uniqueClassName}FromJson(Map json) => $uniqueClassName();\n")
        }
    }

    /**
     * 构造器的方法
     **/
    private fun construtorMethod(
        construtorMethod: StringBuilder,
        uniqueClassName: String,
        sqliteEnable: Boolean
    ): StringBuilder {
        if (sqliteEnable && !construtorMethod.contains(primaryKey.toCamel())) {
            construtorMethod.append("    this.${primaryKey.toCamel()},\n")
        }
        if (construtorMethod.isNotEmpty()) {
            construtorMethod.insert(0, "\n  ${uniqueClassName}({\n")
            construtorMethod.append("  });\n")
        } else {
            construtorMethod.insert(0, "  ${uniqueClassName}(")
            construtorMethod.append(");\n")
        }
        return construtorMethod
    }

    private fun generateClassHeader(className: String, sqliteEnable: Boolean): String {
        var finalImplementClass = implementClass
        if (sqliteEnable) {
            finalImplementClass = "BaseDbModel"
        }
        val extends = if (extendsClass.isNotEmpty()) " extends $extendsClass" else ""
        val implements =
            if (finalImplementClass.isNotEmpty()) " with $finalImplementClass" else ""
        return "\nclass $className$extends$implements {\n"
    }

    private fun generateUniqueClassName(className: String): String {
        if (classNames.contains(className)) {
            return generateUniqueClassName("${className}x")
        } else {
            classNames.add(className)
            return className
        }
    }

    private fun getParseType(element: Any): String {
        if (element is String) return "asString"
        if (element is Int || element is Long) return "asInt"
        if (element is Double || element is Float || element is BigDecimal) return "asDouble"
        if (element is Boolean) return "asBool"
        return "asString"
    }

    private fun getType(element: Any, isFinal: Boolean = false): String {
        if (element is String) return if (isFinal) "final String" else "String"
        if (element is Int || element is Long) return if (isFinal) "final int" else "int"
        if (element is Double || element is Float || element is BigDecimal) return if (isFinal) "final double" else "double"
        if (element is Boolean) return if (isFinal) "final bool" else "bool"
        return if (isFinal) "final String" else "String"
    }
}
