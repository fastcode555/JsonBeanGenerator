package com.awesome.cli.commands

import com.awesome.core.PluginProps
import picocli.CommandLine.Command
import picocli.CommandLine.Option

@Command(name = "dart", description = ["Generate a Dart bean from JSON."])
class DartCommand : BeanCommand() {

    @Option(names = ["--split-g"], description = ["Also emit a .g.dart part file next to -o"])
    var splitG: Boolean = false

    @Option(names = ["--sqlite"], description = ["Emit json2dart_db sqlite hooks (requires --primary-key)"])
    var sqlite: Boolean = false

    @Option(names = ["--primary-key"], description = ["sqlite primary key field name"])
    var primaryKey: String = ""

    @Option(names = ["--clone"], description = ["Emit clone() method"])
    var clone: Boolean = false

    override fun language() = "dart"

    override fun extraOptions(): Map<String, Any?> = mapOf(
        PluginProps.OPT_SPLIT_G to splitG,
        PluginProps.OPT_SQLITE to sqlite,
        PluginProps.OPT_PRIMARY_KEY to primaryKey,
        PluginProps.OPT_NEED_CLONE to clone,
    )
}
