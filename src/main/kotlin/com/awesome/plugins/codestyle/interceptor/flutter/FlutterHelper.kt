package com.awesome.plugins.codestyle.interceptor.flutter

object FlutterHelper {
    fun background(value: String): String? {
        return ""
    }

    fun borderRadius(value: String): String? {
        return ""
    }

    fun getColor(value: String): String? {
        return value.replace("#", "0xff")
    }

}
