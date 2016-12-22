package io.kotlintest.specs

import io.kotlintest.TestCase
import rx.Observable
import rx.Single
import java.util.*
import java.util.concurrent.TimeUnit

abstract class RxSpec : FreeSpec() {

  fun <T> all(source: Observable<T>, name: String, shouldThrow: Boolean = false, timeout: Int = 3, operation: (T) -> Unit) {
    val noError = IllegalArgumentException("\"$name\" was expected to throw, but didn't")
    val observableError = IllegalArgumentException("\"$name\" : observable thrown an error")
    val timeoutError = AssertionError("\"$name\" : not completed after $timeout seconds")
    val timeoutObservable = Observable.timer(timeout.toLong(), TimeUnit.SECONDS).flatMap {
      Observable.error<T>(timeoutError)
    }
    val sourceWithTimeout = source.timeout(timeout.toLong(), TimeUnit.SECONDS)
    name.minus {
      val blocking = sourceWithTimeout.toBlocking().iterator
      var i = 0
      while (true) {
        try {
          if (blocking.hasNext() == false) {
            if (shouldThrow) {
              throw noError
            } else {
              return@minus
            }
          }
        } catch (e: Throwable) {

          if (shouldThrow) {
            "#$i: throwed ${e.message}" {
              // test ok
            }
          } else {
            "#$i: throwed ${e.message}" {
              throw observableError
            }

          }
          return@minus
        }
        val step = blocking.next()
        i++
        val truncate = step.toString().subSequence(0, Math.min(10, step.toString().length))
        "#$i: $truncate" {
          try {
            operation(step!!)
          } catch (e: AssertionError) {
            if (!shouldThrow) throw e
          }
        }
      }
    }

  }

  fun <T> first(source: Observable<T>, name: String, shouldThrow: Boolean = false,  test: (T) -> Unit): TestCase {
    val noItem = AssertionError("No items emitted by obvservable of [$name]")
    val noError = AssertionError("\"$name\" was expected to throw, but didn't")
    return name.invoke {
      var errorThrown = false
      try {
        val toSingle = source.take(1).toSingle()
        test(toSingle.toBlocking().value())
      } catch (e: NoSuchElementException) {
        errorThrown = true
        if (!shouldThrow) throw noItem
      } catch (e: AssertionError) {
        errorThrown = true
        if (!shouldThrow) throw  e
      } finally {
        if (shouldThrow && !errorThrown) {
          throw noError
        }
      }

    }
  }

  inline fun <T> first(source: Single<T>, name: String, shouldThrow: Boolean = false, noinline test: (T) -> Unit) : TestCase
    = first(source.toObservable()!!, name, shouldThrow, test)

}

