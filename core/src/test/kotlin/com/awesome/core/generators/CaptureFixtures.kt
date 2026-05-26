package com.awesome.core.generators

import com.awesome.core.PluginProps
import com.awesome.core.generators.kt.KtFastJsonGenerator
import com.awesome.core.generators.kt.KtGsonGenerator
import com.awesome.core.generators.kt.MapKtJsonGenerator
import com.awesome.core.model.GenerateRequest
import java.nio.file.Files
import java.nio.file.Paths

/**
 * One-shot capture: re-runs the generators against the complex_nested fixture
 * and writes expected.* files into src/test/resources/fixtures/complex_nested/.
 *
 * Run via:  ./gradlew :core:captureFixtures
 * Do NOT call this as a regular test; it overwrites the golden files.
 */
fun main() {
    val dir = Paths.get("src/test/resources/fixtures/complex_nested")
    val json = Files.readString(dir.resolve("input.json"))

    val dartNonSplit = DartJsonGenerator(json, "User", "", "", false, "", false, false)
    Files.writeString(dir.resolve("expected.dart"), dartNonSplit.toString())

    val dartSplit = DartJsonGenerator(json, "User", "", "", false, "", true, true)
    val out = dartSplit.generate()
    Files.writeString(dir.resolve("expected.dart.main"), out.mainContent)
    Files.writeString(dir.resolve("expected.dart.g"), out.partContent ?: "")

    Files.writeString(dir.resolve("expected.ts"),
        TsJsonGenerator(json, "User", "", "").toString())
    Files.writeString(dir.resolve("expected.py"),
        PythonJsonGenerator(json, "User", "", "").toString())
    Files.writeString(dir.resolve("expected.kt.map"),
        MapKtJsonGenerator(json, "User", "", "").toString())
    Files.writeString(dir.resolve("expected.kt.gson"),
        KtGsonGenerator(json, "User", "", "").toString())
    Files.writeString(dir.resolve("expected.kt.fastjson"),
        KtFastJsonGenerator(json, "User", "", "").toString())

    println("Captured ${dir.toAbsolutePath()}")
}
