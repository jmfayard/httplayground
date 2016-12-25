import io.kotlintest.specs.StringSpec
import kotlin.test.fail

/***
 *
 * --- Day 8: Two-Factor Authentication ---

You come across a door implementing what you can only assume is an implementation of two-factor authentication after a long game of requirements telephone.

To get past the door, you first swipe a keycard (no problem; there was one on a nearby desk).
Then, it displays a code on a little screen, and you type that code on a keypad.
Then, presumably, the door unlocks.

Unfortunately, the screen has been smashed. After a few minutes, you've taken everything apart and figured out how it works.
Now you just have to work out what the screen would have displayed.

The magnetic strip on the card you swiped encodes a series of instructions for the screen; these instructions are your puzzle input.
The screen is 50 pixels wide and 6 pixels tall, all of which start off, and is capable of three somewhat peculiar operations:

rect AxB turns on all of the pixels in a rectangle at the top-left of the screen which is A wide and B tall.

rotate row y=A by B shifts all of the pixels in row A (0 is the top row) right by B pixels. Pixels
that would fall off the right end appear at the left end of the row.

rotate column x=A by B shifts all of the pixels in column A (0 is the left column) down by B pixels.
Pixels that would fall off the bottom appear at the top of the column.
For example, here is a simple sequence on a smaller screen:

rect 3x2 creates a small rectangle in the top-left corner:

###....
###....
.......
rotate column x=1 by 1 rotates the second column down by one pixel:

#.#....
###....
.#.....
rotate row y=0 by 4 rotates the top row right by four pixels:

....#.#
###....
.#.....
rotate column x=1 by 1 again rotates the second column down by one pixel, causing the bottom pixel to wrap back to the top:

.#..#.#
#.#....
.#.....

As you can see, this display technology is extremely powerful, and will soon dominate the tiny-code-displaying-screen market.

That's what the advertisement on the back of the display tries to convince you, anyway.

There seems to be an intermediate check of the voltage used by the display: after you swipe your card, if the screen did work, how many pixels should be lit?



--- Part Two ---

You notice that the screen is only capable of displaying capital letters; in the font it uses, each letter is 5 pixels wide and 6 tall.

After you swipe your card, what code is the screen trying to display?




 */
sealed class Lcd {
    class Rect(val cols: Int, val rows: Int): Lcd() {
        override fun toString(): String = "rect ${cols}x${rows}"
    }
    class RotateRow(val row: Int, val by: Int) : Lcd(){
        override fun toString(): String = "rotate row y=$row by $by"
    }
    class RotateCol(val col: Int, val by: Int) : Lcd(){
        override fun toString(): String = "rotate column x=$col by $by"
    }
    class Invalid(val line: String) : Lcd() {
        override fun toString(): String = "Invalid($line)"
    }
}


class Day8 : StringSpec() {init {

    "Parsing" {
        val instructions = resourceFile("day8.txt").readLines()
        instructions.forEach { line ->
            line.asLcd().toString() shouldBe line
        }
    }

    "Screens" {
        val five = listOf(1, 2, 3, 4, 5)
        five.rotate(7) shouldBe listOf(4, 5, 1, 2, 3)
        five.rotate(5) shouldBe five


        val matrix = MutableMatrix(cols = 7, rows = 3) { col, row -> false }.print()
        matrix.mutate("rect 3x2".asLcd())
        matrix.mutate("rotate column x=1 by 1".asLcd())
        matrix.mutate("rotate row y=0 by 4".asLcd())
        matrix.mutate("rotate column x=1 by 1".asLcd())
        var lights = 0
        matrix.traverseRows { col, row, b ->  if (b) lights++ }
        lights shouldBe 6
    }

    val solution = 123
    "Solution" {
        println()
        println("==== PUZZLE ====")
        val instructions = resourceFile("day8.txt").readLines().map(String::asLcd)
        val matrix = MutableMatrix(cols = 50, rows = 6) { col, row -> false }.print()
        instructions.forEach { instruction ->
            matrix.mutate(instruction)
        }
        var lights = 0
        matrix.traverseRows { col, row, b ->  if (b) lights++ }
        lights shouldBe solution
    }

}}

private fun MutableMatrix<Boolean>.mutate(lcd: Lcd) : MutableMatrix<Boolean> {
    when (lcd) {
        is Lcd.Rect -> this.fill { col: Int, row: Int, value: Boolean ->
            if (col < lcd.cols && row < lcd.rows) {
                true
            } else {
                value
            }
        }
        is Lcd.RotateRow -> {
            val list = mutableListOf<Boolean>()
            traverseRows { col, row, b -> if (row == lcd.row) list+=b }
            val rotated : List<Boolean> = list.rotate(lcd.by)
            fill { col, row, b ->
                if (row == lcd.row) {
                    rotated[col]
                } else {
                    b
                }
            }
        }
        is Lcd.RotateCol -> {
            val list = mutableListOf<Boolean>()
            traverseRows { col, row, b -> if (col == lcd.col) list+=b }
            val rotated : List<Boolean> = list.rotate(lcd.by)
            fill { col, row, b ->
                if (col == lcd.col) {
                    rotated[row]
                } else {
                    b
                }
            }

        }
        is Lcd.Invalid -> fail(lcd.toString())
    }
    println(lcd)
    return this.print()
}

private fun <E> List<E>.rotate(by: Int): List<E> {
    require(by >= 0)
    val limit = size - by.mod(size)
    return subList(limit, size) + subList(0, limit)
}

public fun MutableMatrix<Boolean>.print() : MutableMatrix<Boolean> {
    println((0..(cols-1)).map { it.mod(10) }.joinToString(separator = "", prefix = "  "))
    traverseRows { col, row, b ->
        if (col == 0) print("$row")
        if (col.mod(5) == 0) print(" ")
        print(if (b) '#' else '.')
        if (col == cols -1) println()
    }
    println()
    return this
}


private fun String.asLcd() : Lcd = parseLcd(this)
fun parseLcd(line: String) : Lcd {
    val lex = line.split(' ', '=')
    if (lex.first() == "rect") {
        val list = lex.last().split('x')
        return Lcd.Rect(cols = list[0].toInt(), rows = list[1].toInt())
    } else if (lex.first() == "rotate") {
        val (rowOrCol, by) = lex[3].toInt() to lex[5].toInt()
        return when (lex[1]) {
            "row" ->  Lcd.RotateRow(rowOrCol, by)
            "column" -> Lcd.RotateCol(rowOrCol, by)
            else -> Lcd.Invalid(line)
        }
    } else {
        return Lcd.Invalid(line)
    }
}
