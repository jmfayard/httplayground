import io.kotlintest.specs.StringSpec

data class Room(val number: Int, val encryped: String, val checksum: String)

class Day4 : StringSpec() { init {

    "Parsing" {
        val room = "aaaaa-bbb-z-y-x-123[abxyz]".asRoom()
        room shouldBe Room(123, "aaaaa-bbb-z-y-x", "abxyz")
    }

    "Calculing checksum" {
        val room = "aaaaa-bbb-z-y-x-123[abxyz]".asRoom()
        room.realChecksum() shouldBe room.checksum
    }

    val solution = 409147
    "Finding correct checksums" {
        val file = resourceFile("day4.txt")
        val rooms = file.readLines().map(String::asRoom)
        val realRooms = rooms.filter { room ->
            room.checksum == room.realChecksum()
        }
        val count = realRooms.sumBy { it.number }
        count shouldBe solution
    }

    "Ascii and cie" {
        'a'.toIndex() shouldBe 0
        'y'.rotate(3) shouldBe 'b'
        '-'.rotate(3) shouldBe ' '
    }

    val solution2 = 991
    "Decyphe => solution $solution2" {
        val file = resourceFile("day4.txt")
        val rooms = file.readLines().map(String::asRoom).filter { room ->
            room.checksum == room.realChecksum()
        }
        rooms.forEachIndexed { i, r ->
            "${r.decypher()} <=== $r".debug("#$i")
        }
        val result = rooms.find { r -> r.decypher().contains("northpole object storage") }
        result!!.number shouldBe solution2
    }
}
}


private fun String.asRoom(): Room {
    require(all { c: Char -> c in allowedChars }) { "Invalid string $this" }
    val startNumber = lastIndexOf("-")
    val startChecksum = lastIndexOf("[")
    val number = substring(startNumber + 1, startChecksum).toInt()
    return Room(number, encryped = substring(0, startNumber), checksum = substring(startChecksum + 1, lastIndex))
}


private fun Room.decypher(): String = buildString {
    encryped.forEach { c ->
        append(c.rotate(number))
    }
}


val letters = ('a'..'z')

private fun Char.rotate(number: Int) = when (this) {
    '-' -> ' '
    !in letters -> TODO("Invalid char $this")
    else -> (toIndex() + number).mod(26).toLowerCase()
}

private fun Int.toLowerCase(): Char = (this + 'a'.toInt()).toChar()

private fun Char.toIndex(): Int = this.toInt() - 'a'.toInt()

fun Room.realChecksum(): String {
    val occurences = mutableMapOf<Char, Int>()
    encryped.forEach { c ->
        if (c == '-') return@forEach
        occurences[c] = 1 + (occurences[c] ?: 0)
    }
    val indices = ('a'..'z').sortedByDescending { c -> occurences[c] }.take(5)
    return indices.asString()
}


private val allowedChars = ('0'..'9') + ('a'..'z') + listOf('[', ']', '-')