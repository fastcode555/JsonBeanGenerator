package com.awesome.core.model

/** A single file the caller should write. `name` is a file name only (no path). */
data class GeneratedFile(val name: String, val content: String)
