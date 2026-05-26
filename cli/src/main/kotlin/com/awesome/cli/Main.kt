package com.awesome.cli

import com.awesome.cli.commands.DartCommand
import com.awesome.cli.commands.JavaCommand
import com.awesome.cli.commands.KtCommand
import com.awesome.cli.commands.TsCommand
import picocli.CommandLine
import picocli.CommandLine.Command
import kotlin.system.exitProcess

@Command(
    name = "json2bean",
    mixinStandardHelpOptions = true,
    version = ["json2bean 1.10.0"],
    description = ["Generate language bean files from JSON. Use a subcommand to pick the target language."],
    subcommands = [DartCommand::class, KtCommand::class, TsCommand::class, JavaCommand::class],
)
class MainCommand : Runnable {
    /** Shown when invoked without a subcommand. */
    override fun run() {
        CommandLine.usage(this, System.out)
    }
}

fun main(args: Array<String>) {
    val exit = CommandLine(MainCommand()).execute(*args)
    exitProcess(exit)
}
