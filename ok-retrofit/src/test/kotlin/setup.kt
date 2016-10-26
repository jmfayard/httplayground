import com.fasterxml.jackson.databind.JsonNode
import com.github.fge.jackson.JsonLoader
import com.github.fge.jsonschema.core.load.Dereferencing
import com.github.fge.jsonschema.core.load.configuration.LoadingConfiguration
import com.github.fge.jsonschema.main.JsonSchema
import com.github.fge.jsonschema.main.JsonSchemaFactory
import com.squareup.moshi.FromJson
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson
import io.kotlintest.matchers.HaveWrapper
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.intellij.lang.annotations.Language
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit


val moshi = Moshi.Builder()
        .add(DateAdapter())
        .build()


internal class DateAdapter {
    @ToJson fun toJson(date: Date): String {
        return date.time.toString()
    }

    @FromJson fun fromJson(date: String): Date {
        return Date()
    }
}

val ok by lazy {
    OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .addNetworkInterceptor(
                    HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.HEADERS)
            )
            .build()
}


fun createRetrofitApiService(url: String): ApiService {
    val baseUrl = if (url.endsWith("/")) {
        url
    } else {
        url + "/"
    }

    val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(ok)
            .addConverterFactory(JacksonConverterFactory.create())
//            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
            .build()

    return retrofit.create(ApiService::class.java)
}


infix fun HaveWrapper<out JsonNode>.schema(schema: JsonNode): Unit {
    val validator: JsonSchema = JsonUtils.validator(schema)
    val report = validator.validate(value)

    if (!report.isSuccess)
        throw AssertionError("Json does match the expected json schema\n$value\n${report.toString()}")
}
infix fun HaveWrapper<out JsonNode>.schema(@Language("File") schemaPath: String): Unit {
    val validator: JsonSchema = JsonUtils.validator(schemaPath)
    val report = validator.validate(value)

    if (!report.isSuccess)
        throw AssertionError("Json does match the expected json schema\n$value\n${report.toString()}")
}

object JsonUtils {

    val cfg = LoadingConfiguration.newBuilder().dereferencing(Dereferencing.INLINE).freeze()
    val factory = JsonSchemaFactory.newBuilder().setLoadingConfiguration(cfg).freeze()
    fun json(string: String): JsonNode = JsonLoader.fromString(string)
    fun  validator(schema: JsonNode): JsonSchema = factory.getJsonSchema(schema)
    fun  validator( @Language("File") schemaPath: String): JsonSchema {

        val file = when {
            schemaPath.startsWith("/") -> File(schemaPath)
            System.getProperty("user.dir").endsWith("httpplayground") ->   File("ok-retrofit/src/test/resources/$schemaPath")
            else -> File("src/test/resources/$schemaPath")
        }
        val schema = JsonLoader.fromFile(file)
        return factory.getJsonSchema(schema)
    }
}
