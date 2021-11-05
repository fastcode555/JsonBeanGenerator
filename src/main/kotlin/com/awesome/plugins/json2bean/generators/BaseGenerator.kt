package com.awesome.plugins.json2bean.generators

import com.alibaba.fastjson.JSON
import toJSON

abstract class BaseGenerator(content: String) {
    protected var json: JSON? = null

    init {
        json = content.toJSON()
    }

    abstract fun toJson(): String
}