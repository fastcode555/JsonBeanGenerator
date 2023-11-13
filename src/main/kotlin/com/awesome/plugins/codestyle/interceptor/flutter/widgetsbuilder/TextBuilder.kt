package com.awesome.plugins.codestyle.interceptor.flutter.widgetsbuilder

import com.awesome.plugins.codestyle.interceptor.flutter.FlutterBuilder

/**
 *用于构造Text组件
 **/
class TextBuilder(private val style: FlutterBuilder, private val isChain: Boolean) :
    BaseWidgetBuilder(style, isChain) {
    override fun build(): String {
        val builder = StringBuilder("Text('Text'*50,")
        val maxLine = maxLine()
        if (maxLine > 1) {
            builder.append("maxLines: ${maxLine},")
        }
        if (isChain) {
            builder.append("style: ${buildChainTextStyle()}),")
        } else {
            builder.append("style: ${buildNormalTextStyle()},),")
        }
        return builder.toString()
    }

    private fun maxLine(): Int {
        if (style.lineHeight != null) {
            val line = ((style.height ?: 0).toDouble() / (style.lineHeight ?: 0).toDouble()).toInt()
            return if (line <= 0) 1 else line
        }
        return 1
    }

    private fun buildNormalTextStyle(): String {
        val builder = StringBuilder("TextStyle(")
        if (style.fontSize != 14.0 && style.fontSize != null) {
            builder.append("fontSize: ${style.fontSize}.sp,")
        }
        if (style.fontWeight != 400 && style.fontWeight != null) {
            builder.append("fontWeight: FontWeight.w${style.fontWeight},")
        }
        if (style.color != null) {
            val colorName = style.colorMap[style.color]
            if (colorName != null) {
                builder.append("color: Colours.$colorName${style.crTail()},")
            } else {
                builder.append("color: const Color(${style.color}),")
            }
        }
        return builder.append("overflow: TextOverflow.ellipsis)").toString()
    }

    private fun buildChainTextStyle(): String {
        val builder = StringBuilder("ts")
        if (style.fontSize != 14.0 && style.fontSize != null) {
            builder.append(".f${style.fontSize!!.toInt()}")
        }
        if (style.fontWeight != 400 && style.fontWeight != null) {
            builder.append(if (style.fontWeight == 700) ".bold" else ".w${style.fontWeight}")
        }
        if (style.color != null) {
            val colorName = style.colorMap[style.color]
            if (colorName != null) {
                builder.append(".$colorName")
            } else {
                builder.append(".Color(${style.color})")
            }
        }
        return builder.append(".ellipsis").append(".mk").toString()
    }

}
