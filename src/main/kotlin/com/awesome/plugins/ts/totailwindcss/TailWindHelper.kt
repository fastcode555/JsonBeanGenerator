package com.awesome.plugins.ts.totailwindcss

object TailWindHelper {
    fun fontWeight(key: String, value: String): String {
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

    fun border(key: String, value: String): String {
        if (value.contains("solid")) {
            val results = value.split("solid")
            val size = results.first().trim()
            val color = results.last().trim()
            return "border-[$size] border-solid border-[$color]"
        }
        return value
    }

    fun opacity(key: String, value: String): String {
        val size = (value.trim().toDouble() * 100).toInt()
        if (size == 100) return ""
        if (size % 5 == 0) {
            return "$key-${size}"
        }
        return "$key-[$size]"
    }

    fun padding(key: String, value: String): String = dealMarginOrPadding(key, value, "p").replaceEmpty("p")

    fun margin(key: String, value: String): String = dealMarginOrPadding(key, value, "m").replaceEmpty("m")

    private fun String.replaceEmpty(mark: String = "m"): String {
        if (this.contains("[0]")) {
            return this.replace(Regex("$mark.*?-\\[0\\]"), "")
        }
        return this;
    }

    fun String.replaceRegex(regexString: String): String {
        return this.replace(Regex(regexString), "").trim()
    }

    private fun dealMarginOrPadding(key: String, value: String, mark: String = "m"): String {
        if (value.contains(" ")) {
            val results = value.split(" ").clearEmpty()
            if (results.isSame()) {
                return "$mark-[${results[0].trim()}]"
            }
            if (results.size == 4) {
                if (results[0] == results[2] && results[1] == results[3]) {
                    return "${mark}x-[${results[1]}] ${mark}y-[${results[0]}]"
                }
                if (results[0] == results[2] && results[1] != results[3]) {
                    return "${mark}r-[${results[1]}] ${mark}l-[${results[3]}] ${mark}y-[${results[0]}]"
                }
                if (results[0] != results[2] && results[1] == results[3]) {
                    return "${mark}x-[${results[1]}] ${mark}t-[${results[0]}] ${mark}b-[${results[2]}]"
                }
                return "${mark}t-[${results[0]}] ${mark}r-[${results[1]}] ${mark}b-[${results[2]}] ${mark}l-[${results[3]}]"
            }
            if (results.size == 3) {
                if (results[0] == results[2]) {
                    return "${mark}x-[${results[1]}] ${mark}y-[${results[0]}]"
                }
                if (results[0] == results[1]) {
                    return "${mark}x-[${results[1]}] ${mark}t-[${results[0]}] ${mark}b-[${results[2]}]"
                }
            }
            if (results.size == 2) {
                return "${mark}x-[${results[0]}] ${mark}y-[${results[1]}]"
            }
        }
        return "${mark}-[$value]"
    }

    fun borderRadius(key: String, value: String): String {
        if (value.contains(" ")) {
            val results = value.split(" ").clearEmpty()
            if (results.isSame()) {
                return "rounded-[${results[0].trim()}]"
            }
            if (results.size == 4) {
                if (results[0] == results[1] && results[2] == results[3]) {
                    return "rounded-t-[${results[0]}] rounded-b-[${results[2]}]"
                }
                if (results[0] == results[1] && results[2] != results[3]) {
                    return "rounded-t-[${results[0]}] rounded-br-[${results[2]}] rounded-bl-[${results[3]}]"
                }
                if (results[0] != results[1] && results[2] == results[3]) {
                    return "rounded-tl-[${results[0]}] rounded-tr-[${results[1]}] rounded-b-[${results[2]}]"
                }
                if (results[0] == results[3] && results[1] == results[2]) {
                    return "rounded-l-[${results[0]}] rounded-r-${results[1]}"
                }
                if (results[0] == results[3] && results[1] != results[2]) {
                    return "rounded-l-[${results[0]}] rounded-tr-${results[1]} rounded-br-${results[2]}"
                }
                if (results[0] != results[3] && results[1] == results[2]) {
                    return "rounded-tl-[${results[0]}] rounded-bl-[${results[3]}] rounded-r-${results[1]}"
                }
            }
            if (results.size == 3) {
                if (results[0] == results[1]) {
                    return "rounded-t-[${results[0]}] rounded-br-[${results[1]}] rounded-bl-[${results[2]}]"
                }
                if (results[0] == results[2]) {
                    return "rounded-l-[${results[0]}] rounded-r-[${results[1]}]"
                }
            }
            if (results.size == 2) {
                return "rounded-tl-[${results[0]}] rounded-tr-[${results[1]}] round-br-[${results[0]}] rounded-bl-[${results[1]}]"
            }
        }
        return "rounded-[$value]"
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


}
