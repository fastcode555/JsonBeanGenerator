package com.awesome.core

/**
 * Centralised property keys shared between the IntelliJ plugin (which reads
 * them from `plugins.properties`) and the CLI (which gets them via flags).
 *
 * Keep keys in sync between this object and the docs in the design spec.
 */
object PluginProps {
    const val properties = "plugins.properties"
    const val assetsIgnoreDirs = "plugin.assetsIgnoreDirs"
    const val generateAssetDirs = "plugin.generateAssetDirs"
    const val languageAssetsDir = "plugin.languageAssetsDir"
    const val modelType = "plugin.modelType"
    const val clone = "plugin.clone"
    const val depType = "plugin.depType"
    const val languageDir = "plugin.languageDir"
    const val languages = "plugin.languages"
    const val needTranslate = "plugin.needTranslate"
    const val rawLanguage = "plugin.rawLanguage"
    const val flutterChain = "plugin.flutterChain"
    const val translationKey = "plugin.translationKey"
    const val splitGFile = "plugin.splitGFile"

    // Option keys used in GenerateRequest.options (CLI & plugin both honour these)
    const val OPT_SPLIT_G = "splitGFile"
    const val OPT_SQLITE = "sqliteEnable"
    const val OPT_PRIMARY_KEY = "primaryKey"
    const val OPT_NEED_CLONE = "needClone"
    const val OPT_KT_DEP = "kotlinDep"          // "gson" | "fastjson" | "none"
}
