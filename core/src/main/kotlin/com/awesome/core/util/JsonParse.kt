package com.awesome.core.util

import com.alibaba.fastjson2.JSON
import com.alibaba.fastjson2.JSONArray
import com.alibaba.fastjson2.JSONObject

fun String?.formatJson(): String? {
    if (this.isNullOrEmpty()) return this
    val json: Any = if (this.startsWith("{")) JSONObject.parseObject(this) else JSONArray.parseArray(this)
    return JSON.toJSONString(json)
}

fun String.toJSON(): Any? = when {
    startsWith("{") -> {
        @Suppress("UNCHECKED_CAST")
        val map = JSONObject.parseObject(this, LinkedHashMap::class.java) as LinkedHashMap<String, *>
        JSONObject(map)
    }
    startsWith("[") -> JSONArray.parseArray(this)
    else -> null
}

fun JSONArray.mergeKeys(): Any {
    val result = this[0]
    if (result is String || result is Int || result is Long || result is Double || result is Boolean || result is Float || result is JSONArray) {
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
