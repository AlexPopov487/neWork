package com.example.netologydiploma.api

import com.example.netologydiploma.dto.Post
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }


private val okhttp = OkHttpClient.Builder()
    .addInterceptor(logging)
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(GsonConverterFactory.create())
    .baseUrl(ApiService.BASE_URL)
    .client(okhttp)
    .build()

object PostApi{
    val retrofitService : ApiService by lazy{
        retrofit.create(ApiService::class.java)
    }
}

interface ApiService {

    companion object {
        const val BASE_URL = "https://net-diploma.herokuapp.com/api/"
    }

    @GET("posts")
    suspend fun getAllPosts(): Response<List<Post>>

    @POST("posts")
    suspend fun createPost(@Body post: Post): Response<Post>

    @DELETE("posts/{id}")
    suspend fun deletePost(@Path("id")postId: Long): Response<Unit>

}