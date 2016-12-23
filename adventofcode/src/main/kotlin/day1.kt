import Direction.*
import Instruction.*
import io.kotlintest.specs.FreeSpec
import kotlin.test.fail


/***
 *
--- Day 1: No Time for a Taxicab ---

Santa's sleigh uses a very high-precision clock to guide its movements, and the
clock's oscillator is regulated by stars. Unfortunately, the stars have been
stolen... by the Easter Bunny. To save Christmas, Santa needs you to retrieve
all fifty stars by December 25th.

Collect stars by solving puzzles. Two puzzles will be made available on each
day in the advent calendar; the second puzzle is unlocked when you complete the
first. Each puzzle grants one star. Good luck!

You're airdropped near Easter Bunny Headquarters in a city somewhere. "Near",
unfortunately, is as close as you can get - the instructions on the Easter
Bunny Recruiting Document the Elves intercepted start here, and nobody had time
to work them out further.

The Document indicates that you should start at the given coordinates (where
you just landed) and face North. Then, follow the provided sequence: either
turn left (L) or right (R) 90 degrees, then walk forward the given number of
blocks, ending at a new intersection.

There's no time to follow such ridiculous instructions on foot, though, so you
take a moment and work out the destination. Given that you can only walk on the
street grid of the city, how far is the shortest path to the destination?

For example:

Following R2, L3 leaves you 2 blocks East and 3 blocks North, or 5 blocks away.
R2, R2, R2 leaves you 2 blocks due South of your starting position, which is 2
blocks away.  R5, L5, R5, R3 leaves you 12 blocks away.  How many blocks away
is Easter Bunny HQ?


 */

class Day1 : FreeSpec() { init {

    "Turning" {
        NORTH.turnRight() shouldBe EAST
        WEST.turnRight() shouldBe NORTH
        NORTH.turnLeft() shouldBe WEST
        WEST.turnLeft() shouldBe SOUTH
    }

    "Parsing input" {
        Advance(3) shouldBe Advance(3)
        val instructions = parsePuzzle("R3, L2")
        instructions shouldBe listOf(TurnRight, Advance(3), TurnLeft, Advance(2))
    }

    "Next state" {
        val states = listOf(
            State.initial(),
            State(1, EAST, 0, 0),
            State(2, EAST, 3, 0),
            State(3, NORTH, 3, 0),
            State(4, NORTH, 3, 2)
        )
        val instructions = parsePuzzle("R3, L2")
        for (i in 0..3) {
            states[i].next(instructions[i]) shouldBe states[i + 1]
        }
    }

    val puzzle = """
    R3, L2, L2, R4, L1, R2, R3, R4, L2, R4, L2, L5, L1, R5, R2, R2, L1, R4, R1, L5, L3, R4, R3, R1, L1, L5, L4, L2, R5, L3, L4, R3, R1, L3, R1, L3, R3, L4, R2, R5, L190, R2, L3, R47, R4, L3, R78, L1, R3, R190, R4, L3, R4, R2, R5, R3, R4, R3, L1, L4, R3, L4, R1, L4, L5, R3, L3, L4, R1, R2, L4, L3, R3, R3, L2, L5, R1, L4, L1, R5, L5, R1, R5, L4, R2, L2, R1, L5, L4, R4, R4, R3, R2, R3, L1, R4, R5, L2, L5, L4, L1, R4, L4, R4, L4, R1, R5, L1, R1, L5, R5, R1, R1, L3, L1, R4, L1, L4, L4, L3, R1, R4, R1, R1, R2, L5, L2, R4, L1, R3, L5, L2, R5, L4, R5, L5, R3, R4, L3, L3, L2, R2, L5, L5, R3, R4, R3, R4, R3, R1
"""


    "Solutions : 262 and 277" {
        val input = parsePuzzle(puzzle)
        val states = mutableListOf(State.initial())
        var found = false
        input.forEach { instruction ->
            val current = states.last()
            states += current.next(instruction)
        }
        states.forEach { println(it) }
        val last = states.last()
        println("Last state $last is at a distance of ${last.distance()}")
        last.distance() shouldBe 262

        val travelled = mutableMapOf<Pair<Int, Int>, List<State>>()

        states
            .filter { state -> state.step.mod(2) == 0}
            .forEach { state ->
                val pair = state.x to state.y
                travelled[pair] = travelled.getOrElse(pair) { emptyList() } + state
            }

        val visitedTwice = travelled
            .filterValues { it.size > 1 }
            .flatMap { it.value }
            .sortedBy { it.step }

        visitedTwice.forEach { it.debug("twice") }
        val visited = visitedTwice.first()
        "$visited (distance ${visited.distance()}) was the first visited twice".debug("Part2")
        visited.distance() shouldBe 277
    }
}
}

enum class Direction(val angle: Int) {
    NORTH(0),
    EAST(90),
    SOUTH(180),
    WEST(270);

    fun turnRight(): Direction = fromAngle(angle + 90)
    fun turnLeft(): Direction = fromAngle(angle - 90)

    private fun fromAngle(angle: Int): Direction {
        val a = (angle + 360).mod(360)
        val result = Direction.values().firstOrNull { it.angle == a }
        return requireNotNull(result) { "Invalid angle $angle" }
    }
}

sealed class Instruction {
    object TurnRight : Instruction() {
        override fun toString() = "TurnRight()"
    }

    object TurnLeft : Instruction() {
        override fun toString() = "TurnLeft()"
    }

    class Advance(val number: Int) : Instruction() {
        override fun equals(other: Any?): Boolean =
            other is Advance && other.number == number

        override fun hashCode(): Int = number
        override fun toString() = "Advance($number)"
    }
}

data class State(val step: Int, val direction: Direction, val x: Int, val y: Int) {

    fun next(instruction: Instruction): State = when (instruction) {
        Instruction.TurnRight -> copy(step = step + 1, direction = direction.turnRight())
        Instruction.TurnLeft -> copy(step = step + 1, direction = direction.turnLeft())
        is Advance -> {
            val next = nextStep(instruction.number)
            copy(step = step + 1, x = next.first, y = next.second)
        }
    }

    fun distance(): Int = Math.abs(x) + Math.abs(y)

    fun nextStep(number: Int): Pair<Int, Int> = when (direction) {
        Direction.NORTH -> x to (y + number)
        Direction.EAST -> (x + number) to y
        Direction.SOUTH -> x to (y - number)
        Direction.WEST -> (x - number) to y
    }

    companion object {
        fun initial() = State(0, NORTH, 0, 0)

    }
}

fun parsePuzzle(puzzle: String): List<Instruction> =
    puzzle.split(",")
        .map { it.trim() }
        .flatMap { s: String -> // R4 L2
            val turn = when (s.first()) {
                'R' -> Instruction.TurnRight
                'L' -> Instruction.TurnLeft
                else -> fail("Invalid puzzle $s")
            }
            val number = s.substring(1).toInt()
            listOf(turn, Instruction.Advance(number))
        }

