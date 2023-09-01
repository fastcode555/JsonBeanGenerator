package com.awesome.plugins.ts.totailwindcss

import com.awesome.utils.isNumber

object TailWindHelper {

    /**
     * 处理FontWeight 属性
     **/
    fun fontWeight(key: String, value: String): String {
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
    fun border(key: String, value: String): String {
        if (value.contains("solid")) {
            val results = value.split("solid")
            val color = results.last().trim()
            return "${TailWindHelper.borderWidth("border", results.first().trim())} border-solid border-[$color]"
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

    fun padding(key: String, value: String): String = dealMarginOrPadding(key, value, "p").replaceEmpty("p")

    fun margin(key: String, value: String): String = dealMarginOrPadding(key, value, "m").replaceEmpty("m")

    private fun String.replaceEmpty(mark: String = "m"): String {
        if (this.contains("[0]")) {
            return this.replace(Regex("$mark.*?-\\[0\\]"), "")
        }
        return this;
    }

    fun String.replaceRegex(regexString: String): String = this.replace(Regex(regexString), "").trim()

    /**
     * 处理Margin值和Padding值
     **/
    private fun dealMarginOrPadding(key: String, value: String, mark: String = "m"): String {
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
                "1rem" -> "'2xl'"
                "1.5rem" -> "'3xl'"
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
    fun borderRadius(key: String, value: String): String {
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
            "flex-start" -> "justify-start"
            "flex-end" -> "justify-end"
            "center" -> "justify-center"
            "space-between" -> "justify-between"
            "space-around" -> "justify-around"
            "space-evenly" -> "justify-evenly"
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
    fun flexShrink(value: String): String {
        return when (value) {
            "1" -> "flex-shrink"
            "0" -> "flex-shrink-0"
            else -> "flex-shrink"
        }
    }

    /**
     * 处理颜色属性
     **/
    fun color(key: String, value: String): String {
        if (value.startsWith("#")) {
            return "text-[$value]"
        }
        return "text-$value"
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

    fun letterSpacing(key: String, value: String): String {
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

    fun lineHeight(key: String, value: String): String {
        val finalValue = when (value) {
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

    fun fontSize(key: String, value: String): String {
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

    fun fontFamily(key: String, value: String): String {
        val finalValue = when (value) {
            "ui-sans-serif", "sans-serif", "Apple Color Emoji", "Segoe UI Emoji", "Segoe UI Symbol", "Noto Color Emoji" -> "sans"
            "ui-serif", "Georgia", "Cambria", "Times New Roman", "Times", "serif" -> "serif"
            "ui-monospace", "SFMono-Regular", "Menlo", "Monaco", "Consolas", "Liberation Mono", "Courier New", "monospace" -> "mono"
            else -> "[$value]"
        }
        return "font-$finalValue"
    }

    fun order(key: String, value: String): String {
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
            "50%" -> "2/4"
            "75%" -> "3/4"
            "100%" -> "full"
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
            "8.333333%" -> "1/12"
            "16.666667%" -> "2/12"
            "25%" -> "3/12"
            "33.333333%" -> "4/12"
            "41.666667%" -> "5/12"
            "50%" -> "6/12"
            "58.333333%" -> "7/12"
            "66.666667%" -> "8/12"
            "75%" -> "9/12"
            "83.333333%" -> "10/12"
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

    fun aspectRatio(key: String, text: String): String {
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
            return "blur-$finalValue"
        }
        return "$key-[$text]"
    }

    fun boxShadow(key: String, value: String): String {
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

    fun textTransform(key: String, value: String): String {
        val finalValue = when (value) {
            "uppercase" -> "uppercase"
            "lowercase" -> "lowercase"
            "capitalize" -> "capitalize"
            "none" -> ""
            else -> ""
        }
        return finalValue.ifEmpty { "" }
    }

    fun fontStyle(key: String, value: String): String {
        val finalValue = when (value) {
            "italic" -> "italic"
            "normal" -> ""
            else -> ""
        }
        return finalValue.ifEmpty { "" }
    }

    fun backgroundClip(key: String, value: String): String {
        val finalValue = when (value) {
            "text" -> "bg-clip-text"
            "content-box" -> "bg-clip-content"
            "padding-box" -> "bg-clip-padding"
            "border-box" -> "bg-clip-border"
            else -> ""
        }
        return finalValue
    }

    fun textDecorationStyle(key: String, value: String): String {
        val finalValue = when (value) {
            "solid" -> "decoration-solid"
            "double" -> "decoration-double"
            "dotted" -> "decoration-dotted"
            "dashed" -> "decoration-dashed"
            "wavy" -> "decoration-wavy"
            else -> ""
        }
        return finalValue.ifEmpty { "" }
    }

    fun textDecorationLine(key: String, value: String): String {
        val finalValue = when (value) {
            "underline" -> "underline"
            "overline" -> "overline"
            "line-through" -> "line-through"
            "none" -> "no-underline"
            else -> ""
        }
        return finalValue.ifEmpty { "" }
    }

    fun bgBlendMode(key: String, value: String): String {
        val finalValue = when (value) {
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
        return finalValue.ifEmpty { "" }
    }

    fun mixBlendMode(key: String, value: String): String {
        val finalValue = when (value) {
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
        return finalValue.ifEmpty { "" }
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
            else -> ""
        }
        return if (finalValue.isNotEmpty()) "$marker-${finalValue}" else ""
    }

    fun strokeWidth(key: String, value: String): String {
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

    fun textDecorationThickness(key: String, value: String): String {
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

    fun textUnderLineOffset(key: String, value: String): String {
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

    fun zIndex(key: String, value: String): String {
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


}
