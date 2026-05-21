# json2bean CLI 设计稿

- **日期**：2026-05-21
- **作者**：Barry（与 Claude 协作）
- **状态**：草案，待审核
- **目标版本**：1.10.0

## 1. 背景与目标

`JsonBeanGenerator` 目前是一个 IntelliJ IDEA 插件，提供"JSON → Dart/TS/Python/Kotlin 数据模型"等能力。本设计稿描述如何在保留插件功能的前提下，新增一组同源命令行工具（CLI），覆盖以下场景：

- 本地/团队脚本化（开发者批量转换 JSON 文件、配合 shell）
- CI/CD 流水线集成（后端 schema 出 → 前端 model 自动同步）
- 开源分发给非 IntelliJ 系（VSCode、Cursor、Vim 用户）
- 作为 IDE 插件的等价替代/补充，行为完全一致

第一版（v1）范围：**`json2bean {dart|kt|ts|java}` 四个子命令，仅 JSON → Bean**。其它插件功能（R.dart 资源、多语言翻译、Tailwind、stringassociate）不在 v1 范围。

## 2. 关键设计决策一览

| 决策 | 选择 | 主要理由 |
|---|---|---|
| 仓库结构 | 同仓库 + 多 Gradle 模块（`core` / `plugin` / `cli`） | 生成器单一源，插件/CLI 不漂移 |
| 打包形式 | Fat JAR，`java -jar json2bean.jar` | YAGNI；native/brew 后续按需补 |
| CLI 形态 | 单一 binary + 语言子命令（`json2bean dart …`） | 一份文档、一棵 help 树 |
| 输入 | `-i FILE` 或 `--json STRING`，互斥 | 文件 + 直接字符串两种最常用 |
| 输出 | `-o FILE`（单主文件，必填） | 简单、可预测；`--split-g` 时附带 sibling 文件 |
| Java 风格 | Gson（与 KtGson 对齐） | 单一风格降低 v1 复杂度 |
| 配置 | 仅 CLI flag，不读 `plugins.properties` | 无隐含状态，CI 可复现 |

## 3. 模块结构

```
JsonBeanGenerator/
├── settings.gradle                 ← include 'core', 'plugin', 'cli'
├── build.gradle                    ← 根 build，共享 version/repo
├── core/
│   ├── build.gradle                ← 只依赖 fastjson2 + guava(CaseFormat) + kotlin-stdlib
│   └── src/main/kotlin/com/awesome/core/
│       ├── generators/
│       │   ├── BaseGenerator.kt
│       │   ├── DartJsonGenerator.kt
│       │   ├── TsJsonGenerator.kt
│       │   ├── PythonJsonGenerator.kt
│       │   ├── JavaJsonGenerator.kt              ← 新增
│       │   └── kt/{MapKtJsonGenerator, KtGsonGenerator, KtFastJsonGenerator}.kt
│       ├── model/
│       │   ├── GeneratedFile.kt
│       │   └── GenerateRequest.kt
│       ├── util/{Naming, JsonParse, KeywordTable}.kt
│       └── PluginProps.kt
│   └── src/test/kotlin/                          ← core 全量 golden 测试落这里
│
├── plugin/                         ← 原 src/ 主体迁移到这里（git mv 保历史）
│   ├── build.gradle                ← intellijPlatform + project(':core')
│   └── src/main/kotlin/com/awesome/{common,plugins,utils,resources}/
│
└── cli/
    ├── build.gradle                ← shadow plugin + project(':core')
    └── src/main/kotlin/com/awesome/cli/
        ├── Main.kt                                ← picocli 入口
        ├── commands/{Dart,Kt,Ts,Java}Command.kt
        └── io/InputResolver.kt
```

**边界契约**：

- `core`：**零** IntelliJ 依赖、**零**文件 IO。纯 `(JSON, options) → List<GeneratedFile>`
- `plugin`：负责 PSI/Dialog/写文件，是 core 的调用方之一
- `cli`：负责参数解析/文件 IO/退出码，是 core 的另一调用方

## 4. core 模块统一 API

```kotlin
package com.awesome.core.model

data class GeneratedFile(val name: String, val content: String)

data class GenerateRequest(
    val json: String,
    val className: String,
    val extendsClass: String = "",
    val implementsClass: String = "",
    val options: Map<String, Any?> = emptyMap(),
)

package com.awesome.core.generators

interface JsonGenerator {
    fun generate(req: GenerateRequest): List<GeneratedFile>
}
```

**`options` 约定 key**（统一在 `PluginProps`）：

| Key | 类型 | 适用语言 | 说明 |
|---|---|---|---|
| `splitGFile` | Boolean | dart | 拆出 `.g.dart` part 文件 |
| `sqliteEnable` | Boolean | dart | 启用 sqlite 主键字段 |
| `primaryKey` | String | dart | sqlite 主键名 |
| `needClone` | Boolean | dart | 生成 `clone()` 方法 |
| `kotlinDep` | String | kt | `gson` / `fastjson` / `none` |

### 4.1 现有 generator 收敛

| 类 | 现状构造 | 目标 |
|---|---|---|
| `DartJsonGenerator` | `(content, name, ext, impl, sqlite, pk, clone, splitG).generate(): Output` | `generate(req): List<GeneratedFile>`（1 或 2 文件） |
| `TsJsonGenerator` | `(content, name, ext, impl).toString()` | 1 文件 |
| `PythonJsonGenerator` | 同 TS | 1 文件 |
| `MapKt / KtGson / KtFastJson` | `(content, name, ext, impl, psiDir?).generate()` 直接写盘（Phase 0 已把 psiDir 改可空） | 完全去掉 psiDir，返回 1 文件 |
| `JavaJsonGenerator`（新增） | — | Gson 风格 POJO + `@SerializedName`，1 文件 |

> **Phase 0 已提前做**：本设计阶段把 KT 三件套的 `psiDir` 改为 `PsiDirectory?` 并在 `generate()` 内早返回，让 `toString()` 可脱 PSI 调用，为 spec 的 golden output 提供运行通道。完整迁移到 core 时这层会自然消失。

### 4.2 写文件契约

`core` **不**写文件。调用方拿到 `List<GeneratedFile>` 后：

- plugin 侧：循环 `File(mDirectory.virtualFile.path, it.name).writeText(it.content)`
- CLI 侧：主文件落 `-o` 路径；如果 list 长度 > 1（仅 dart split-g），sibling 文件落在 `-o` 的同目录、文件名取自 `GeneratedFile.name`

### 4.3 sqlite DAO 不入 core

`DartDataBaseGenerator` 重度依赖 PSI（搜索/创建文件、改 import）。第一版**留在 plugin 端不动**，CLI v1 的 `--sqlite` 仅控制 bean 类本身的 sqlite 字段，**不**生成 DAO。DAO 留作 v2 议题。

## 5. CLI 接口（picocli）

**入口**：

```
json2bean [PARENT OPTIONS] <subcommand> [SUBCMD OPTIONS]
```

**选 picocli 的理由**：JVM 原生、注解式 ergonomic、自动 help/version、子命令树成熟、shadow jar 友好。

### 5.1 公共选项（所有子命令共享，放父命令）

```
-i, --input <FILE>      读取 JSON 文件
-j, --json <STRING>     直接传 JSON 字符串
-o, --output <FILE>     输出主文件路径（必填）
-n, --class-name <NAME> 类名（默认从 -o 文件名 stem 推导，UpperCamel）
-e, --extends <CLASS>
-m, --implements <CLASSES>
    --force             -o 已存在时覆盖（默认拒绝）
-q, --quiet             静默
-v, --version
-h, --help
```

`-i` 与 `-j` 互斥；二者皆缺时 exit code 2。

### 5.2 子命令

```
# Dart
json2bean dart -i user.json -o User.dart \
  [--split-g] [--sqlite --primary-key id] [--clone]

# Kotlin
json2bean kt -i user.json -o User.kt [--dep gson|fastjson|none]   # 默认 gson

# TypeScript
json2bean ts -i user.json -o User.ts

# Java（固定 Gson 风格）
json2bean java -i user.json -o User.java
```

### 5.3 类名推导

`-o foo_bar.dart` → `FooBar`（用 `Naming.toUpperCamel`）。`-n` 显式给出则覆盖。

### 5.4 退出码

| code | 含义 |
|---|---|
| 0 | 成功 |
| 1 | JSON 解析错 |
| 2 | 参数错（互斥违反、必填缺失） |
| 3 | 输出已存在且未 `--force` |
| 4 | IO 错（无权限、目录不存在） |
| 64 | 内部错（请反馈 issue） |

## 6. plugin 端改造点

`core` 收敛后仅 4 个文件触及：

1. **`JsonBeanDialog.onGenerate()`**（dart 分支）：将 `GeneratorHelper.dartGenerate(...)` 改为返回 `List<GeneratedFile>`，循环写盘
2. **`JsonBeanDialog.onGenerateJavaOrKt()`**：`GeneratorHelper.json2KtOrJava` 不再自写文件，返回 list，dialog 这边写
3. **`GeneratorHelper`**：收敛成 `generate(language, req): List<GeneratedFile>` 的总分发，去掉所有 `psiDirectory` 参数
4. **`PreViewDialog`**：调用 `generate(...).first().content` 取主文件预览

`DartDataBaseGenerator` 不动。

## 7. 测试策略

### 7.1 core 单测（`core/src/test/`，JUnit5 + Kotest assertions）

每种 generator 至少 3 个 golden 测试：

```
core/src/test/resources/fixtures/
├── basic/
│   ├── input.json
│   ├── expected.dart
│   ├── expected.dart.g           ← split-g 模式
│   ├── expected.ts
│   ├── expected.py
│   ├── expected.kt.gson
│   ├── expected.kt.fastjson
│   ├── expected.kt.map
│   └── expected.java
├── complex_nested/
│   └── …                          ← 本文档第 9 节的 fixture 作为 seed
└── empty_array/
    └── …                          ← 覆盖 `[]` → 空 class 边界
```

### 7.2 CLI 集成测（`cli/src/test/`）

用 picocli 的 `CommandLine.execute(args)` 跑端到端，断言 stdout/stderr/exit code。关键路径：

- `-i` 文件
- `-j` 字符串
- `-o` 拒绝覆盖
- `--force` 覆盖
- `--split-g` 双文件
- `--dep fastjson` 切风格

## 8. 构建与分发

- **Gradle 多模块**：根 `settings.gradle` `include 'core', 'plugin', 'cli'`
- **core**：纯 jar，不上传仓库
- **plugin**：原 `intellij-platform` 配置 + `implementation project(':core')`，`buildPlugin` 任务把 core 一起塞进 plugin zip
- **cli**：`com.github.johnrengelman.shadow` 8.x，产物 `cli/build/libs/json2bean-<ver>-all.jar`
- **顶层任务**：`./gradlew :cli:shadowJar` 出 CLI；`./gradlew :plugin:buildPlugin` 出 IDE 包
- **版本**：三模块共享根 version；CLI 首发 `1.10.0`
- **分发**：GitHub Releases 挂 JAR + plugin zip + checksums；README 加 "CLI Usage" 章节

## 9. 真实输入输出 example

> 以下所有输出均由本设计阶段在 master 分支上 `./gradlew runGenerators` 实跑得到，**已修复 8 处暴露的 bug**（见第 10 节）。Java 部分尚无生成器，按"对齐 KtGson 风格"手写期望产物，作为 v1 golden target。

### 9.1 fixture JSON

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

覆盖：snake_case 键、嵌套对象、对象数组、基本类型数组、**2D 数组**、空数组、`BigDecimal` 浮点字段。

### 9.2 Dart（非拆分）

```dart
import 'dart:convert';

import 'package:json2dart_safe/json2dart.dart';

class User {
  final int userId;
  final String userName;
  final bool isVip;
  final double balance;
  List<String>? tags;
  List<List<int>>? scores;
  List<EmptyList>? emptyList;
  Address? address;
  List<Orders>? orders;

  User({
    required this.userId,
    required this.userName,
    required this.isVip,
    required this.balance,
    required this.tags,
    required this.scores,
    this.emptyList,
    this.address,
    this.orders,
  });

  Map<String, dynamic> toJson() => {
        'user_id': userId,
        'user_name': userName,
        'is_vip': isVip,
        'balance': balance,
        'tags': tags,
        'scores': scores,
        'empty_list': emptyList?.map((v) => v.toJson()).toList(),
        'address': address?.toJson(),
        'orders': orders?.map((v) => v.toJson()).toList(),
      };

  factory User.fromJson(Map json) {
    return User(
      userId: json.asInt('user_id'),
      userName: json.asString('user_name'),
      isVip: json.asBool('is_vip'),
      balance: json.asDouble('balance'),
      tags: json.asList<String>('tags'),
      scores: json.asArray2d<int>('scores'),
      emptyList: json.asList<EmptyList>('empty_list', EmptyList.fromJson),
      address: json.asBean('address', Address.fromJson),
      orders: json.asList<Orders>('orders', Orders.fromJson),
    );
  }

  @override
  String toString() => jsonEncode(toJson());
}

// ... EmptyList, Geo, Address, Items, Orders 类省略，结构与 User 同
```

CLI 调用：
```bash
json2bean dart -i user.json -o User.dart
```

### 9.3 Dart（split-g，配合 `--clone`）

主文件 `User.dart`：

```dart
import 'dart:convert';

import 'package:json2dart_safe/json2dart.dart';

part 'User.g.dart';

class User {
  final int userId;
  final String userName;
  // … 字段同上

  User({ … });

  User clone() => _$UserClone(this);

  Map<String, dynamic> toJson() => _$UserToJson(this);

  factory User.fromJson(Map json) => _$UserFromJson(json);

  @override
  String toString() => jsonEncode(toJson());
}
// EmptyList / Geo / Address / Items / Orders 同样是瘦委托
```

伴随 `User.g.dart`：

```dart
part of 'User.dart';

User _$UserClone(User i) => User(
        userId: i.userId,
        userName: i.userName,
        isVip: i.isVip,
        balance: i.balance,
        tags: List<String>.from(i.tags??[]),
        scores: i.scores?.map((e) => List<int>.from(e)).toList(),
        emptyList: i.emptyList?.map((v) => v.clone()).toList(),
        address: i.address?.clone(),
        orders: i.orders?.map((v) => v.clone()).toList(),
    );

Map<String, dynamic> _$UserToJson(User i) => {
        'user_id': i.userId,
        'user_name': i.userName,
        // …
    };

User _$UserFromJson(Map json) => User(
      userId: json.asInt('user_id'),
      // …
    );

// 其它类的 _$Clone / _$ToJson / _$FromJson 同理
```

CLI 调用：
```bash
json2bean dart -i user.json -o User.dart --split-g --clone
```

### 9.4 TypeScript

```ts
export interface User {
  user_id?: number
  user_name?: string
  is_vip?: boolean
  balance?: number
  tags?: string[]
  scores?: number[][]
  empty_list?: EmptyList[]
  address?: Address
  orders?: Orders[]
}

export interface EmptyList {
}

export interface Geo {
  lat?: number
  lng?: number
}

export interface Address {
  city?: string
  zip_code?: string
  geo?: Geo
}

export interface Items {
  sku?: string
  qty?: number
}

export interface Orders {
  order_id?: string
  total?: number
  items?: Items[]
}
```

CLI 调用：
```bash
json2bean ts -i user.json -o User.ts
```

> 已知差异：TS interface 字段名直接复刻 JSON key（`user_id`），不做 camelCase 转换。如果需要 camelCase，将来加 `--key-camel` flag。

### 9.5 Python

```python
import json


class User(object):
  def __init__(self,user_id: int = None,user_name: str = None,is_vip: bool = None,balance: float = None,tags: List[str] = None,scores: list = None,empty_list = None,address = None,orders: list = None):
    self.user_id = user_id
    self.user_name = user_name
    self.is_vip = is_vip
    self.balance = balance
    self.tags = tags
    self.scores = scores
    self.empty_list = empty_list
    self.address = address
    self.orders = orders

  @classmethod
  def fromJson(cls, *args):
    if len(args) == 0:
      return
    _dict = json.loads(args[0]) if isinstance(args[0], str) else args[0]
    if isinstance(_dict, tuple):
      return cls(
        user_id = _dict[0] if len(_dict) > 0 else None,
        # …
      )
    else:
      return cls(
        user_id = _dict.get('user_id'),
        # …
      )

  def toJson(self):
    return {
      'user_id': self.user_id,
      'user_name': self.user_name,
      'is_vip': self.is_vip,
      'balance': self.balance,
      'tags': self.tags,
      'scores': self.scores,
      'empty_list': [element.toJson() for element in self.empty_list if element],
      'address': self.address.toJson() if self.address is not None else None,
      'orders': [element.toJson() for element in self.orders if element],
    }

  def toString(self):
    return json.dumps(self.toJson(), indent=2, ensure_ascii=False)


class EmptyList(object):
  def __init__(self):
    pass

  # fromJson / toJson / toString 同模式

# Geo / Address / Items / Orders 同模式
```

CLI 调用：
```bash
json2bean py -i user.json -o user.py    # 注：v1 不支持 py，留 v2
```

> v1 不暴露 `py` 子命令，仅 plugin 内可用。如需开放，加一行 picocli 子命令即可。

### 9.6 Kotlin（Map）

```kotlin
data class User (
    val user_id: Int?,
    val user_name: String?,
    val is_vip: Boolean?,
    val balance: Double?,
    val tags: List<String>?,
    val scores: List<List<Int>>?,
    val empty_list: List<EmptyList>?,
    val address: Address?,
    val orders: List<Orders>?,
)

class EmptyList

data class Geo (
    val lat: Double?,
    val lng: Double?,
)

// Orders / Address / Items 同模式
```

> Map 模式保留原始 JSON key（snake_case），与其它 Kt 模式不同。这是设计意图：Map 模式假定调用方手工映射，不引入序列化注解。

CLI 调用：
```bash
json2bean kt -i user.json -o User.kt --dep none
```

### 9.7 Kotlin（Gson）

```kotlin
data class User (
    @SerializedName("user_id") val userId: Int?,
    @SerializedName("user_name") val userName: String?,
    @SerializedName("is_vip") val isVip: Boolean?,
    @SerializedName("balance") val balance: Double?,
    @SerializedName("tags") val tags: List<String>?,
    @SerializedName("scores") val scores: List<List<Int>>?,
    @SerializedName("empty_list") val emptyList: List<EmptyList>?,
    @SerializedName("address") val address: Address?,
    @SerializedName("orders") val orders: List<Orders>?,
)

class EmptyList

data class Geo (
    @SerializedName("lat") val lat: Double?,
    @SerializedName("lng") val lng: Double?,
)

// Orders / Address / Items 同模式
```

CLI 调用：
```bash
json2bean kt -i user.json -o User.kt --dep gson      # 默认风格
```

### 9.8 Kotlin（FastJson）

```kotlin
data class User (
    @JSONField(name = "user_id") val userId: Int?,
    @JSONField(name = "user_name") val userName: String?,
    @JSONField(name = "is_vip") val isVip: Boolean?,
    @JSONField(name = "balance") val balance: Double?,
    @JSONField(name = "tags") val tags: List<String>?,
    @JSONField(name = "scores") val scores: List<List<Int>>?,
    @JSONField(name = "empty_list") val emptyList: List<EmptyList>?,
    @JSONField(name = "address") val address: Address?,
    @JSONField(name = "orders") val orders: List<Orders>?,
)

class EmptyList

// Geo / Orders / Address / Items 同模式
```

CLI 调用：
```bash
json2bean kt -i user.json -o User.kt --dep fastjson
```

### 9.9 Java（Gson 风格，v1 新写）

JavaJsonGenerator 不存在；下面是 v1 golden target，逻辑紧贴 `KtGsonGenerator`（同样的 camelCase 字段、`@SerializedName` 注解、空类降级、2D 数组、BigDecimal/Long 防御）。

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

CLI 调用：
```bash
json2bean java -i user.json -o User.java
```

JavaJsonGenerator 实现约束：

- 包装类型（`Integer/Long/Double/Boolean`）而非原始类型，与 Gson 反序列化行为对齐（缺字段返回 null 而不是 0）
- `public` 字段而非 getter/setter，与 KtGson 的 `val` 直暴露对齐
- 空类降级为普通 `class`（无 record/data class 概念，Java 普通 class 本就允许空体）
- 嵌套类目前**平铺**在同一文件（仿 Kt 三件套），不做 nested static class

## 10. 修复记录（实跑暴露的 bug）

本设计阶段实跑 fixture 时修复了 9 处既有 bug + 1 处提前重构（KT `psiDir` 可空化，为 Phase 2 铺路），全部已在 master 分支落地：

| # | 文件 | 问题 | 修复 |
|---|---|---|---|
| 1 | `TsJsonGenerator.kt` | 2D 数组触发 `ClassCastException` | 补 2D 分支，对齐 Dart |
| 2 | `DartJsonGenerator.kt` | `BigDecimal` 浮点被当 String | `getType` / `getParseType` 加 `BigDecimal` |
| 3 | `TsJsonGenerator.kt` | 同 #2 | `getType` 加 `BigDecimal` |
| 4 | `PythonJsonGenerator.kt` | 同 #2 | `getParseType` 加 `BigDecimal` |
| 5 | `PythonJsonGenerator.kt` | 基本类型 list 漏写 toJson 字段（`tags` 在 toJson 里消失） | 在 primitive list 分支补 `toJsonMethod.append` |
| 6 | `PythonJsonGenerator.kt` | 空字段类的 `__init__` 函数体为空（Python SyntaxError） | `initMethod` 为空时补 `pass` |
| 7 | `Kt{Map,Gson,FastJson}Generator.kt` | 2D 数组触发 `ClassCastException` | 补 2D 分支，对齐 Dart |
| 8 | `Kt{Map,Gson,FastJson}Generator.kt` | 空 `data class X ()` 编译错误 | 空字段降级为 `class X` |
| 9 | `Kt{Map,Gson,FastJson}Generator.kt`、`Dart/Python` 各 generator | 不识别 `Long` 类型（大整数兜底） | 加 `Long` 类型分支 |
| 10 | `Kt{Map,Gson,FastJson}Generator.kt` | `psiDir` 强非空，`toString()` 路径不可脱 PSI 调用 | 改为 `PsiDirectory?`，`generate()` 早返回 |

`runGenerators` Gradle 任务（`src/test/kotlin/RunGenerators.kt` + `src/test/java/TestHelpers.java`）保留在仓库内，作为后续 spec 更新和回归排查工具。

## 11. 已知限制（v1 不修）

- **关键字字段未转义**：JSON key 若为目标语言关键字（如 Dart 的 `class`/`new`/`extends`，Java 的 `class`/`int`），生成代码不合法。`JsonHelper.KEYS` 仅覆盖 Dart 原始类型名（`num/int/String/double/bool`）。修复成本：每种语言一份 keyword table + `prefix()` 加守卫。v2 议题。
- **空数组 → 空类**：JSON 中 `[]` 会被推断为对象数组，生成空 `EmptyList` 类（Dart/Java/Kt 已修为合法的空 class，Python 已补 `pass`，TS interface 本就允许空）。语义上仍是噪音。建议用户在源 JSON 提供非空示例。v2 可加 `--treat-empty-as-any` flag 改输出为 `List<Any>?`。
- **TS 字段名不 camelCase**：保留 JSON 原 key。如要切到 camelCase，加 `--key-camel`。v2。
- **MapKt 字段名不 camelCase**：同上，但这是 MapKt 模式的设计意图（无注解时 1:1 映射 JSON key），不算 bug。
- **嵌套数字精度**：fastjson2 在 `LinkedHashMap` 目标下把小数解析为 `BigDecimal`，整数视范围为 `Int`/`Long`。已在 #2-#4、#9 中防御，但极大/极小数边界值未覆盖。
- **sqlite DAO 不入 CLI**：第一版 `--sqlite` 仅控制 bean 类的 sqlite 字段；DAO 文件留 v2。

## 12. 工作分解（不展开步骤，留给 writing-plans）

```
Phase 1  Gradle 多模块脚手架 + 文件物理迁移（git mv 保历史）
Phase 2  core 统一 API（JsonGenerator/GeneratedFile/GenerateRequest），收敛现有 4 个 Kt/Dart/TS/Python 生成器
Phase 3  JavaJsonGenerator (Gson) 新增 + 全量 golden 测试（覆盖第 9 节 fixture）
Phase 4  plugin 端 4 个文件改造（GeneratorHelper / JsonBeanDialog onGenerate / onGenerateJavaOrKt / PreViewDialog），手测跑通 IDE
Phase 5  CLI 模块（picocli 子命令 + IO + 集成测）
Phase 6  shadowJar + README "CLI Usage" + GitHub Release 草稿
```
