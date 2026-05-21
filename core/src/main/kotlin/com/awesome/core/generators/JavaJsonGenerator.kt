package com.awesome.core.generators

import com.alibaba.fastjson2.JSONArray
import com.alibaba.fastjson2.JSONObject
import com.awesome.core.model.GenerateRequest
import com.awesome.core.model.GeneratedFile
import com.awesome.core.util.mergeKeys
import com.awesome.core.util.toCamel
import com.awesome.core.util.toJSON
import com.awesome.core.util.toUpperCamel
import java.math.BigDecimal

/**
 * Java Gson-style POJO generator.
 *
 * Output shape matches [com.awesome.core.generators.kt.KtGsonGenerator] one-for-one:
 * - public fields with @SerializedName annotations
 * - wrapper numeric types (Integer/Long/Double) so missing keys deserialise to null
 * - nested classes are emitted flat in the same file (no static inner classes)
 * - empty objects degrade to a bare class with no body
 */
class JavaJsonGenerator : JsonGenerator {

    override fun generate(req: GenerateRequest): List<GeneratedFile> {
        val parsed = req.json.toJSON()
        val classNames = mutableListOf<String>()
        val classBodies = LinkedHashMap<String, StringBuilder>()
        val mainName = req.className.toUpperCamel()
        emitClass(parsed, mainName, classBodies, classNames, isRoot = true)

        val header = buildString {
            append("import com.google.gson.annotations.SerializedName;\n")
            append("\n")
            append("import java.util.List;\n")
            append("\n")
        }

        val combined = StringBuilder(header)
        // root first, then nested in insertion order
        combined.append(classBodies[mainName])
        for ((name, body) in classBodies) {
            if (name == mainName) continue
            combined.append("\n")
            combined.append(body)
        }
        return listOf(GeneratedFile("$mainName.java", combined.toString()))
    }

    private fun emitClass(
        obj: Any?,
        className: String,
        bodies: LinkedHashMap<String, StringBuilder>,
        seen: MutableList<String>,
        isRoot: Boolean,
    ) {
        val uniqueName = uniqueClassName(className, seen)
        val builder = StringBuilder()
        val accessModifier = if (isRoot) "public " else ""

        val parseObj: JSONObject = when (obj) {
            is JSONObject -> obj
            is JSONArray -> obj.mergeKeys() as JSONObject
            else -> JSONObject()
        }

        if (parseObj.isEmpty()) {
            builder.append("${accessModifier}class $uniqueName {\n}\n")
            bodies[uniqueName] = builder
            return
        }

        builder.append("${accessModifier}class $uniqueName {\n")
        val fieldEntries = parseObj.entries.toList()
        for ((i, entry) in fieldEntries.withIndex()) {
            val key = entry.key
            val value = entry.value
            val camel = key.toCamel()
            val upper = key.toUpperCamel()
            val type = javaTypeOf(value, upper, bodies, seen)
            if (i > 0) builder.append("\n")
            builder.append("    @SerializedName(\"$key\")\n")
            builder.append("    public $type $camel;\n")
        }
        builder.append("}\n")
        bodies[uniqueName] = builder
    }

    /**
     * Resolves the Java type string for a value, recursing into nested objects.
     */
    private fun javaTypeOf(
        value: Any?,
        upper: String,
        bodies: LinkedHashMap<String, StringBuilder>,
        seen: MutableList<String>,
    ): String = when (value) {
        is String -> "String"
        is Boolean -> "Boolean"
        is Int -> "Integer"
        is Long -> "Long"
        is Double, is Float, is BigDecimal -> "Double"
        is JSONObject -> {
            emitClass(value, upper, bodies, seen, isRoot = false)
            upper
        }
        is JSONArray -> arrayType(value, upper, bodies, seen)
        else -> "String"
    }

    private fun arrayType(
        arr: JSONArray,
        upper: String,
        bodies: LinkedHashMap<String, StringBuilder>,
        seen: MutableList<String>,
    ): String {
        if (arr.isEmpty()) {
            // Unknown element type, mirror Kt behaviour: empty class as placeholder
            emitClass(JSONObject(), upper, bodies, seen, isRoot = false)
            return "List<$upper>"
        }
        val first = arr.mergeKeys()
        return when (first) {
            is String -> "List<String>"
            is Boolean -> "List<Boolean>"
            is Int -> "List<Integer>"
            is Long -> "List<Long>"
            is Double, is Float, is BigDecimal -> "List<Double>"
            is JSONArray -> {
                val inner = first.mergeKeys()
                val innerType = when (inner) {
                    is String -> "String"
                    is Boolean -> "Boolean"
                    is Int -> "Integer"
                    is Long -> "Long"
                    is Double, is Float, is BigDecimal -> "Double"
                    is JSONObject -> { emitClass(inner, upper, bodies, seen, isRoot = false); upper }
                    else -> "String"
                }
                "List<List<$innerType>>"
            }
            is JSONObject -> {
                emitClass(first, upper, bodies, seen, isRoot = false)
                "List<$upper>"
            }
            else -> "List<String>"
        }
    }

    private fun uniqueClassName(name: String, seen: MutableList<String>): String {
        if (seen.contains(name)) return uniqueClassName("${name}x", seen)
        seen.add(name)
        return name
    }
}
