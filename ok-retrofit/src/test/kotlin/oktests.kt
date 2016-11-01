import com.fasterxml.jackson.databind.node.ObjectNode
import com.squareup.moshi.JsonReader
import io.kotlintest.matchers.be
import io.kotlintest.specs.ShouldSpec
import io.kotlintest.specs.StringSpec
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.BufferedSource


object OkRequests {
    val HOST = "httpbin.org"
    val scheme = "HTTPS"


    fun get(args: Map<String, String>): Request = buildRequest {

        buildUrl {
            scheme("https")
            host(HOST)
            addPathSegment("get")
            for ((key, value) in args) {
                addQueryParameter(key, value)
            }
        }
    }

    fun statusCode(code : Int) : Request = buildRequest {
        buildUrl {
            scheme("https")
            host(HOST)
            addPathSegment("status")
            addPathSegment(code.toString())
        }
    }



    fun redirect(n : Int) : Request = buildRequest {
        buildUrl {
            scheme("https")
            host(HOST)
            addPathSegment("redirect")
            addPathSegment(n.toString())
        }
    }


}


class OKTests : ShouldSpec() { init {

    val client: OkHttpClient = buildOk { }
    infix fun Request.mustHave(tests: (Response) -> Unit) {
        val name = "succeed ${this.method()}  ${this.url()}"
        should(name) {
            val response = client.newCall(this).execute()
            response.isSuccessful shouldBe true
            tests(response)
        }
    }

    fun Map<String,Any>.childObject(name: String): Map<String,Any> {
        if (get(name) is Map<*,*>) {
            return get(name) as Map<String,Any>
        } else {
            throw AssertionError("No child object with name [$name] in \n   $this")
        }
    }



    "GET /args" {
        val request = OkRequests.get(hashMapOf("id" to "1", "gender" to "MALE"))

        request mustHave { response: Response ->
            val map = Parser.read(response.body().source())
            map.keys should containInAnyOrder("args", "headers", "origin", "url")
            map["headers"] should be a Map::class

        }

        OkRequests.get(emptyMap()) mustHave { response: Response ->
            val map = Parser.read(response.body().source())
            map.childObject("args").keys should beEmpty()
        }
    }

    "GET /status/:code OK" {
        val codes = (200..210) + (301..303) + 307
        forAll(codes) { code ->
            OkRequests.statusCode(code) mustHave { response: Response ->
                response.isSuccessful
            }
        }
    }

    "GET /status/:code KO" {
        val badCodes = (400..406) + (500..510) + listOf(300,304,305,306,308,309,310,410)
        for (code in badCodes) {
            should("fail for $code") {
                val request = OkRequests.statusCode(code)
                val response = client.newCall(request).execute()
                response.isSuccessful shouldBe false
            }
        }
    }


    "GET /redirect/:n" {
        val redirects = (1..8)
        for (redirect in redirects) {
            OkRequests.redirect(redirect) mustHave { response: Response ->
                response.isSuccessful
            }
        }

    }


}
}



object Parser {
    lateinit var reader: JsonReader
    lateinit var map: MutableMap<String, Any>
    fun read(source: BufferedSource): Map<String, Any> {
        reader = JsonReader.of(source)
        map = mutableMapOf<String, Any>()
        reader.beginObject()
        while (reader.hasNext()) {
            val name = reader.nextName()
            if (name in listOf("args", "headers")) {
                readObject(name)
            } else if (name in listOf("origin", "url")) {
                val string = reader.nextString()
                map[name] = string
            }
        }
        reader.endObject()
        return map
    }

    private fun readObject(name: String) {
        val o = mutableMapOf<String, Any>()
        reader.beginObject()
        while (reader.hasNext()) {
            val key = reader.nextName()
            o[key] = reader.nextString()
        }
        reader.endObject()
        map[name] = o

    }
}


