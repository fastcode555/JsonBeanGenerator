import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.google.common.base.CaseFormat

//dart中的关键词需要转换成普通词
var KEYS: Array<String> = arrayOf("num", "int", "String", "double", "bool")
var UPPER_KEYS: Array<String> = arrayOf("List", "Map")
var SPECIAL_SYMBOL: Array<String> = arrayOf("-", ",", ".", "=", "'", "?", "!","！", "？","，","。", " ", "\\", "/", "…", "+")

fun String?.toCamel(): String {
    var result = this.clearSymbol()
    result = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, result)
    return if (KEYS.contains(result)) "${result}x" else result!!

}

fun String?.toUpperCamel(): String {
    val result = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, this.clearSymbol())
    return if (UPPER_KEYS.contains(result)) "${result}x" else result
}

fun String?.clearSymbol(): String? {
    if (this == null || this.isEmpty()) return this
    var key: String = this
    for (symbol in SPECIAL_SYMBOL) {
        if (key.contains(symbol)) {
            key = key.replace(symbol, "_")
        }
    }
    return key
}

fun String?.formatJson(): String? {
    if (this == null || this.isEmpty()) return this;
    val json = if (this.startsWith("{")) JSONObject.parseObject(this) else JSONArray.parse(this)
    return JSON.toJSONString(json, true)
}

fun String.toJSON(): JSON? {
    return (if (startsWith("{")) JSONObject.parseObject(this) else JSONArray.parse(this)) as JSON?
}