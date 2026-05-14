# Dart `.g.dart` 拆分生成 — 设计文档

- 日期：2026-05-14
- 目标范围：`DartJsonGenerator` 及其调用链
- 动机：当前 Dart 实体类把 `fromJson` / `toJson` / `clone` 方法体都内联在主类中，类越大、AI 读取消耗的 token 越多。沿用 `json_serializable` 的 `part` / `part of` 惯例把这些方法体抽到 `.g.dart` 中，主文件只保留字段、构造、瘦委托和无法移出的类绑定成员，从而显著缩小主文件体积。

## 1. 决策清单

| # | 决策 | 选择 |
|---|---|---|
| 1 | 拆分模式 | `part` / `part of`（`json_serializable` 风格） |
| 2 | 哪些方法移出 | `fromJson`、`toJson`、`clone`（类绑定成员 `==`/`hashCode`/`primaryKeyAndValue` 必须留在类内） |
| 3 | UI | `JsonBeanDialog` 新增「拆分 .g.dart 文件」复选框，opt-in，仅 Dart 模式可见 |
| 4 | 与 sqlite 关系 | 可同时勾选；主类保留 sqlite 必需的类绑定成员，DAO 文件照旧生成 |
| 5 | 预览 | `PreViewDialog` 显示两段文本拼接，用 `// ===== file.dart =====` 注释分隔 |

## 2. 生成内容形态

### 2.1 主文件 `<name>.dart`（拆分开启时）

```dart
import 'dart:convert';
import 'package:json2dart_safe/json2dart.dart';
// sqlite 开启时：import 'package:json2dart_db/json2dart_db.dart';

part '<name>.g.dart';

class User {
  String? name;
  List<Address>? addresses;

  User({
    required this.name,
    this.addresses,
  });

  factory User.fromJson(Map json) => _$UserFromJson(json);
  Map<String, dynamic> toJson() => _$UserToJson(this);
  User clone() => _$UserClone(this);   // 仅 needClone=true 时存在

  @override
  String toString() => jsonEncode(toJson());
}
```

`toString()` 留在主类中（一行）。`clone` 的瘦委托仅在 `needClone=true` 时存在。

### 2.2 Part 文件 `<name>.g.dart`

```dart
part of '<name>.dart';

User _$UserFromJson(Map json) => User(
      name: json.asString('name'),
      addresses: json.asList<Address>('addresses', Address.fromJson),
    );

Map<String, dynamic> _$UserToJson(User i) => {
      'name': i.name,
      'addresses': i.addresses?.map((v) => v.toJson()).toList(),
    };

User _$UserClone(User i) => User(
      name: i.name,
      addresses: i.addresses?.map((v) => v.clone()).toList(),
    );

// 嵌套类的 _$AddressFromJson / _$AddressToJson / _$AddressClone …
```

- Part 文件不写 `import`，因为 `part of` 直接继承主文件的所有 import。
- 顶层函数命名固定为 `_$<UniqueClassName>FromJson` / `_$<UniqueClassName>ToJson` / `_$<UniqueClassName>Clone`，`UniqueClassName` 复用现有 `generateUniqueClassName()` 的结果（同名嵌套会变成 `Userx`，函数名也跟随变成 `_$UserxFromJson`）。

### 2.3 Sqlite 主键场景

主类在以上基础上额外保留：

```dart
class User with BaseDbModel {
  // …字段 + 构造 + 三个委托…

  @override
  Map<String, dynamic> primaryKeyAndValue() => {"id": id};

  @override
  int get hashCode => id?.hashCode ?? super.hashCode;

  @override
  bool operator ==(Object other) {
    if (other is User && id != null) return other.id == id;
    return super == other;
  }
}
```

`_$UserToJson` 是 sqlite 变体，包含 `'id': i.id`；`@override` 写在主类的瘦委托上而非 part 函数上。

## 3. 代码改动

### 3.1 `DartJsonGenerator.kt`

- 构造函数新增参数 `splitGFile: Boolean`。
- 新增内嵌数据结构：
  ```kotlin
  data class Output(
      val mainContent: String,
      val partContent: String?,      // splitGFile=false 时为 null
      val mainFileName: String,      // e.g. "user.dart"
      val partFileName: String?,     // e.g. "user.g.dart"
  )
  fun generate(): Output
  ```
- `parseJson(...)` 中原有的五个 `StringBuilder` 不变其内容生成逻辑，但拆分目标：
  - 类壳 / 字段 / 构造 / 瘦委托 / `toString` / sqlite 类绑定成员 → main builder
  - `_$XxxFromJson` / `_$XxxToJson` / `_$XxxClone` 方法体 → part builder
- 主文件头部插入 `part '<name>.g.dart';`（在 import 之后、首个 `class` 之前）。
- **文件名 vs 类名约定**：主文件名直接使用用户输入 `tvClassField.text`（如 `user_profile`，保留原始大小写/下划线），part 文件名为 `<tvClassField.text>.g.dart`；类名仍走 `fileName.toUpperCamel()`（如 `UserProfile`）。`part` 指令中的字符串必须与磁盘上的实际 part 文件名严格一致。
- `toString()` 保持向后兼容：
  - `splitGFile=false`：返回单文件内容（与今天一致）。
  - `splitGFile=true`：返回 `// ===== file.dart =====\n…\n\n// ===== file.g.dart =====\n…`，仅供预览。

### 3.2 `GeneratorHelper.kt`

- 现有 `json2Bean(...)` 保留作为预览路径。
- 新增 `dartGenerate(...): DartJsonGenerator.Output`，专供写文件路径调用。
- 两者共享同一个 `DartJsonGenerator` 实例构造逻辑。
- `splitGFile` 仅传给 Dart 生成器；TS / Python / Kt 生成器不变。

### 3.3 `JsonBeanDialog.kt` + `JsonBeanDialog.form`

- 新增 `var cbSplitGFile: JCheckBox?`，form 文件同步新增控件，label 建议「拆分 .g.dart 文件」（与现有 Clone 复选框风格一致）。
- 可见性：仅 `fileType == ".dart"` 时显示；切语言时随 `mMethodPanel` 一起隐藏。
- 持久化：参照 `rbClone` 模式，键 `PluginProps.splitGFile`，默认 `false`。
- `onGenerate()` 中 Dart 路径走 `GeneratorHelper.dartGenerate(...)`：
  - 写主文件 `<name>.dart`。
  - 若 `output.partContent != null`，写 `<name>.g.dart`。
  - sqlite DAO 写入逻辑不变。
- 存在性检查只看主文件 `.dart`，与今天的行为保持一致（避免「主文件不在但 g 文件残留」成为阻塞）。
- 非 Dart 路径继续走 `json2Bean()` 单文件写。
- 预览路径 `onPreView()` 不动，继续使用 `json2Bean()`。

### 3.4 `PluginProps.kt`

- 新增常量 `const val splitGFile = "splitGFile"`。

### 3.5 不受影响的代码

- `FromJsonGenerateAction` / `ToJsonGenerateAction` / `ToAndFromJsonGenerateAction` / `CloneJsonGenerateAction`（这些是给已有类追加方法，不走整体生成）。
- `TsJsonGenerator` / `PythonJsonGenerator` / 各 Kt 生成器。
- `DartDataBaseGenerator`（sqlite DAO 文件）。
- `PreViewDialog`。

## 4. 验证清单

| 场景 | 期望 |
|---|---|
| 普通 JSON + 不拆分 | 单文件，与改造前字节级一致（或仅有空白差异） |
| 普通 JSON + 拆分 | 主文件含 `part '<n>.g.dart';`；part 文件含 `part of '<n>.dart';` |
| 嵌套对象 + 拆分 | 每个嵌套类在 part 文件中都有 `_$XxxFromJson/ToJson/Clone` |
| 同名嵌套触发 `Userx` + 拆分 | part 函数名同步为 `_$UserxFromJson` 等，瘦委托引用一致 |
| sqlite + 拆分 | 主类保留 `with BaseDbModel`、`==`、`hashCode`、`primaryKeyAndValue`；DAO 文件照常生成；`_$UserToJson` 含主键 |
| 不勾选 clone + 拆分 | part 文件无 `_$XxxClone`，主类无 clone 委托 |
| 二维数组 + 拆分 | part 文件中 `_$XxxFromJson` 使用 `json.asArray2d<T>(...)` |
| 切换到 Python/TS/Kt | 复选框隐藏，行为不变 |
| 复选框持久化 | 重开 Dialog 复选框状态恢复 |

生成产物用 `dart analyze` 跑一遍，确认 `part` 指令合法、函数引用解析正常、类型签名无误。

## 5. 越界范围（YAGNI）

- 不引入构建期代码生成（不使用 build_runner）—— 我们的输出本身就是已生成代码。
- 不重写其他生成器（TS / Python / Kt）。
- 不改 DAO 生成、不改预览对话框布局。
- 不为 4 个增量 Action（`FromJsonGenerateAction` 等）添加拆分支持 —— 这些场景是给已有类「补丁式」追加方法，不在本次目标范围。
