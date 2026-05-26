package com.awesome.core.generators

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class TsGoldenTest {
    @Test
    fun `output matches expected ts`() {
        val json = fixtureInput("complex_nested")
        val actual = TsJsonGenerator(json, "User", "", "").toString()
        actual shouldBe fixture("complex_nested", "expected.ts")
    }
}
