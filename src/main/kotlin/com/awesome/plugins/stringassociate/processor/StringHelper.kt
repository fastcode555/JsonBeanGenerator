package com.awesome.plugins.stringassociate.processor

object StringHelper {
    public fun toHexColor(rgba: String): String {
        var color = rgba.replace("rgba(", "").replace("rgb(", "").trim()
        color = color.substring(0, color.length - 1).trim()
        val results = color.split(",")
        if (results.size == 4) {
            val r = results[0].trim().toInt().toString(16)
            val g = results[1].trim().toInt().toString(16)
            val b = results[2].trim().toInt().toString(16)
            val a = (results[3].trim().toDouble() * 255).toInt().toString(16)
            return if (a == "ff") "#$r$g$b".uppercase() else "#$r$g$b$a".uppercase()
        } else if (results.size == 3) {
            val r = results[0].trim().toInt().toString(16)
            val g = results[1].trim().toInt().toString(16)
            val b = results[2].trim().toInt().toString(16)
            return "#$r$g$b".uppercase()
        }
        return rgba
    }
}
