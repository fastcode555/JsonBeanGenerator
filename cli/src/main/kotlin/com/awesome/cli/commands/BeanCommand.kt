package com.awesome.cli.commands

import com.awesome.cli.io.InputResolver
import com.awesome.core.generators.Generators
import com.awesome.core.model.GenerateRequest
import com.awesome.core.model.GeneratedFile
import com.awesome.core.util.toUpperCamel
import picocli.CommandLine.Option
import java.io.File
import java.util.concurrent.Callable

/**
 * Shared CLI plumbing: input flags, output flag, generic exit codes.
 * Subclasses provide [language] and [extraOptions].
 */
abstract class BeanCommand : Callable<Int> {

    @Option(names = ["-i", "--input"], description = ["JSON input file"])
    var inputFile: String? = null

    @Option(names = ["-j", "--json"], description = ["JSON input string"])
    var inlineJson: String? = null

    @Option(names = ["-o", "--output"], required = true, description = ["Output file path"])
    lateinit var output: String

    @Option(names = ["-n", "--class-name"], description = ["Class name (default: derived from -o filename stem)"])
    var className: String? = null

    @Option(names = ["-e", "--extends"], description = ["Class to extend"])
    var extendsClass: String = ""

    @Option(names = ["-m", "--implements"], description = ["Classes to implement (comma-separated)"])
    var implementsClass: String = ""

    @Option(names = ["--force"], description = ["Overwrite existing output file"])
    var force: Boolean = false

    @Option(names = ["-q", "--quiet"], description = ["Suppress informational output"])
    var quiet: Boolean = false

    abstract fun language(): String

    /** Subclasses inject language-specific options. */
    open fun extraOptions(): Map<String, Any?> = emptyMap()

    override fun call(): Int {
        val json = try {
            InputResolver.resolve(inputFile, inlineJson)
        } catch (e: IllegalArgumentException) {
            System.err.println("error: ${e.message}")
            return 2
        }

        val trimmed = json.trimStart()
        if (!trimmed.startsWith('{') && !trimmed.startsWith('[')) {
            System.err.println("error: invalid JSON: input does not start with '{' or '['")
            return 1
        }

        val mainFile = File(output)
        if (mainFile.exists() && !force) {
            System.err.println("error: output exists (use --force to overwrite): $output")
            return 3
        }

        val effectiveName = className ?: File(output).nameWithoutExtension.toUpperCamel()
        val req = GenerateRequest(
            json = json,
            className = effectiveName,
            extendsClass = extendsClass,
            implementsClass = implementsClass,
            options = extraOptions(),
        )

        val files = try {
            Generators.generate(language(), req)
        } catch (e: com.alibaba.fastjson2.JSONException) {
            System.err.println("error: invalid JSON: ${e.message}")
            return 1
        } catch (e: IllegalStateException) {
            System.err.println("error: ${e.message}")
            return 2
        } catch (e: Throwable) {
            System.err.println("internal error (please file an issue): ${e.javaClass.simpleName}: ${e.message}")
            return 64
        }

        // Write main file at -o; siblings go in the same directory under their reported name.
        val outDir = mainFile.absoluteFile.parentFile
        try {
            files.forEachIndexed { i, f ->
                val target = if (i == 0) mainFile else File(outDir, f.name)
                if (target.exists() && !force) {
                    System.err.println("error: sibling output exists: ${target.path}")
                    return 3
                }
                target.writeText(f.content)
                if (!quiet) System.out.println("wrote ${target.path} (${f.content.length} bytes)")
            }
        } catch (e: Exception) {
            System.err.println("error: write failed: ${e.message}")
            return 4
        }

        return 0
    }
}
