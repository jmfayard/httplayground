import io.kotlintest.specs.StringSpec

/***
 *
--- Day 2: Bathroom Security ---

You arrive at Easter Bunny Headquarters under cover of darkness.
However, you left in such a rush that you forgot to use the bathroom!
Fancy office buildings like this one usually have keypad locks on their bathrooms, so you search the front desk for the code.

"In order to improve security," the document you find says, "bathroom codes will no longer be written down.
Instead, please memorize and follow the procedure below to access the bathrooms."

The document goes on to explain that each button to be pressed can be found
by starting on the previous button and moving to adjacent buttons on the keypad:
U moves up, D moves down, L moves left, and R moves right.

Each line of instructions corresponds to one button, starting at the previous button (or, for the first line, the "5" button);

press whatever button you're on at the end of each line. If a move doesn't lead to a button, ignore it.

You can't hold it much longer, so you decide to figure out the code as you walk to the bathroom. You picture a keypad like this:

1 2 3
4 5 6
7 8 9
Suppose your instructions are:

ULL
RRDDD
LURDL
UUUUD

You start at "5" and move up (to "2"), left (to "1"), and left (you can't, and stay on "1"), so the first button is 1.
Starting from the previous button ("1"), you move right twice (to "3") and then down three times (stopping at "9" after two moves and ignoring the third), ending up with 9.
Continuing from "9", you move left, up, right, down, and left, ending with 8.
Finally, you move up four times (stopping at "2"), then down once, ending with 5.
So, in this example, the bathroom code is 1985.

Your puzzle input is the instructions from the document you found at the front desk. What is the bathroom code?


--- Part Two ---

You finally arrive at the bathroom (it's a several minute walk from the lobby so visitors can behold the many
fancy conference rooms and water coolers on this floor) and go to punch in the code.

Much to your bladder's dismay, the keypad is not at all like you imagined it.

Instead, you are confronted with the result of hundreds of man-hours of bathroom-keypad-design meetings:

    1
  2 3 4
5 6 7 8 9
  A B C
    D
You still start at "5" and stop when you're at an edge, but given the same instructions as above, the outcome is very different:

You start at "5" and don't move at all (up and left are both edges), ending at 5.
Continuing from "5", you move right twice and down three times (through "6", "7", "B", "D", "D"), ending at D.
Then, from "D", you move five more times (through "D", "B", "C", "C", "B"), ending at B.
Finally, after five more moves, you end at 3.
So, given the actual keypad layout, the code would be 5DB3.

Using the same instructions in your puzzle input, what is the correct bathroom code?



 */
class Day2 : StringSpec() { init {
    "Matrix" {
        val matrix = KeyPad.simpleKeypad().matrix
        matrix[1][1] shouldBe 1
        matrix[1][3] shouldBe 3
        matrix[3][1] shouldBe 7
    }
    "Moves" {
        val pad = KeyPad.simpleKeypad()

        val myTable = table(
            headers("nb", "x", "y", "left", "right", "up", "down"),
            row(1, 1, 1, 1, 2, 1, 4),
            row(3, 1, 3, 2, 3, 3, 6),
            row(4, 2, 1, 4, 5, 1, 7),
            row(5, 2, 2, 4, 6, 2, 8),
            row(7, 3, 1, 7, 8, 4, 7)
        )
        forAll(myTable) { nb, x, y, left, right, up, down ->
            fun testValue(expected: Int, operation: KeyPad.() -> Unit) {
                pad.row = x
                pad.col= y
                pad.operation()
                pad.value() shouldBe expected
            }
            testValue(left, KeyPad::left)
            testValue(right, KeyPad::right)
            testValue(up, KeyPad::up)
            testValue(down, KeyPad::down)
        }
    }

    "Digicodes" {
        val digicode = Digicode.initial()
        val t = table(
            headers("line", "state"),
            row("ULL", 1),
            row("RRDDD", 9),
            row("LURDL", 8),
            row("UUUUD", 5)
        )
        forAll(t) { line, expected ->
            digicode.readLine(line)
            digicode.found.last() shouldBe expected
        }
    }


    val puzzle = """
    RDLULDLDDRLLLRLRULDRLDDRRRRURLRLDLULDLDLDRULDDLLDRDRUDLLDDRDULLLULLDULRRLDURULDRUULLLUUDURURRDDLDLDRRDDLRURLLDRRRDULDRULURURURURLLRRLUDULDRULLDURRRLLDURDRRUUURDRLLDRURULRUDULRRRRRDLRLLDRRRDLDUUDDDUDLDRUURRLLUDUDDRRLRRDRUUDUUULDUUDLRDLDLLDLLLLRRURDLDUURRLLDLDLLRLLRULDDRLDLUDLDDLRDRRDLULRLLLRUDDURLDLLULRDUUDRRLDUDUDLUURDURRDDLLDRRRLUDULDULDDLLULDDDRRLLDURURURUUURRURRUUDUUURULDLRULRURDLDRDDULDDULLURDDUDDRDRRULRUURRDDRLLUURDRDDRUDLUUDURRRLLRR
    RDRRLURDDDDLDUDLDRURRLDLLLDDLURLLRULLULUUURLDURURULDLURRLRULDDUULULLLRLLRDRRUUDLUUDDUDDDRDURLUDDRULRULDDDLULRDDURRUURLRRLRULLURRDURRRURLDULULURULRRLRLUURRRUDDLURRDDUUDRDLLDRLRURUDLDLLLLDLRURDLLRDDUDDLDLDRRDLRDRDLRRRRUDUUDDRDLULUDLUURLDUDRRRRRLUUUDRRDLULLRRLRLDDDLLDLLRDDUUUUDDULUDDDUULDDUUDURRDLURLLRUUUUDUDRLDDDURDRLDRLRDRULRRDDDRDRRRLRDULUUULDLDDDUURRURLDLDLLDLUDDLDLRUDRLRLDURUDDURLDRDDLLDDLDRURRULLURULUUUUDLRLUUUDLDRUDURLRULLRLLUUULURLLLDULLUDLLRULRRLURRRRLRDRRLLULLLDURDLLDLUDLDUDURLURDLUURRRLRLLDRLDLDRLRUUUDRLRUDUUUR
    LLLLULRDUUDUUDRDUUURDLLRRLUDDDRLDUUDDURLDUDULDRRRDDLLLRDDUDDLLLRRLURDULRUUDDRRDLRLRUUULDDULDUUUDDLLDDDDDURLDRLDDDDRRDURRDRRRUUDUUDRLRRRUURUDURLRLDURDDDUDDUDDDUUDRUDULDDRDLULRURDUUDLRRDDRRDLRDLRDLULRLLRLRLDLRULDDDDRLDUURLUUDLLRRLLLUUULURUUDULRRRULURUURLDLLRURUUDUDLLUDLDRLLRRUUDDRLUDUDRDDRRDDDURDRUDLLDLUUDRURDLLULLLLUDLRRRUULLRRDDUDDDUDDRDRRULURRUUDLUDLDRLLLLDLUULLULLDDUDLULRDRLDRDLUDUDRRRRLRDLLLDURLULUDDRURRDRUDLLDRURRUUDDDRDUUULDURRULDLLDLDLRDUDURRRRDLDRRLUDURLUDRRLUDDLLDUULLDURRLRDRLURURLUUURRLUDRRLLULUULUDRUDRDLUL
    LRUULRRUDUDDLRRDURRUURDURURLULRDUUDUDLDRRULURUDURURDRLDDLRUURLLRDLURRULRRRUDULRRULDLUULDULLULLDUDLLUUULDLRDRRLUURURLLUUUDDLLURDUDURULRDLDUULDDRULLUUUURDDRUURDDDRUUUDRUULDLLULDLURLRRLRULRLDLDURLRLDLRRRUURLUUDULLLRRURRRLRULLRLUUDULDULRDDRDRRURDDRRLULRDURDDDDDLLRRDLLUUURUULUDLLDDULDUDUUDDRURDDURDDRLURUDRDRRULLLURLUULRLUDUDDUUULDRRRRDLRLDLLDRRDUDUUURLRURDDDRURRUDRUURUUDLRDDDLUDLRUURULRRLDDULRULDRLRLLDRLURRUUDRRRLRDDRLDDLLURLLUDL
    ULURLRDLRUDLLDUDDRUUULULUDDDDDRRDRULUDRRUDLRRRLUDLRUULRDDRRLRUDLUDULRULLUURLLRLLLLDRDUURDUUULLRULUUUDRDRDRUULURDULDLRRULUURURDULULDRRURDLRUDLULULULUDLLUURULDLLLRDUDDRRLULUDDRLLLRURDDLDLRLLLRDLDRRUUULRLRDDDDRUDRUULDDRRULLDRRLDDRRUDRLLDUDRRUDDRDLRUDDRDDDRLLRDUULRDRLDUDRLDDLLDDDUUDDRULLDLLDRDRRUDDUUURLLUURDLULUDRUUUDURURLRRDULLDRDDRLRDULRDRURRUDLDDRRRLUDRLRRRRLLDDLLRLDUDUDDRRRUULDRURDLLDLUULDLDLDUUDDULUDUDRRDRLDRDURDUULDURDRRDRRLLRLDLU
    """.trimIndent()

    val solution1 = "33444"
    "Simple Keypad : Solution $solution1" {
        val digicode = Digicode.initial()
        for (line in puzzle.lines()) {
            digicode.readLine(line)
        }
        digicode.code() shouldBe solution1
    }

    val solution2 = "446A6"
    "Brain-fucked keypad example has solution $solution2" {
        val digicode = Digicode(KeyPad.brainFuck(), mutableListOf())
        digicode.keypad.value() shouldBe 5
        for (line in puzzle.lines()) {
            digicode.readLine(line)
        }
        digicode.code() shouldBe solution2
    }
}
}


data class Digicode(val keypad: KeyPad, val found: MutableList<Int>) {

    fun code(): String = buildString {
        for (i in found) {
            if (i in 1..9) {
                append(i)
            } else {
                append(when (i) {
                    10 -> 'A'
                    11 -> 'B'
                    12 -> 'C'
                    13 -> 'D'
                    else -> 'Z'
                })
            }
        }
    }
    fun readLine(line: String) {
        line.forEach { readLetter(it) }
        found += keypad.value()
    }

    fun readLetter(direction: Char) {
        when (direction) {
            'L' -> keypad.left()
            'R' -> keypad.right()
            'U' -> keypad.up()
            'D' -> keypad.down()
            else -> TODO("Invalid character $direction")
        }
    }

    companion object {
        fun initial() = Digicode(KeyPad.simpleKeypad(), mutableListOf())
    }
}





data class KeyPad(var row: Int, var col: Int, val size: Int, val max: Int, val matrix: List<List<Int>>) {

    fun value() = matrix[row][col]
    fun left() = tryMove(row, col - 1)
    fun right() = tryMove(row, col + 1)
    fun up() = tryMove(row - 1, col)
    fun down() = tryMove(row + 1, col)

    private fun tryMove(xx: Int, yy: Int) {
        if (matrix[xx][yy] != 0) {
            row = xx
            col = yy
        }
    }

    companion object {

        fun simpleKeypad() = KeyPad(2, 2, 3, 9, listOf(
            listOf(0, 0, 0, 0, 0),
            listOf(0, 1, 2, 3, 0),
            listOf(0, 4, 5, 6, 0),
            listOf(0, 7, 8, 9, 0),
            listOf(0, 0, 0, 0, 0)
        ))

        fun brainFuck() = KeyPad(3, 1, 3, 9, listOf(
listOf(0 , 0 , 0  , 0  , 0  , 0 , 0) ,
listOf(0 , 0 , 0  , 1  , 0  , 0 , 0) ,
listOf(0 , 0 , 2  , 3  , 4  , 0 , 0) ,
listOf(0 , 5 , 6  , 7  , 8  , 9 , 0) ,
listOf(0 , 0 , 10 , 11 , 12 , 0 , 0) ,
listOf(0 , 0 , 0  , 13 , 0  , 0 , 0) ,
listOf(0 , 0 , 0  , 0  , 0  , 0 , 0)
        ))

    }
}

