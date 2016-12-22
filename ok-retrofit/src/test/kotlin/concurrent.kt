import com.github.jmfayard.debug
import io.kotlintest.specs.StringSpec
import rx.Observable
import rx.schedulers.Schedulers
import java.util.concurrent.*

class StringSpecExample : StringSpec() {
    init {
        newtests()
//        tests()
    }


    fun newtests() {
        val executor = Executors.newFixedThreadPool(5)

        "CountDownLatch" {
            var countedDown = false
            val countdown = CountDownLatch(1)

            val tasks = (1..10).map { i ->
                Callable {
                    countdown.await()
                    println(i)
                    countedDown shouldBe true
                }
            }

            Observable.timer(2, TimeUnit.SECONDS)
                    .observeOn(Schedulers.computation())
                    .map {
                true shouldBe true
                println("Counting down")
                countedDown = true
                countdown.countDown()
            }.toBlocking().first()

        }
    }


    fun tests() {

        val executor = Executors.newFixedThreadPool(5)
        "Barrier" {
            val produced = mutableListOf<Int>()
            val expected = (1..9).toList().toTypedArray()
            val barrier = CyclicBarrier(3, {
                println("3 Threads reached the barrier! Releasing it")
            })
            for (i in 1..10) {
                executor.submit {
                    Thread.sleep(i * 200L)
                    println(Thread.currentThread().name + " => awating")
                    barrier.await()
                    produced += i
                    println(Thread.currentThread().name + " => producing $i")
                }
            }
            executor.awaitTermination(30, TimeUnit.SECONDS)
            produced.debug("Produced")
            produced should containAll(*expected)
            (10 in produced) shouldBe false
        }



    }
}


data class DelayedProducer(val i: Int, val barrier: CyclicBarrier) : Callable<Int> {
    override fun call(): Int {
        Thread.sleep(i * 200L)
        println(Thread.currentThread().name + " => awating")
        barrier.await(2, TimeUnit.SECONDS)
        println(Thread.currentThread().name + " => producing $i")
        return i
    }
}