import io.kotlintest.matchers.have
import io.kotlintest.specs.StringSpec
import java.util.*

/***
The first floor contains a thulium generator, a thulium-compatible microchip, a plutonium generator, and a strontium generator.
The second floor contains a plutonium-compatible microchip and a strontium-compatible microchip.
The third floor contains a promethium generator, a promethium-compatible microchip, a ruthenium generator, and a ruthenium-compatible microchip.
The fourth floor contains nothing relevant.
 ***/

/***
The first floor contains a hydrogen-compatible microchip and a lithium-compatible microchip.
The second floor contains a hydrogen generator.
The third floor contains a lithium generator.
The fourth floor contains nothing relevant.
**/

public fun elems(vararg e: String): Elems = Elems(listOf(*e).sorted())

class Day11 : StringSpec() { init {

    "Explode ?" {
        val list = elems("HM", "HG", "LM")
        list.chips() shouldBe elems("HM", "LM")
        list.activeChips() shouldBe elems("HM")
        elems("HG", "HM", "LG").isStable() shouldBe true
        elems("HM", "LG").isStable() shouldBe false
        elems("HM", "LM").isStable() shouldBe true
        elems("HM", "HG").isStable() shouldBe true
        list.isStable() shouldBe false
        list.takeOneOrTwo() should containInAnyOrder(
            elems("HM"), elems("HG"), elems("LM"), elems("HM", "HG"), elems("HM", "LM"), elems("HG", "LM")
        )
        list.takeOneOrTwo() should haveSize(6)
    }

    "Moves" {
        val generator = Radioisotope(2, listOf(
            elems("LM"), elems("HG", "HM"), elems("LG"), elems()
        ))
        println(generator)
        generator.isStable() shouldBe true
        generator.upMoves() shouldBe listOf(elems("HG", "HM"), elems("HG"))
        generator.copy(elevator = 1).downMoves() should haveSize(0)
        generator.copy(elevator = 4).upMoves() should haveSize(0)
        generator.downMoves() shouldBe listOf(elems("HM"))

    }

    "Resolvers" {
//        val initial = Radioisotope(1, listOf(
//            elems("HM", "LM"),
//            elems("HG"),
//            elems("LG"),
//            elems()
//        ))
        /***
        The first floor contains a thulium generator, a thulium-compatible microchip, a plutonium generator, and a strontium generator.
        The second floor contains a plutonium-compatible microchip and a strontium-compatible microchip.
        The third floor contains a promethium generator, a promethium-compatible microchip, a ruthenium generator, and a ruthenium-compatible microchip.
        The fourth floor contains nothing relevant.
         */
        val initial = Radioisotope(1, listOf(
            elems("TG", "TM", "PlG", "SG"),
            elems("PlM", "SM"),
            elems("PrG", "PrM", "RG", "RM"),
            elems()
        ))
        println(initial.upMoves())
        println("=== Initial Sate ===")
        Radioisotope(1, listOf(elems(), elems(), elems(), elems("HM", "LM", "HG", "LG"))).isSuccess() shouldBe true
        val resolution = RadioisotopeResolution()
        resolution.tryAllMoves(initial)
        resolution.printResult()
    }

}}

class RadioisotopeResolution() {

    var result : Int = 35

    val random = Random()
    val analyzed = mutableListOf<Radioisotope>()


    fun tryAllMoves(current: Radioisotope, stack : Int = 0) {

        if (current.isStable() == false) return

        val nextStack = stack + 1
        if (result != Int.MAX_VALUE  && nextStack >= result) {
            return
        }
        if (current in analyzed) {
            return
        }
        if (current.isSuccess()) {
            result = stack
            println("Found result in $result steps")
            return
        }
        analyzed += current
        for (move in current.upMoves()) {
            tryAllMoves(current.moveElevator(up = true, with = move)!!, nextStack)
        }
        for (move in current.downMoves()) {
            tryAllMoves(current.moveElevator(up = false, with = move)!!, nextStack)
        }
    }

    fun printResult() {
        println(result)
    }


}


data class Radioisotope(val elevator: Int, val levels: List<Elems>) {
    init {
        require(levels.size==4) { "Expected 4 levels" }
    }
    fun isStable(): Boolean = levels.all(Elems::isStable)

    fun isSuccess(): Boolean = levels.sumBy { it.size } == levels[3].size

    override fun toString() = buildString {
        for (i in 4.downTo(1)) {
            append("F$i ")
            if (i == elevator) {
                append("E ")
            } else {
                append(". ")
            }
            val level = levels[i-1]
            append(level.joinToString(separator = " ", postfix = "\n"))

        }
    }

    fun moveElevator(up: Boolean, with: Elems) : Radioisotope? {
        val nextElevator = if (up) (elevator+1) else (elevator-1)
        if (nextElevator !in 1..4) return null
        val ll = levels.mapIndexed { i, elems ->
            when (i+1) {
                elevator -> Elems(elems - with)
                nextElevator -> Elems(elems + with)
                else -> elems
            }
        }
        val next = Radioisotope(nextElevator, ll)
        if (next.isStable()) {
            return  next
        } else {
            return null
        }
    }

    fun upMoves(): List<Elems> =
        levels[elevator-1]
            .takeOneOrTwo()
            .filter {  move ->
                this.moveElevator(true, move) != null
            }

    fun downMoves(): List<Elems> =
        levels[elevator-1]
            .takeOneOrTwo()
            .filter {  move ->
                this.moveElevator(false, move) != null
            }
}

data class Elems(val l : List<String>) : List<String> by l {
    fun activeChips(): Elems {
        val chips = chips().l.filter { s -> l.contains(s.replace('M', 'G')) }
        return Elems(chips)
    }

    fun isStable(): Boolean = when {
        generators().isEmpty() -> true
        chips() == activeChips() -> true
        else -> false
    }

    fun  chips(): Elems  = Elems(l.filter { s -> s.last() == 'M' })
    fun  generators(): Elems  = Elems(l.filter { s -> s.last() == 'G' })
    fun  takeOneOrTwo(): List<Elems> {
        val result = mutableListOf<Elems>()
        for (i in l.indices) {
            for (j in l.indices) {
                if (j > i) {
                    result += elems(l[i], l[j])
                }
            }
        }
        for (i in l.indices) {
            result += elems(l[i])
        }
        return  result
    }

}



