import java.io.File

fun <T> T.debug(name: String): T {
    println("DEBUG: ${name} = ${toString()}")
    return this
}

fun resourceFile(path: String): File {
    val fileName = "src/main/resources/$path"
    val file = File(fileName)
    assert(file.canRead()) { "Cannot read testResourceFile($path) = ${file.absolutePath}" }
    return file
}