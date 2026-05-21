package com.awesome.plugins.codestyle.base

import com.intellij.openapi.editor.Editor

abstract class BaseProcessor(private val editor: Editor) {

    /**
     * 使用责任链模式，让每个Interceptor处理一些需要自己处理的事件
     **/
    val interceptors = arrayListOf<BaseInterceptor>()

    /**
     * 让子类自己实现需要处理的代码
     **/
    abstract fun process(text: String): String
}
