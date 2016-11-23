import io.kotlintest.matchers.be
import io.kotlintest.specs.FeatureSpec
import rx.Observable

class RxExample : FeatureSpec() {
    init {
        tests()
    }

    fun tests() {
        feature("odd or even") {
            val ints = Observable.from(0..6)

            rxScenario("odd", ints) { nb ->
                nb.mod(2) shouldBe 0
            }
        }

        feature("Crashes") {
            val ints = Observable.from(0..6)
            val crashes = ints.map { nb ->
                if (nb >= 3) throw RuntimeException("nb >= 3, let's crash hard!!")
                nb
            }
            rxScenario(".. for nb >= 3", crashes) { nb ->
                nb should be lt 3
            }

        }
    }
}