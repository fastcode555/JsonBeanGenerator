package com.awesome.generators

import com.alibaba.fastjson.JSON
import toJSON

abstract class BaseGenerator(content: String, fileName: String, extendsClass: String, implementClass: String) {
    protected var json: JSON? = null

    init {
        json = content.toJSON()
    }

    abstract fun toJson(): String
}