package com.awesome.core.generators

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class PythonGoldenTest {
    @Test
    fun `output matches expected py`() {
        val json = fixtureInput("complex_nested")
        val actual = PythonJsonGenerator(json, "User", "", "").toString()
        actual shouldBe fixture("complex_nested", "expected.py")
    }
}
