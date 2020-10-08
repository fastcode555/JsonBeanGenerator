package com.awesome.json2dartex.generator

abstract class BaseGenerator(content: String, fileName: String) {
    abstract fun toJson(): String
}