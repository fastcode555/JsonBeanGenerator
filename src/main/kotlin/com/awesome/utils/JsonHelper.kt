import com.alibaba.fastjson2.JSON
import com.alibaba.fastjson2.JSONArray
import com.alibaba.fastjson2.JSONObject
import com.awesome.utils.regex
import com.google.common.base.CaseFormat
import com.google.gson.JsonElement

//dart中的关键词需要转换成普通词
var KEYS: Array<String> = arrayOf("num", "int", "String", "double", "bool")
var UPPER_KEYS: Array<String> = arrayOf("List", "Map")

const val REGEX_SYMBOL = "[~'`!@#\$%^&*()_\\-+=<>?:\"{}|,./;’\\[\\]·！@#￥%……&*（）——\\-+=\\{\\}|《》？：“”【】；‘’，。、]*";

fun String?.toCamel(): String {
    var result = this.clearSymbol()?.trim()
    if (this == result && !result!!.contains("_")) {
        //这里增加，强制将单词的第一个字母变为小写
        val firstWord = result.substring(0, 1)
        result = firstWord.lowercase() + result.substring(1, result.length)
        return if (KEYS.contains(result)) "${result}x" else result
    }
    result = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, result)
    return if (KEYS.contains(result)) "${result}x" else result!!
}

///将第一个字符，转换成大写
fun String.firstUpperCamel(): String {
    val header = this.substring(0, 1).uppercase()
    val tail = this.substring(1, this.length)
    return "$header$tail"
}

fun String?.toUpperCamel(): String {
    if (this.isNullOrEmpty()) return ""
    if (this.contains("_") || this.contains(" ")) {
        val result = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, this.clearSymbol())
        return if (UPPER_KEYS.contains(result)) "${result}x" else result
    }
    val result = this.clearSymbol()?.firstUpperCamel()
    return if (UPPER_KEYS.contains(result)) "${result}x" else "$result"
}

fun String?.toLowerUnderScore(): String {
    if (this.isNullOrEmpty()) return ""
    return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, this.clearSymbol().toCamel())
}

fun String?.toUpperUnderScore(): String {
    if (this.isNullOrEmpty()) return ""
    return CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, this.clearSymbol().toCamel())
}

fun String?.clearSymbol(): String? {
    if (this.isNullOrEmpty()) return this
    val key: String = this
    var finalKey: String = key
    key.regex(REGEX_SYMBOL) {
        if (it.trim().isEmpty()) return@regex
        finalKey = finalKey.replace(it, "_")
    }
    return finalKey.replace("\n", "").replace("\\", "").removeStartSymbol()
}

fun String.removeStartSymbol(): String {
    var value = this
    if (this.startsWith("_")) {
        value = value.substring(1, value.length)
        return value.removeStartSymbol()
    }
    return this
}

fun String?.formatJson(): String? {
    if (this.isNullOrEmpty()) return this
    val json = if (this.startsWith("{")) JSONObject.parseObject(this) else JSONArray.parseArray(this)
    return JSON.toJSONString(json)
}


fun String.toJSON(): Any? {
    return when {
        startsWith("{") -> {
            // 先用 LinkedHashMap 保顺序，再手动转成 JSONObject
            val map = JSONObject.parseObject(this, LinkedHashMap::class.java) as LinkedHashMap<String, *>
            JSONObject(map)
        }

        startsWith("[") -> {
            // JSONArray 本身是 List，有序的，直接 parse 就行
            JSONArray.parseArray(this)
        }

        else -> null
    }
}

fun JSONArray.mergeKeys(): Any {
    val result = this[0]
    if (result is String || result is Int || result is Double || result is Boolean || result is Float || result is JSONArray) {
        return result
    }
    val obj = JSONObject()
    for (jsonObject in this) {
        if (jsonObject is JSONObject) {
            for (key in jsonObject.keys) {
                if (!obj.containsKey(key)) {
                    obj[key] = jsonObject[key]
                }
            }
        }
    }
    return obj
}
