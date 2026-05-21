# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

IntelliJ Platform plugin "Json2Dart-Null-Safety" (id `org.awesome.JsonBeanGenerator`). Provides JSON → Dart/TypeScript/Python/Kotlin bean generation plus Flutter-flavored helpers (asset R.dart generator, multi-language translation, Tailwind/CSS → Flutter widget conversion, etc.).

Built with Kotlin 2.1.10 + `org.jetbrains.intellij.platform` 2.2.0, targets IntelliJ IDEA Community 2024.3.1 (`sinceBuild=241.14494.240`, `untilBuild=261.*`).

## Common commands

```bash
# build the plugin zip → build/distributions/
./gradlew buildPlugin

# launch a sandbox IDE with the plugin installed (interactive)
./gradlew runIde

# run plugin verifier against declared IDE versions
./gradlew runPluginVerifier

# tests (src/test/kotlin contains standalone scripts, not a real test suite)
./gradlew test
```

There is no lint configuration beyond `kotlin.code.style=official`. There is no CI workflow.

## Architecture

### Action surface (`src/main/resources/META-INF/plugin.xml`)

Every user-facing feature is an `AnAction` registered in `plugin.xml` under one of:
- `NewGroup` — Cmd+N "New …" menu (top-level generators: Json2Dart, R.dart, Tailwind, Languages-Res)
- `GenerateGroup` — Cmd+N inside an editor (fromJson/toJson/clone, ToMapField, To Code, String Association, Insert Image Link)
- `RefactoringMenu` — `RefactorAction` for resource refactor

When adding a new feature, add **both** the action class under `com.awesome.plugins.<feature>/` **and** an `<action>` entry in `plugin.xml`. Without the XML entry the action is invisible to the IDE.

### Action base classes (`com.awesome.common`)

- `BaseAnAction` — extends `AnAction`, declares `getActionUpdateThread = BGT`, and auto-toggles visibility based on `fileType()` (extensions the action supports). Use this for actions tied to file extensions.
- Actions tied to a selected `PsiDirectory` (e.g. `JsonBeanGeneratorAction`) extend `AnAction` directly and gate on `CommonDataKeys.PSI_ELEMENT is PsiDirectory`.

### Plugin configuration: `plugins.properties` + `PluginProps`

User-project–level settings live in a `plugins.properties` file at the user's project root (not in this repo). `PropertiesHelper(psiElement)` resolves it via `PsiFileUtils.getFileByName` (module scope first, then project scope, else writes to `project.basePath`). All property keys are centralized in `com.awesome.common.PluginProps` — **always add new keys there**, never inline strings. Dialogs persist UI state by calling `properties.setProperty(PluginProps.<key>, …)` in their radio/checkbox listeners (see `JsonBeanDialog.initRadioButtons`).

### json2bean generator pipeline (`plugins/json2bean`)

- Entry action `JsonBeanGeneratorAction` → opens `JsonBeanDialog` (Swing `.form`).
- `JsonBeanDialog` picks a target language radio (`.dart`/`.ts`/`.py`/`.kt`) and delegates to `GeneratorHelper`:
  - `.dart` write path → `GeneratorHelper.dartGenerate(...)` returns a `DartJsonGenerator.Output(mainContent, partContent, mainFileName, partFileName)` — `partContent`/`partFileName` are non-null only when `splitGFile=true` (Dart `.g.dart` part-file split, see `PluginProps.splitGFile`).
  - `.dart` preview / `.ts` / `.py` → `GeneratorHelper.json2Bean(...)` returns a single string.
  - `.kt` → `GeneratorHelper.json2KtOrJava(...)` dispatches to `MapKtJsonGenerator` / `KtGsonGenerator` / `KtFastJsonGenerator` based on `depType` (`none`/`gson`/`fastjson`).
- All language generators inherit `BaseGenerator` which parses input via fastjson2 (`content.toJSON()` extension in `JsonHelper.kt`).
- If SQLite support is checked, `DartDataBaseGenerator` runs as a second pass to emit the DAO alongside the bean.

When adding a new target language or generator variant, prefer extending `GeneratorHelper` (single dispatch point) over branching inside `JsonBeanDialog`.

### codestyle: chain-of-responsibility (`plugins/codestyle`)

CSS-to-code conversion uses `BaseProcessor` (holds an `ArrayList<BaseInterceptor>`) with each interceptor mutating the text in turn. `StrategyManager` picks `FlutterProcessor` for `.dart` files, `TailWindProcessor` otherwise. To add a new CSS rule, write a `BaseInterceptor` subclass and `interceptors.add(...)` it in the processor's `init` — don't fork the processor.

### Shared utilities (`com.awesome.utils`)

- `JsonHelper.kt` — top-level string extensions (`toCamel`, `toUpperCamel`, `toLowerUnderScore`, `formatJson`, `toJSON`, `mergeKeys`) used pervasively. Imports look like `import toCamel` because these are top-level (no package).
- `PsiFileUtils.kt` — `PsiElement.basePath()` walks up to find the nearest `pubspec.yaml` (supports Flutter sub-modules); `PsiElement.moduleName()` reads its `name:`. Use these instead of `project.basePath` when paths must respect Flutter workspace layout.
- `HttpClient3` / `HttpApi` — Google Translate calls for the Languages-Res feature (rate-limited by Google).

### Resource generation conventions

- `R.dart` generation respects `plugin.assetsIgnoreDirs` and `plugin.generateAssetDirs` from `plugins.properties`.
- Language generation requires `plugin.languages`, `plugin.countryCode`, `plugin.languageDir`, `plugin.needTranslate`, `plugin.rawLanguage` — see `README.md` for the user-facing setup.

## Conventions

- All UI dialogs are Swing `.kt` + matching `.form` files (IntelliJ GUI Designer). Field declarations in the `.kt` must mirror the `.form` exactly (`var foo: JButton? = null`).
- Source comments are predominantly Chinese; keep new comments consistent with surrounding style.
- The `src/test/kotlin/` files (`multi_dd.kt`, `ConvertColor.kt`) are scratch experiments, not a JUnit suite. Don't rely on `./gradlew test` for verification.
