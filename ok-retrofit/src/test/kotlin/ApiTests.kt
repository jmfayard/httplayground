import H.CacheControl
import H.ETag
import com.fasterxml.jackson.databind.node.ObjectNode
import io.kotlintest.matchers.have
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Response
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.http.*
import rx.Single


val URL = "https://httpbin.org/"

val retrofit = buildRetrofit {
    baseUrl(URL)
    addConverterFactory(JacksonConverterFactory.create())
    addCallAdapterFactory(RxJavaCallAdapterFactory.create())
    buildOk {
        val logger = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.HEADERS)
        addNetworkInterceptor(logger)
    }
}

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
                  @Query("gender") gender: String)
            : Single<Response<ObjectNode>>

    /** Returns given HTTP Status code. **/
    @GET("/status/{code}")
    fun status(@Path("code") code: Int): Call<String>

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

    /** Returns POST data. || url-encoded ***/
    @POST("/post")
    @FormUrlEncoded
    fun postUrlEncoded(@FieldMap postData: Map<String, String>): Single<Response<ObjectNode>>

    /** Returns POST data. || JSON body ***/
    @POST("/post")
    fun postJson(@Body postData: Map<String, String>): Single<Response<ObjectNode>>


    /** Returns POST data. || Multipart form-data ***/
    @POST("/post")
    @Multipart
    fun postFormData(@Part("topping") topping: String,
                     @Part("size") size: String,
                     @Part("comments") comments: String,
                     @Part("delivery") delivery: String)
            : Single<Response<ObjectNode>>

    /** Returns PUT data || url-encoded ***/
    @PUT("/put")
    @FormUrlEncoded
    fun putUrlEncoded(@FieldMap postData: Map<String, String>): Single<Response<ObjectNode>>

    /** Returns PUT data || JSON body ***/
    @PUT("/put")
    fun putJson(@Body putData: Map<String, String>): Single<Response<ObjectNode>>


}


class ApiTests : RxSpec() { init {


    val httpbin = retrofit.create(ApiService::class.java)

    feature("GET /user-agent") {
        retrofitScenario("verify schema", httpbin.userAgent()) {
            this should have schema "schemas/user-agent.json"
        }
    }

    feature("GET /ip") {
        retrofitScenario("verify schema", httpbin.ip()) {
            this should have schema "schemas/ip.json"
        }
    }

    feature("GET /get") {
        val args = hashMapOf("id" to "1", "gender" to "MALE")
        retrofitScenario("verify schema", httpbin.getMap(args)) {
            this should have schema "schemas/get.json"
        }

        retrofitScenario("verify schema", httpbin.getParams("1", "MALE")) {
            this should have schema "schemas/get.json"
        }
    }

    feature("GET /status") {
        scenario("status:418") {
            val call = httpbin.status(418)
            val response: Response<String> = call.execute()
            response.code() shouldBe 418
        }
    }


    feature("GET /headers") {
        val headers = hashMapOf("Content-Type" to "application/json", "X-Requested-With" to "okhttp3")
        val keys = headers.keys.toTypedArray()

        retrofitScenario("headersMap", httpbin.headersMap(headers)) {
            this should have schema "schemas/headers.json"
            childObject("headers").keys() should containAll(*keys)
        }

        retrofitScenario("headersParam", httpbin.headersParam("application/json", "okhttp3")) {
            this should have schema "schemas/headers.json"
            childObject("headers").keys() should containAll(*keys)
        }
    }


    feature("GET /response-headers?key=val") {

        scenario("Receiving ${ETag} and ${CacheControl}") {
            val call = httpbin.responseHeaders(etag = "42", cache = "public")
            val response: Response<ObjectNode> = call.execute()
            response.isSuccessful shouldBe true
            response.header(ETag) shouldBe "42"
            response.header(CacheControl) shouldBe "public"
        }

    }

    feature("POST /post") {

        val postData = hashMapOf(
                "topping" to "cheese",
                "size" to "small",
                "delivery" to "13h45",
                "comments" to "dish was great"
        )
        val keys = postData.keys.toTypedArray()

        retrofitScenario("/post x-www-form-urlencoded", httpbin.postUrlEncoded(postData)) {
            this should have schema "schemas/post.json"
            childObject("form").keys() should containAll(*keys)
        }

        retrofitScenario("/post multipart form-data", httpbin.postFormData("cheese", "small", "comments", "13h45")) {
            this should have schema "schemas/post.json"
            childObject("form").keys() should containAll(*keys)
        }

        retrofitScenario("/post JSON body", httpbin.postJson(postData)) {
            this should have schema "schemas/post.json"
            childObject("json").keys() should containAll(*keys)
        }
    }



    feature("PUT /put") {

        val putData = hashMapOf(
                "topping" to "cheese",
                "size" to "small",
                "delivery" to "13h45",
                "comments" to "dish was great"
        )

        retrofitScenario("/put x-www-form-urlencoded", httpbin.putUrlEncoded(putData)) {
            this should have schema "schemas/post.json"
        }

        retrofitScenario("/put JSON body", httpbin.putJson(putData)) {
            this should have schema "schemas/post.json"
        }
    }


}

}

