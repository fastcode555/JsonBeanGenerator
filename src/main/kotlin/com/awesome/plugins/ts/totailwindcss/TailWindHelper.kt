package com.awesome.plugins.ts.totailwindcss

import com.awesome.utils.isNumber

object TailWindHelper {

    /**
     * 处理FontWeight 属性
     **/
    fun fontWeight(value: String): String {
        if (value.isNumber()) {
            val size = value.trim().toInt()
            return when (size) {
                100 -> "font-thin"
                200 -> "font-extralight"
                300 -> "font-light"
                400 -> ""
                500 -> "font-medium"
                600 -> "font-semibold"
                700 -> "font-bold"
                800 -> "font-extrabold"
                900 -> "font-black"
                else -> ""
            }
        }
        return "font-$value"
    }

    /**
     * 处理边缘属性
     **/
    fun border(marker: String, value: String): String {
        val lists =
            arrayListOf(
                "none",
                "hidden",
                "dotted",
                "dashed",
                "solid",
                "double",
                "groove",
                "ridge",
                "inset",
                "outset"
            )
        if (value == "none") {
            return "$marker-none"
        }
        for (type in lists) {
            if (value.contains(type)) {
                val results = value.split(type)
                val color = results.last().trim()
                return "${borderWidth(marker, results.first().trim())} border-$type $marker-[$color]"
            }
        }
        return value
    }

    /**
     * 处理透明度属性
     **/
    fun opacity(marker: String, value: String): String {
        val size = (value.trim().toDouble() * 100).toInt()
        if (size == 100) return ""
        if (size % 5 == 0) {
            return "$marker-${size}"
        }
        return "$marker-[$size]"
    }

    fun padding(value: String): String = dealMarginOrPadding(value, "p")

    fun margin(value: String): String = dealMarginOrPadding(value, "m")

    fun String.replaceRegex(regexString: String): String = this.replace(Regex(regexString), "").trim()

    /**
     * 处理Margin值和Padding值
     **/
    private fun dealMarginOrPadding(value: String, mark: String = "m"): String {
        if (value.contains(" ")) {
            val results = value.split(" ").clearEmpty().checkSpacing()
            if (results.isSame()) {
                return "$mark-${results[0].trim()}"
            }
            if (results.size == 4) {
                if (results[0] == results[2] && results[1] == results[3]) {
                    return "${mark}x-${results[1]} ${mark}y-${results[0]}"
                }
                if (results[0] == results[2] && results[1] != results[3]) {
                    return "${mark}r-${results[1]} ${mark}l-${results[3]} ${mark}y-${results[0]}"
                }
                if (results[0] != results[2] && results[1] == results[3]) {
                    return "${mark}x-${results[1]} ${mark}t-${results[0]} ${mark}b-${results[2]}"
                }
                return "${mark}t-${results[0]} ${mark}r-${results[1]} ${mark}b-${results[2]} ${mark}l-${results[3]}"
            }
            if (results.size == 3) {
                if (results[0] == results[2]) {
                    return "${mark}x-${results[1]} ${mark}y-${results[0]}"
                }
                if (results[0] == results[1]) {
                    return "${mark}x-${results[1]} ${mark}t-${results[0]} ${mark}b-${results[2]}"
                }
                return "${mark}t-${results[0]} ${mark}x-${results[1]} ${mark}b-${results[2]}"
            }
            if (results.size == 2) {
                return "${mark}x-${results[0]} ${mark}y-${results[1]}"
            }
        }
        return "${mark}-${spacing("", value)}"
    }

    /**
     * 检查BorderRadius的值，转换为正常的Border值
     **/
    private fun List<String>.borderRadius(): List<String> {
        val list = arrayListOf<String>()
        for (index in this.indices) {
            val value = this[index]
            val result = when (value) {
                "0px" -> "none"
                "0.125rem" -> "sm"
                "0.25rem" -> "DEFAULT"
                "0.375rem" -> "md"
                "0.5rem" -> "lg"
                "0.75rem" -> "xl"
                "1rem" -> "2xl"
                "1.5rem" -> "3xl"
                "9999px" -> "full"
                else -> "[$value]"
            }
            list.add(result)
        }
        return list
    }

    /**
     * 检查spcing的值，转换为普通正常值
     **/
    private fun List<String>.checkSpacing(): List<String> {
        val lists = arrayListOf<String>()
        for (index in this.indices) {
            val value = this[index]
            val result = spacing("", value)
            lists.add(result)
        }
        return lists
    }

    /**
     * 处理圆角属性
     **/
    fun borderRadius(value: String): String {
        if (value.contains(" ")) {
            val results = value.split(" ").clearEmpty().borderRadius()
            if (results.isSame()) {
                return "rounded-${results[0].trim()}"
            }
            if (results.size == 4) {
                if (results[0] == results[1] && results[2] == results[3]) {
                    return "rounded-t-${results[0]} rounded-b-${results[2]}"
                }
                if (results[0] == results[1] && results[2] != results[3]) {
                    return "rounded-t-${results[0]} rounded-br-${results[2]} rounded-bl-${results[3]}"
                }
                if (results[0] != results[1] && results[2] == results[3]) {
                    return "rounded-tl-${results[0]} rounded-tr-${results[1]} rounded-b-${results[2]}"
                }
                if (results[0] == results[3] && results[1] == results[2]) {
                    return "rounded-l-${results[0]} rounded-r-${results[1]}"
                }
                if (results[0] == results[3] && results[1] != results[2]) {
                    return "rounded-l-${results[0]} rounded-tr-${results[1]} rounded-br-${results[2]}"
                }
                if (results[0] != results[3] && results[1] == results[2]) {
                    return "rounded-tl-${results[0]} rounded-bl-${results[3]} rounded-r-${results[1]}"
                }
                return "rounded-tl-${results[0]} rounded-tr-${results[1]} rounded-br-${results[2]} rounded-bl-${results[3]}"
            }
            if (results.size == 3) {
                if (results[0] == results[1]) {
                    return "rounded-t-${results[0]} rounded-br-${results[1]} rounded-bl-${results[2]}"
                }
                if (results[0] == results[2]) {
                    return "rounded-l-${results[0]} rounded-r-${results[1]}"
                }
            }
            if (results.size == 2) {
                return "rounded-tl-${results[0]} rounded-tr-${results[1]} round-br-${results[0]} rounded-bl-${results[1]}"
            }
        }
        val result = arrayListOf(value).borderRadius().first()
        return "rounded-$result"
    }

    /**
     * 清掉数组中是空的数据
     **/
    private fun List<String>.clearEmpty(): ArrayList<String> {
        val results = arrayListOf<String>()
        for (text in this) {
            if (text.trim().isEmpty()) continue
            results.add(text.trim())
        }
        return results
    }

    /**
     * 判断数组中的数据是否相等，忽略空字符串的问题
     **/
    private fun List<String>.isSame(): Boolean {
        if (this.isEmpty()) return true
        var isSame = true
        var string = this[0].trim()
        for (text in this) {
            isSame = (isSame && (string == text.trim()))
            if (!isSame) return isSame
        }
        return isSame
    }

    /**
     * justify-start: 项目被对齐到容器的开始位置，相当于 CSS 的 justify-content: flex-start
     * justify-end: 项目被对齐到容器的结束位置，相当于 CSS 的 justify-content: flex-end
     * justify-center: 项目在容器中居中，相当于 CSS 的 justify-content: center
     * justify-between: 项目之间的间距都相等，相当于 CSS 的 justify-content: space-between
     * justify-around: 项目的两侧的间距相等，相当于 CSS 的 justify-content: space-around
     * justify-evenly: 项目之间的间距和项目与容器边框之间的间距都相等，相当于 CSS 的 justify-content: space-evenly
     **/
    fun justifyContent(value: String): String {
        return when (value) {
            "normal" -> "justify-normal"
            "flex-start" -> "justify-start"
            "flex-end" -> "justify-end"
            "center" -> "justify-center"
            "space-between" -> "justify-between"
            "space-around" -> "justify-around"
            "space-evenly" -> "justify-evenly"
            "stretch" -> "justify-stretch"
            else -> "justify-center"
        }
    }

    /**
     * items-start: 项目被对齐到容器的开始位置，相当于 CSS 的 align-items: flex-start
     * items-end: 项目被对齐到容器的结束位置，相当于 CSS 的 align-items: flex-end
     * items-center: 项目在容器中居中，相当于 CSS 的 align-items: center
     * items-baseline: 项目的基线对齐，相当于 CSS 的 align-items: baseline
     * items-stretch: 如果项目未设置高度或设为 auto，将占满整个容器的高度，相当于 CSS 的 align-items: stretch.
     **/
    fun alignItems(value: String): String {
        return when (value) {
            "flex-start" -> "items-start"
            "flex-end" -> "items-end"
            "center" -> "items-center"
            "baseline" -> "items-baseline"
            "stretch" -> "items-stretch"
            else -> "items-center"
        }
    }

    /**
     * self-auto: 自动对齐，相当于 CSS 的 align-self: auto
     * self-start: 靠容器开始位置对齐，相当于 CSS 的 align-self: flex-start
     * self-end: 靠容器结束位置对齐，相当于 CSS 的 align-self: flex-end
     * self-center: 在容器中居中对齐，相当于 CSS 的 align-self: center
     * self-stretch: 在容器内拉伸以占据额外空间，相当于 CSS 的 align-self: stretch
     **/
    fun alignSelf(value: String): String {
        return when (value) {
            "auto" -> "self-auto"
            "flex-start" -> "self-start"
            "flex-end" -> "self-end"
            "center" -> "self-center"
            "stretch" -> "self-stretch"
            "baseline" -> "self-baseline"
            else -> "self-center"
        }
    }

    /**
     * content-start: 行组被放置在容器的起头部，相当于 CSS 的 align-content: flex-start.
     * content-center: 行组被放置在容器的中央，相当于 CSS 的 align-content: center.
     * content-end: 行组被放置在容器的尾部，相当于 CSS 的 align-content: flex-end.
     * content-between: 行组之间的间距相等，相当于 CSS 的 align-content: space-between.
     * content-around: 行组两侧的间距相等，相当于 CSS 的 align-content: space-around.
     * content-evenly: 行组之间和周围的空间都平均分布，相当于 CSS 的 align-content: space-evenly.
     **/
    fun alignContent(value: String): String {
        return when (value) {
            "flex-start" -> "content-start"
            "center" -> "content-center"
            "flex-end" -> "content-end"
            "space-between" -> "content-between"
            "space-around" -> "content-around"
            "space-evenly" -> "content-evenly"
            else -> "content-center"
        }
    }

    /**
     * flex-grow: 开启元素的伸展效果，相当于 CSS 的 flex-grow: 1
     * flex-grow-0: 禁用元素的伸展效果，相当于 CSS 的 flex-grow: 0
     **/
    fun flexGrow(value: String): String {
        return when (value) {
            "1" -> "flex-grow"
            "0" -> "flex-grow-0"
            else -> "flex-grow"
        }
    }

    /**
     * flex-shrink: 开启元素的收缩效果，相当于 CSS 的 flex-shrink: 1
     * flex-shrink-0: 禁用元素的收缩效果，相当于 CSS 的 flex-shrink: 0
     **/
    fun flexShrink(marker: String, value: String): String {
        return when (value) {
            "1" -> "$marker"
            "0" -> "$marker-0"
            else -> "$marker"
        }
    }

    /**
     * 处理颜色属性
     **/
    fun color(key: String, value: String): String {
        if (value.startsWith("#") || value.startsWith("rgb")) {
            return "$key-[$value]"
        }
        return "$key-[${defaultCssColor(value)}]"
    }

    private fun defaultCssColor(value: String): String {
        return when (value) {
            "aliceblue" -> "#f0f8ff"
            "antiquewhite" -> "#faebd7"
            "aqua" -> "#00ffff"
            "aquamarine" -> "#7fffd4"
            "azure" -> "#f0ffff"
            "beige" -> "#f5f5dc"
            "bisque" -> "#ffe4c4"
            "black" -> "#000000"
            "blanchedalmond" -> "#ffebcd"
            "blue" -> "#0000ff"
            "blueviolet" -> "#8a2be2"
            "brown" -> "#a52a2a"
            "burlywood" -> "#deb887"
            "cadetblue" -> "#5f9ea0"
            "chartreuse" -> "#7fff00"
            "chocolate" -> "#d2691e"
            "coral" -> "#ff7f50"
            "cornflowerblue" -> "#6495ed"
            "cornsilk" -> "#fff8dc"
            "crimson" -> "#dc143c"
            "cyan" -> "#00ffff"
            "darkblue" -> "#00008b"
            "darkcyan" -> "#008b8b"
            "darkgoldenrod" -> "#b8860b"
            "darkgray" -> "#a9a9a9"
            "darkgreen" -> "#006400"
            "darkgrey" -> "#a9a9a9"
            "darkkhaki" -> "#bdb76b"
            "darkmagenta" -> "#8b008b"
            "darkolivegreen" -> "#556b2f"
            "darkorange" -> "#ff8c00"
            "darkorchid" -> "#9932cc"
            "darkred" -> "#8b0000"
            "darksalmon" -> "#e9967a"
            "darkseagreen" -> "#8fbc8f"
            "darkslateblue" -> "#483d8b"
            "darkslategray" -> "#2f4f4f"
            "darkslategrey" -> "#2f4f4f"
            "darkturquoise" -> "#00ced1"
            "darkviolet" -> "#9400d3"
            "deeppink" -> "#ff1493"
            "deepskyblue" -> "#00bfff"
            "dimgray" -> "#696969"
            "dimgrey" -> "#696969"
            "dodgerblue" -> "#1e90ff"
            "firebrick" -> "#b22222"
            "floralwhite" -> "#fffaf0"
            "forestgreen" -> "#228b22"
            "fuchsia" -> "#ff00ff"
            "gainsboro" -> "#dcdcdc"
            "ghostwhite" -> "#f8f8ff"
            "gold" -> "#ffd700"
            "goldenrod" -> "#daa520"
            "gray" -> "#808080"
            "green" -> "#008000"
            "greenyellow" -> "#adff2f"
            "grey" -> "#808080"
            "honeydew" -> "#f0fff0"
            "hotpink" -> "#ff69b4"
            "indianred" -> "#cd5c5c"
            "indigo" -> "#4b0082"
            "ivory" -> "#fffff0"
            "khaki" -> "#f0e68c"
            "lavender" -> "#e6e6fa"
            "lavenderblush" -> "#fff0f5"
            "lawngreen" -> "#7cfc00"
            "lemonchiffon" -> "#fffacd"
            "lightblue" -> "#add8e6"
            "lightcoral" -> "#f08080"
            "lightcyan" -> "#e0ffff"
            "lightgoldenrodyellow" -> "#fafad2"
            "lightgray" -> "#d3d3d3"
            "lightgreen" -> "#90ee90"
            "lightgrey" -> "#d3d3d3"
            "lightpink" -> "#ffb6c1"
            "lightsalmon" -> "#ffa07a"
            "lightseagreen" -> "#20b2aa"
            "lightskyblue" -> "#87cefa"
            "lightslategray" -> "#778899"
            "lightslategrey" -> "#778899"
            "lightsteelblue" -> "#b0c4de"
            "lightyellow" -> "#ffffe0"
            "lime" -> "#00ff00"
            "limegreen" -> "#32cd32"
            "linen" -> "#faf0e6"
            "magenta" -> "#ff00ff"
            "maroon" -> "#800000"
            "mediumaquamarine" -> "#66cdaa"
            "mediumblue" -> "#0000cd"
            "mediumorchid" -> "#ba55d3"
            "mediumpurple" -> "#9370db"
            "mediumseagreen" -> "#3cb371"
            "mediumslateblue" -> "#7b68ee"
            "mediumspringgreen" -> "#00fa9a"
            "mediumturquoise" -> "#48d1cc"
            "mediumvioletred" -> "#c71585"
            "midnightblue" -> "#191970"
            "mintcream" -> "#f5fffa"
            "mistyrose" -> "#ffe4e1"
            "moccasin" -> "#ffe4b5"
            "navajowhite" -> "#ffdead"
            "navy" -> "#000080"
            "oldlace" -> "#fdf5e6"
            "olive" -> "#808000"
            "olivedrab" -> "#6b8e23"
            "orange" -> "#ffa500"
            "orangered" -> "#ff4500"
            "orchid" -> "#da70d6"
            "palegoldenrod" -> "#eee8aa"
            "palegreen" -> "#98fb98"
            "paleturquoise" -> "#afeeee"
            "palevioletred" -> "#db7093"
            "papayawhip" -> "#ffefd5"
            "peachpuff" -> "#ffdab9"
            "peru" -> "#cd853f"
            "pink" -> "#ffc0cb"
            "plum" -> "#dda0dd"
            "powderblue" -> "#b0e0e6"
            "purple" -> "#800080"
            "rebeccapurple" -> "#663399"
            "red" -> "#ff0000"
            "rosybrown" -> "#bc8f8f"
            "royalblue" -> "#4169e1"
            "saddlebrown" -> "#8b4513"
            "salmon" -> "#fa8072"
            "sandybrown" -> "#f4a460"
            "seagreen" -> "#2e8b57"
            "seashell" -> "#fff5ee"
            "sienna" -> "#a0522d"
            "silver" -> "#c0c0c0"
            "skyblue" -> "#87ceeb"
            "slateblue" -> "#6a5acd"
            "slategray" -> "#708090"
            "slategrey" -> "#708090"
            "snow" -> "#fffafa"
            "springgreen" -> "#00ff7f"
            "steelblue" -> "#4682b4"
            "tan" -> "#d2b48c"
            "teal" -> "#008080"
            "thistle" -> "#d8bfd8"
            "tomato" -> "#ff6347"
            "turquoise" -> "#40e0d0"
            "violet" -> "#ee82ee"
            "wheat" -> "#f5deb3"
            "white" -> "#ffffff"
            "whitesmoke" -> "#f5f5f5"
            "yellow" -> "#ffff00"
            "yellowgreen" -> "#9acd32"
            else -> value
        }
    }

    fun rotate(key: String, value: String): String {
        val finalValue = when (value) {
            "0deg" -> "0"
            "1deg" -> "1"
            "2deg" -> "2"
            "3deg" -> "3"
            "6deg" -> "6"
            "12deg" -> "12"
            "45deg" -> "45"
            "90deg" -> "90"
            "180deg" -> "180"
            else -> "[$value]"
        }
        return "$key-$finalValue"
    }

    fun letterSpacing(value: String): String {
        val finalValue = when (value) {
            "-0.05rem" -> "tighter"
            "-0.025rem" -> "tight"
            "0rem" -> "normal"
            "0.025rem" -> "wide"
            "0.05rem" -> "wider"
            "0.1rem" -> "widest"
            else -> "[$value]"
        }
        return "tracking-$finalValue"
    }

    fun lineHeight(value: String): String {
        val finalValue = when (value) {
            "none" -> "none"
            "tight" -> "tight"
            "snug" -> "snug"
            "normal" -> "normal"
            "relaxed" -> "relaxed"
            "loose" -> "loose"
            "1" -> "none"
            "1.25" -> "tight"
            "1.375" -> "snug"
            "1.5" -> "normal"
            "1.625" -> "relaxed"
            "2" -> "loose"
            "0.75rem" -> "3"
            ".75rem" -> "3"
            "1rem" -> "4"
            "1.25rem" -> "5"
            "1.5rem" -> "6"
            "1.75rem" -> "7"
            "2rem" -> "8"
            "2.25rem" -> "9"
            "2.5rem" -> "10"
            else -> "[$value]"
        }
        return "leading-$finalValue"
    }

    fun fontSize(value: String): String {
        val finalValue = when (value) {
            "0.75rem" -> "xs"
            "0.875rem" -> "sm"
            "1rem" -> "base"
            "1.125rem" -> "lg"
            "1.25rem" -> "xl"
            "1.5rem" -> "2xl"
            "1.875rem" -> "3xl"
            "2.25rem" -> "4xl"
            "3rem" -> "5xl"
            "3.75rem" -> "6xl"
            "4.5rem" -> "7xl"
            "6rem" -> "8xl"
            "8rem" -> "9xl"
            else -> "[$value]"
        }
        return "text-$finalValue"
    }

    fun fontFamily(value: String): String {
        val finalValue = when (value) {
            "ui-sans-serif", "sans-serif", "Apple Color Emoji", "Segoe UI Emoji", "Segoe UI Symbol", "Noto Color Emoji" -> "sans"
            "ui-serif", "Georgia", "Cambria", "Times New Roman", "Times", "serif" -> "serif"
            "ui-monospace", "SFMono-Regular", "Menlo", "Monaco", "Consolas", "Liberation Mono", "Courier New", "monospace" -> "mono"
            else -> "[$value]"
        }
        return "font-$finalValue"
    }

    fun order(value: String): String {
        val finalValue = when (value) {
            "-9999" -> "first"
            "9999" -> "last"
            "0" -> "none"
            "1" -> "1"
            "2" -> "2"
            "3" -> "3"
            "4" -> "4"
            "5" -> "5"
            "6" -> "6"
            "7" -> "7"
            "8" -> "8"
            "9" -> "9"
            "10" -> "10"
            "11" -> "11"
            "12" -> "12"
            else -> "[$value]"
        }
        return "order-$finalValue"
    }

    fun spacing(marker: String, value: String, key: String = ""): String {
        val finalValue = when (value) {
            "1px" -> "px"
            "0px" -> "0"
            "0.125rem" -> "0.5"
            "0.25rem" -> "1"
            "0.375rem" -> "1.5"
            "0.5rem" -> "2"
            "0.625rem" -> "2.5"
            "0.75rem" -> "3"
            "0.875rem" -> "3.5"
            "1rem" -> "4"
            "1.25rem" -> "5"
            "1.5rem" -> "6"
            "1.75rem" -> "7"
            "2rem" -> "8"
            "2.25rem" -> "9"
            "2.5rem" -> "10"
            "2.75rem" -> "11"
            "3rem" -> "12"
            "3.5rem" -> "14"
            "4rem" -> "16"
            "5rem" -> "20"
            "6rem" -> "24"
            "7rem" -> "28"
            "8rem" -> "32"
            "9rem" -> "36"
            "10rem" -> "40"
            "11rem" -> "44"
            "12rem" -> "48"
            "13rem" -> "52"
            "14rem" -> "56"
            "15rem" -> "60"
            "16rem" -> "64"
            "18rem" -> "72"
            "20rem" -> "80"
            "24rem" -> "96"

            "50%" -> "1/2"
            "33.333333%" -> "1/3"
            "66.666667%" -> "2/3"
            "25%" -> "1/4"
            "75%" -> "3/4"
            "100%" -> "full"
            "33.33%" -> "1/3"
            "66.66%" -> "2/3"
            "66.67%" -> "2/3"
            "20%" -> "1/5"
            "40%" -> "2/5"
            "60%" -> "3/5"
            "80%" -> "4/5"
            "16.666667%" -> "1/6"
            "83.33%" -> "5/6"
            "83.333333%" -> "5/6"
            "8.333333%" -> "1/12"
            "16.66%" -> "2/12"
            "16.67%" -> "2/12"
            "41.666667%" -> "5/12"
            "58.333333%" -> "7/12"
            "91.66%" -> "11/12"
            "91.666667%" -> "11/12"
            else -> {
                if (key == "flex-basis" || key == "text-indent") {
                    value
                } else {
                    "[$value]"
                }
            }
        }
        if (marker.isNullOrEmpty()) {
            return finalValue
        }
        return "$marker-$finalValue"
    }

    fun borderWidth(marker: String, value: String): String {
        val finalValue = when (value) {
            "1px" -> "DEFAULT"
            "0px" -> "0"
            "2px" -> "2"
            "4px" -> "4"
            "8px" -> "8"
            else -> "[$value]"
        }
        return "$marker-$finalValue"
    }

    fun aspectRatio(text: String): String {
        var value = text.replace(" ", "").trim()
        val finalValue = when (value) {
            "auto" -> "auto"
            "1/1" -> "square"
            "16/9" -> "video"
            else -> "[$value]"
        }
        return "aspect-$finalValue"
    }

    fun filter(key: String, text: String): String {
        if (text.contains("blur(")) {
            var value = text.replace("blur(", "")
            value = value.replace(")", "")
            val finalValue = when (value) {
                "0" -> "0"
                "4px" -> "sm"
                "8px" -> "DEFAULT"
                "12px" -> "md"
                "16px" -> "lg"
                "24px" -> "xl"
                "40px" -> "2xl"
                "64px" -> "3xl"
                else -> "[$value]"
            }
            if (key == "backdrop-filter") {
                return "backdrop-blur-$finalValue"
            }
            return "blur-$finalValue"
        }
        return "$key-[$text]"
    }

    fun boxShadow(value: String): String {
        val finalValue = when (value) {
            "0 1px 2px 0 rgb(0 0 0 / 0.05)" -> "sm"
            "0 1px 3px 0 rgb(0 0 0 / 0.1), 0 1px 2px -1px rgb(0 0 0 / 0.1)" -> "DEFAULT"
            "0 4px 6px -1px rgb(0 0 0 / 0.1), 0 2px 4px -2px rgb(0 0 0 / 0.1)" -> "md"
            "0 10px 15px -3px rgb(0 0 0 / 0.1), 0 4px 6px -4px rgb(0 0 0 / 0.1)" -> "lg"
            "0 20px 25px -5px rgb(0 0 0 / 0.1), 0 8px 10px -6px rgb(0 0 0 / 0.1)" -> "xl"
            "0 25px 50px -12px rgb(0 0 0 / 0.25)" -> "2xl'"
            "inset 0 2px 4px 0 rgb(0 0 0 / 0.05)" -> "inner"
            "none" -> "none"
            else -> ""
        }
        return "shadow-$finalValue"
    }

    fun textTransform(value: String): String {
        return when (value) {
            "uppercase" -> "uppercase"
            "lowercase" -> "lowercase"
            "capitalize" -> "capitalize"
            "none" -> ""
            else -> ""
        }
    }

    fun fontStyle(value: String): String {
        return when (value) {
            "italic" -> "italic"
            "normal" -> ""
            else -> ""
        }
    }

    fun backgroundClip(value: String): String {
        val finalValue = when (value) {
            "text" -> "bg-clip-text"
            "content-box" -> "bg-clip-content"
            "padding-box" -> "bg-clip-padding"
            "border-box" -> "bg-clip-border"
            else -> ""
        }
        return finalValue
    }

    fun textDecorationStyle(value: String): String {
        return when (value) {
            "solid" -> "decoration-solid"
            "double" -> "decoration-double"
            "dotted" -> "decoration-dotted"
            "dashed" -> "decoration-dashed"
            "wavy" -> "decoration-wavy"
            else -> ""
        }
    }

    fun textDecorationLine(value: String): String {
        return when (value) {
            "underline" -> "underline"
            "overline" -> "overline"
            "line-through" -> "line-through"
            "none" -> "no-underline"
            else -> ""
        }
    }

    fun bgBlendMode(value: String): String {
        return when (value) {
            "normal" -> "bg-blend-normal"
            "multiply" -> "bg-blend-multiply"
            "screen" -> "bg-blend-screen"
            "overlay" -> "bg-blend-overlay"
            "darken" -> "bg-blend-darken"
            "lighten" -> "bg-blend-lighten"
            "color-dodge" -> "bg-blend-color-dodge"
            "color-burn" -> "bg-blend-color-burn"
            "hard-light" -> "bg-blend-hard-light"
            "soft-light" -> "bg-blend-soft-light"
            "difference" -> "bg-blend-difference"
            "exclusion" -> "bg-blend-exclusion"
            "hue" -> "bg-blend-hue"
            "saturation" -> "bg-blend-saturation"
            "color" -> "bg-blend-color"
            "luminosity" -> "bg-blend-luminosity"
            else -> ""
        }
    }

    fun mixBlendMode(value: String): String {
        return when (value) {
            "normal" -> "mix-blend-normal"
            "multiply" -> "mix-blend-multiply"
            "screen" -> "mix-blend-screen"
            "overlay" -> "mix-blend-overlay"
            "darken" -> "mix-blend-darken"
            "lighten" -> "mix-blend-lighten"
            "color-dodge" -> "mix-blend-color-dodge"
            "color-burn" -> "mix-blend-color-burn"
            "hard-light" -> "mix-blend-hard-light"
            "soft-light" -> "mix-blend-soft-light"
            "difference" -> "mix-blend-difference"
            "exclusion" -> "mix-blend-exclusion"
            "hue" -> "mix-blend-hue"
            "saturation" -> "mix-blend-saturation"
            "color" -> "mix-blend-color"
            "luminosity" -> "mix-blend-luminosity"
            "plus-lighter" -> "mix-blend-plus-lighter"
            else -> ""
        }
    }

    fun scale(marker: String, value: String): String {
        val finalValue = when (value) {
            "0" -> "0"
            "0.5" -> "50"
            ".5" -> "50"
            "0.75" -> "75"
            ".75" -> "75"
            "0.9" -> "90"
            ".9" -> "90"
            "0.95" -> "95"
            ".95" -> "95"
            "1" -> "100"
            "1.05" -> "105"
            "1.1" -> "110"
            "1.25" -> "125"
            "1.5" -> "150"
            else -> {
                "[${value.toDouble() * 100}]"
            }
        }
        return if (finalValue.isNotEmpty()) "$marker-${finalValue}" else ""
    }

    fun strokeWidth(value: String): String {
        val finalValue = when (value) {
            "0" -> "0"
            "1" -> "1"
            "2" -> "2"
            else -> "[$value]"
        }
        if (finalValue.isNotEmpty()) {
            return "stroke-$finalValue"
        }
        return ""
    }

    fun textDecorationThickness(value: String): String {
        val finalValue = when (value) {
            "auto" -> "auto"
            "from-font" -> "from-font"
            "0px" -> "0"
            "1px" -> "1"
            "2px" -> "2"
            "4px" -> "4"
            "8px" -> "8"
            else -> "auto"
        }
        return "decoration-$finalValue"
    }

    fun textUnderLineOffset(value: String): String {
        val finalValue = when (value) {
            "auto" -> "auto"
            "from-font" -> "from-font"
            "0px" -> "0"
            "1px" -> "1"
            "2px" -> "2"
            "4px" -> "4"
            "8px" -> "8"
            else -> "auto"
        }
        return "underline-offset-$finalValue"
    }

    fun zIndex(value: String): String {
        val finalValue = when (value) {
            "auto" -> "auto"
            "0" -> "0"
            "10" -> "10"
            "20" -> "20"
            "30" -> "30"
            "40" -> "40"
            "50" -> "50"
            else -> "[$value]"
        }
        return "z-${finalValue}"
    }

    fun verticalAlign(value: String): String {
        val finalValue = when (value) {
            "baseline" -> "align-baseline"
            "top" -> "align-top"
            "middle" -> "align-middle"
            "bottom" -> "align-bottom"
            "text-top" -> "align-text-top"
            "text-bottom" -> "align-text-bottom"
            "sub" -> "align-sub"
            "super" -> "align-super"
            else -> "align-$value"
        }
        return finalValue
    }

    /**
     * TODO：
     **/
    fun transform(key: String, value: String): String {
        var result = value
        if (value.startsWith("scale(")) {
            val scale = result.replace("scale(", "").replace(")", "").trim()
            return scale("scale", scale)
        } else if (value.startsWith("translate(")) {

        } else if (value.startsWith("rotate(")) {

        } else if (value.startsWith("skew(")) {

        }
        return "$key-[$value]"
    }

    fun flexDirection(value: String): String {
        return when (value) {
            "row" -> "flex-row"
            "row-reverse" -> "flex-row-reverse"
            "column" -> "flex-col"
            "column-reverse" -> "flex-col-reverse"
            else -> ""
        }
    }

    fun flexWrap(value: String): String {
        return when (value) {
            "wrap" -> "flex-wrap"
            "wrap-reverse" -> "flex-wrap-reverse"
            "nowrap" -> "flex-nowrap"
            else -> ""
        }
    }

    fun gridAutoFlow(value: String): String {
        return when (value) {
            "row" -> "grid-flow-row"
            "column" -> "grid-flow-col"
            "dense" -> "grid-flow-dense"
            "row dense" -> "grid-flow-row-dense"
            "column dense" -> "grid-flow-col-dense"
            else -> ""
        }
    }

    fun breakAfter(value: String): String {
        return when (value) {
            "auto" -> "break-after-auto"
            "avoid" -> "break-after-avoid"
            "all" -> "break-after-all"
            "avoid-page" -> "break-after-avoid-page"
            "page" -> "break-after-page"
            "left" -> "break-after-left"
            "right" -> "break-after-right"
            "column" -> "break-after-column"
            else -> ""
        }
    }

    fun breakBefore(value: String): String {
        return when (value) {
            "auto" -> "break-before-auto"
            "avoid" -> "break-before-avoid"
            "all" -> "break-before-all"
            "avoid-page" -> "break-before-avoid-page"
            "page" -> "break-before-page"
            "left" -> "break-before-left"
            "right" -> "break-before-right"
            "column" -> "break-before-column"
            else -> ""
        }
    }

    fun breakInside(value: String): String {
        return when (value) {
            "auto" -> "break-inside-auto"
            "avoid" -> "break-inside-avoid"
            "avoid-page" -> "break-inside-avoid-page"
            "avoid-column" -> "break-inside-avoid-column"
            else -> ""
        }
    }

    fun resize(value: String): String {
        return when (value) {
            "none" -> "resize-none"
            "vertical" -> "resize-y"
            "horizontal" -> "resize-x"
            "both" -> "resize"
            else -> ""
        }
    }

    fun scrollSnapAlign(value: String): String {
        return when (value) {
            "start" -> "snap-start"
            "end" -> "snap-end"
            "center" -> "snap-center"
            "none" -> "snap-align-none"
            else -> ""
        }
    }

    fun placeContent(value: String): String {
        return when (value) {
            "center" -> "place-content-center"
            "start" -> "place-content-start"
            "end" -> "place-content-end"
            "space-between" -> "place-content-between"
            "space-around" -> "place-content-around"
            "space-evenly" -> "place-content-evenly"
            "baseline" -> "place-content-baseline"
            "stretch" -> "place-content-stretch"
            else -> ""
        }
    }

    fun backgroundRepeat(value: String): String {
        return when (value) {
            "repeat" -> "bg-repeat"
            "no-repeat" -> "bg-no-repeat"
            "repeat-x" -> "bg-repeat-x"
            "repeat-y" -> "bg-repeat-y"
            "round" -> "bg-repeat-round"
            "space" -> "bg-repeat-space"
            else -> ""
        }
    }

    fun backgroundOrigin(value: String): String {
        return when (value) {
            "border-box" -> "bg-origin-border"
            "padding-box" -> "bg-origin-padding"
            "content-box" -> "bg-origin-content"
            else -> ""
        }
    }

    fun isolation(value: String): String {
        return when (value) {
            "isolate" -> "isolate"
            "auto" -> "isolation-auto"
            else -> ""
        }
    }

    fun wordBreak(value: String): String {
        return when (value) {
            "break-all" -> "break-all"
            "keep-all" -> "break-keep"
            else -> ""
        }
    }

    fun overflowWrap(value: String): String {
        return when (value) {
            "break-word" -> "break-word"
            "normal" -> "break-normal"
            else -> ""
        }
    }

    fun columns(key: String, value: String): String {
        val result = when (value) {
            "1" -> "1"
            "10" -> "10"
            "11" -> "11"
            "12" -> "12"
            "2" -> "2"
            "3" -> "3"
            "48rem" -> "3xl"
            "16rem" -> "3xs"
            "4" -> "4"
            "56rem" -> "4xl"
            "5" -> "5"
            "64rem" -> "5xl"
            "6" -> "6"
            "7" -> "7"
            "80rem" -> "7xl"
            "8" -> "8"
            "9" -> "9"
            "auto" -> "auto"
            "32rem" -> "lg"
            "28rem" -> "md"
            "24rem" -> "sm"
            "36rem" -> "xl"
            "20rem" -> "xs"
            else -> "[$value]"
        }
        return "$key-$result"
    }

    fun bgPosition(value: String): String {
        return "bg-${value.replace(" ", "-")}"
    }

    fun boxSizing(value: String): String {
        return when (value) {
            "border-box" -> "box-border"
            "content-box" -> "box-content"
            else -> value
        }
    }
}
