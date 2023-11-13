package com.awesome.plugins.codestyle.interceptor.flutter.widgetsbuilder

import com.awesome.plugins.codestyle.interceptor.flutter.FlutterBuilder
import com.awesome.plugins.codestyle.interceptor.flutter.FlutterHelper

/**
 * 用于构造Container组件
 **/
class ContainerBuilder(val style: FlutterBuilder, val isChain: Boolean) : BaseWidgetBuilder(style, isChain) {
    override fun build(): String {
        val builder = StringBuilder("Container(")
        if (style.width == style.height) {
            builder.append("width: ${style.width}.r,")
            builder.append("height: ${style.height}.r,")
        } else {
            builder.append("width: ${style.width}.w,")
            builder.append("height: ${style.width}.h,")
        }
        ///如果没有，就认定为普通的Container
        val bg = FlutterHelper.background(style.background!!)
        if (style.border == null && style.borderRadius == null && bg.startsWith("0x")) {
            val colorName = style.colorMap[bg]
            if (colorName != null) {
                builder.append("color: Colours.$colorName${style.crTail()}")
            } else {
                builder.append("color: const Color($bg)")
            }
            builder.append(")")
            return builder.toString()
        }
        if (isChain) {
            builder.append("decoration: ${buildChainDecoration()}")
        } else {
            builder.append("decoration: ${buildDecoration()}")
        }
        builder.append(")")
        return builder.toString()
    }

    private fun buildDecoration(): String {
        TODO("Not yet implemented")
    }

    private fun buildChainDecoration(): String {
        TODO("Not yet implemented")
    }

}
