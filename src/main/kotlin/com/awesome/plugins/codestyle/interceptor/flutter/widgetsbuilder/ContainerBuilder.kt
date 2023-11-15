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
            builder.append("height: ${style.height}.h,")
        }
        ///如果没有，就认定为普通的Container
        val bg = FlutterHelper.background(style.background!!)
        if (style.border == null && style.borderRadius == null && bg.startsWith("0x")) {
            builder.append(style.colorProp(FlutterHelper.getColor(style.background ?: "")))
            builder.append(")")
            return builder.toString()
        }
        if (isChain) {
            builder.append("decoration: ${buildChainDecoration()}")
        } else {
            builder.append("decoration: ${buildDecoration()}")
        }
        return builder.append("),").toString()
    }

    private fun buildDecoration(): String {
        val builder = StringBuilder("BoxDecoration(")
        builder.append(style.colorProp(FlutterHelper.getColor(style.background ?: "")))
        FlutterHelper.borderRadius(style.borderRadius)?.apply {
            builder.append("borderRadius: ${FlutterHelper.removeUselessRadius(this)}")
        }
        return builder.append("),").toString()
    }

    private fun buildChainDecoration(): String {
        val builder = StringBuilder("bd.")
        builder.append(style.colorChainProp(FlutterHelper.getColor(style.background ?: "")))
        FlutterHelper.borderRadius(style.borderRadius)?.apply { builder.append("borderRadius: $this") }
        return builder.append("),").toString()
    }

}
