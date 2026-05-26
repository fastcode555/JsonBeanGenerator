# json2bean CLI 使用文档

`json2bean` 是 Json2Dart-Null-Safety 插件的命令行版本。它和 IntelliJ 插件共用同一套生成引擎（`:core` 模块），可以在任意终端、CI 流水线、或非 IntelliJ 编辑器（VSCode / Cursor / Vim）里把 JSON 转成数据模型。

支持目标语言：**Dart / Kotlin / TypeScript / Java**。

---

## 1. 环境要求

- **JDK 17+**（运行 fat jar 需要 JVM）。检查：`java -version`。

---

## 2. 安装

### 方式 A：项目自带的安装脚本（推荐）

在仓库根目录执行：

```bash
./scripts/install-cli.sh
```

脚本会：构建 fat jar → 复制到 `~/.local/share/json2bean/` → 在 `~/.local/bin/` 放一个 `json2bean` 启动器 → 必要时把 `~/.local/bin` 加进 `PATH`。

装完后 **新开一个终端**（或 `source ~/.zshrc`），任意目录运行：

```bash
json2bean --help
```

### 方式 B：手动用 jar

```bash
./gradlew :cli:shadowJar
# 产物：cli/build/libs/json2bean-<version>-all.jar
alias json2bean='java -jar /绝对路径/json2bean-1.10.0-SNAPSHOT-all.jar'
```

把 `alias` 写进 `~/.zshrc` 即可长期使用。

---

## 3. 命令结构

```
json2bean <language> [OPTIONS]
```

`<language>` 是 `dart` / `kt` / `ts` / `java` 之一。

### 通用选项（所有子命令共享）

| 选项 | 说明 |
|---|---|
| `-i, --input <FILE>` | 从文件读取 JSON |
| `-j, --json <STRING>` | 直接传 JSON 字符串 |
| `-o, --output <FILE>` | 输出文件路径（**必填**） |
| `-n, --class-name <NAME>` | 类名（默认从 `-o` 文件名推导为 UpperCamel） |
| `-e, --extends <CLASS>` | 让生成的类继承某个父类 |
| `-m, --implements <CLASSES>` | 让生成的类实现某些接口 |
| `--force` | `-o` 已存在时覆盖（默认拒绝） |
| `-q, --quiet` | 静默，不打印 "wrote ..." 信息 |
| `-h, --help` | 帮助 |
| `-V, --version` | 版本 |

> `-i` 与 `-j` 互斥；两者都不给会报错（退出码 2）。

---

## 4. 各语言子命令

### Dart

```bash
json2bean dart -i user.json -o User.dart [--split-g] [--clone] [--sqlite --primary-key id]
```

| Dart 专有选项 | 说明 |
|---|---|
| `--split-g` | 额外生成 `.g.dart` part 文件（在 `-o` 同目录） |
| `--clone` | 生成 `clone()` 方法 |
| `--sqlite` | 生成 json2dart_db sqlite 钩子（**必须配合 `--primary-key`**） |
| `--primary-key <NAME>` | sqlite 主键字段名 |

生成的 Dart 模型依赖 [`json2dart_safe`](https://pub.dev/packages/json2dart_safe)（`fromJson`/`toJson`/`asInt` 等扩展方法来自该库），sqlite 模式还需 `json2dart_db`。在目标 Flutter 工程的 `pubspec.yaml` 里加上这些依赖即可。

### Kotlin

```bash
json2bean kt -i user.json -o User.kt [--dep gson|fastjson|none]
```

| Kotlin 专有选项 | 说明 |
|---|---|
| `--dep gson` | （默认）`@SerializedName` 注解的 data class |
| `--dep fastjson` | `@JSONField` 注解的 data class |
| `--dep none` | 纯 data class，字段名保留原始 JSON key |

### TypeScript

```bash
json2bean ts -i user.json -o User.ts
```

生成 `export interface`，字段全部可选（`?`），字段名保留原始 JSON key。

### Java

```bash
json2bean java -i user.json -o User.java
```

生成 Gson 风格 POJO：`public` 字段 + `@SerializedName` 注解 + 包装类型（`Integer`/`Long`/`Double`/`Boolean`，缺字段反序列化为 `null`）。需要目标工程引入 Gson。

---

## 5. 退出码

| 码 | 含义 |
|---|---|
| 0 | 成功 |
| 1 | JSON 解析错误（不是合法 JSON / 不以 `{` 或 `[` 开头） |
| 2 | 参数错误（`-i`/`-j` 互斥冲突、缺输入、`--sqlite` 缺 `--primary-key`） |
| 3 | 输出文件已存在且未加 `--force` |
| 4 | 写文件失败（无权限、目录不存在） |
| 64 | 内部错误（请提 issue） |

脚本里可以这样判断：

```bash
json2bean dart -i x.json -o X.dart || echo "失败，退出码 $?"
```

---

## 6. 常用示例

```bash
# 文件 → Dart
json2bean dart -i api/user.json -o lib/models/User.dart

# Dart 拆 .g.dart + clone()
json2bean dart -i api/user.json -o lib/models/User.dart --split-g --clone

# 直接传字符串 → TS，打印到指定文件
json2bean ts -j '{"id":1,"name":"x"}' -o types/User.ts

# Kotlin fastjson 风格
json2bean kt -i user.json -o User.kt --dep fastjson

# Java POJO，覆盖已有文件
json2bean java -i user.json -o User.java --force

# 指定类名（不跟文件名走）
json2bean dart -i raw.json -o out.dart -n AudioBook

# CI 里批量转换
for f in schemas/*.json; do
  json2bean ts -i "$f" -o "src/types/$(basename "$f" .json).ts" --force -q
done
```

---

## 7. 已知限制

- **关键字字段未转义**：JSON key 若恰好是目标语言关键字（如 Dart 的 `class`/`new`，Java 的 `int`），生成的字段名可能不合法，需手动改。
- **空数组 → 空类**：JSON 里 `"x": []` 会被推断为对象数组并生成一个空类（如 `class EmptyList {}`）。建议源 JSON 提供非空示例。
- **TS / Kotlin(none) 字段名不转 camelCase**：保留原始 JSON key（设计如此）。其余语言会转 camelCase 并加注解保留原 key。
- **CLI 不支持 Python / R.dart / 多语言翻译**：这些仍只在 IntelliJ 插件里可用。CLI v1 范围只有 4 种 bean 语言。
- **sqlite DAO 不生成**：CLI 的 `--sqlite` 只影响 bean 类本身的 sqlite 字段，不生成 DAO 文件（DAO 仍是插件专属）。

---

## 8. 卸载

```bash
rm -rf ~/.local/share/json2bean ~/.local/bin/json2bean
```

如果安装脚本往 `~/.zshrc` 加过 PATH 行，可手动删除标记为 `# json2bean CLI` 的那一行。
