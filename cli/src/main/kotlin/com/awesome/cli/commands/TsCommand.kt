package com.awesome.cli.commands

import picocli.CommandLine.Command

@Command(name = "ts", description = ["Generate a TypeScript interface from JSON."])
class TsCommand : BeanCommand() {
    override fun language() = "ts"
}
