import com.awesome.generators.DartJsonGenerator
import java.io.File


fun main(args: Array<String>) {
    val fileName = "orderPage_all.json"
    val content = File(fileName).readText().trim()
    print(DartJsonGenerator(content, fileName.replace(".json", "").toUpperCamel()).toJson())
}
