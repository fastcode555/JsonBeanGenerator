package com.awesome.plugins.codestyle.base

import com.intellij.openapi.editor.Editor

abstract class BaseInterceptor(private val editor: Editor) {
    abstract fun process(content: String): String
}
