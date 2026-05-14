# Dart `.g.dart` 拆分生成 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Let users opt into emitting Dart entity classes as two files — a slim `<name>.dart` (fields + thin delegates) plus a `<name>.g.dart` part file containing the `fromJson` / `toJson` / `clone` method bodies, mirroring `json_serializable` conventions so AI tools reading the model burn fewer tokens.

**Architecture:** A new `splitGFile: Boolean` flag flows from a `JsonBeanDialog` checkbox through `GeneratorHelper` into `DartJsonGenerator`. The generator gains a `generate(): Output` method returning both file contents; its existing `toString()` keeps producing a single concatenated string for the preview path. When the flag is on, `JsonBeanDialog.onGenerate()` writes two files; when off, behavior is byte-for-byte unchanged. Other generators (TS / Python / Kotlin) and the sqlite DAO writer are untouched.

**Tech Stack:** Kotlin 2.1.10, IntelliJ Platform Gradle Plugin 2.2.0, Swing forms (`.form` UI Designer), fastjson2 for JSON parsing.

**Reference spec:** `docs/superpowers/specs/2026-05-14-dart-g-file-split-design.md`

**Testing posture:** This project has no `src/test/...` directory or test framework configured. Each task ends with a `./gradlew compileKotlin` build check. Task 6 is a manual smoke test in the sandbox IDE following the spec's verification checklist.

---

## Task 1: Add `splitGFile` to PluginProps

**Files:**
- Modify: `src/main/kotlin/com/awesome/common/PluginProps.kt`

- [ ] **Step 1: Add the property key constant**

Edit `PluginProps.kt` — append the following constant **inside** the `object PluginProps { ... }` block, just after the existing `translationKey` line (~line 41):

```kotlin
    ///是否在生成 Dart 时拆分出 .g.dart part 文件
    const val splitGFile = "plugin.splitGFile"
```

- [ ] **Step 2: Verify compilation**

Run: `./gradlew compileKotlin`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Commit**

```bash
git add src/main/kotlin/com/awesome/common/PluginProps.kt
git commit -m "feat: add splitGFile property key for Dart .g.dart toggle"
```

---

## Task 2: Refactor `DartJsonGenerator` to emit two-file output

This is the core change. We add a `splitGFile` constructor parameter, an `Output` data class, a `generate(): Output` method, and split the per-field content into a main builder vs a part builder based on the flag. When the flag is off, output is byte-for-byte identical to today.

**Files:**
- Modify: `src/main/kotlin/com/awesome/plugins/json2bean/generators/DartJsonGenerator.kt`

- [ ] **Step 1: Replace the entire file**

Overwrite `DartJsonGenerator.kt` with this content (paths and behavior preserved for `splitGFile=false`):

```kotlin
package com.awesome.plugins.json2bean.generators

import com.alibaba.fastjson2.JSONArray
import com.alibaba.fastjson2.JSONObject
import mergeKeys
import toCamel
import toUpperCamel

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
        // import 段以最后一个 import 的换行结束，class 段以 "\nclass " 开头，
        // 在那个位置之前插入 part 行。
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
        if (element is Int) return "asInt"
        if (element is Double || element is Float) return "asDouble"
        if (element is Boolean) return "asBool"
        return "asString"
    }

    private fun getType(element: Any, isFinal: Boolean = false): String {
        if (element is String) return if (isFinal) "final String" else "String"
        if (element is Int) return if (isFinal) "final int" else "int"
        if (element is Double || element is Float) return if (isFinal) "final double" else "double"
        if (element is Boolean) return if (isFinal) "final bool" else "bool"
        return if (isFinal) "final String" else "String"
    }
}
```

Notes on this rewrite:
- New `splitGFile` parameter with default `false` so existing callers compile unchanged.
- `selfRef` ("i." vs "") is the only place per-field generation differs between modes — keeping the loop body unified.
- `appendCloneSection` / `appendToJsonSection` / `appendFromJsonSection` are extracted from the old inline blocks (lines 134-167 in the original). When `splitGFile=false`, each one reproduces the original behavior exactly (same comment/whitespace shape).
- The old `cloneMethod(...)` helper is replaced by `appendCloneSection`; the old construction was returning a StringBuilder that the caller appended — we now append directly to `builder`, simplifying the control flow.

- [ ] **Step 2: Verify compilation**

Run: `./gradlew compileKotlin`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Quick non-split sanity check (smoke)**

Open a Kotlin REPL or insert the following one-off in a scratch file to confirm `splitGFile=false` still produces the same string the old code did. (Skip if confident in the diff — the structural transform was mechanical.)

```kotlin
// Optional one-off check; delete after running
val gen = com.awesome.plugins.json2bean.generators.DartJsonGenerator(
    content = """{"name":"alice","age":18}""",
    fileName = "user",
    extendsClass = "",
    implementClass = "",
    sqliteSupport = false,
    primaryKey = "",
    needClone = true,
    splitGFile = false,
)
println(gen.toString())
```
Expected: output begins with `import 'dart:convert';` and contains a single `class User` with inlined `clone() => User(...)`, `Map<String, dynamic> toJson() => { ... }`, and `factory User.fromJson(Map json) { return User(...); }`. No `part` directive.

- [ ] **Step 4: Commit**

```bash
git add src/main/kotlin/com/awesome/plugins/json2bean/generators/DartJsonGenerator.kt
git commit -m "feat: add splitGFile mode to DartJsonGenerator for .g.dart output"
```

---

## Task 3: Add `dartGenerate` to GeneratorHelper

`json2Bean(...)` stays as-is for preview/non-Dart callers. The dialog's Dart write path will use a new helper that returns the full `Output` object.

**Files:**
- Modify: `src/main/kotlin/com/awesome/plugins/json2bean/utils/GeneratorHelper.kt`

- [ ] **Step 1: Add `dartGenerate` and thread `splitGFile` through `json2Bean`**

Replace the contents of `GeneratorHelper.kt` with:

```kotlin
package com.awesome.plugins.json2bean.utils

import com.awesome.plugins.json2bean.generators.DartJsonGenerator
import com.awesome.plugins.json2bean.generators.PythonJsonGenerator
import com.awesome.plugins.json2bean.generators.TsJsonGenerator
import com.awesome.plugins.json2bean.generators.ktgenerators.KtFastJsonGenerator
import com.awesome.plugins.json2bean.generators.ktgenerators.KtGsonGenerator
import com.awesome.plugins.json2bean.generators.ktgenerators.MapKtJsonGenerator
import com.intellij.psi.PsiDirectory

object GeneratorHelper {

    /**
     * json转成bean的方法（预览或非 Dart 路径用，返回单个字符串）
     **/
    fun json2Bean(
        fileType: String, content: String,
        className: String,
        extendName: String,
        impName: String,
        isSqliteEnable: Boolean,
        primaryKey: String,
        depType: String,
        psiDirectory: PsiDirectory,
        needClone: Boolean,
        splitGFile: Boolean = false,
    ): String {
        if (fileType == ".dart") {
            return DartJsonGenerator(
                content,
                className,
                extendName,
                impName,
                isSqliteEnable,
                primaryKey,
                needClone,
                splitGFile,
            ).toString()
        } else if (fileType == ".ts") {
            return TsJsonGenerator(
                content,
                className,
                extendName,
                impName
            ).toString()
        } else if (fileType == ".py") {
            return PythonJsonGenerator(
                content,
                className,
                extendName,
                impName
            ).toString()
        }
        return DartJsonGenerator(
            content,
            className,
            extendName,
            impName,
            isSqliteEnable,
            primaryKey,
            needClone,
            splitGFile,
        ).toString()
    }

    /**
     * Dart 双文件生成入口：写文件路径用，返回主文件与可选 part 文件的内容。
     */
    fun dartGenerate(
        content: String,
        className: String,
        extendName: String,
        impName: String,
        isSqliteEnable: Boolean,
        primaryKey: String,
        needClone: Boolean,
        splitGFile: Boolean,
    ): DartJsonGenerator.Output {
        return DartJsonGenerator(
            content,
            className,
            extendName,
            impName,
            isSqliteEnable,
            primaryKey,
            needClone,
            splitGFile,
        ).generate()
    }

    fun json2KtOrJava(
        fileType: String,
        fileName: String,
        content: String,
        extendName: String,
        impName: String,
        depType: String,
        psiDirectory: PsiDirectory
    ) {
        if (fileType == ".kt") {
            if (depType == "none") {
                MapKtJsonGenerator(content, fileName, extendName, impName, psiDirectory).generate()
            } else if (depType == "gson") {
                KtGsonGenerator(content, fileName, extendName, impName, psiDirectory).generate()
            } else if (depType == "fastjson") {
                KtFastJsonGenerator(content, fileName, extendName, impName, psiDirectory).generate()
            }
        } else if (fileType == ".java") {

        }
    }

}
```

Key points:
- `json2Bean` adds `splitGFile: Boolean = false` (default keeps existing callers compiling — `DartJsonGenerator`'s preview path now reflects the toggle).
- `dartGenerate` is the new entry; it constructs the same generator and returns the structured `Output`.

- [ ] **Step 2: Verify compilation**

Run: `./gradlew compileKotlin`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Commit**

```bash
git add src/main/kotlin/com/awesome/plugins/json2bean/utils/GeneratorHelper.kt
git commit -m "feat: add GeneratorHelper.dartGenerate for two-file Dart output"
```

---

## Task 4: Add `Split .g.dart` checkbox to JsonBeanDialog form

`mMethodPanel` (id `ab17f`) currently has `column-count="1"` and houses only `rbClone`. We bump column count to 2 and add a second JCheckBox bound to `cbSplitGFile`.

**Files:**
- Modify: `src/main/kotlin/com/awesome/plugins/json2bean/JsonBeanDialog.form`

- [ ] **Step 1: Edit `mMethodPanel` to add the new checkbox**

In `JsonBeanDialog.form`, locate the block:

```xml
      <grid id="ab17f" binding="mMethodPanel" layout-manager="GridLayoutManager" row-count="1" column-count="1" same-size-horizontally="false" same-size-vertically="false" hgap="-1" vgap="-1">
```

Change `column-count="1"` to `column-count="2"`.

Then locate the closing tag of the `mMethodPanel` `<children>` block (the line `</children>` immediately followed by `</grid>` and another `</children>` and `</grid>`). Right before that inner `</children>`, insert this new component:

```xml
          <component id="d8a01" class="javax.swing.JCheckBox" binding="cbSplitGFile">
            <constraints>
              <grid row="0" column="1" row-span="1" col-span="1" vsize-policy="0" hsize-policy="3" anchor="8" fill="0" indent="0" use-parent-layout="false"/>
            </constraints>
            <properties>
              <text value="Split .g.dart"/>
            </properties>
          </component>
```

So the resulting `mMethodPanel` block looks like:

```xml
      <grid id="ab17f" binding="mMethodPanel" layout-manager="GridLayoutManager" row-count="1" column-count="2" same-size-horizontally="false" same-size-vertically="false" hgap="-1" vgap="-1">
        <margin top="0" left="0" bottom="0" right="0"/>
        <constraints>
          <grid row="3" column="0" row-span="1" col-span="1" vsize-policy="3" hsize-policy="3" anchor="0" fill="3" indent="0" use-parent-layout="false"/>
        </constraints>
        <properties/>
        <border type="none"/>
        <children>
          <component id="4e150" class="javax.swing.JRadioButton" binding="rbClone">
            <constraints>
              <grid row="0" column="0" row-span="1" col-span="1" vsize-policy="0" hsize-policy="3" anchor="8" fill="0" indent="0" use-parent-layout="false"/>
            </constraints>
            <properties>
              <text value="clone"/>
            </properties>
          </component>
          <component id="d8a01" class="javax.swing.JCheckBox" binding="cbSplitGFile">
            <constraints>
              <grid row="0" column="1" row-span="1" col-span="1" vsize-policy="0" hsize-policy="3" anchor="8" fill="0" indent="0" use-parent-layout="false"/>
            </constraints>
            <properties>
              <text value="Split .g.dart"/>
            </properties>
          </component>
        </children>
      </grid>
```

The label `Split .g.dart` matches existing English UI labels (Sqlite Support, Format, Generate, clone, Gson, FastJson).

- [ ] **Step 2: Verify compilation**

Run: `./gradlew compileKotlin`
Expected: `BUILD SUCCESSFUL` (form file isn't compiled here, but this confirms nothing else broke).

The `.form` binding will fail at runtime if `JsonBeanDialog.kt` doesn't declare a matching `cbSplitGFile` field — that's added in Task 5.

- [ ] **Step 3: Commit (deferred)**

Hold the commit until after Task 5 so the form change and its Kotlin binding land together (otherwise a build between the two would have a dangling binding).

---

## Task 5: Wire `cbSplitGFile` in JsonBeanDialog.kt

Bind the new checkbox, persist its state to properties, mirror the visibility pattern used by `cbSqlite`/`mMethodPanel`, and split the file-writing flow into a dual-write Dart path.

**Files:**
- Modify: `src/main/kotlin/com/awesome/plugins/json2bean/JsonBeanDialog.kt`

- [ ] **Step 1: Add the field declaration**

In `JsonBeanDialog.kt`, locate the field declarations near the top of the class (around line 82, just after `rbClone`):

```kotlin
    //是否支持Clone的方法
    var rbClone: JRadioButton? = null
```

Immediately after that line, add:

```kotlin
    //是否生成 .g.dart 拆分文件
    var cbSplitGFile: JCheckBox? = null
```

- [ ] **Step 2: Update `json2Bean()` to pass the new flag**

Locate the `json2Bean()` method (around line 174-186). Change it to:

```kotlin
    /**
     *  将Json转成Bean对象
     **/
    private fun json2Bean(): String {
        return GeneratorHelper.json2Bean(
            fileType, tvField!!.text,
            tvClassField!!.text,
            tvExtends!!.text,
            tvImplements!!.text,
            isSqliteEnable(),
            tvPrimaryKeyListener.getText(),
            depType,
            mDirectory,
            rbClone!!.isSelected,
            cbSplitGFile?.isSelected == true,
        )
    }
```

(Adds the new positional argument; null-safe in case the field hasn't been wired yet on first call.)

- [ ] **Step 3: Replace `onGenerate()` to take the dual-write Dart path**

Replace the entire `onGenerate()` method (currently lines 132-162) with:

```kotlin
    private fun onGenerate() {
        tvError?.text = ""
        if (isEmpty(tvClassField?.text)) {
            tvClassField!!.text = "auto_root"
        }
        val mainFile = File(mDirectory.virtualFile.path, tvClassField?.text + fileType)
        if (mainFile.exists()) {
            dispose()
            return
        }
        try {
            if (fileType == ".dart") {
                val output = GeneratorHelper.dartGenerate(
                    tvField!!.text,
                    tvClassField!!.text,
                    tvExtends!!.text,
                    tvImplements!!.text,
                    isSqliteEnable(),
                    tvPrimaryKeyListener.getText(),
                    rbClone!!.isSelected,
                    cbSplitGFile?.isSelected == true,
                )
                mainFile.writeText(output.mainContent)
                if (output.partContent != null && output.partFileName != null) {
                    File(mDirectory.virtualFile.path, output.partFileName).writeText(output.partContent)
                }
                // sqlite DAO 写入逻辑保持不变
                println("isSelected:${cbSqlite!!.isSelected}  $fileType")
                if (cbSqlite!!.isSelected && !TextUtils.isEmpty(tvPrimaryKeyListener.getText())) {
                    println("进入DartDataBaseGenerator")
                    DartDataBaseGenerator(
                        tvField!!.text,
                        tvClassField!!.text,
                        mDirectory,
                        tvPrimaryKeyListener.getText(),
                    ).startWrite()
                }
            } else {
                // 非 Dart 维持原有单文件写
                mainFile.writeText(json2Bean())
            }
            dispose()
        } catch (e: Exception) {
            tvError?.text = "JSON Error!!"
            println(e)
        }
    }
```

Behavior preserved:
- File-exists short-circuit still keyed on the main file path.
- The exact same `println` debug lines and `DartDataBaseGenerator` invocation move with the Dart branch.
- The non-Dart branch keeps using `json2Bean()` exactly as before.

- [ ] **Step 4: Initialize and persist `cbSplitGFile` in `initRadioButtons()`**

Locate the `rbClone` initialization block in `initRadioButtons()` (around lines 274-277):

```kotlin
        rbClone!!.isSelected = properties?.getProperty(PluginProps.clone) == "true"
        rbClone!!.addActionListener {
            properties!!.setProperty(PluginProps.clone, "${rbClone!!.isSelected}")
        }
```

Immediately after that block, add:

```kotlin
        cbSplitGFile!!.isSelected = properties?.getProperty(PluginProps.splitGFile) == "true"
        cbSplitGFile!!.addActionListener {
            properties!!.setProperty(PluginProps.splitGFile, "${cbSplitGFile!!.isSelected}")
        }
```

Visibility of `cbSplitGFile` is inherited from its parent `mMethodPanel` (`mMethodPanel.isVisible = fileType == ".dart"`), so no extra visibility wiring is needed — the existing toggles at lines 281 and 334 already cover this.

- [ ] **Step 5: Verify compilation**

Run: `./gradlew compileKotlin`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 6: Commit (form + Kotlin together)**

```bash
git add src/main/kotlin/com/awesome/plugins/json2bean/JsonBeanDialog.form \
        src/main/kotlin/com/awesome/plugins/json2bean/JsonBeanDialog.kt
git commit -m "feat: add Split .g.dart checkbox and dual-file Dart write path"
```

---

## Task 6: Manual smoke test in sandbox IDE

The project has no automated test harness. We validate by launching the sandbox IDE and walking the spec's verification matrix.

**Files:** none (verification only)

- [ ] **Step 1: Launch sandbox IDE**

Run: `./gradlew runIde`
Expected: a sandboxed IntelliJ window opens with the plugin installed. May take 1–2 minutes the first time.

- [ ] **Step 2: Open a temporary Dart project**

Inside the sandbox IDE, open or create any folder. Right-click on a directory and choose **New → Json2Bean** (or whichever menu the action is registered under).

- [ ] **Step 3: Verify checkbox visibility**

In the dialog:
- Select `.dart` → "Split .g.dart" checkbox visible alongside "clone".
- Select `.ts` / `.py` / `.kt` → "Split .g.dart" hidden (because `mMethodPanel` hides).

If any of these fail, the visibility wiring in `JsonBeanDialog.kt` at lines 281/334 is the place to look.

- [ ] **Step 4: Run the verification matrix**

Use the JSON below as the canonical input (nested object + simple list + 2D list + same-key collision):

```json
{
  "name": "alice",
  "age": 18,
  "tags": ["x", "y"],
  "grid": [[1, 2], [3, 4]],
  "address": {"city": "SF"},
  "addresses": [{"city": "NY"}]
}
```

For each row in the matrix below, set the dialog options as indicated, name the class `user` (lowercase to keep file name `user.dart`), click **Generate**, then inspect the produced file(s).

| # | Options | Expected files | Spot checks |
|---|---|---|---|
| A | clone off, split off, sqlite off | `user.dart` only | matches behavior before this change (no `part` directive, `fromJson` body inline) |
| B | clone on, split off, sqlite off | `user.dart` only | inline `clone()`, inline `toJson()`, inline `factory User.fromJson` |
| C | clone on, split on, sqlite off | `user.dart` + `user.g.dart` | main has `part 'user.g.dart';` after imports; main `factory User.fromJson(Map json) => _$UserFromJson(json);` etc.; `.g.dart` starts with `part of 'user.dart';` and has `_$UserFromJson` / `_$UserToJson` / `_$UserClone` for every class (User + Address + Addresses); 2D array uses `json.asArray2d<int>('grid')`; toJson bodies reference `i.name`/`i.tags` etc. |
| D | clone off, split on, sqlite off | `user.dart` + `user.g.dart` | `.g.dart` has no `_$*Clone` functions; main has no `clone()` delegate |
| E | clone on, split on, sqlite on, primaryKey=`id` | `user.dart` + `user.g.dart` + DAO file from `DartDataBaseGenerator` | main class still has `with BaseDbModel`, `primaryKeyAndValue()`, `operator ==`, `hashCode`, and `int? id;` field; `_$UserToJson` includes `'id': i.id` |
| F | Re-open dialog | (checkbox persistence) | `Split .g.dart` checkbox state matches last session's value |

- [ ] **Step 5: Lint the generated Dart**

If a Flutter / Dart SDK is available, in the directory containing the generated `user.dart` + `user.g.dart`, run:

```bash
dart analyze .
```
Expected: no errors. Acceptable warnings/info: missing top-level package context if you ran in a bare folder. The critical thing is no resolution errors for `_$UserFromJson`, no `part of` mismatch, no missing imports.

- [ ] **Step 6: Close the sandbox IDE and finalize**

If everything passes, no further commits are required — Task 5's commit is the final code change. If a scenario in the matrix fails, file the specific output against the spec and patch the affected task before re-running.

---

## Self-review

**Spec coverage:**
- Spec §1 decision table — every decision (part/part-of, fromJson/toJson/clone moved, opt-in checkbox, sqlite-compatible, concatenated preview) maps to a task.
- Spec §2.1/2.2 content shape — implemented in Task 2 (`appendFromJsonSection`, `appendToJsonSection`, `appendCloneSection`, `part` insertion logic).
- Spec §2.3 sqlite — Task 2 preserves the class-bound members in the main builder; `appendToJsonSection` keeps the `@override` on the delegate when `needsOverride`.
- Spec §3.1 generator change — Task 2.
- Spec §3.2 GeneratorHelper — Task 3.
- Spec §3.3 Dialog change — Tasks 4 + 5.
- Spec §3.4 PluginProps — Task 1.
- Spec §4 verification — Task 6.

**Placeholder scan:** No `TODO`/`TBD`/"similar to" references. All code blocks are complete.

**Type consistency:**
- `DartJsonGenerator.Output` referenced consistently in Task 2 (definition) and Task 3 (`dartGenerate` return type).
- Function names `_$<Class>FromJson` / `_$<Class>ToJson` / `_$<Class>Clone` used identically in Task 2's main delegate output and part output.
- `cbSplitGFile` and `PluginProps.splitGFile` spelling consistent across Tasks 1, 4, 5.
- `output.mainContent` / `output.partContent` / `output.partFileName` field names match the data class in Task 2.
