package ru.netology.nmedia.api


import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import ru.netology.nmedia.BuildConfig
import okhttp3.logging.HttpLoggingInterceptor

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.Post


private const val BASE_URL = "${BuildConfig.BASE_URL}/api/slow/"

private val logging = HttpLoggingInterceptor().apply {
    if (BuildConfig.DEBUG) {
        level = HttpLoggingInterceptor.Level.BODY
    }
}

private val okhttp = OkHttpClient.Builder()
    .addInterceptor(logging)
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(GsonConverterFactory.create())
    .baseUrl(BASE_URL)
    .client(okhttp)
    .build()

interface PostApiService {
    @GET("posts")
    suspend fun getAll(): Response<List<Post>>

    @GET("posts/{id}")
    suspend fun getById(@Path("id") id: Long): Response<Post>

    @GET("posts/{id}/newer")
    suspend fun getNewer(@Path("id") id: Long): Response<List<Post>>

    @POST("posts")
    suspend fun save(@Body post: Post) : Response<Post>

    @DELETE("posts/{id}")
    suspend fun removeById(@Path("id")id: Long) : Response<Unit>

    @POST("posts/{id}/likes")
    suspend fun likeById(@Path("id")id: Long) : Response<Post>

    @DELETE("posts/{id}/likes")
    suspend fun unlikeById(@Path("id")id: Long) : Response<Post>

    @Multipart
    @POST("media")
    suspend fun uploadMedia(@Part file: MultipartBody.Part): Response<Media>

}

object PostApi{
    val retrofitService: PostApiService by lazy {
        retrofit.create()
    }
}