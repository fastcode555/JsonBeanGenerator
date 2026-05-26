package com.awesome.core.model

/**
 * Uniform request to any [com.awesome.core.generators.JsonGenerator].
 *
 * Language-specific knobs live in [options]; key names are centralised in
 * [com.awesome.core.PluginProps].
 */
data class GenerateRequest(
    val json: String,
    val className: String,
    val extendsClass: String = "",
    val implementsClass: String = "",
    val options: Map<String, Any?> = emptyMap(),
) {
    fun boolOption(key: String): Boolean = options[key] as? Boolean ?: false
    fun stringOption(key: String, default: String = ""): String = options[key] as? String ?: default
}
