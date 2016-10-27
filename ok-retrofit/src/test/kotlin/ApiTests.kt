import H.*
import com.fasterxml.jackson.databind.node.ObjectNode
import io.kotlintest.matchers.have
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Response
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.http.*
import rx.Single

/***
 * Retrofit interface for http://httpbin.org/
 *
 * httpbin(1): HTTP Request & Response Service
 *
 * Testing an HTTP Library can become difficult sometimes.
 * RequestBin is fantastic for testing POST requests, but doesn't let you control the response.
 * This exists to cover all kinds of HTTP scenarios. Additional endpoints are being considered.
 * All endpoint responses are JSON-encoded.
 */
private val URL = "https://httpbin.org/"
interface ApiService {

    /**   Returns Origin IP   */
    @GET("/ip")
    fun ip(): Single<Response<ObjectNode>>

    /**  Returns user-agent  */
    @GET("/user-agent")
    fun userAgent(): Single<Response<ObjectNode>>

    /**  Returns GET data.   */
    @GET("/get")
    fun getMap(@QueryMap map: Map<String, String>): Single<Response<ObjectNode>>

    /**  Returns GET data.   */
    @GET("/get")
    fun getParams(@Query("id") id: String,
                  @Query("gender")   gender: String)
            : Single<Response<ObjectNode>>

    /** Returns given HTTP Status code. **/
    @GET("/status/{code}")
    fun status(@Path("code") code: Int) : Call<String>

    /** Returns headers dict  **/
    @GET("/headers")
    fun headersMap(@HeaderMap headers: Map<String, String>): Single<Response<ObjectNode>>

    /** Returns headers dict  **/
    @GET("/headers")
    fun headersParam(@Header("Content-Type") contentType: String,
                     @Header("X-Requested-With") requestedWith: String)
            : Single<Response<ObjectNode>>

    /** Returns given response headers.  **/
    @GET("/response-headers")
    fun responseHeaders(
            @Query("Cache-Control") cache: String,
            @Query("ETag") etag: String)
            : Call<ObjectNode>






}

private val retrofit = buildRetrofit {
    baseUrl(URL)
    addConverterFactory(JacksonConverterFactory.create())
    addCallAdapterFactory(RxJavaCallAdapterFactory.create())
    buildOk {
        val logger = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.HEADERS)
        addNetworkInterceptor(logger)
    }
}


class ApiTests : RxSpec() { init {


    val service = retrofit.create(ApiService::class.java)

    feature("GET /user-agent") {
        retrofitScenario("verify schema", service.userAgent()) {
            this should have schema "schemas/user-agent.json"
        }
    }

    feature("GET /ip") {
        retrofitScenario("verify schema", service.ip()) {
            this should have schema "schemas/ip.json"
        }
    }

    feature("GET /get") {
        val args = hashMapOf("id" to "1", "gender" to "MALE")
        retrofitScenario("verify schema", service.getMap(args)) {
            this should have schema "schemas/get.json"
        }

        retrofitScenario("verify schema", service.getParams("1", "MALE")) {
            this should have schema "schemas/get.json"
        }
    }

    feature("GET /status") {
        scenario("status:418") {
            val call = service.status(418)
            val response: Response<String> = call.execute()
            response.code() shouldBe 418
        }
    }


    feature("GET /headers") {
        val headers = hashMapOf("Content-Type" to "application/json", "X-Requested-With" to "okhttp3")

        retrofitScenario("headersMap", service.headersMap(headers)) {
            this should have schema "schemas/headers.json"
            val headersJson = get("headers") as ObjectNode
            val names = headersJson.fieldNames().asSequence().toList()
            names should containAll("Content-Type", "X-Requested-With")
        }

        retrofitScenario("headersParam", service.headersParam("application/json", "okhttp3")) {
            this should have schema "schemas/headers.json"
            val headersJson = get("headers") as ObjectNode
            val names = headersJson.fieldNames().asSequence().toList()
            names should containAll("Content-Type", "X-Requested-With")
        }
    }


    feature("GET /response-headers?key=val") {

        scenario("Receiving ${ETag} and ${CacheControl}") {
            val call = service.responseHeaders(etag = "42", cache = "public")
            val response: Response<ObjectNode> = call.execute()
            response.isSuccessful shouldBe true
            response.header(ETag) shouldBe "42"
            response.header(CacheControl) shouldBe "public"
        }

    }





}

}

