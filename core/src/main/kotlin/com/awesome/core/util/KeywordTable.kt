package com.awesome.core.util

/**
 * Identifier names that collide with Dart primitive types. Generators append
 * an "x" suffix when a field name lands here.
 *
 * This is intentionally Dart-flavoured (the original use case); other language
 * generators that need their own collision tables should add their own files.
 */
val KEYS: Array<String> = arrayOf("num", "int", "String", "double", "bool")

/** Type names that collide with Dart generic containers (List, Map). */
val UPPER_KEYS: Array<String> = arrayOf("List", "Map")

const val REGEX_SYMBOL: String = "[~'`!@#\$%^&*()_\\-+=<>?:\"{}|,./;’\\[\\]·！@#￥%……&*（）——\\-+=\\{\\}|《》？：“”【】；‘’，。、]*"
