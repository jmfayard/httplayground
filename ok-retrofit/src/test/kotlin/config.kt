
/*** CONFIG *****/
val ENVS = hashMapOf(
    "localhost" to "http://127.0.0.1:5000/",
    "httpbin" to "https://httpbin.org"
)
val BASE_URL = ENVS["localhost"]!!


val service: ApiService = createRetrofitApiService(BASE_URL)