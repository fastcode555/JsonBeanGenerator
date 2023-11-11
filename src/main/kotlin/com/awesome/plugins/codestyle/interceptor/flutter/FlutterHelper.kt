package com.awesome.plugins.codestyle.interceptor.flutter

object FlutterHelper {
    fun background(value: String): String? {
        return ""
    }

    fun borderRadius(value: String): String? {
        return ""
    }

    fun getColor(value: String): String {
        val color = value.replace("#", "").lowercase()
        if (color.length == 6) {
            return "0xff$color"
        } else if (color.length == 8) {
            return "0x$color"
        }
        return color
    }

}
