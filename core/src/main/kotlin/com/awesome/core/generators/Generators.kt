package com.awesome.core.generators

import com.awesome.core.PluginProps
import com.awesome.core.generators.kt.KtFastJsonGenerator
import com.awesome.core.generators.kt.KtGsonGenerator
import com.awesome.core.generators.kt.MapKtJsonGenerator
import com.awesome.core.model.GenerateRequest
import com.awesome.core.model.GeneratedFile
import com.awesome.core.util.toUpperCamel

/**
 * Single entry point. Pick a generator by language tag and call [generate].
 *
 * Language tags: "dart", "ts", "py", "java", "kt".
 */
object Generators {
    fun generate(language: String, req: GenerateRequest): List<GeneratedFile> {
        return when (language) {
            "dart" -> DartAdapter.generate(req)
            "ts" -> TsAdapter.generate(req)
            "py" -> PythonAdapter.generate(req)
            "java" -> JavaJsonGenerator().generate(req)
            "kt" -> KtAdapter.generate(req)
            else -> error("Unknown language: $language")
        }
    }
}

private object DartAdapter : JsonGenerator {
    override fun generate(req: GenerateRequest): List<GeneratedFile> {
        val gen = DartJsonGenerator(
            content = req.json,
            fileName = req.className,
            extendsClass = req.extendsClass,
            implementClass = req.implementsClass,
            sqliteSupport = req.boolOption(PluginProps.OPT_SQLITE),
            primaryKey = req.stringOption(PluginProps.OPT_PRIMARY_KEY),
            needClone = req.boolOption(PluginProps.OPT_NEED_CLONE),
            splitGFile = req.boolOption(PluginProps.OPT_SPLIT_G),
        )
        val out = gen.generate()
        val files = mutableListOf(GeneratedFile(out.mainFileName, out.mainContent))
        if (out.partContent != null && out.partFileName != null) {
            files += GeneratedFile(out.partFileName, out.partContent)
        }
        return files
    }
}

private object TsAdapter : JsonGenerator {
    override fun generate(req: GenerateRequest): List<GeneratedFile> {
        val content = TsJsonGenerator(req.json, req.className, req.extendsClass, req.implementsClass).toString()
        return listOf(GeneratedFile("${req.className.toUpperCamel()}.ts", content))
    }
}

private object PythonAdapter : JsonGenerator {
    override fun generate(req: GenerateRequest): List<GeneratedFile> {
        val content = PythonJsonGenerator(req.json, req.className, req.extendsClass, req.implementsClass).toString()
        return listOf(GeneratedFile("${req.className}.py", content))
    }
}

private object KtAdapter : JsonGenerator {
    override fun generate(req: GenerateRequest): List<GeneratedFile> {
        val dep = req.stringOption(PluginProps.OPT_KT_DEP, "gson")
        val name = req.className
        val content = when (dep) {
            "none" -> MapKtJsonGenerator(req.json, name, req.extendsClass, req.implementsClass).toString()
            "fastjson" -> KtFastJsonGenerator(req.json, name, req.extendsClass, req.implementsClass).toString()
            else -> KtGsonGenerator(req.json, name, req.extendsClass, req.implementsClass).toString()
        }
        return listOf(GeneratedFile("${name.toUpperCamel()}.kt", content))
    }
}
