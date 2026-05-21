package com.awesome.cli

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import picocli.CommandLine
import java.nio.file.Files
import java.nio.file.Path

class CliIntegrationTest {

    private fun exec(vararg args: String): Int =
        CommandLine(MainCommand()).execute(*args)

    private fun writeInput(tmp: Path, json: String): Path {
        val f = tmp.resolve("input.json")
        Files.writeString(f, json)
        return f
    }

    @Test
    fun `dart subcommand writes file with -i and -o`(@TempDir tmp: Path) {
        val input = writeInput(tmp, """{"name":"Foo","age":30}""")
        val output = tmp.resolve("Foo.dart")

        val code = exec("dart", "-i", input.toString(), "-o", output.toString(), "-q")
        code shouldBe 0
        Files.exists(output) shouldBe true
        val content = Files.readString(output)
        content shouldContain "class Foo {"
        content shouldContain "json.asString('name')"
        content shouldContain "json.asInt('age')"
    }

    @Test
    fun `kt subcommand defaults to gson dep`(@TempDir tmp: Path) {
        val output = tmp.resolve("Foo.kt")
        val code = exec("kt", "-j", """{"name":"Foo"}""", "-o", output.toString(), "-q")
        code shouldBe 0
        Files.readString(output) shouldContain "@SerializedName"
    }

    @Test
    fun `kt subcommand with --dep fastjson switches annotation`(@TempDir tmp: Path) {
        val output = tmp.resolve("Foo.kt")
        val code = exec("kt", "-j", """{"name":"Foo"}""", "-o", output.toString(), "--dep", "fastjson", "-q")
        code shouldBe 0
        Files.readString(output) shouldContain "@JSONField"
    }

    @Test
    fun `dart --split-g writes both main and g file`(@TempDir tmp: Path) {
        val output = tmp.resolve("User.dart")
        val partFile = tmp.resolve("User.g.dart")
        val code = exec("dart", "-j", """{"a":1}""", "-o", output.toString(), "--split-g", "--clone", "-q")
        code shouldBe 0
        Files.exists(output) shouldBe true
        Files.exists(partFile) shouldBe true
        Files.readString(output) shouldContain "part 'User.g.dart';"
        Files.readString(partFile) shouldContain "part of 'User.dart';"
    }

    @Test
    fun `refuses to overwrite without --force`(@TempDir tmp: Path) {
        val output = tmp.resolve("Foo.dart")
        Files.writeString(output, "// existing")
        val code = exec("dart", "-j", """{"a":1}""", "-o", output.toString())
        code shouldBe 3
        Files.readString(output) shouldBe "// existing"
    }

    @Test
    fun `--force overwrites existing file`(@TempDir tmp: Path) {
        val output = tmp.resolve("Foo.dart")
        Files.writeString(output, "// existing")
        val code = exec("dart", "-j", """{"a":1}""", "-o", output.toString(), "--force", "-q")
        code shouldBe 0
        Files.readString(output) shouldContain "class Foo"
    }

    @Test
    fun `both -i and --json is exit code 2`(@TempDir tmp: Path) {
        val input = writeInput(tmp, """{}""")
        val output = tmp.resolve("X.dart")
        val code = exec("dart", "-i", input.toString(), "-j", """{}""", "-o", output.toString())
        code shouldBe 2
    }

    @Test
    fun `missing input is exit code 2`(@TempDir tmp: Path) {
        val output = tmp.resolve("X.dart")
        val code = exec("dart", "-o", output.toString())
        code shouldBe 2
    }

    @Test
    fun `java subcommand emits Gson POJO`(@TempDir tmp: Path) {
        val output = tmp.resolve("Foo.java")
        val code = exec("java", "-j", """{"user_id":1}""", "-o", output.toString(), "-q")
        code shouldBe 0
        val content = Files.readString(output)
        content shouldContain "import com.google.gson.annotations.SerializedName;"
        content shouldContain "@SerializedName(\"user_id\")"
        content shouldContain "public Integer userId;"
    }

    @Test
    fun `ts subcommand emits TS interface`(@TempDir tmp: Path) {
        val output = tmp.resolve("Foo.ts")
        val code = exec("ts", "-j", """{"name":"x","age":1}""", "-o", output.toString(), "-q")
        code shouldBe 0
        val content = Files.readString(output)
        content shouldContain "export interface Foo {"
        content shouldContain "name?: string"
        content shouldContain "age?: number"
    }
}
