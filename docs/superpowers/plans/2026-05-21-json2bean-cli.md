# json2bean CLI Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a command-line interface (`json2bean dart|kt|ts|java`) that shares all bean-generation logic with the existing IntelliJ plugin via a new shared `core` Gradle module.

**Architecture:** Split the existing single-module Gradle project into three modules: `core` (pure JSON-to-bean generators, zero IntelliJ deps), `plugin` (current IDE plugin migrated wholesale, depends on `:core`), and `cli` (new picocli-based CLI, depends on `:core`). The CLI ships as a fat jar via the Shadow plugin.

**Tech Stack:** Kotlin 2.1.10, Gradle 8.x, fastjson2 2.0.53, picocli 4.7.6, JUnit5 5.10.2, Kotest assertions 5.9.0, Shadow plugin 8.1.1, IntelliJ Platform gradle plugin 2.2.0.

**Reference spec:** `docs/superpowers/specs/2026-05-21-json2bean-cli-design.md`

---

## File Structure

After this plan completes, the repository will look like:

```
JsonBeanGenerator/
├── settings.gradle              ← include 'core', 'plugin', 'cli'
├── build.gradle                 ← root: shared kotlin/version config only
├── gradle.properties            ← unchanged
├── core/
│   ├── build.gradle             ← fastjson2 + guava + kotlin-stdlib; junit5 + kotest test
│   └── src/
│       ├── main/kotlin/com/awesome/core/
│       │   ├── PluginProps.kt
│       │   ├── model/
│       │   │   ├── GeneratedFile.kt
│       │   │   └── GenerateRequest.kt
│       │   ├── generators/
│       │   │   ├── JsonGenerator.kt
│       │   │   ├── BaseGenerator.kt
│       │   │   ├── DartJsonGenerator.kt
│       │   │   ├── TsJsonGenerator.kt
│       │   │   ├── PythonJsonGenerator.kt
│       │   │   ├── JavaJsonGenerator.kt
│       │   │   └── kt/
│       │   │       ├── MapKtJsonGenerator.kt
│       │   │       ├── KtGsonGenerator.kt
│       │   │       └── KtFastJsonGenerator.kt
│       │   └── util/
│       │       ├── Naming.kt
│       │       ├── JsonParse.kt
│       │       └── KeywordTable.kt
│       └── test/
│           ├── kotlin/com/awesome/core/generators/
│           │   ├── DartGoldenTest.kt
│           │   ├── TsGoldenTest.kt
│           │   ├── PythonGoldenTest.kt
│           │   ├── JavaGoldenTest.kt
│           │   └── kt/KtGoldenTest.kt
│           └── resources/fixtures/complex_nested/
│               ├── input.json
│               ├── expected.dart
│               ├── expected.dart.main
│               ├── expected.dart.g
│               ├── expected.ts
│               ├── expected.py
│               ├── expected.java
│               ├── expected.kt.map
│               ├── expected.kt.gson
│               └── expected.kt.fastjson
│
├── plugin/
│   ├── build.gradle             ← intellij-platform + project(':core')
│   └── src/                     ← migrated from old root src/
│       └── ... (existing structure)
│
└── cli/
    ├── build.gradle             ← shadow + picocli + project(':core') + junit5
    └── src/
        ├── main/kotlin/com/awesome/cli/
        │   ├── Main.kt
        │   ├── commands/
        │   │   ├── DartCommand.kt
        │   │   ├── KtCommand.kt
        │   │   ├── TsCommand.kt
        │   │   └── JavaCommand.kt
        │   └── io/
        │       └── InputResolver.kt
        └── test/kotlin/com/awesome/cli/
            └── CliIntegrationTest.kt
```

**Notes on file boundaries:**
- `core` has zero IntelliJ-platform / Swing / PSI imports. Verified by Phase 2 final step.
- Generator classes own their language-specific output logic; `JsonGenerator` interface gives a uniform `generate(req): List<GeneratedFile>` adapter on top of each class's existing `toString()` / `generate()` paths.
- `cli` knows nothing about plugin internals; uses only `com.awesome.core.*`.

---

## Phase 1 — Multi-Module Gradle Skeleton

### Task 1: Add empty core and cli modules to settings.gradle

**Files:**
- Modify: `settings.gradle`

- [ ] **Step 1: Update settings.gradle to declare three modules**

Replace the entire file content with:

```groovy
rootProject.name = 'JsonBeanGenerator'

include 'core', 'plugin', 'cli'
```

- [ ] **Step 2: Create empty module directories**

Run:
```bash
mkdir -p core/src/main/kotlin core/src/main/resources core/src/test/kotlin core/src/test/resources
mkdir -p cli/src/main/kotlin cli/src/main/resources cli/src/test/kotlin
mkdir -p plugin
```

- [ ] **Step 3: Create empty placeholder build.gradle files so Gradle accepts the modules**

Create `core/build.gradle`:
```groovy
plugins {
    id 'org.jetbrains.kotlin.jvm'
}
// Real config arrives in Task 3
```

Create `cli/build.gradle`:
```groovy
plugins {
    id 'org.jetbrains.kotlin.jvm'
}
// Real config arrives in Task 15
```

Create `plugin/build.gradle` as an empty file for now:
```bash
touch plugin/build.gradle
```

- [ ] **Step 4: Verify Gradle still sees the project (will fail compilation, that's OK)**

Run: `./gradlew projects --console=plain`
Expected output: lists `core`, `plugin`, `cli` under `JsonBeanGenerator`.

- [ ] **Step 5: Commit**

```bash
git add settings.gradle core/ cli/ plugin/build.gradle
git commit -m "build: declare empty core / plugin / cli Gradle modules"
```

### Task 2: Migrate root src/ → plugin/src/ via git mv (no logic change)

**Files:**
- Move: entire `src/` → `plugin/src/`
- Modify: root `build.gradle` → move its content (minus shared bits) into `plugin/build.gradle`

- [ ] **Step 1: Move the source tree with git history preservation**

Run:
```bash
git mv src plugin/src
```

- [ ] **Step 2: Move current root build.gradle content into plugin/build.gradle**

Read current root `build.gradle`. Copy everything into `plugin/build.gradle`. Then trim the root `build.gradle` to:

```groovy
// Root project — only shared metadata. Module configs live in core/, plugin/, cli/.
group 'org.example'
version '1.10.0-SNAPSHOT'
```

`plugin/build.gradle` should now contain the full old content (plugins block, repositories, dependencies, patchPluginXml, runGenerators task). No changes to substance.

- [ ] **Step 3: Verify plugin builds with the migrated source tree**

Run: `./gradlew :plugin:buildPlugin --no-daemon`
Expected: BUILD SUCCESSFUL, produces `plugin/build/distributions/JsonBeanGenerator-1.10.0-SNAPSHOT.zip`.

If it fails with intellij-platform repository errors, check that the `intellijPlatform { defaultRepositories() }` block landed in `plugin/build.gradle`.

- [ ] **Step 4: Verify runGenerators task still runs**

Run: `./gradlew :plugin:runGenerators 2>&1 | tail -20`
Expected: All 7 sections (Dart non-split, Dart split-g, TypeScript, Python, Kotlin Map/Gson/FastJson) print without errors.

- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "build: migrate root src/ into plugin/ module (git mv, no logic change)"
```

---

## Phase 2 — Core Module Extraction

### Task 3: Configure core module dependencies and Kotlin target

**Files:**
- Modify: `core/build.gradle`

- [ ] **Step 1: Write the full core build.gradle**

Replace `core/build.gradle` with:

```groovy
plugins {
    id 'org.jetbrains.kotlin.jvm'
}

repositories {
    mavenCentral()
}

dependencies {
    api "com.alibaba.fastjson2:fastjson2:2.0.53"
    api "com.google.guava:guava:33.3.1-jre"   // for CaseFormat (used by Naming.kt)
    implementation "org.jetbrains.kotlin:kotlin-stdlib"

    testImplementation "org.junit.jupiter:junit-jupiter:5.10.2"
    testImplementation "io.kotest:kotest-assertions-core:5.9.0"
    testRuntimeOnly "org.junit.platform:junit-platform-launcher"
}

test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
```

- [ ] **Step 2: Verify core compiles (empty source set)**

Run: `./gradlew :core:compileKotlin --no-daemon`
Expected: BUILD SUCCESSFUL (nothing to compile yet).

- [ ] **Step 3: Commit**

```bash
git add core/build.gradle
git commit -m "build(core): configure deps and JUnit5/Kotest test runner"
```

### Task 4: Add core model types (GeneratedFile, GenerateRequest, JsonGenerator interface)

**Files:**
- Create: `core/src/main/kotlin/com/awesome/core/model/GeneratedFile.kt`
- Create: `core/src/main/kotlin/com/awesome/core/model/GenerateRequest.kt`
- Create: `core/src/main/kotlin/com/awesome/core/generators/JsonGenerator.kt`
- Create: `core/src/main/kotlin/com/awesome/core/PluginProps.kt`

- [ ] **Step 1: Write GeneratedFile**

`core/src/main/kotlin/com/awesome/core/model/GeneratedFile.kt`:

```kotlin
package com.awesome.core.model

/** A single file the caller should write. `name` is a file name only (no path). */
data class GeneratedFile(val name: String, val content: String)
```

- [ ] **Step 2: Write GenerateRequest**

`core/src/main/kotlin/com/awesome/core/model/GenerateRequest.kt`:

```kotlin
package com.awesome.core.model

/**
 * Uniform request to any [com.awesome.core.generators.JsonGenerator].
 *
 * Language-specific knobs live in [options]; key names are centralised in
 * [com.awesome.core.PluginProps].
 */
data class GenerateRequest(
    val json: String,
    val className: String,
    val extendsClass: String = "",
    val implementsClass: String = "",
    val options: Map<String, Any?> = emptyMap(),
) {
    fun boolOption(key: String): Boolean = options[key] as? Boolean ?: false
    fun stringOption(key: String, default: String = ""): String = options[key] as? String ?: default
}
```

- [ ] **Step 3: Write JsonGenerator interface**

`core/src/main/kotlin/com/awesome/core/generators/JsonGenerator.kt`:

```kotlin
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
```

- [ ] **Step 4: Write PluginProps**

`core/src/main/kotlin/com/awesome/core/PluginProps.kt`:

```kotlin
package com.awesome.core

/**
 * Centralised property keys shared between the IntelliJ plugin (which reads
 * them from `plugins.properties`) and the CLI (which gets them via flags).
 *
 * Keep keys in sync between this object and the docs in the design spec.
 */
object PluginProps {
    const val properties = "plugins.properties"
    const val assetsIgnoreDirs = "plugin.assetsIgnoreDirs"
    const val generateAssetDirs = "plugin.generateAssetDirs"
    const val languageAssetsDir = "plugin.languageAssetsDir"
    const val modelType = "plugin.modelType"
    const val clone = "plugin.clone"
    const val depType = "plugin.depType"
    const val languageDir = "plugin.languageDir"
    const val languages = "plugin.languages"
    const val needTranslate = "plugin.needTranslate"
    const val rawLanguage = "plugin.rawLanguage"
    const val flutterChain = "plugin.flutterChain"
    const val translationKey = "plugin.translationKey"
    const val splitGFile = "plugin.splitGFile"

    // Option keys used in GenerateRequest.options (CLI & plugin both honour these)
    const val OPT_SPLIT_G = "splitGFile"
    const val OPT_SQLITE = "sqliteEnable"
    const val OPT_PRIMARY_KEY = "primaryKey"
    const val OPT_NEED_CLONE = "needClone"
    const val OPT_KT_DEP = "kotlinDep"          // "gson" | "fastjson" | "none"
}
```

- [ ] **Step 5: Verify core compiles**

Run: `./gradlew :core:compileKotlin --no-daemon`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 6: Commit**

```bash
git add core/src/main/kotlin
git commit -m "feat(core): add GenerateRequest / GeneratedFile / JsonGenerator interface + PluginProps"
```

### Task 5: Move JsonHelper extensions into core/util (Naming + JsonParse + KeywordTable)

**Files:**
- Create: `core/src/main/kotlin/com/awesome/core/util/KeywordTable.kt`
- Create: `core/src/main/kotlin/com/awesome/core/util/Naming.kt`
- Create: `core/src/main/kotlin/com/awesome/core/util/JsonParse.kt`
- Modify: `plugin/src/main/kotlin/com/awesome/utils/JsonHelper.kt` → keep only `regex` callsite-dependent stuff if any; delete top-level functions that moved
- Modify: every plugin import of `import toCamel` / `import toUpperCamel` / etc.

- [ ] **Step 1: Carve out KeywordTable**

`core/src/main/kotlin/com/awesome/core/util/KeywordTable.kt`:

```kotlin
package com.awesome.core.util

/**
 * Identifier names that collide with Dart primitive types. Generators append
 * an "x" suffix when a field name lands here.
 *
 * This is intentionally Dart-flavoured (the original use case); other language
 * generators that need their own collision tables should add their own files.
 */
val KEYS: Array<String> = arrayOf("num", "int", "String", "double", "bool")

/** Type names that collide with Dart generic containers (List, Map). */
val UPPER_KEYS: Array<String> = arrayOf("List", "Map")

const val REGEX_SYMBOL: String = "[~'`!@#\$%^&*()_\\-+=<>?:\"{}|,./;'\\[\\]·！@#￥%……&*（）——\\-+=\\{\\}|《》？：“”【】；'’，。、]*"
```

(Copy the actual REGEX_SYMBOL string verbatim from the existing `plugin/src/main/kotlin/com/awesome/utils/JsonHelper.kt` line 12 — don't retype, use the exact Unicode characters.)

- [ ] **Step 2: Write Naming utilities**

`core/src/main/kotlin/com/awesome/core/util/Naming.kt`:

```kotlin
package com.awesome.core.util

import com.google.common.base.CaseFormat

fun String?.toCamel(): String {
    var result = this.clearSymbol()?.trim()
    if (this == result && !result!!.contains("_")) {
        val firstWord = result.substring(0, 1)
        result = firstWord.lowercase() + result.substring(1, result.length)
        return if (KEYS.contains(result)) "${result}x" else result
    }
    result = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, result)
    return if (KEYS.contains(result)) "${result}x" else result!!
}

fun String.firstUpperCamel(): String {
    val header = this.substring(0, 1).uppercase()
    val tail = this.substring(1, this.length)
    return "$header$tail"
}

fun String?.toUpperCamel(): String {
    if (this.isNullOrEmpty()) return ""
    if (this.contains("_") || this.contains(" ")) {
        val result = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, this.clearSymbol())
        return if (UPPER_KEYS.contains(result)) "${result}x" else result
    }
    val result = this.clearSymbol()?.firstUpperCamel()
    return if (UPPER_KEYS.contains(result)) "${result}x" else "$result"
}

fun String?.toLowerUnderScore(): String {
    if (this.isNullOrEmpty()) return ""
    return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, this.clearSymbol().toCamel())
}

fun String?.toUpperUnderScore(): String {
    if (this.isNullOrEmpty()) return ""
    return CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, this.clearSymbol().toCamel())
}

internal fun String?.clearSymbol(): String? {
    if (this.isNullOrEmpty()) return this
    var finalKey: String = this
    Regex(REGEX_SYMBOL).findAll(this).forEach { match ->
        val it = match.value
        if (it.trim().isEmpty()) return@forEach
        finalKey = finalKey.replace(it, "_")
    }
    return finalKey.replace("\n", "").replace("\\", "").removeStartSymbol()
}

internal fun String.removeStartSymbol(): String {
    var value = this
    if (this.startsWith("_")) {
        value = value.substring(1, value.length)
        return value.removeStartSymbol()
    }
    return this
}
```

Note: this replaces the plugin's `String?.regex { ... }` callback-style helper inside `clearSymbol` with a direct `Regex.findAll`. The semantics are identical (the original iterated matches and replaced each); this version drops the dependency on `com.awesome.utils.regex` which lives in plugin-only territory.

- [ ] **Step 3: Write JsonParse utilities**

`core/src/main/kotlin/com/awesome/core/util/JsonParse.kt`:

```kotlin
package com.awesome.core.util

import com.alibaba.fastjson2.JSON
import com.alibaba.fastjson2.JSONArray
import com.alibaba.fastjson2.JSONObject

fun String?.formatJson(): String? {
    if (this.isNullOrEmpty()) return this
    val json: Any = if (this.startsWith("{")) JSONObject.parseObject(this) else JSONArray.parseArray(this)
    return JSON.toJSONString(json)
}

fun String.toJSON(): Any? = when {
    startsWith("{") -> {
        @Suppress("UNCHECKED_CAST")
        val map = JSONObject.parseObject(this, LinkedHashMap::class.java) as LinkedHashMap<String, *>
        JSONObject(map)
    }
    startsWith("[") -> JSONArray.parseArray(this)
    else -> null
}

fun JSONArray.mergeKeys(): Any {
    val result = this[0]
    if (result is String || result is Int || result is Long || result is Double || result is Boolean || result is Float || result is JSONArray) {
        return result
    }
    val obj = JSONObject()
    for (jsonObject in this) {
        if (jsonObject is JSONObject) {
            for (key in jsonObject.keys) {
                if (!obj.containsKey(key)) {
                    obj[key] = jsonObject[key]
                }
            }
        }
    }
    return obj
}
```

Note: `mergeKeys` now treats `Long` as a primitive too (matches the same defence we added to the generators in commit 91a2f6b).

- [ ] **Step 4: Verify core compiles standalone**

Run: `./gradlew :core:compileKotlin --no-daemon`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 5: Delete the now-duplicated definitions in plugin's JsonHelper.kt**

Open `plugin/src/main/kotlin/com/awesome/utils/JsonHelper.kt`. Delete every top-level fn/val that was just copied into core (`KEYS`, `UPPER_KEYS`, `REGEX_SYMBOL`, `toCamel`, `firstUpperCamel`, `toUpperCamel`, `toLowerUnderScore`, `toUpperUnderScore`, `clearSymbol`, `removeStartSymbol`, `formatJson`, `toJSON`, `mergeKeys`). If the file ends up empty, delete it via `git rm plugin/src/main/kotlin/com/awesome/utils/JsonHelper.kt`.

- [ ] **Step 6: Wire plugin module to depend on core**

In `plugin/build.gradle`, inside the `dependencies { ... }` block (the FIRST one, not the `intellijPlatform` one), add:

```groovy
implementation project(':core')
```

- [ ] **Step 7: Update plugin imports — global find-and-replace**

In `plugin/src/main/kotlin`, replace the bare top-level imports with their new package:

Run (verify zero hits first, then run with `-i` to edit):
```bash
grep -rl "^import toCamel\|^import toUpperCamel\|^import toLowerUnderScore\|^import toUpperUnderScore\|^import firstUpperCamel\|^import mergeKeys\|^import formatJson\|^import toJSON" plugin/src
```

For each file found, replace:
- `import toCamel` → `import com.awesome.core.util.toCamel`
- `import toUpperCamel` → `import com.awesome.core.util.toUpperCamel`
- `import toLowerUnderScore` → `import com.awesome.core.util.toLowerUnderScore`
- `import toUpperUnderScore` → `import com.awesome.core.util.toUpperUnderScore`
- `import firstUpperCamel` → `import com.awesome.core.util.firstUpperCamel`
- `import mergeKeys` → `import com.awesome.core.util.mergeKeys`
- `import formatJson` → `import com.awesome.core.util.formatJson`
- `import toJSON` → `import com.awesome.core.util.toJSON`

One-shot with `sed` (macOS BSD sed; adjust `-i ''` for Linux):
```bash
find plugin/src -name "*.kt" -print0 | xargs -0 sed -i '' \
  -e 's|^import toCamel$|import com.awesome.core.util.toCamel|' \
  -e 's|^import toUpperCamel$|import com.awesome.core.util.toUpperCamel|' \
  -e 's|^import toLowerUnderScore$|import com.awesome.core.util.toLowerUnderScore|' \
  -e 's|^import toUpperUnderScore$|import com.awesome.core.util.toUpperUnderScore|' \
  -e 's|^import firstUpperCamel$|import com.awesome.core.util.firstUpperCamel|' \
  -e 's|^import mergeKeys$|import com.awesome.core.util.mergeKeys|' \
  -e 's|^import formatJson$|import com.awesome.core.util.formatJson|' \
  -e 's|^import toJSON$|import com.awesome.core.util.toJSON|'
```

- [ ] **Step 8: Build plugin**

Run: `./gradlew :plugin:buildPlugin --no-daemon`
Expected: BUILD SUCCESSFUL.

If you see `Unresolved reference: regex` (because the plugin's old `clearSymbol` used `String.regex { ... }`), confirm Step 5 removed `clearSymbol` from `JsonHelper.kt` — plugin should now import the core version transparently via its callers.

- [ ] **Step 9: Commit**

```bash
git add core/src plugin/build.gradle plugin/src
git commit -m "refactor: extract Naming / JsonParse / KeywordTable into :core, plugin depends on it"
```

### Task 6: Move all 6 existing generators into core

**Files:**
- Move: `plugin/src/main/kotlin/com/awesome/plugins/json2bean/generators/` → `core/src/main/kotlin/com/awesome/core/generators/`
- Move: `plugin/src/main/kotlin/com/awesome/plugins/json2bean/generators/ktgenerators/` → `core/src/main/kotlin/com/awesome/core/generators/kt/`
- Modify: every generator file's `package` declaration and imports

- [ ] **Step 1: Move the files with git history preservation**

Run:
```bash
mkdir -p core/src/main/kotlin/com/awesome/core/generators/kt
git mv plugin/src/main/kotlin/com/awesome/plugins/json2bean/generators/BaseGenerator.kt \
       core/src/main/kotlin/com/awesome/core/generators/BaseGenerator.kt
git mv plugin/src/main/kotlin/com/awesome/plugins/json2bean/generators/DartJsonGenerator.kt \
       core/src/main/kotlin/com/awesome/core/generators/DartJsonGenerator.kt
git mv plugin/src/main/kotlin/com/awesome/plugins/json2bean/generators/TsJsonGenerator.kt \
       core/src/main/kotlin/com/awesome/core/generators/TsJsonGenerator.kt
git mv plugin/src/main/kotlin/com/awesome/plugins/json2bean/generators/PythonJsonGenerator.kt \
       core/src/main/kotlin/com/awesome/core/generators/PythonJsonGenerator.kt
git mv plugin/src/main/kotlin/com/awesome/plugins/json2bean/generators/ktgenerators/MapKtJsonGenerator.kt \
       core/src/main/kotlin/com/awesome/core/generators/kt/MapKtJsonGenerator.kt
git mv plugin/src/main/kotlin/com/awesome/plugins/json2bean/generators/ktgenerators/KtGsonGenerator.kt \
       core/src/main/kotlin/com/awesome/core/generators/kt/KtGsonGenerator.kt
git mv plugin/src/main/kotlin/com/awesome/plugins/json2bean/generators/ktgenerators/KtFastJsonGenerator.kt \
       core/src/main/kotlin/com/awesome/core/generators/kt/KtFastJsonGenerator.kt
```

- [ ] **Step 2: Update package declarations**

For each of the 4 files in `core/src/main/kotlin/com/awesome/core/generators/`:
- Change `package com.awesome.plugins.json2bean.generators` → `package com.awesome.core.generators`

For each of the 3 files in `core/src/main/kotlin/com/awesome/core/generators/kt/`:
- Change `package com.awesome.plugins.json2bean.generators.ktgenerators` → `package com.awesome.core.generators.kt`

One-shot:
```bash
sed -i '' 's|^package com.awesome.plugins.json2bean.generators$|package com.awesome.core.generators|' \
    core/src/main/kotlin/com/awesome/core/generators/*.kt
sed -i '' 's|^package com.awesome.plugins.json2bean.generators.ktgenerators$|package com.awesome.core.generators.kt|' \
    core/src/main/kotlin/com/awesome/core/generators/kt/*.kt
```

- [ ] **Step 3: Update imports inside core (remove the bare top-level imports, add core.util imports)**

In each of the 7 generator files, replace any `import toCamel` / `import toUpperCamel` / `import mergeKeys` etc. with `import com.awesome.core.util.toCamel` etc. Same sed as Task 5 Step 7, applied to `core/src/main/kotlin`:

```bash
find core/src/main/kotlin -name "*.kt" -print0 | xargs -0 sed -i '' \
  -e 's|^import toCamel$|import com.awesome.core.util.toCamel|' \
  -e 's|^import toUpperCamel$|import com.awesome.core.util.toUpperCamel|' \
  -e 's|^import toLowerUnderScore$|import com.awesome.core.util.toLowerUnderScore|' \
  -e 's|^import firstUpperCamel$|import com.awesome.core.util.firstUpperCamel|' \
  -e 's|^import mergeKeys$|import com.awesome.core.util.mergeKeys|' \
  -e 's|^import toJSON$|import com.awesome.core.util.toJSON|'
```

- [ ] **Step 4: Remove the IntelliJ PsiDirectory dependency from the 3 Kt generators**

The Kt generators currently import `com.intellij.psi.PsiDirectory` for their `generate()` writeFile path. Since `core` has zero IntelliJ deps, this must go.

In `KtGsonGenerator.kt`, `KtFastJsonGenerator.kt`, `MapKtJsonGenerator.kt`:
- Delete the line `import com.intellij.psi.PsiDirectory`
- In the class constructor, delete the `private val psiDir: PsiDirectory?` parameter entirely
- Delete the `generate()` and `write2File()` methods (file-writing logic moves to the plugin/CLI callers in Task 7 via the `JsonGenerator` interface)

After this, each Kt generator only exposes `toString()` and (internal) `parseJson`. The `JsonGenerator.generate(req)` adapter added in Task 7 will translate `toString()` output into a single-element `List<GeneratedFile>`.

- [ ] **Step 5: Build core in isolation**

Run: `./gradlew :core:compileKotlin --no-daemon`
Expected: BUILD SUCCESSFUL.

If you see `Unresolved reference: PsiDirectory` in some file you missed, repeat Step 4 for it.

- [ ] **Step 6: Update plugin imports to point at the new packages**

```bash
find plugin/src -name "*.kt" -print0 | xargs -0 sed -i '' \
  -e 's|com\.awesome\.plugins\.json2bean\.generators\.ktgenerators|com.awesome.core.generators.kt|g' \
  -e 's|com\.awesome\.plugins\.json2bean\.generators\.DartJsonGenerator|com.awesome.core.generators.DartJsonGenerator|g' \
  -e 's|com\.awesome\.plugins\.json2bean\.generators\.TsJsonGenerator|com.awesome.core.generators.TsJsonGenerator|g' \
  -e 's|com\.awesome\.plugins\.json2bean\.generators\.PythonJsonGenerator|com.awesome.core.generators.PythonJsonGenerator|g' \
  -e 's|com\.awesome\.plugins\.json2bean\.generators\.BaseGenerator|com.awesome.core.generators.BaseGenerator|g' \
  -e 's|com\.awesome\.plugins\.json2bean\.generators\.ktgenerators\.|com.awesome.core.generators.kt.|g'
```

- [ ] **Step 7: Migrate PluginProps in plugin code to the core copy**

```bash
find plugin/src -name "*.kt" -print0 | xargs -0 sed -i '' \
  -e 's|import com\.awesome\.common\.PluginProps|import com.awesome.core.PluginProps|g'
git rm plugin/src/main/kotlin/com/awesome/common/PluginProps.kt
```

- [ ] **Step 8: Build the plugin (will fail because Kt generator call sites still pass psiDirectory)**

Run: `./gradlew :plugin:compileKotlin --no-daemon 2>&1 | head -30`
Expected: errors at `GeneratorHelper.json2KtOrJava` where it constructs `MapKtJsonGenerator(content, fileName, ext, impl, psiDirectory)` — too many arguments now.

You'll fix this in Task 7. For now, commit the move.

- [ ] **Step 9: Commit (knowing plugin is broken; will fix in Task 7)**

```bash
git add -A
git commit -m "refactor(core): move 6 generators from plugin into :core, drop PsiDirectory from Kt generators"
```

### Task 7: Add JsonGenerator interface adapters + rewire plugin GeneratorHelper

**Files:**
- Create: `core/src/main/kotlin/com/awesome/core/generators/Generators.kt` (factory + adapters)
- Modify: `plugin/src/main/kotlin/com/awesome/plugins/json2bean/utils/GeneratorHelper.kt`
- Modify: `plugin/src/main/kotlin/com/awesome/JsonBeanDialog.kt`
- Modify: `plugin/src/main/kotlin/com/awesome/plugins/json2bean/PreViewDialog.kt`

- [ ] **Step 1: Write the JsonGenerator implementations**

`core/src/main/kotlin/com/awesome/core/generators/Generators.kt`:

```kotlin
package com.awesome.core.generators

import com.awesome.core.PluginProps
import com.awesome.core.generators.kt.KtFastJsonGenerator
import com.awesome.core.generators.kt.KtGsonGenerator
import com.awesome.core.generators.kt.MapKtJsonGenerator
import com.awesome.core.model.GenerateRequest
import com.awesome.core.model.GeneratedFile
import com.awesome.core.util.toUpperCamel

/**
 * Single entry point. Pick a generator by language tag and call [generate].
 *
 * Language tags: "dart", "ts", "py", "java", "kt".
 */
object Generators {
    fun generate(language: String, req: GenerateRequest): List<GeneratedFile> {
        return when (language) {
            "dart" -> DartAdapter.generate(req)
            "ts" -> TsAdapter.generate(req)
            "py" -> PythonAdapter.generate(req)
            "java" -> JavaJsonGenerator().generate(req)
            "kt" -> KtAdapter.generate(req)
            else -> error("Unknown language: $language")
        }
    }
}

private object DartAdapter : JsonGenerator {
    override fun generate(req: GenerateRequest): List<GeneratedFile> {
        val gen = DartJsonGenerator(
            content = req.json,
            fileName = req.className,
            extendsClass = req.extendsClass,
            implementClass = req.implementsClass,
            sqliteSupport = req.boolOption(PluginProps.OPT_SQLITE),
            primaryKey = req.stringOption(PluginProps.OPT_PRIMARY_KEY),
            needClone = req.boolOption(PluginProps.OPT_NEED_CLONE),
            splitGFile = req.boolOption(PluginProps.OPT_SPLIT_G),
        )
        val out = gen.generate()
        val files = mutableListOf(GeneratedFile(out.mainFileName, out.mainContent))
        if (out.partContent != null && out.partFileName != null) {
            files += GeneratedFile(out.partFileName, out.partContent)
        }
        return files
    }
}

private object TsAdapter : JsonGenerator {
    override fun generate(req: GenerateRequest): List<GeneratedFile> {
        val content = TsJsonGenerator(req.json, req.className, req.extendsClass, req.implementsClass).toString()
        return listOf(GeneratedFile("${req.className.toUpperCamel()}.ts", content))
    }
}

private object PythonAdapter : JsonGenerator {
    override fun generate(req: GenerateRequest): List<GeneratedFile> {
        val content = PythonJsonGenerator(req.json, req.className, req.extendsClass, req.implementsClass).toString()
        return listOf(GeneratedFile("${req.className}.py", content))
    }
}

private object KtAdapter : JsonGenerator {
    override fun generate(req: GenerateRequest): List<GeneratedFile> {
        val dep = req.stringOption(PluginProps.OPT_KT_DEP, "gson")
        val name = req.className
        val content = when (dep) {
            "none" -> MapKtJsonGenerator(req.json, name, req.extendsClass, req.implementsClass).toString()
            "fastjson" -> KtFastJsonGenerator(req.json, name, req.extendsClass, req.implementsClass).toString()
            else -> KtGsonGenerator(req.json, name, req.extendsClass, req.implementsClass).toString()
        }
        return listOf(GeneratedFile("${name.toUpperCamel()}.kt", content))
    }
}
```

Note: each Kt generator's 5-arg constructor `(content, name, ext, impl, psiDir)` has been collapsed to 4 args in Task 6 Step 4. If you skipped that, go back and remove the `psiDir` parameter from each.

- [ ] **Step 2: Stub JavaJsonGenerator (real implementation arrives in Phase 3)**

`core/src/main/kotlin/com/awesome/core/generators/JavaJsonGenerator.kt`:

```kotlin
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
```

- [ ] **Step 3: Verify core compiles**

Run: `./gradlew :core:compileKotlin --no-daemon`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Rewrite plugin's GeneratorHelper to delegate to Generators**

Replace `plugin/src/main/kotlin/com/awesome/plugins/json2bean/utils/GeneratorHelper.kt` content with:

```kotlin
package com.awesome.plugins.json2bean.utils

import com.awesome.core.PluginProps
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
```

- [ ] **Step 5: Rewrite JsonBeanDialog's two write paths**

In `plugin/src/main/kotlin/com/awesome/JsonBeanDialog.kt`:

Replace the body of `onGenerate()` (the dart path) with:

```kotlin
private fun onGenerate() {
    tvError?.text = ""
    if (isEmpty(tvClassField?.text)) {
        tvClassField!!.text = "auto_root"
    }
    val mainFile = java.io.File(mDirectory.virtualFile.path, tvClassField?.text + fileType)
    if (mainFile.exists()) {
        dispose()
        return
    }
    try {
        val req = buildRequest()
        com.awesome.plugins.json2bean.utils.GeneratorHelper.generateAndWrite(fileType, req, mDirectory)
        // sqlite DAO write path is unchanged (plugin-only, not in :core)
        if (cbSqlite!!.isSelected && !org.apache.http.util.TextUtils.isEmpty(tvPrimaryKeyListener.getText())) {
            com.awesome.plugins.json2bean.database.DartDataBaseGenerator(
                tvField!!.text,
                tvClassField!!.text,
                mDirectory,
                tvPrimaryKeyListener.getText(),
            ).startWrite()
        }
        dispose()
    } catch (e: Exception) {
        tvError?.text = "JSON Error!!"
        println(e)
    }
}
```

Replace the body of `onGenerateJavaOrKt()`:

```kotlin
private fun onGenerateJavaOrKt() {
    tvError?.text = ""
    if (isEmpty(tvClassField?.text)) {
        tvClassField!!.text = "auto_root"
    }
    val file = java.io.File(mDirectory.virtualFile.path, tvClassField?.text.toUpperCamel() + fileType)
    if (file.exists()) { dispose(); return }
    try {
        val req = buildRequest()
        com.awesome.plugins.json2bean.utils.GeneratorHelper.generateAndWrite(fileType, req, mDirectory)
        dispose()
    } catch (e: Exception) {
        tvError?.text = "JSON Error!!"
        println(e)
    }
}
```

Add a new private helper next to them:

```kotlin
private fun buildRequest(): com.awesome.core.model.GenerateRequest {
    val options = mutableMapOf<String, Any?>()
    options[com.awesome.core.PluginProps.OPT_SPLIT_G] = cbSplitGFile?.isSelected == true
    options[com.awesome.core.PluginProps.OPT_SQLITE] = isSqliteEnable()
    options[com.awesome.core.PluginProps.OPT_PRIMARY_KEY] = tvPrimaryKeyListener.getText()
    options[com.awesome.core.PluginProps.OPT_NEED_CLONE] = rbClone!!.isSelected
    options[com.awesome.core.PluginProps.OPT_KT_DEP] = depType
    return com.awesome.core.model.GenerateRequest(
        json = tvField!!.text,
        className = tvClassField!!.text,
        extendsClass = tvExtends!!.text,
        implementsClass = tvImplements!!.text,
        options = options,
    )
}
```

Remove the now-dead `json2Bean()` private fn — replace its single caller (in `onPreView`):

```kotlin
private fun onPreView() {
    tvError?.text = ""
    if (isEmpty(tvClassField!!.text)) {
        tvClassField!!.text = "auto_root"
    }
    try {
        val preview = com.awesome.plugins.json2bean.utils.GeneratorHelper.previewMain(fileType, buildRequest())
        com.awesome.plugins.json2bean.PreViewDialog(preview).showDialog()
    } catch (e: Exception) {
        tvError?.text = "JSON Error!!"
        println(e)
    }
}
```

- [ ] **Step 6: Build plugin**

Run: `./gradlew :plugin:buildPlugin --no-daemon 2>&1 | tail -30`
Expected: BUILD SUCCESSFUL.

If anything fails to resolve, use the IDE or `grep` to find stale references to `com.awesome.plugins.json2bean.generators.*` and update them to `com.awesome.core.generators.*`.

- [ ] **Step 7: Run the legacy runGenerators task to confirm behaviour unchanged**

Note: the task currently lives in `plugin/build.gradle` and points at `RunGeneratorsKt` in `plugin/src/test/kotlin`. Update the file to import from `com.awesome.core.generators.*` instead of `com.awesome.plugins.json2bean.generators.*`. Also remove the now-unused `null` arg to the Kt generators (they only take 4 args now).

Run: `./gradlew :plugin:runGenerators 2>&1 | tail -30`
Expected: all 7 sections print as before.

- [ ] **Step 8: Commit**

```bash
git add -A
git commit -m "refactor: rewire plugin dialogs to call core Generators via :core API"
```

### Task 8: Verify core is IntelliJ-free

**Files:** verification only.

- [ ] **Step 1: Scan core for forbidden imports**

Run:
```bash
grep -rE "import com\.intellij|import javax\.swing|import org\.apache\.http" core/src/main || echo "CORE CLEAN"
```
Expected: `CORE CLEAN`. If any matches, refactor them out before continuing.

- [ ] **Step 2: Verify core test source set compiles (still empty)**

Run: `./gradlew :core:compileTestKotlin --no-daemon`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: No commit needed (verification step only)**

---

## Phase 3 — Golden Tests + JavaJsonGenerator (TDD)

### Task 9: Set up golden test infrastructure + fixture for complex_nested

**Files:**
- Create: `core/src/test/resources/fixtures/complex_nested/input.json`
- Create: `core/src/test/kotlin/com/awesome/core/generators/GoldenTestSupport.kt`

- [ ] **Step 1: Create the input fixture**

`core/src/test/resources/fixtures/complex_nested/input.json`:

```json
{
  "user_id": 1001,
  "user_name": "Barry",
  "is_vip": true,
  "balance": 99.5,
  "tags": ["dart", "kotlin", "ts"],
  "scores": [[90, 85], [88, 92]],
  "empty_list": [],
  "address": {
    "city": "Shanghai",
    "zip_code": "200000",
    "geo": {
      "lat": 31.23,
      "lng": 121.47
    }
  },
  "orders": [
    {
      "order_id": "A001",
      "total": 199.99,
      "items": [
        {"sku": "X1", "qty": 2}
      ]
    }
  ]
}
```

- [ ] **Step 2: Write a tiny test-support helper**

`core/src/test/kotlin/com/awesome/core/generators/GoldenTestSupport.kt`:

```kotlin
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
```

- [ ] **Step 3: Verify it compiles**

Run: `./gradlew :core:compileTestKotlin --no-daemon`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add core/src/test
git commit -m "test(core): add complex_nested fixture + golden test support helper"
```

### Task 10: Generate expected files for Dart/TS/Python/Kt by capturing current output

Strategy: we already trust the current generators (Phase 0 fixed all known bugs and the runner produced clean output). Capture that output into `expected.*` files; these become regression contracts.

**Files:**
- Create: `core/src/test/resources/fixtures/complex_nested/expected.dart`
- Create: `core/src/test/resources/fixtures/complex_nested/expected.dart.main`
- Create: `core/src/test/resources/fixtures/complex_nested/expected.dart.g`
- Create: `core/src/test/resources/fixtures/complex_nested/expected.ts`
- Create: `core/src/test/resources/fixtures/complex_nested/expected.py`
- Create: `core/src/test/resources/fixtures/complex_nested/expected.kt.map`
- Create: `core/src/test/resources/fixtures/complex_nested/expected.kt.gson`
- Create: `core/src/test/resources/fixtures/complex_nested/expected.kt.fastjson`

- [ ] **Step 1: Write a fresh capture script under core test sources**

`core/src/test/kotlin/com/awesome/core/generators/CaptureFixtures.kt`:

```kotlin
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
    val dir = Paths.get("core/src/test/resources/fixtures/complex_nested")
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
```

- [ ] **Step 2: Register a Gradle task to run it**

Append to `core/build.gradle`:

```groovy
tasks.register('captureFixtures', JavaExec) {
    group = 'verification'
    description = 'Recapture expected.* golden files from current generator output.'
    classpath = sourceSets.test.runtimeClasspath
    mainClass = 'com.awesome.core.generators.CaptureFixturesKt'
}
```

- [ ] **Step 3: Run it**

Run: `./gradlew :core:captureFixtures --no-daemon`
Expected: prints `Captured /absolute/path/...`. 8 new files appear under `core/src/test/resources/fixtures/complex_nested/`.

- [ ] **Step 4: Sanity-check the captured files against the spec**

Open `expected.dart`, `expected.ts`, `expected.kt.gson`, `expected.py`. Spot-check that the structure matches the examples in `docs/superpowers/specs/2026-05-21-json2bean-cli-design.md` §9.2 / §9.4 / §9.5 / §9.7.

If any file looks wrong, it means a generator regressed since Phase 0. Stop and investigate before committing.

- [ ] **Step 5: Commit fixtures**

```bash
git add core/src/test/resources core/src/test/kotlin/com/awesome/core/generators/CaptureFixtures.kt core/build.gradle
git commit -m "test(core): capture golden fixtures for Dart/TS/Python/Kt against complex_nested"
```

### Task 11: Write golden tests that assert generator output matches fixtures

**Files:**
- Create: `core/src/test/kotlin/com/awesome/core/generators/DartGoldenTest.kt`
- Create: `core/src/test/kotlin/com/awesome/core/generators/TsGoldenTest.kt`
- Create: `core/src/test/kotlin/com/awesome/core/generators/PythonGoldenTest.kt`
- Create: `core/src/test/kotlin/com/awesome/core/generators/kt/KtGoldenTest.kt`

- [ ] **Step 1: Write DartGoldenTest**

`core/src/test/kotlin/com/awesome/core/generators/DartGoldenTest.kt`:

```kotlin
package com.awesome.core.generators

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class DartGoldenTest {
    private val json = fixtureInput("complex_nested")

    @Test
    fun `non-split output matches expected dart`() {
        val actual = DartJsonGenerator(json, "User", "", "", false, "", false, false).toString()
        actual shouldBe fixture("complex_nested", "expected.dart")
    }

    @Test
    fun `split-g main file matches expected`() {
        val out = DartJsonGenerator(json, "User", "", "", false, "", true, true).generate()
        out.mainContent shouldBe fixture("complex_nested", "expected.dart.main")
    }

    @Test
    fun `split-g part file matches expected`() {
        val out = DartJsonGenerator(json, "User", "", "", false, "", true, true).generate()
        (out.partContent ?: "") shouldBe fixture("complex_nested", "expected.dart.g")
    }
}
```

- [ ] **Step 2: Write TsGoldenTest**

`core/src/test/kotlin/com/awesome/core/generators/TsGoldenTest.kt`:

```kotlin
package com.awesome.core.generators

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class TsGoldenTest {
    @Test
    fun `output matches expected ts`() {
        val json = fixtureInput("complex_nested")
        val actual = TsJsonGenerator(json, "User", "", "").toString()
        actual shouldBe fixture("complex_nested", "expected.ts")
    }
}
```

- [ ] **Step 3: Write PythonGoldenTest**

`core/src/test/kotlin/com/awesome/core/generators/PythonGoldenTest.kt`:

```kotlin
package com.awesome.core.generators

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class PythonGoldenTest {
    @Test
    fun `output matches expected py`() {
        val json = fixtureInput("complex_nested")
        val actual = PythonJsonGenerator(json, "User", "", "").toString()
        actual shouldBe fixture("complex_nested", "expected.py")
    }
}
```

- [ ] **Step 4: Write KtGoldenTest**

`core/src/test/kotlin/com/awesome/core/generators/kt/KtGoldenTest.kt`:

```kotlin
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
```

- [ ] **Step 5: Run all core tests**

Run: `./gradlew :core:test --no-daemon`
Expected: BUILD SUCCESSFUL. 7 tests pass (3 Dart + 1 TS + 1 Python + 3 Kt).

- [ ] **Step 6: Commit**

```bash
git add core/src/test/kotlin
git commit -m "test(core): golden tests for 5 generators against complex_nested fixture"
```

### Task 12: Implement JavaJsonGenerator (TDD)

**Files:**
- Modify: `core/src/main/kotlin/com/awesome/core/generators/JavaJsonGenerator.kt` (replace stub)
- Create: `core/src/test/kotlin/com/awesome/core/generators/JavaJsonGeneratorTest.kt`
- Create: `core/src/test/resources/fixtures/complex_nested/expected.java`

- [ ] **Step 1: Write the expected output for the complex_nested fixture**

`core/src/test/resources/fixtures/complex_nested/expected.java`:

```java
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class User {
    @SerializedName("user_id")
    public Integer userId;

    @SerializedName("user_name")
    public String userName;

    @SerializedName("is_vip")
    public Boolean isVip;

    @SerializedName("balance")
    public Double balance;

    @SerializedName("tags")
    public List<String> tags;

    @SerializedName("scores")
    public List<List<Integer>> scores;

    @SerializedName("empty_list")
    public List<EmptyList> emptyList;

    @SerializedName("address")
    public Address address;

    @SerializedName("orders")
    public List<Orders> orders;
}

class EmptyList {
}

class Geo {
    @SerializedName("lat")
    public Double lat;

    @SerializedName("lng")
    public Double lng;
}

class Address {
    @SerializedName("city")
    public String city;

    @SerializedName("zip_code")
    public String zipCode;

    @SerializedName("geo")
    public Geo geo;
}

class Items {
    @SerializedName("sku")
    public String sku;

    @SerializedName("qty")
    public Integer qty;
}

class Orders {
    @SerializedName("order_id")
    public String orderId;

    @SerializedName("total")
    public Double total;

    @SerializedName("items")
    public List<Items> items;
}
```

- [ ] **Step 2: Write the first failing test (simple flat object)**

`core/src/test/kotlin/com/awesome/core/generators/JavaJsonGeneratorTest.kt`:

```kotlin
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
}
```

- [ ] **Step 3: Run the test to confirm it fails**

Run: `./gradlew :core:test --tests "com.awesome.core.generators.JavaJsonGeneratorTest.flat primitives generate Gson POJO with @SerializedName" --no-daemon`
Expected: FAIL — the stub returns `"// JavaJsonGenerator not yet implemented\n"`.

- [ ] **Step 4: Implement JavaJsonGenerator (initial version covers flat case)**

Replace `core/src/main/kotlin/com/awesome/core/generators/JavaJsonGenerator.kt`:

```kotlin
package com.awesome.core.generators

import com.alibaba.fastjson2.JSONArray
import com.alibaba.fastjson2.JSONObject
import com.awesome.core.model.GenerateRequest
import com.awesome.core.model.GeneratedFile
import com.awesome.core.util.mergeKeys
import com.awesome.core.util.toCamel
import com.awesome.core.util.toJSON
import com.awesome.core.util.toUpperCamel
import java.math.BigDecimal

/**
 * Java Gson-style POJO generator.
 *
 * Output shape matches [com.awesome.core.generators.kt.KtGsonGenerator] one-for-one:
 * - public fields with @SerializedName annotations
 * - wrapper numeric types (Integer/Long/Double) so missing keys deserialise to null
 * - nested classes are emitted flat in the same file (no static inner classes)
 * - empty objects degrade to a bare class with no body
 */
class JavaJsonGenerator : JsonGenerator {

    override fun generate(req: GenerateRequest): List<GeneratedFile> {
        val parsed = req.json.toJSON()
        val classNames = mutableListOf<String>()
        val classBodies = LinkedHashMap<String, StringBuilder>()
        val mainName = req.className.toUpperCamel()
        emitClass(parsed, mainName, classBodies, classNames, isRoot = true)

        val header = buildString {
            append("import com.google.gson.annotations.SerializedName;\n")
            append("\n")
            append("import java.util.List;\n")
            append("\n")
        }

        val combined = StringBuilder(header)
        // root first, then nested in insertion order
        combined.append(classBodies[mainName])
        for ((name, body) in classBodies) {
            if (name == mainName) continue
            combined.append("\n")
            combined.append(body)
        }
        return listOf(GeneratedFile("$mainName.java", combined.toString()))
    }

    private fun emitClass(
        obj: Any?,
        className: String,
        bodies: LinkedHashMap<String, StringBuilder>,
        seen: MutableList<String>,
        isRoot: Boolean,
    ) {
        val uniqueName = uniqueClassName(className, seen)
        val builder = StringBuilder()
        val accessModifier = if (isRoot) "public " else ""

        val parseObj: JSONObject = when (obj) {
            is JSONObject -> obj
            is JSONArray -> obj.mergeKeys() as JSONObject
            else -> JSONObject()
        }

        if (parseObj.isEmpty()) {
            builder.append("${accessModifier}class $uniqueName {\n}\n")
            bodies[uniqueName] = builder
            return
        }

        builder.append("${accessModifier}class $uniqueName {\n")
        val fieldEntries = parseObj.entries.toList()
        for ((i, entry) in fieldEntries.withIndex()) {
            val key = entry.key
            val value = entry.value
            val camel = key.toCamel()
            val upper = key.toUpperCamel()
            val type = javaTypeOf(value, upper, bodies, seen)
            if (i > 0) builder.append("\n")
            builder.append("    @SerializedName(\"$key\")\n")
            builder.append("    public $type $camel;\n")
        }
        builder.append("}\n")
        bodies[uniqueName] = builder
    }

    /**
     * Resolves the Java type string for a value, recursing into nested objects.
     */
    private fun javaTypeOf(
        value: Any?,
        upper: String,
        bodies: LinkedHashMap<String, StringBuilder>,
        seen: MutableList<String>,
    ): String = when (value) {
        is String -> "String"
        is Boolean -> "Boolean"
        is Int -> "Integer"
        is Long -> "Long"
        is Double, is Float, is BigDecimal -> "Double"
        is JSONObject -> {
            emitClass(value, upper, bodies, seen, isRoot = false)
            upper
        }
        is JSONArray -> arrayType(value, upper, bodies, seen)
        else -> "String"
    }

    private fun arrayType(
        arr: JSONArray,
        upper: String,
        bodies: LinkedHashMap<String, StringBuilder>,
        seen: MutableList<String>,
    ): String {
        if (arr.isEmpty()) {
            // Unknown element type, mirror Kt behaviour: empty class as placeholder
            emitClass(JSONObject(), upper, bodies, seen, isRoot = false)
            return "List<$upper>"
        }
        val first = arr.mergeKeys()
        return when (first) {
            is String -> "List<String>"
            is Boolean -> "List<Boolean>"
            is Int -> "List<Integer>"
            is Long -> "List<Long>"
            is Double, is Float, is BigDecimal -> "List<Double>"
            is JSONArray -> {
                val inner = first.mergeKeys()
                val innerType = when (inner) {
                    is String -> "String"
                    is Boolean -> "Boolean"
                    is Int -> "Integer"
                    is Long -> "Long"
                    is Double, is Float, is BigDecimal -> "Double"
                    is JSONObject -> { emitClass(inner, upper, bodies, seen, isRoot = false); upper }
                    else -> "String"
                }
                "List<List<$innerType>>"
            }
            is JSONObject -> {
                emitClass(first, upper, bodies, seen, isRoot = false)
                "List<$upper>"
            }
            else -> "List<String>"
        }
    }

    private fun uniqueClassName(name: String, seen: MutableList<String>): String {
        if (seen.contains(name)) return uniqueClassName("${name}x", seen)
        seen.add(name)
        return name
    }
}
```

- [ ] **Step 5: Re-run the flat test to confirm pass**

Run: `./gradlew :core:test --tests "com.awesome.core.generators.JavaJsonGeneratorTest" --no-daemon`
Expected: PASS.

- [ ] **Step 6: Add the complex_nested golden assertion**

Append to `JavaJsonGeneratorTest.kt`:

```kotlin
    @Test
    fun `complex_nested matches expected java fixture`() {
        val json = fixtureInput("complex_nested")
        val out = JavaJsonGenerator().generate(GenerateRequest(json = json, className = "User"))
        out.size shouldBe 1
        out[0].name shouldBe "User.java"
        out[0].content shouldBe fixture("complex_nested", "expected.java")
    }
```

- [ ] **Step 7: Run the golden test**

Run: `./gradlew :core:test --tests "com.awesome.core.generators.JavaJsonGeneratorTest.complex_nested matches expected java fixture" --no-daemon`
Expected: PASS. If it fails on whitespace, compare byte-by-byte and adjust either generator or fixture so they line up exactly.

- [ ] **Step 8: Run the entire core test suite**

Run: `./gradlew :core:test --no-daemon`
Expected: all tests pass (3 Dart + 1 TS + 1 Python + 3 Kt + 2 Java = 10).

- [ ] **Step 9: Commit**

```bash
git add core/src
git commit -m "feat(core): JavaJsonGenerator (Gson POJO style) with golden tests"
```

---

## Phase 4 — Plugin smoke test

### Task 13: Manual smoke test of the IDE plugin

**Files:** verification only.

- [ ] **Step 1: Launch the IDE sandbox**

Run: `./gradlew :plugin:runIde --no-daemon`
Expected: IntelliJ IDEA Community sandbox launches with the plugin installed.

- [ ] **Step 2: Open any project, then trigger Cmd+N → Json2Dart-Null-Safety**

Pick any directory, paste the complex_nested fixture JSON into the dialog, click "generate" with default settings (dart, no split, no sqlite). Verify `User.dart` is written into the chosen directory and its content matches `core/src/test/resources/fixtures/complex_nested/expected.dart`.

- [ ] **Step 3: Test split-g**

Same dialog, check `Split .g.dart` and `Clone`, click generate. Verify two files appear: `User.dart` and `User.g.dart`, content matching the respective `expected.dart.main` / `expected.dart.g`.

- [ ] **Step 4: Test Kotlin Gson path**

Cmd+N → Json2Dart, switch radio to `Kt`, leave `Gson` dep selected, generate. Verify `User.kt` matches `expected.kt.gson`.

- [ ] **Step 5: Close the sandbox IDE; no commit needed (verification only)**

If anything misbehaves, file an issue and pause before continuing.

---

## Phase 5 — CLI Module

### Task 14: Configure cli module build with picocli + Shadow

**Files:**
- Modify: `cli/build.gradle`
- Modify: root `build.gradle` (add Shadow plugin classpath if needed by Gradle plugin DSL)

- [ ] **Step 1: Write cli/build.gradle**

Replace `cli/build.gradle` with:

```groovy
plugins {
    id 'org.jetbrains.kotlin.jvm'
    id 'com.github.johnrengelman.shadow' version '8.1.1'
    id 'application'
}

repositories {
    mavenCentral()
}

dependencies {
    implementation project(':core')
    implementation "info.picocli:picocli:4.7.6"

    testImplementation "org.junit.jupiter:junit-jupiter:5.10.2"
    testImplementation "io.kotest:kotest-assertions-core:5.9.0"
    testRuntimeOnly "org.junit.platform:junit-platform-launcher"
}

test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass = 'com.awesome.cli.MainKt'
}

shadowJar {
    archiveBaseName.set('json2bean')
    archiveClassifier.set('all')
    mergeServiceFiles()
}
```

- [ ] **Step 2: Verify cli builds (no sources yet)**

Run: `./gradlew :cli:compileKotlin --no-daemon`
Expected: BUILD SUCCESSFUL (nothing to compile).

- [ ] **Step 3: Commit**

```bash
git add cli/build.gradle
git commit -m "build(cli): configure picocli + Shadow fat-jar module"
```

### Task 15: Implement InputResolver (file vs --json mutex)

**Files:**
- Create: `cli/src/main/kotlin/com/awesome/cli/io/InputResolver.kt`
- Create: `cli/src/test/kotlin/com/awesome/cli/io/InputResolverTest.kt`

- [ ] **Step 1: Write the failing test**

`cli/src/test/kotlin/com/awesome/cli/io/InputResolverTest.kt`:

```kotlin
package com.awesome.cli.io

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

class InputResolverTest {

    @Test
    fun `reads json from -i file`(@TempDir tmp: Path) {
        val file = tmp.resolve("u.json")
        Files.writeString(file, """{"a":1}""")
        InputResolver.resolve(inputFile = file.toString(), inlineJson = null) shouldBe """{"a":1}"""
    }

    @Test
    fun `reads json from --json string`() {
        InputResolver.resolve(inputFile = null, inlineJson = """{"b":2}""") shouldBe """{"b":2}"""
    }

    @Test
    fun `errors when both supplied`() {
        val ex = shouldThrow<IllegalArgumentException> {
            InputResolver.resolve(inputFile = "a.json", inlineJson = """{}""")
        }
        ex.message!! shouldContain "mutually exclusive"
    }

    @Test
    fun `errors when neither supplied`() {
        val ex = shouldThrow<IllegalArgumentException> {
            InputResolver.resolve(inputFile = null, inlineJson = null)
        }
        ex.message!! shouldContain "required"
    }
}
```

- [ ] **Step 2: Run the test to confirm it fails**

Run: `./gradlew :cli:test --no-daemon`
Expected: FAIL with `Unresolved reference: InputResolver`.

- [ ] **Step 3: Implement InputResolver**

`cli/src/main/kotlin/com/awesome/cli/io/InputResolver.kt`:

```kotlin
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
```

- [ ] **Step 4: Re-run tests**

Run: `./gradlew :cli:test --no-daemon`
Expected: 4 tests pass.

- [ ] **Step 5: Commit**

```bash
git add cli/src
git commit -m "feat(cli): InputResolver supports -i FILE and --json STRING with mutex"
```

### Task 16: Implement Main + subcommand skeleton (picocli)

**Files:**
- Create: `cli/src/main/kotlin/com/awesome/cli/Main.kt`
- Create: `cli/src/main/kotlin/com/awesome/cli/commands/DartCommand.kt`
- Create: `cli/src/main/kotlin/com/awesome/cli/commands/KtCommand.kt`
- Create: `cli/src/main/kotlin/com/awesome/cli/commands/TsCommand.kt`
- Create: `cli/src/main/kotlin/com/awesome/cli/commands/JavaCommand.kt`

- [ ] **Step 1: Write the parent Main + version**

`cli/src/main/kotlin/com/awesome/cli/Main.kt`:

```kotlin
package com.awesome.cli

import com.awesome.cli.commands.DartCommand
import com.awesome.cli.commands.JavaCommand
import com.awesome.cli.commands.KtCommand
import com.awesome.cli.commands.TsCommand
import picocli.CommandLine
import picocli.CommandLine.Command
import kotlin.system.exitProcess

@Command(
    name = "json2bean",
    mixinStandardHelpOptions = true,
    version = ["json2bean 1.10.0"],
    description = ["Generate language bean files from JSON. Use a subcommand to pick the target language."],
    subcommands = [DartCommand::class, KtCommand::class, TsCommand::class, JavaCommand::class],
)
class MainCommand : Runnable {
    /** Shown when invoked without a subcommand. */
    override fun run() {
        CommandLine.usage(this, System.out)
    }
}

fun main(args: Array<String>) {
    val exit = CommandLine(MainCommand()).execute(*args)
    exitProcess(exit)
}
```

- [ ] **Step 2: Write the common base for subcommands**

`cli/src/main/kotlin/com/awesome/cli/commands/BeanCommand.kt`:

```kotlin
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
```

- [ ] **Step 3: Write the 4 subcommand stubs**

`cli/src/main/kotlin/com/awesome/cli/commands/DartCommand.kt`:

```kotlin
package com.awesome.cli.commands

import com.awesome.core.PluginProps
import picocli.CommandLine.Command
import picocli.CommandLine.Option

@Command(name = "dart", description = ["Generate a Dart bean from JSON."])
class DartCommand : BeanCommand() {

    @Option(names = ["--split-g"], description = ["Also emit a .g.dart part file next to -o"])
    var splitG: Boolean = false

    @Option(names = ["--sqlite"], description = ["Emit json2dart_db sqlite hooks (requires --primary-key)"])
    var sqlite: Boolean = false

    @Option(names = ["--primary-key"], description = ["sqlite primary key field name"])
    var primaryKey: String = ""

    @Option(names = ["--clone"], description = ["Emit clone() method"])
    var clone: Boolean = false

    override fun language() = "dart"

    override fun extraOptions(): Map<String, Any?> = mapOf(
        PluginProps.OPT_SPLIT_G to splitG,
        PluginProps.OPT_SQLITE to sqlite,
        PluginProps.OPT_PRIMARY_KEY to primaryKey,
        PluginProps.OPT_NEED_CLONE to clone,
    )
}
```

`cli/src/main/kotlin/com/awesome/cli/commands/KtCommand.kt`:

```kotlin
package com.awesome.cli.commands

import com.awesome.core.PluginProps
import picocli.CommandLine.Command
import picocli.CommandLine.Option

@Command(name = "kt", description = ["Generate a Kotlin bean from JSON."])
class KtCommand : BeanCommand() {

    @Option(names = ["--dep"], description = ["Serialization style: gson (default) | fastjson | none"])
    var dep: String = "gson"

    override fun language() = "kt"

    override fun extraOptions(): Map<String, Any?> = mapOf(
        PluginProps.OPT_KT_DEP to dep,
    )
}
```

`cli/src/main/kotlin/com/awesome/cli/commands/TsCommand.kt`:

```kotlin
package com.awesome.cli.commands

import picocli.CommandLine.Command

@Command(name = "ts", description = ["Generate a TypeScript interface from JSON."])
class TsCommand : BeanCommand() {
    override fun language() = "ts"
}
```

`cli/src/main/kotlin/com/awesome/cli/commands/JavaCommand.kt`:

```kotlin
package com.awesome.cli.commands

import picocli.CommandLine.Command

@Command(name = "java", description = ["Generate a Java Gson POJO from JSON."])
class JavaCommand : BeanCommand() {
    override fun language() = "java"
}
```

- [ ] **Step 4: Compile**

Run: `./gradlew :cli:compileKotlin --no-daemon`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 5: Run --help to verify the command tree shows up**

Run: `./gradlew :cli:run --args="--help" --no-daemon 2>&1 | tail -20`
Expected: Usage block listing `dart`, `kt`, `ts`, `java` as subcommands.

- [ ] **Step 6: Commit**

```bash
git add cli/src
git commit -m "feat(cli): picocli Main + dart/kt/ts/java subcommands"
```

### Task 17: CLI integration test for `dart -i ... -o ...`

**Files:**
- Create: `cli/src/test/kotlin/com/awesome/cli/CliIntegrationTest.kt`

- [ ] **Step 1: Write failing tests**

`cli/src/test/kotlin/com/awesome/cli/CliIntegrationTest.kt`:

```kotlin
package com.awesome.cli

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import picocli.CommandLine
import java.nio.file.Files
import java.nio.file.Path

class CliIntegrationTest {

    private fun exec(vararg args: String): Int =
        CommandLine(MainCommand()).execute(*args)

    private fun writeInput(tmp: Path, json: String): Path {
        val f = tmp.resolve("input.json")
        Files.writeString(f, json)
        return f
    }

    @Test
    fun `dart subcommand writes file with -i and -o`(@TempDir tmp: Path) {
        val input = writeInput(tmp, """{"name":"Foo","age":30}""")
        val output = tmp.resolve("Foo.dart")

        val code = exec("dart", "-i", input.toString(), "-o", output.toString(), "-q")
        code shouldBe 0
        Files.exists(output) shouldBe true
        val content = Files.readString(output)
        content shouldContain "class Foo {"
        content shouldContain "json.asString('name')"
        content shouldContain "json.asInt('age')"
    }

    @Test
    fun `kt subcommand defaults to gson dep`(@TempDir tmp: Path) {
        val output = tmp.resolve("Foo.kt")
        val code = exec("kt", "-j", """{"name":"Foo"}""", "-o", output.toString(), "-q")
        code shouldBe 0
        Files.readString(output) shouldContain "@SerializedName"
    }

    @Test
    fun `kt subcommand with --dep fastjson switches annotation`(@TempDir tmp: Path) {
        val output = tmp.resolve("Foo.kt")
        val code = exec("kt", "-j", """{"name":"Foo"}""", "-o", output.toString(), "--dep", "fastjson", "-q")
        code shouldBe 0
        Files.readString(output) shouldContain "@JSONField"
    }

    @Test
    fun `dart --split-g writes both main and g file`(@TempDir tmp: Path) {
        val output = tmp.resolve("User.dart")
        val partFile = tmp.resolve("User.g.dart")
        val code = exec("dart", "-j", """{"a":1}""", "-o", output.toString(), "--split-g", "--clone", "-q")
        code shouldBe 0
        Files.exists(output) shouldBe true
        Files.exists(partFile) shouldBe true
        Files.readString(output) shouldContain "part 'User.g.dart';"
        Files.readString(partFile) shouldContain "part of 'User.dart';"
    }

    @Test
    fun `refuses to overwrite without --force`(@TempDir tmp: Path) {
        val output = tmp.resolve("Foo.dart")
        Files.writeString(output, "// existing")
        val code = exec("dart", "-j", """{"a":1}""", "-o", output.toString())
        code shouldBe 3
        Files.readString(output) shouldBe "// existing"
    }

    @Test
    fun `--force overwrites existing file`(@TempDir tmp: Path) {
        val output = tmp.resolve("Foo.dart")
        Files.writeString(output, "// existing")
        val code = exec("dart", "-j", """{"a":1}""", "-o", output.toString(), "--force", "-q")
        code shouldBe 0
        Files.readString(output) shouldContain "class Foo"
    }

    @Test
    fun `both -i and --json is exit code 2`(@TempDir tmp: Path) {
        val input = writeInput(tmp, """{}""")
        val output = tmp.resolve("X.dart")
        val code = exec("dart", "-i", input.toString(), "-j", """{}""", "-o", output.toString())
        code shouldBe 2
    }

    @Test
    fun `missing input is exit code 2`(@TempDir tmp: Path) {
        val output = tmp.resolve("X.dart")
        val code = exec("dart", "-o", output.toString())
        code shouldBe 2
    }

    @Test
    fun `java subcommand emits Gson POJO`(@TempDir tmp: Path) {
        val output = tmp.resolve("Foo.java")
        val code = exec("java", "-j", """{"user_id":1}""", "-o", output.toString(), "-q")
        code shouldBe 0
        val content = Files.readString(output)
        content shouldContain "import com.google.gson.annotations.SerializedName;"
        content shouldContain "@SerializedName(\"user_id\")"
        content shouldContain "public Integer userId;"
    }

    @Test
    fun `ts subcommand emits TS interface`(@TempDir tmp: Path) {
        val output = tmp.resolve("Foo.ts")
        val code = exec("ts", "-j", """{"name":"x","age":1}""", "-o", output.toString(), "-q")
        code shouldBe 0
        val content = Files.readString(output)
        content shouldContain "export interface Foo {"
        content shouldContain "name?: string"
        content shouldContain "age?: number"
    }
}
```

- [ ] **Step 2: Run tests, expect all to pass**

Run: `./gradlew :cli:test --no-daemon`
Expected: 10 tests pass.

If any fail, read the error carefully — most likely cause is a missed field-name conversion (className derivation, file vs file-stem). Fix in `BeanCommand.call()`.

- [ ] **Step 3: Commit**

```bash
git add cli/src/test
git commit -m "test(cli): integration coverage for all 4 subcommands + IO flags + exit codes"
```

### Task 18: Build the fat jar and smoke test it from the shell

**Files:** verification only.

- [ ] **Step 1: Build the shadow jar**

Run: `./gradlew :cli:shadowJar --no-daemon`
Expected: BUILD SUCCESSFUL. Artifact appears at `cli/build/libs/json2bean-1.10.0-SNAPSHOT-all.jar` (version comes from root `build.gradle`).

- [ ] **Step 2: Smoke test --help**

Run: `java -jar cli/build/libs/json2bean-*-all.jar --help`
Expected: usage text with the 4 subcommands.

- [ ] **Step 3: Smoke test a real generation**

Run:
```bash
echo '{"user_id": 1, "items": [{"sku":"A","qty":2}]}' > /tmp/u.json
java -jar cli/build/libs/json2bean-*-all.jar dart -i /tmp/u.json -o /tmp/U.dart
cat /tmp/U.dart
```
Expected: a valid Dart file is printed, exit code 0.

- [ ] **Step 4: Smoke test all 4 languages**

Run:
```bash
for lang in dart kt ts java; do
  java -jar cli/build/libs/json2bean-*-all.jar $lang \
    -j '{"user_id":1,"items":[{"sku":"A"}]}' \
    -o /tmp/U.$lang --force -q && echo "OK $lang"
done
```
Expected: `OK dart`, `OK kt`, `OK ts`, `OK java`.

- [ ] **Step 5: No commit (verification only)**

---

## Phase 6 — Docs and Release Polish

### Task 19: Update README and CLAUDE.md

**Files:**
- Modify: `README.md`
- Modify: `CLAUDE.md`

- [ ] **Step 1: Append CLI Usage section to README.md**

Add at the bottom of `README.md`:

```markdown
# Command-line tool (json2bean CLI)

For users not on IntelliJ IDEA — same generator engine, run from the shell.

## Install

Download `json2bean-<version>-all.jar` from [Releases](https://github.com/fastcode555/JsonBeanGenerator/releases), then:

```bash
alias json2bean='java -jar /path/to/json2bean-1.10.0-all.jar'
```

Requires JDK 17+.

## Usage

```bash
# Dart (default, non-split)
json2bean dart -i user.json -o User.dart

# Dart with .g.dart part file + clone()
json2bean dart -i user.json -o User.dart --split-g --clone

# Kotlin (default Gson; alternatives: fastjson | none)
json2bean kt -i user.json -o User.kt --dep gson

# TypeScript interface
json2bean ts -i user.json -o User.ts

# Java Gson POJO
json2bean java -i user.json -o User.java

# Inline JSON instead of a file
json2bean ts -j '{"name":"x"}' -o X.ts
```

Run `json2bean <subcommand> --help` for the full option list.
```

- [ ] **Step 2: Update CLAUDE.md "## Project" section**

In `CLAUDE.md`, replace the "## Project" paragraph with:

```markdown
## Project

Multi-module Gradle project containing:

- **`core`** — pure JSON-to-bean generators (Dart / TS / Python / Java / Kotlin Map+Gson+FastJson). Zero IntelliJ-platform dependencies; all language logic lives here.
- **`plugin`** — IntelliJ IDEA Community plugin (id `org.awesome.JsonBeanGenerator`, name "Json2Dart-Null-Safety"). Built with Kotlin 2.1.10 + `org.jetbrains.intellij.platform` 2.2.0, targets IDEA 2024.3.1 (`sinceBuild=241.14494.240`, `untilBuild=261.*`). Depends on `:core`.
- **`cli`** — `json2bean` command-line tool exposing `dart` / `kt` / `ts` / `java` subcommands. Built as a fat jar via the Shadow plugin. Depends on `:core`.
```

- [ ] **Step 3: Replace the "## Common commands" section in CLAUDE.md**

Replace it with:

```markdown
## Common commands

```bash
# build the IDE plugin zip → plugin/build/distributions/
./gradlew :plugin:buildPlugin

# build the CLI fat jar → cli/build/libs/json2bean-<ver>-all.jar
./gradlew :cli:shadowJar

# launch a sandbox IDE with the plugin installed (interactive)
./gradlew :plugin:runIde

# regression suite (all golden tests for the generators + CLI integration tests)
./gradlew test

# ad-hoc generator dump (debug helper)
./gradlew :plugin:runGenerators
```

The `core` module has no plugin verifier task; that one stays on `:plugin`:
`./gradlew :plugin:runPluginVerifier`.
```

- [ ] **Step 4: Replace the "## Architecture" section pointers in CLAUDE.md**

Within the "## Architecture" section, every path that previously read `src/main/kotlin/com/awesome/...` should now read `plugin/src/main/kotlin/com/awesome/...` for IDE-specific code, or `core/src/main/kotlin/com/awesome/core/...` for the generators and utilities. Specifically:

- "Action registration model (`plugin.xml` ↔ `com.awesome.plugins.*`)" → still under `plugin/`, no change to package name
- The "json2bean generator pipeline" bullets: `DartJsonGenerator`, `TsJsonGenerator`, `PythonJsonGenerator`, `KtGsonGenerator`, `KtFastJsonGenerator`, `MapKtJsonGenerator`, `JavaJsonGenerator`, `BaseGenerator`, `JsonGenerator`, `Generators` all live under `core/src/main/kotlin/com/awesome/core/generators/`
- `JsonHelper.kt` extensions: now split across `core/src/main/kotlin/com/awesome/core/util/{Naming,JsonParse,KeywordTable}.kt`
- `PluginProps` moved to `core/src/main/kotlin/com/awesome/core/PluginProps.kt`
- Add a new "### CLI entry point" bullet pointing at `cli/src/main/kotlin/com/awesome/cli/Main.kt` and noting that all subcommands extend `BeanCommand`

Leave every other section (codestyle, shared utilities, conventions) untouched — those still describe plugin-only code.

- [ ] **Step 5: Commit**

```bash
git add README.md CLAUDE.md
git commit -m "docs: README CLI usage section + CLAUDE.md multi-module layout"
```

### Task 20: Final regression sweep

**Files:** verification only.

- [ ] **Step 1: Build everything from a clean state**

Run: `./gradlew clean build --no-daemon`
Expected: BUILD SUCCESSFUL across all three modules.

- [ ] **Step 2: Run all tests**

Run: `./gradlew test --no-daemon`
Expected: `:core:test` (10 tests) and `:cli:test` (10 tests) all pass.

- [ ] **Step 3: Build the plugin zip**

Run: `./gradlew :plugin:buildPlugin --no-daemon`
Expected: `plugin/build/distributions/JsonBeanGenerator-1.10.0-SNAPSHOT.zip` exists.

- [ ] **Step 4: Build the fat jar**

Run: `./gradlew :cli:shadowJar --no-daemon`
Expected: `cli/build/libs/json2bean-1.10.0-SNAPSHOT-all.jar` exists.

- [ ] **Step 5: No commit (verification only)**

---

## Done

After Task 20 passes, the deliverables are:

- `cli/build/libs/json2bean-1.10.0-SNAPSHOT-all.jar` — the CLI fat jar
- `plugin/build/distributions/JsonBeanGenerator-1.10.0-SNAPSHOT.zip` — the IDE plugin zip
- `core` module — shared, IntelliJ-free, golden-tested generator library

Cut a `1.10.0` Git tag and attach both artifacts to a GitHub Release with the README's "Install" section referencing the JAR.
