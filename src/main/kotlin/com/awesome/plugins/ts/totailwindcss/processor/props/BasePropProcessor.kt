package com.awesome.plugins.ts.totailwindcss.processor.props

/**
 * 属性基类
 **/
abstract class BasePropProcessor {
    abstract fun process(key: String, value: String): String
}
