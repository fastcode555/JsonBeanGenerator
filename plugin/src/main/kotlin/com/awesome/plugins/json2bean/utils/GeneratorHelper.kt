package com.awesome.plugins.json2bean.utils

import com.awesome.core.generators.Generators
import com.awesome.core.model.GenerateRequest
import com.awesome.core.model.GeneratedFile
import com.intellij.psi.PsiDirectory

/**
 * Plugin-side façade over [com.awesome.core.generators.Generators]. Handles
 * file-system writes via PSI; pure generation lives in :core.
 */
object GeneratorHelper {
    /**
     * Run a generator and write each [GeneratedFile] into [psiDirectory].
     * Returns the list of files for callers that want to inspect them
     * (e.g. preview dialog).
     */
    fun generateAndWrite(
        fileType: String,
        req: GenerateRequest,
        psiDirectory: PsiDirectory,
    ): List<GeneratedFile> {
        val language = fileType.removePrefix(".")
        val files = Generators.generate(language, req)
        files.forEach { f ->
            java.io.File(psiDirectory.virtualFile.path, f.name).writeText(f.content)
        }
        return files
    }

    /** Preview-only path: returns generated content without writing files. */
    fun previewMain(fileType: String, req: GenerateRequest): String {
        val language = fileType.removePrefix(".")
        return Generators.generate(language, req).first().content
    }
}
