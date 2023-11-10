package com.awesome.plugins.codestyle.base

/**
 * 属性基类
 **/
abstract class BasePropInterceptor {
    abstract fun process(key: String, value: String): String
}
