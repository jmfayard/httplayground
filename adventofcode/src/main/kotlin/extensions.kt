import java.io.File

fun <T> T.debug(name: String): T {
    println("DEBUG: ${name} = ${toString()}")
    return this
}

/** Transform a list of Char into a UTF8 String **/
fun  <T> Iterable<T>.asString(): String = joinToString(separator = "")

/** Gives a valid file in src/main/resources **/
fun resourceFile(path: String): File {
    val fileName = "src/main/resources/$path"
    val file = File(fileName)
    assert(file.canRead()) { "Cannot read testResourceFile($path) = ${file.absolutePath}" }
    return file
}