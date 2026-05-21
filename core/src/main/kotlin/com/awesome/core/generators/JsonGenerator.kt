package com.awesome.core.generators

import com.awesome.core.model.GenerateRequest
import com.awesome.core.model.GeneratedFile

/**
 * Uniform language generator. Implementations are stateless from the caller's
 * point of view: each call to [generate] is self-contained.
 */
interface JsonGenerator {
    fun generate(req: GenerateRequest): List<GeneratedFile>
}
