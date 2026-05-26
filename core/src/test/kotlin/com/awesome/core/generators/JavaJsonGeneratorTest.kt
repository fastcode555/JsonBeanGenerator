package com.awesome.core.generators

import com.awesome.core.model.GenerateRequest
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test

class JavaJsonGeneratorTest {

    @Test
    fun `flat primitives generate Gson POJO with @SerializedName`() {
        val out = JavaJsonGenerator().generate(GenerateRequest(
            json = """{"user_id": 1, "name": "x"}""",
            className = "User",
        ))
        out.size shouldBe 1
        out[0].name shouldBe "User.java"
        val content = out[0].content
        content shouldContain "import com.google.gson.annotations.SerializedName;"
        content shouldContain "public class User {"
        content shouldContain "@SerializedName(\"user_id\")"
        content shouldContain "public Integer userId;"
        content shouldContain "@SerializedName(\"name\")"
        content shouldContain "public String name;"
    }

    @Test
    fun `complex_nested matches expected java fixture`() {
        val json = fixtureInput("complex_nested")
        val out = JavaJsonGenerator().generate(GenerateRequest(json = json, className = "User"))
        out.size shouldBe 1
        out[0].name shouldBe "User.java"
        out[0].content shouldBe fixture("complex_nested", "expected.java")
    }
}
