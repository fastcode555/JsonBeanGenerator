package com.awesome.core.generators

import java.nio.file.Files
import java.nio.file.Paths

/** Reads a fixture from `core/src/test/resources/fixtures/<dir>/<name>`. */
fun fixture(dir: String, name: String): String {
    val url = Thread.currentThread().contextClassLoader.getResource("fixtures/$dir/$name")
        ?: error("Fixture not found on classpath: fixtures/$dir/$name")
    return Files.readString(Paths.get(url.toURI()))
}

/** Convenience: read input.json for the given fixture directory. */
fun fixtureInput(dir: String): String = fixture(dir, "input.json")
