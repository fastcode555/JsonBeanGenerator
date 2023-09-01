package com.awesome.plugins.ts.totailwindcss.processor.props

import com.awesome.plugins.ts.totailwindcss.TailWindHelper

/**
 * 高度的处理
 **/
class HeightProcessor(private val mark: String = "h") : BasePropProcessor() {
    override fun process(key: String, text: String): String {
        var value = text
        if (text.endsWith("%") || text.endsWith("vh") || text.endsWith("content")) {
            value = when (text) {
                "50%" -> "1/2"
                "33.333333%" -> "1/3"
                "66.666667%" -> "2/3"
                "25%" -> "1/4"
                "50%" -> "2/4"
                "75%" -> "3/4"
                "20%" -> "1/5"
                "40%" -> "2/5"
                "60%" -> "3/5"
                "80%" -> "4/5"
                "16.666667%" -> "1/6"
                "33.333333%" -> "2/6"
                "50%" -> "3/6"
                "66.666667%" -> "4/6"
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
            value = "[$text]"
        }
        return "$mark-$value"
    }
}
