package com.awesome.core.generators

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class DartGoldenTest {
    private val json = fixtureInput("complex_nested")

    @Test
    fun `non-split output matches expected dart`() {
        val actual = DartJsonGenerator(json, "User", "", "", false, "", false, false).toString()
        actual shouldBe fixture("complex_nested", "expected.dart")
    }

    @Test
    fun `split-g main file matches expected`() {
        val out = DartJsonGenerator(json, "User", "", "", false, "", true, true).generate()
        out.mainContent shouldBe fixture("complex_nested", "expected.dart.main")
    }

    @Test
    fun `split-g part file matches expected`() {
        val out = DartJsonGenerator(json, "User", "", "", false, "", true, true).generate()
        (out.partContent ?: "") shouldBe fixture("complex_nested", "expected.dart.g")
    }
}
