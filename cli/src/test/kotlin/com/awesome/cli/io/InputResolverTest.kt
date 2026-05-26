package com.awesome.cli.io

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

class InputResolverTest {

    @Test
    fun `reads json from -i file`(@TempDir tmp: Path) {
        val file = tmp.resolve("u.json")
        Files.writeString(file, """{"a":1}""")
        InputResolver.resolve(inputFile = file.toString(), inlineJson = null) shouldBe """{"a":1}"""
    }

    @Test
    fun `reads json from --json string`() {
        InputResolver.resolve(inputFile = null, inlineJson = """{"b":2}""") shouldBe """{"b":2}"""
    }

    @Test
    fun `errors when both supplied`() {
        val ex = shouldThrow<IllegalArgumentException> {
            InputResolver.resolve(inputFile = "a.json", inlineJson = """{}""")
        }
        ex.message!! shouldContain "mutually exclusive"
    }

    @Test
    fun `errors when neither supplied`() {
        val ex = shouldThrow<IllegalArgumentException> {
            InputResolver.resolve(inputFile = null, inlineJson = null)
        }
        ex.message!! shouldContain "required"
    }
}
