import com.fasterxml.jackson.databind.node.ObjectNode
import com.squareup.moshi.JsonReader
import io.kotlintest.matchers.be
import io.kotlintest.specs.ShouldSpec
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


