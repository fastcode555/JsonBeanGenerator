package com.awesome.cli.commands

import picocli.CommandLine.Command

@Command(name = "java", description = ["Generate a Java Gson POJO from JSON."])
class JavaCommand : BeanCommand() {
    override fun language() = "java"
}
