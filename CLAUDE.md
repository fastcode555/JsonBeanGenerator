# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Multi-module Gradle project containing:

- **`core`** — pure JSON-to-bean generators (Dart / TS / Python / Java / Kotlin Map+Gson+FastJson). Zero IntelliJ-platform dependencies; all language logic lives here.
- **`plugin`** — IntelliJ IDEA Community plugin (id `org.awesome.JsonBeanGenerator`, name "Json2Dart-Null-Safety"). Built with Kotlin 2.1.10 + `org.jetbrains.intellij.platform` 2.2.0, targets IDEA 2024.3.1 (`sinceBuild=241.14494.240`, `untilBuild=261.*`). Depends on `:core`.
- **`cli`** — `json2bean` command-line tool exposing `dart` / `kt` / `ts` / `java` subcommands. Built as a fat jar via the Shadow plugin. Depends on `:core`.

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

## Architecture

### Action surface (`plugin/src/main/resources/META-INF/plugin.xml`)

Every user-facing feature is an `AnAction` registered in `plugin.xml` under one of:
- `NewGroup` — Cmd+N "New …" menu (top-level generators: Json2Dart, R.dart, Tailwind, Languages-Res)
- `GenerateGroup` — Cmd+N inside an editor (fromJson/toJson/clone, ToMapField, To Code, String Association, Insert Image Link)
- `RefactoringMenu` — `RefactorAction` for resource refactor

When adding a new feature, add **both** the action class under `com.awesome.plugins.<feature>/` **and** an `<action>` entry in `plugin.xml`. Without the XML entry the action is invisible to the IDE.

### Action base classes (`plugin/src/main/kotlin/com/awesome/common`)

- `BaseAnAction` — extends `AnAction`, declares `getActionUpdateThread = BGT`, and auto-toggles visibility based on `fileType()` (extensions the action supports). Use this for actions tied to file extensions.
- Actions tied to a selected `PsiDirectory` (e.g. `JsonBeanGeneratorAction`) extend `AnAction` directly and gate on `CommonDataKeys.PSI_ELEMENT is PsiDirectory`.

### Plugin configuration: `plugins.properties` + `PluginProps`

User-project–level settings live in a `plugins.properties` file at the user's project root (not in this repo). `PropertiesHelper(psiElement)` resolves it via `PsiFileUtils.getFileByName` (module scope first, then project scope, else writes to `project.basePath`). All property keys are centralized in `core/src/main/kotlin/com/awesome/core/PluginProps.kt` — **always add new keys there**, never inline strings. Dialogs persist UI state by calling `properties.setProperty(PluginProps.<key>, …)` in their radio/checkbox listeners (see `JsonBeanDialog.initRadioButtons`).

### json2bean generator pipeline

- Entry action `JsonBeanGeneratorAction` → opens `JsonBeanDialog` (Swing `.form`, in `plugin/src/main/kotlin/com/awesome/plugins/json2bean/`).
- `JsonBeanDialog` picks a target language radio (`.dart`/`.ts`/`.py`/`.kt`) and delegates to `GeneratorHelper`:
  - `.dart` write path → `GeneratorHelper.dartGenerate(...)` returns a `DartJsonGenerator.Output(mainContent, partContent, mainFileName, partFileName)` — `partContent`/`partFileName` are non-null only when `splitGFile=true` (Dart `.g.dart` part-file split, see `PluginProps.splitGFile`).
  - `.dart` preview / `.ts` / `.py` → `GeneratorHelper.json2Bean(...)` returns a single string.
  - `.kt` → `GeneratorHelper.json2KtOrJava(...)` dispatches to `MapKtJsonGenerator` / `KtGsonGenerator` / `KtFastJsonGenerator` based on `depType` (`none`/`gson`/`fastjson`).
- All language generators (`DartJsonGenerator`, `TsJsonGenerator`, `PythonJsonGenerator`, `KtGsonGenerator`, `KtFastJsonGenerator`, `MapKtJsonGenerator`, `JavaJsonGenerator`) inherit `BaseGenerator` and live in `core/src/main/kotlin/com/awesome/core/generators/`.
- `BaseGenerator` and the `JsonGenerator` interface are in `core/src/main/kotlin/com/awesome/core/generators/`.
- If SQLite support is checked, `DartDataBaseGenerator` runs as a second pass to emit the DAO alongside the bean.

When adding a new target language or generator variant, prefer extending `GeneratorHelper` (single dispatch point) over branching inside `JsonBeanDialog`.

### CLI entry point

`cli/src/main/kotlin/com/awesome/cli/Main.kt` is the entry point for the `json2bean` command-line tool. All subcommands (`DartCommand`, `KtCommand`, `TsCommand`, `JavaCommand`) extend `BeanCommand` and delegate to the same generators in `core/src/main/kotlin/com/awesome/core/generators/`. The CLI uses picocli for argument parsing and outputs to disk or stdout based on user flags.

### codestyle: chain-of-responsibility (`plugin/src/main/kotlin/com/awesome/plugins/codestyle/`)

CSS-to-code conversion uses `BaseProcessor` (holds an `ArrayList<BaseInterceptor>`) with each interceptor mutating the text in turn. `StrategyManager` picks `FlutterProcessor` for `.dart` files, `TailWindProcessor` otherwise. To add a new CSS rule, write a `BaseInterceptor` subclass and `interceptors.add(...)` it in the processor's `init` — don't fork the processor.

### Shared utilities

- **Core utilities** (`core/src/main/kotlin/com/awesome/core/util/`):
  - `Naming.kt` — top-level string extensions (`toCamel`, `toUpperCamel`, `toLowerUnderScore`) for identifier generation, used by all generators.
  - `JsonParse.kt` — JSON parsing helpers, including `toJSON()` extension used by `BaseGenerator`.
  - `KeywordTable.kt` — language-specific keyword tables for Dart, Kotlin, TypeScript, etc.
- **Plugin utilities** (`plugin/src/main/kotlin/com/awesome/common/util/`):
  - `PsiFileUtils.kt` — `PsiElement.basePath()` walks up to find the nearest `pubspec.yaml` (supports Flutter sub-modules); `PsiElement.moduleName()` reads its `name:`. Use these instead of `project.basePath` when paths must respect Flutter workspace layout.
  - `HttpClient3` / `HttpApi` — Google Translate calls for the Languages-Res feature (rate-limited by Google).

### Resource generation conventions

- `R.dart` generation respects `plugin.assetsIgnoreDirs` and `plugin.generateAssetDirs` from `plugins.properties`.
- Language generation requires `plugin.languages`, `plugin.countryCode`, `plugin.languageDir`, `plugin.needTranslate`, `plugin.rawLanguage` — see `README.md` for the user-facing setup.

## Conventions

- All UI dialogs are Swing `.kt` + matching `.form` files (IntelliJ GUI Designer). Field declarations in the `.kt` must mirror the `.form` exactly (`var foo: JButton? = null`).
- Source comments are predominantly Chinese; keep new comments consistent with surrounding style.
- The `src/test/kotlin/` files (`multi_dd.kt`, `ConvertColor.kt`) are scratch experiments, not a JUnit suite. Don't rely on `./gradlew test` for verification.
