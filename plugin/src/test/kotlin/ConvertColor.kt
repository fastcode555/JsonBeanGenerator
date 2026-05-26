import com.awesome.plugins.stringassociate.processor.StringHelper
import com.awesome.utils.regexOne

fun main() {
    val text = "static const Color transparent = Color(0x00000000)\n" +
            "static const Color black = Color(0xFF000000)\n" +
            "static const Color black87 = Color(0xDD000000)\n" +
            "static const Color black54 = Color(0x8A000000)\n" +
            "static const Color black45 = Color(0x73000000)\n" +
            "static const Color black38 = Color(0x61000000)\n" +
            "static const Color black26 = Color(0x42000000)\n" +
            "static const Color black12 = Color(0x1F000000)\n" +
            "static const Color white = Color(0xFFFFFFFF)\n" +
            "static const Color white70 = Color(0xB3FFFFFF)\n" +
            "static const Color white60 = Color(0x99FFFFFF)\n" +
            "static const Color white54 = Color(0x8AFFFFFF)\n" +
            "static const Color white38 = Color(0x62FFFFFF)\n" +
            "static const Color white30 = Color(0x4DFFFFFF)\n" +
            "static const Color white24 = Color(0x3DFFFFFF)\n" +
            "static const Color white12 = Color(0x1FFFFFFF)\n" +
            "static const Color white10 = Color(0x1AFFFFFF)"

    val lines = text.split("\n")
    val builder = StringBuilder()
    for (line in lines) {
        val name = line.regexOne("(?<=Color ).*?(?= \\=)")
        var color = line.regexOne("(?<=Color\\(0x).*?(?=\\))")?.lowercase()
        if (color != null && color.startsWith("ff") && color.length == 8) {
            color = color.substring(2, color.length)
        }
        builder.append("\"#$color\"->\"$name\"\n")
    }
    println(builder)
}
