package com.awesome.plugins.codestyle.interceptor.flutter.widgetsbuilder

import com.awesome.plugins.codestyle.interceptor.flutter.FlutterBuilder

abstract class BaseWidgetBuilder(private val builder: FlutterBuilder, private val isChain: Boolean) {
    abstract fun build(): String
}
