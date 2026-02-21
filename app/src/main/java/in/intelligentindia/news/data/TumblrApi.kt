package in.intelligentindia.news.data

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

private const val BASE_URL = "https://api.tumblr.com/"

interface TumblrApi {
    @GET("v2/blog/{blogIdentifier}/posts")
    suspend fun posts(
        @Path("blogIdentifier") blogIdentifier: String,
        @Query("api_key") apiKey: String,
        @Query("tag") tag: String? = null,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): TumblrEnvelope
}

object TumblrApiFactory {
    fun create(): TumblrApi {
        val logger = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logger)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TumblrApi::class.java)
    }
}
