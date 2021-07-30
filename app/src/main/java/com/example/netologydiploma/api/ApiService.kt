package com.example.netologydiploma.api

import com.example.netologydiploma.BuildConfig
import com.example.netologydiploma.auth.AppAuth
import com.example.netologydiploma.dto.Post
import com.example.netologydiploma.model.AuthJsonModel
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
    .addInterceptor { chain ->
        AppAuth.getInstance().authStateFlow.value.token?.let { token ->
            val newRequest = chain.request().newBuilder()
                .addHeader("Authorization", token)
                .build()
            return@addInterceptor chain.proceed(newRequest)
        }
        chain.proceed(chain.request())
    }
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
        const val BASE_URL = "${BuildConfig.BASE_URL}/api/"
    }

    @FormUrlEncoded
    @POST("users/authentication")
    suspend fun signIn(
        @Field("login") login: String,
        @Field("pass") password: String
    ): Response<AuthJsonModel>

    @FormUrlEncoded
    @POST("users/registration")
    suspend fun signUp(
        @Field("login") login: String,
        @Field("pass") password: String,
        @Field("name") name: String
    )   : Response<AuthJsonModel>

    @GET("posts")
    suspend fun getAllPosts(): Response<List<Post>>

    @GET("posts/{id}")
    suspend fun getPostById(@Path("id")id: Long): Response<Post>

    @POST("posts")
    suspend fun createPost(@Body post: Post): Response<Post>

    @DELETE("posts/{id}")
    suspend fun deletePost(@Path("id") postId: Long): Response<Unit>

}