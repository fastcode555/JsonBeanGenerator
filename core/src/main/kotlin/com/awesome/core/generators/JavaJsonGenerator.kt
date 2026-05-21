package com.awesome.core.generators

import com.awesome.core.model.GenerateRequest
import com.awesome.core.model.GeneratedFile

/**
 * Java Gson-style POJO generator. Real implementation lands in Phase 3
 * (Task 10-12) via TDD. This stub keeps the Generators dispatcher compilable.
 */
class JavaJsonGenerator : JsonGenerator {
    override fun generate(req: GenerateRequest): List<GeneratedFile> =
        listOf(GeneratedFile("${req.className}.java", "// JavaJsonGenerator not yet implemented\n"))
}
