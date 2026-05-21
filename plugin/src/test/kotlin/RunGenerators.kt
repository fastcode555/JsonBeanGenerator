import com.awesome.plugins.json2bean.generators.DartJsonGenerator
import com.awesome.plugins.json2bean.generators.PythonJsonGenerator
import com.awesome.plugins.json2bean.generators.TsJsonGenerator
import com.awesome.plugins.json2bean.generators.ktgenerators.KtFastJsonGenerator
import com.awesome.plugins.json2bean.generators.ktgenerators.KtGsonGenerator
import com.awesome.plugins.json2bean.generators.ktgenerators.MapKtJsonGenerator
import com.intellij.psi.PsiDirectory

/**
 * 调试用入口：把一个复杂 JSON 喂给 Dart / TS / Python 三个不依赖 PSI 的生成器，
 * 打印输出供 spec 文档采纳为 example。
 *
 * 不在这里调 Kt 三件套（MapKt / KtGson / KtFastJson），因为它们 generate() 依赖
 * PsiDirectory；但它们的 toString() 是无 PSI 依赖的，调用方式是
 *   KtGsonGenerator(json, "User", "", "", FAKE_PSI).toString()
 * —— 暂时通过 reflection 跳过 psiDir 不现实，下一步会在 RunKtGenerators 里
 * 把 toString() 路径单独跑（构造时传 null 强转，仅用于 toString()）。
 */
const val FIXTURE_JSON = """
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
"""

private fun banner(title: String) {
    println()
    println("========== $title ==========")
}

fun main() {
    val json = FIXTURE_JSON.trim()
    val className = "User"

    banner("Dart (non-split)")
    println(
        DartJsonGenerator(
            content = json,
            fileName = className,
            extendsClass = "",
            implementClass = "",
            sqliteSupport = false,
            primaryKey = "",
            needClone = false,
            splitGFile = false,
        ).toString()
    )

    banner("Dart (split-g)")
    println(
        DartJsonGenerator(
            content = json,
            fileName = className,
            extendsClass = "",
            implementClass = "",
            sqliteSupport = false,
            primaryKey = "",
            needClone = true,
            splitGFile = true,
        ).toString()
    )

    banner("TypeScript")
    println(
        TsJsonGenerator(
            content = json,
            fileName = className,
            extendsClass = "",
            implementClass = "",
        ).toString()
    )

    banner("Python")
    println(
        PythonJsonGenerator(
            content = json,
            fileName = className,
            extendsClass = "",
            implementClass = "",
        ).toString()
    )

    // KT 三件套：psiDir 已改为可空，CLI/测试路径走 toString() 不解引用
    banner("Kotlin (Map)")
    println(
        MapKtJsonGenerator(json, className, "", "", null).toString()
    )

    banner("Kotlin (Gson)")
    println(
        KtGsonGenerator(json, className, "", "", null).toString()
    )

    banner("Kotlin (FastJson)")
    println(
        KtFastJsonGenerator(json, className, "", "", null).toString()
    )
}
