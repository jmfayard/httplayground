import com.fasterxml.jackson.databind.node.ObjectNode
import io.kotlintest.matchers.have
import retrofit2.Response
import retrofit2.http.GET
import rx.Single

interface ApiService {

    @GET("/ip")
    fun ip(): Single<Response<ObjectNode>>;

    @GET("/user-agent")
    fun userAgent(): Single<Response<ObjectNode>>;

}


class ApiTests : RxSpec() { init {


    retrofitFeature("GET /user-agent", service.userAgent()) {

        scenario("verify schema") {
            this should have schema ("schemas/user-agent.json")
        }
    }

    retrofitFeature("GET /ip", service.ip()) {
        scenario("verify schema") {
            this should have schema ("schemas/ip.json")
        }
    }


}

}

