package com.awesome.generators

abstract class BaseGenerator(content: String, fileName: String) {
    abstract fun toJson(): String
}