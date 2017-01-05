import kotlin.system.measureNanoTime

fun main(args: Array<String>) {
    val elements = elems("PrG", "PrM", "RG", "RM", "PlG", "PlM", "SG", "SM", "TG", "TM")
    val search = Radioisotope(2, listOf(
        elems("TG", "TM", "PlG", "SG"),
        elems("PlM", "SM"),
        elems("PrG", "PrM", "RG", "RM"),
        elems()
    ))

    val search2 = IsotopeFactory(1, listOf(3, 3, 3, 3, 1, 2, 1, 2, 1, 1))
    benchmark("Old Hashcode") { search.hashCode() }
    benchmark("New Hashcode") { search2.hashCode() }
    benchmark("Old isValid()") { search.isStable() }
    benchmark("Old isValid()") { search2.isValid() }
    benchmark("Old moves") {
        search.downMoves().map { move -> search.moveElevator(false, move) }
        search.upMoves().map { move -> search.moveElevator(true, move) }
    }
    benchmark("New moves") {
        search2.downMoves() + search2.upMoves()
    }
    println(search.downMoves())
    println(search2.downMoves())
    println(search.upMoves())
    println(search2.upMoves())
}


inline fun benchmark(name: String, block: () -> Unit) : Unit {
    val result = measureNanoTime {
        for (i in 1..10000) {
            block()
        }
    }
    println("Benchmark $name => $result")
}