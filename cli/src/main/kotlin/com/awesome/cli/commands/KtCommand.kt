package com.awesome.cli.commands

import com.awesome.core.PluginProps
import picocli.CommandLine.Command
import picocli.CommandLine.Option

@Command(name = "kt", description = ["Generate a Kotlin bean from JSON."])
class KtCommand : BeanCommand() {

    @Option(names = ["--dep"], description = ["Serialization style: gson (default) | fastjson | none"])
    var dep: String = "gson"

    override fun language() = "kt"

    override fun extraOptions(): Map<String, Any?> = mapOf(
        PluginProps.OPT_KT_DEP to dep,
    )
}
