import com.fasterxml.jackson.databind.node.ObjectNode
import io.kotlintest.matchers.be
import io.kotlintest.matchers.have
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.QueryMap
import rx.Single

interface ApiService {

    @GET("ip")
    fun ip(): Single<Response<ObjectNode>>

    @GET("user-agent")
    fun userAgent(): Single<Response<ObjectNode>>

    @GET("get")
    fun get(@QueryMap map: Map<String, String>): Single<Response<ObjectNode>>

    @GET("headers")
    fun headers(@HeaderMap headers: Map<String, String>): Single<Response<ObjectNode>>

}


class ApiTests : RxSpec() { init {


    retrofitFeature("GET /user-agent", service.userAgent()) {
        scenario("verify schema") {
            this should have schema "schemas/user-agent.json"
        }
    }

    retrofitFeature("GET /ip", service.ip()) {
        scenario("verify schema") {
            this should have schema "schemas/ip.json"
        }
    }

    val args = hashMapOf("id" to "1", "gender" to "MALE")
    retrofitFeature("GET /get?params", service.get(args)) {
        scenario("verify schema") {
            this should have schema "schemas/get.json"
        }
    }

    val headers = hashMapOf(
            "X-Requested-With" to "httpbin",
            "Content-Type" to "application/json"
    )
    retrofitFeature("GET /response-headers", service.headers(headers)) {
        scenario("verify schema") {
            this should have schema "schemas/headers.json"
        }
        scenario("list of headers") {
            val headers = get("headers") as ObjectNode
            val names = headers.fieldNames().asSequence().toList()
            println(names)
            names should containAll("Content-Type", "Content-Length", "X-Requested-With")
        }
    }


}

}

