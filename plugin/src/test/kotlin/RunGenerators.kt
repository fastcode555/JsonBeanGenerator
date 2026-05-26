import com.awesome.core.generators.DartJsonGenerator
import com.awesome.core.generators.PythonJsonGenerator
import com.awesome.core.generators.TsJsonGenerator
import com.awesome.core.generators.kt.KtFastJsonGenerator
import com.awesome.core.generators.kt.KtGsonGenerator
import com.awesome.core.generators.kt.MapKtJsonGenerator

/**
 * 调试用入口：把一个复杂 JSON 喂给所有生成器，打印输出供 spec 文档采纳为 example。
 * 所有生成器已移入 :core，不再依赖 PSI。
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

    // KT 三件套：psiDir 参数已移除，直接调用 toString()
    banner("Kotlin (Map)")
    println(
        MapKtJsonGenerator(json, className, "", "").toString()
    )

    banner("Kotlin (Gson)")
    println(
        KtGsonGenerator(json, className, "", "").toString()
    )

    banner("Kotlin (FastJson)")
    println(
        KtFastJsonGenerator(json, className, "", "").toString()
    )
}
