import io.kotlintest.TestBase
import io.kotlintest.TestCase
import io.kotlintest.TestSuite
import io.kotlintest.matchers.be
import io.kotlintest.specs.FreeSpec
import io.kotlintest.specs.RxSpec
import rx.Observable
import rx.Single
import java.util.*
import java.util.concurrent.TimeUnit

class RxExample : RxSpec() {
  init {
    "first(observable or single) OK tests" - {
      val observable = Observable.from(0..6)
      val single = Single.just(0)
      val delayed = Observable.timer(2, TimeUnit.SECONDS).map { l ->
        l.toInt()
      }

      first(observable, "is 0") { nb ->
        nb shouldEqual 0
      }
      first(single, "is 0") { nb ->
        nb shouldEqual 0
      }
      first(observable, "is 1 (shouldThrow)", shouldThrow = true) { nb ->
        nb shouldEqual 1
      }
      first(Observable.empty<Int>(), "Empty observable (shouldThrow)", shouldThrow = true) {
        throw RuntimeException("We should not be here")
      }

      first(delayed, "Timer") { nb ->
        nb shouldEqual 1
      }

    }

    "first(observalbe or single) KO tests" - {
      val observable = Observable.just(0)
      val single = Single.just(0)


      first(observable, "is 0 (shouldThrow)", shouldThrow = true) { nb ->
        nb shouldEqual 0
      }
      first(single, "is 0 (shouldThrow)", shouldThrow = true) { nb ->
        nb shouldEqual 0
      }

      first(observable, "is 1") { nb ->
        nb shouldEqual 1
      }


      first(Observable.empty<Int>(), "Empty observable") {
        throw RuntimeException("We should not be here")
      }

      val errorSingle = Single.error<Int>(IllegalArgumentException("Single returned an error"))
      val errorObservable = Observable.error<Int>(IllegalArgumentException("Observable returned an error"))
      first(errorSingle, "single returning error") {
      }
      first(errorObservable, "observable returning error") {
      }
      first(single, "test throwing error") { nb ->
        throw IllegalArgumentException("fails")
      }


    }

    "Observable.all() OK" - {
      val ints = Observable.from(0..6)
      val odds = ints.filter { nb -> nb.mod(2) == 0 }
      val even = ints.filter { nb -> nb.mod(2) == 1 }

      all(odds, "is odd") { nb ->
        nb.mod(2) shouldBe 0
      }

      all(even, "is even") { nb ->
        nb.mod(2) shouldBe 1
      }

      val errorObservable = Observable.error<Int>(IllegalArgumentException("Observable returned an error"))
      all(errorObservable, "observable throwing error", shouldThrow = true) { nb ->
        throw IllegalArgumentException("fails")
      }

      val timer = Observable.interval(1, TimeUnit.SECONDS).take(4)
      all(timer, "run for 4s, timouet=1, shouldThrow=true", timeout = 1, shouldThrow = true) { nb ->
        nb should be lt 10
      }

    }

    "Observable.all() KO" - {
      val ints = Observable.from(0..6)

      all(ints, "is odd") { nb ->
        nb.mod(2) shouldBe 0
      }

      all(ints, "is even") { nb ->
        nb.mod(2) shouldBe 1
      }

      val timer = Observable.interval(1, TimeUnit.SECONDS).take(4)
      all(timer, "run for 4s, timouet=1, shouldThrow=false", timeout = 1, shouldThrow = false) { nb ->
        nb should be lt 10
      }

      val errorObservable = Observable.error<Int>(IllegalArgumentException("Observable returned an error"))
      all(Observable.merge(Observable.from(1..3), errorObservable), "observable throwing error") { nb: Int ->
        nb < 3
      }
    }
  }
}
