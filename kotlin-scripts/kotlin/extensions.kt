import okio.BufferedSink
import okio.BufferedSource
import okio.Okio
import org.intellij.lang.annotations.Language
import org.zeroturnaround.exec.ProcessExecutor
import java.io.File

fun <T> T.debug(name: String): T {
    println("DEBUG: ${name} = ${toString()}")
    return this
}
fun <T> List<T>.printList(name: String){
    forEachIndexed { i, t ->
        println("$name[$i] : $t")
    }
}

fun BufferedSink.newLine() = writeUtf8("\n")


fun File.okSource(): BufferedSource {
    require(canRead()) { "Cannot read from $absolutePath" }
    return Okio.buffer(Okio.source(this))
}

fun File.okSink(): BufferedSink {
    val ok = if (exists()) canWrite() else parentFile.canWrite()
    require(ok) { "Cannot write to $absolutePath" }
    return Okio.buffer(Okio.sink(this))
}

fun File.okAppendingSink(): BufferedSink = Okio.buffer(Okio.appendingSink(this))


fun osxOpenFile(file: File)  {
    println("$ /usr/bin/open ${file.absolutePath}")
    require(file.canRead()) { System.exit(1); "ERROR File not found" }
    val errorValue = ProcessExecutor().command("/usr/bin/open", file.absolutePath).execute().exitValue
    if (errorValue!=0) { "Process exited with error: $errorValue" }
}

public fun printAsTable(vararg pairs: Pair<Any, Any>){
    if (pairs.isEmpty()) return
    val length = 3 + pairs.map { key -> key.first.toString().count() }.max()!!
    val format = "%-${length}s %s"
    for ((first, second) in pairs) {
        println(String.format(format, first, second))
    }
}

/** Gives a valid file in src/main/resources **/
public fun resourceFile(@Language("File") path: String): File {
    val fileName = "src/main/resources/$path"
    val file = File(fileName)
    assert(file.canRead()) { "Cannot read testResourceFile($path) = ${file.absolutePath}" }
    return file
}

/** Gives a valid file in src/test/resources **/
public fun testFile(@Language("File") path: String): File {
    val fileName = "test/resources/$path"
    val file = File(fileName)
    assert(file.canRead()) { "Cannot read testResourceFile($path) = ${file.absolutePath}" }
    return file
}