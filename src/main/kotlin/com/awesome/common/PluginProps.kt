package com.awesome.common

object PluginProps {
    ///文件名
    const val properties = "plugins.properties"

    ///Assets文件夹，添加忽略的文件夹，就不会对生成的文件夹进行R文件的生成
    const val assetsIgnoreDirs = "plugin.assetsIgnoreDirs"

    ///指定生成Assets所在的文件夹
    const val generateAssetDirs = "plugin.generateAssetDirs"

    ///指定生成语言所在的文件夹
    const val languageAssetsDir = "plugin.languageAssetsDir"

    ///生成Bean类的模型
    const val modelType = "plugin.modelType"

    ///生成指定的clone模型
    const val clone = "plugin.clone"

    ///储存用户所使用的依赖库的类型
    const val depType = "plugin.depType"

    ///生成语言所在的文件夹
    const val languageDir = "plugin.languageDir"

    ///生成所需要支持的语言
    const val languages = "plugin.languages"

    ///是否需要翻译
    const val needTranslate = "plugin.needTranslate"

    ///原型语言是什么,如果设置是英语，配合needTranslate，就不会去获取翻译的内容了,多数情况下无用，适用于无法连接网络的情况
    const val rawLanguage = "plugin.rawLanguage"
}
