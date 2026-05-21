package com.awesome.core.generators

import com.alibaba.fastjson2.JSON
import com.awesome.core.util.toJSON

abstract class BaseGenerator(content: String) {
    protected var json: Any? = null

    init {
        json = content.toJSON()
    }

    abstract override fun toString(): String
}