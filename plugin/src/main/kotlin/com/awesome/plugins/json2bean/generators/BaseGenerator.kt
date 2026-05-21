package com.awesome.plugins.json2bean.generators

import com.alibaba.fastjson2.JSON
import toJSON

abstract class BaseGenerator(content: String) {
    protected var json: Any? = null

    init {
        json = content.toJSON()
    }

    abstract override fun toString(): String
}