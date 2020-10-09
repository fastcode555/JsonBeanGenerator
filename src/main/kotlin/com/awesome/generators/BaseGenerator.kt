package com.awesome.generators

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject

abstract class BaseGenerator(content: String, fileName: String, extendsClass: String, implementClass: String) {
    protected var json: JSON? = null

    init {
        json = (if (content.startsWith("{")) JSONObject.parseObject(content) else JSONArray.parse(content)) as JSON?
    }

    abstract fun toJson(): String
}