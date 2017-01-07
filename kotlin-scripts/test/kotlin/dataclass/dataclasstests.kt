package dataclass

import com.squareup.moshi.Moshi
import io.kotlintest.specs.StringSpec
import newLine
import okSink
import okSource
import testFile
import java.io.File


inline fun <reified T : Any> tryParsing(json: String) {
    println("""
Source
-----
$json

After parsing / unparsing
------
"""
)
    val moshi = Moshi.Builder().build()
    val adapter = moshi.adapter(T::class.java)
    val parsed = adapter.fromJson(json)
    val toJson = adapter.toJson(parsed)
    println(toJson)
}
val items = listOf(
    testFile("json/0_simple.json"),
    testFile("json/1_list.json"),
    testFile("json/2_objet.json"),
    testFile("json/3_object.json"),
    testFile("json/4_complex.json")
)
val classes = items.mapIndexed { i, file ->
    val className = if (i in listOf(1, 4)) "List<T1>" else "T0"
    Pair(file, className)
}.toMap()

class GenerateDataClass : StringSpec() { init {
    var i = 0

    for (file in items) {
        "From ${file.name}..." {
            val json = file.readText()
            val code = generateDataclassCode(file.okSource())
            file.kotlinOutput().okSink().use { sink ->
                val intro = """
package Gen${i++}
import dataclass.tryParsing

fun main(args: Array<String>) {
    tryParsing<${classes[file]}>(json)
}
                """
                sink
                    .writeUtf8(intro)
                    .writeUtf8("val json = \"\"\" ")
                    .writeUtf8(json)
                    .writeUtf8("\"\"\"")
                    .newLine()
                    .writeUtf8(code)
            }
        }
    }
}
}


private fun File.kotlinOutput(): File
    = File("test/kotlin/dataclass/generated_${nameWithoutExtension}.kt")
