package com.awesome.common

val chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"

object MultiBase {
    fun addOne(base62Number: String): String {
        // 定义62进制的字符集
        val base = chars.length // base = 62
        // 转换输入为字符数组，以便操作
        val number = base62Number.toCharArray()
        // 初始化进位标志
        var carry = 1
        // 从最后一位开始向前遍历
        for (i in number.size - 1 downTo 0) {
            if (carry == 0) break // 如果没有进位，跳出循环

            // 找到当前字符在字符集中的位置
            val index = chars.indexOf(number[i])

            // 计算新位置并更新字符
            val newIndex = (index + carry) % base
            number[i] = chars[newIndex]

            // 计算是否有进位
            carry = (index + carry) / base
        }
        // 如果最后还有进位，则在最前面加上字符'1'
        return if (carry > 0) "1${String(number)}" else String(number)
    }
}
