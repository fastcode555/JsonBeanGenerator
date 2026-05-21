package com.awesome.plugins.codestyle.interceptor.tailwind.props

import com.awesome.plugins.codestyle.interceptor.tailwind.TailWindHelper
import com.awesome.plugins.codestyle.base.BasePropInterceptor

/**
 * 高度的处理
 **/
class HeightInterceptor(private val mark: String = "h") : BasePropInterceptor() {
    override fun process(key: String, text: String): String {
        var value = text
        if (text.endsWith("%") || text.endsWith("vh")) {
            value = when (text) {
                "50%" -> "1/2"
                "33.33%" -> "1/3"
                "33.333333%" -> "1/3"
                "66.66%" -> "2/3"
                "66.67%" -> "2/3"
                "66.666667%" -> "2/3"
                "25%" -> "1/4"
                "75%" -> "3/4"
                "20%" -> "1/5"
                "40%" -> "2/5"
                "60%" -> "3/5"
                "80%" -> "4/5"
                "16.66%" -> "1/6"
                "16.67%" -> "1/6"
                "16.666667%" -> "1/6"
                "83.33%" -> "5/6"
                "83.333333%" -> "5/6"
                "100%" -> "full"
                "100vh" -> "screen"
                "100svh" -> "svh"
                "100lvh" -> "lvh"
                "100dvh" -> "dvh"
                "min-content" -> "min"
                "max-content" -> "max"
                "fit-content" -> "fit"
                else -> "[$value]"
            }
        } else if (text.endsWith("rem")) {
            return TailWindHelper.spacing(mark, value)
        } else {
            value = when (text) {
                "min-content" -> "min"
                "max-content" -> "max"
                "fit-content" -> "fit"
                "auto" -> "auto"
                else -> {
                    "[$text]".replace(" ", "")
                }
            }
        }
        return "$mark-$value"
    }
}
