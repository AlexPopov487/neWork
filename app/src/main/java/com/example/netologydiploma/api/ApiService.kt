package com.example.netologydiploma.api

import com.example.netologydiploma.dto.Post
import com.example.netologydiploma.model.AuthJsonModel
import retrofit2.Response
import retrofit2.http.*


interface ApiService {

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