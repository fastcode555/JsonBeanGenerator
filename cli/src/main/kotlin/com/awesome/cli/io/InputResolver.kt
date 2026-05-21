package com.awesome.cli.io

import java.nio.file.Files
import java.nio.file.Paths

object InputResolver {
    /**
     * Returns the raw JSON string, sourced from either `-i FILE` or `--json STRING`.
     * Exactly one of the two must be supplied.
     */
    fun resolve(inputFile: String?, inlineJson: String?): String {
        require(!(inputFile != null && inlineJson != null)) {
            "--input and --json are mutually exclusive"
        }
        if (inputFile != null) {
            return Files.readString(Paths.get(inputFile))
        }
        require(inlineJson != null) { "Either --input <FILE> or --json <STRING> is required" }
        return inlineJson
    }
}
