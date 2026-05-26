package com.awesome.core.generators.kt

import com.awesome.core.generators.fixture
import com.awesome.core.generators.fixtureInput
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class KtGoldenTest {
    private val json = fixtureInput("complex_nested")

    @Test
    fun `map style matches expected`() {
        MapKtJsonGenerator(json, "User", "", "").toString() shouldBe
            fixture("complex_nested", "expected.kt.map")
    }

    @Test
    fun `gson style matches expected`() {
        KtGsonGenerator(json, "User", "", "").toString() shouldBe
            fixture("complex_nested", "expected.kt.gson")
    }

    @Test
    fun `fastjson style matches expected`() {
        KtFastJsonGenerator(json, "User", "", "").toString() shouldBe
            fixture("complex_nested", "expected.kt.fastjson")
    }
}
