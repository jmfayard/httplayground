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



public fun elems(vararg e: String): Elems = Elems(listOf(*e))

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
        Radioisotope(1, listOf(elems(), elems(), elems(), elems("HM", "LM", "HG", "LG"))).isSuccess() shouldBe true


    }

    val solution = 31
    "RadioisotopeResolution => $solution steps" {
        val sampleElements = elems("HM", "LM", "HG", "LG")
        val sampleSearch = Radioisotope(1, listOf(
            elems("HM", "LM"),
            elems("HG"),
            elems("LG"),
            elems()
        ))
        RadioisotopeResolution(sampleElements).searchUntilFind(sampleSearch) shouldBe 7

        /***
        The first floor contains a thulium generator, a thulium-compatible microchip, a plutonium generator, and a strontium generator.
        The second floor contains a plutonium-compatible microchip and a strontium-compatible microchip.
        The third floor contains a promethium generator, a promethium-compatible microchip, a ruthenium generator, and a ruthenium-compatible microchip.
        The fourth floor contains nothing relevant.
         */
        val elements = elems("PrG", "PrM", "RG", "RM", "PlM", "SM", "TG", "TM", "PlG", "SG")
        val search = Radioisotope(1, listOf(
            elems("TG", "TM", "PlG", "SG"),
            elems("PlM", "SM"),
            elems("PrG", "PrM", "RG", "RM"),
            elems()
        ))
        RadioisotopeResolution(elements).searchUntilFind(search) shouldBe solution


        val elementsBis = elems("PrG", "PrM", "RG", "RM", "PlM", "SM", "TG", "TM", "PlG", "SG", "EG", "EM", "DG", "DM")
        val searchBis = Radioisotope(1, listOf(
            elems("TG", "TM", "PlG", "SG", "EG", "EM", "DG", "DM"),
            elems("PlM", "SM"),
            elems("PrG", "PrM", "RG", "RM"),
            elems()
        ))
//        RadioisotopeResolution(elementsBis).searchUntilFind(searchBis) shouldBe solution

    }

}}

class RadioisotopeResolution(val elems: Elems) {

    var step = 0

    var lastStep : List<Radioisotope> = listOf(initialState())
    val hashCodes = lastStep.map { it.hashCode() }.toMutableList()

    private fun  initialState() = Radioisotope(4, listOf(elems(), elems(), elems(), elems))

    fun oneMoreStep() {
        val steps = lastStep.flatMap { isotope ->
            val ups = isotope.upMoves().map { move -> isotope.moveElevator(up = true, with = move) }.filterNotNull()
            val downs = isotope.downMoves().map { move -> isotope.moveElevator(up = false, with = move) }.filterNotNull()
            ups + downs
        }
            .distinct()
            .sortedBy { s -> s.hashCode() }

        /** Search items not previously found
         * optimizing by the fact that both lists are sorted
         */
        val nextSteps = mutableListOf<Radioisotope>()
        var (i, j) = (0 to 0)
        while (i < steps.size && j < hashCodes.size) {
            val new = steps[i].hashCode()
            val old = hashCodes[j].hashCode()
            if (new < old) {
                nextSteps += steps[i]
                i++
            } else if (new == old) {
                i++
                j++
            } else {
                j++
            }
        }
        for (k in i..steps.lastIndex) {
            nextSteps += steps[k]
        }
        lastStep = nextSteps
        hashCodes += nextSteps.map(Radioisotope::hashCode)
        Collections.sort(hashCodes)


        step++
        println("Step = $step => ${lastStep.size} elements")
        lastStep = steps
        hashCodes += steps.map { it.hashCode() }

    }


    fun searchUntilFind(search: Radioisotope) : Int {
        val hashSearch = search.hashCode()
        var i = 0
        while (lastStep.isNotEmpty()) {
            oneMoreStep()
            i++
            if (hashSearch in hashCodes) {
                println(search)
                println("... was found in $i steps")
                return i
            }
        }
        println("$search\nNOT FOUND")
        return  -1
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

data class IsotopeFactory(val elevator: Int, val levels: List<Int>) {
  fun isValid() : Boolean {
      val rangeOk = levels.all { v -> v in 1..4 }
      val haveGenerators = levels.filterIndexed {i, v -> i.isEven() }.distinct()
      val chipsAlones = levels.filterIndexed { i, v ->
          i.isOdd() && levels[i-1] != v
      }
      val chipsBurned = chipsAlones.filter { v -> v in haveGenerators }
      return  rangeOk && chipsBurned.isEmpty()
  }

    private fun moveElevator(up: Boolean, i1: Int, i2: Int) : IsotopeFactory {
        val new = ArrayList(levels)
        val offset = if (up) 1 else -1
        new[i1] += offset
        if (i2 !=  i1) {
            new[i2] += offset
        }
        return copy(elevator = elevator + offset, levels = new)
    }

    val currentLevelIndexes: List<Int> by lazy {
        levels.indices.filter { levels[it] == elevator }
    }

    fun upMoves() : List<IsotopeFactory> =
        if (elevator == 4) {
            emptyList()
        } else {
            val result = mutableListOf<IsotopeFactory>()
            for (i in currentLevelIndexes) {
                for (j in currentLevelIndexes) {
                    val move = moveElevator(true, i, j)
                    if (move.isValid()) {
                        result += move
                    }
                }
            }
            result
        }

    fun downMoves(): List<IsotopeFactory> =
        if (elevator == 0) emptyList()
        else {
            val result = mutableListOf<IsotopeFactory>()
            for (i in currentLevelIndexes) {
                for (j in currentLevelIndexes) {
                    val move = moveElevator(false, i, j)
                    if (move.isValid()) {
                        result += move
                    }
                }
            }
            result
        }

}

data class Elems(val l : List<String>) : List<String> by l {
    init {
        Collections.sort(l)
    }
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



