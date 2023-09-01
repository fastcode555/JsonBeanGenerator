package com.awesome.plugins.ts.totailwindcss.processor

import com.intellij.openapi.editor.Editor

abstract class BaseProcessor(private val editor: Editor) {
    abstract fun process(content: String): String
}
