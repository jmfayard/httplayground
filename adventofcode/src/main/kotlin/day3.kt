import io.kotlintest.specs.StringSpec
import java.io.File

/*

Now that you can think clearly, you move deeper into the labyrinth of hallways and office furniture that makes up this part of Easter Bunny HQ.
This must be a graphic design department; the walls are covered in specifications for triangles.

Or are they?

The design document gives the side lengths of each triangle it describes, but... 5 10 25? Some of these aren't triangles.
You can't help but mark the impossible ones.

In a valid triangle, the sum of any two sides must be larger than the remaining side.
For example, the "triangle" given above is impossible, because 5 + 10 is not larger than 25.

--- Part Two ---

Now that you've helpfully marked up their design documents, it occurs to you
that triangles are specified in groups of three vertically. Each set of three
numbers in a column specifies a triangle. Rows are unrelated.

For example, given the following specification, numbers with the same hundreds digit would be part of the same triangle:

101 301 501
102 302 502
103 303 503
201 401 601
202 402 602
203 403 603

In your puzzle input, and instead reading by columns, how many of the listed triangles are possible?

*/


class Day3 : StringSpec() { init {

    "Parsing" {
        "  566  477  376 ".toInts().toTriangle() shouldBe Triangle(376, 477, 566)
        " 1 2 a".toInts().toTriangle() shouldBe null
    }

    "Valid triangles" {
        Triangle(5, 10, 10).isValid() shouldBe true
        Triangle(5, 10, 15).isValid() shouldBe false
        Triangle(5, 10, 16).isValid() shouldBe false
    }

    val expected = 1050
    val file = resourceFile("day3.txt")

    "Solution: $expected " {

        var solution = 0
        file.forEachLine { line ->
            val triangle = line.toInts().toTriangle()
            if (triangle?.isValid() ?: false) {
                solution++
            }
        }
        println("Found $solution")
        solution shouldBe expected
    }

    val expected2 = 1921
    "Solutino part 2 : $expected2" {

        val matrix = listOf(
            listOf(0, 3, 6),
            listOf(1, 4, 7),
            listOf(2, 5, 8)
        )

        val triangles = mutableListOf<Triangle>()
        val lines = file.readLines()
        var i = 0
        while (i + 2 < lines.size) {
            val ints = lines[i].toInts()!! + lines[i + 1].toInts()!! + lines[i + 2].toInts()!!
            for (row in matrix) {
                val list = row.map { i -> ints[i] }
                triangles += list.toTriangle()!!
            }
            i += 3
        }

        val solution = triangles.filter(Triangle::isValid).count()
        solution shouldBe expected2

    }

}
}


data class Triangle(val small: Int, val medium: Int, val big: Int) {
    fun isValid(): Boolean = small + medium > big
}

val spaces = Regex("\\s+")
private fun String.toInts(): List<Int>? =
    try {
        trim().split(spaces).map(String::toInt).take(3)
    } catch (e: Exception) {
        null
    }

private fun List<Int>?.toTriangle(): Triangle? =
    this?.let { list ->
        val (a, b, c) = list.sorted()
        Triangle(a, b, c)
    }
