import io.kotlintest.specs.FeatureSpec
import okhttp3.ResponseBody
import rx.Observable
import rx.Single


fun <T> FeatureSpec.rxScenario(name: String, source: Observable<T>, operation: (next: T) -> Unit) {
    val blocking = source.toBlocking().iterator
    var i = 0
    while (true) {
        try {
            if (blocking.hasNext() == false) {
                return
            }
        } catch (e: Throwable) {
            scenario("$name Crash at step #$i") {
                throw AssertionError(e.message)
            }
            return
        }
        val step = blocking.next()
        i++
        scenario("$name #$i") {
            operation(step!!)
        }
    }
}

fun <T> FeatureSpec.rxScenario(name: String, source: Single<T>, operation: (next: T) -> Unit) =
        rxScenario(name, source.toObservable(), operation)

fun <T : Any> checkSuccessfull(response: retrofit2.Response<T>): T =
        if (response.isSuccessful) {
            response.body()
        } else {
            throw AssertionError(""""Rertrofit failed with HTTP ${response.code()} ${response.message()}
${truncate(response.errorBody()?.string())}""")
        }

fun checkSuccessfull(response: okhttp3.Response): ResponseBody =
        if (response.isSuccessful) {
            response.body()
        } else {
            throw AssertionError(""""OkHttp failed with HTTP ${response.code()} ${response.message()}""")
        }

private fun truncate(string: String?): String = when {
    string == null -> ""
    string.length < 200 -> string
    else -> string.substring(0, 197) + "..."
}



