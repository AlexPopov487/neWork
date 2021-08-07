package com.example.netologydiploma.api

import com.example.netologydiploma.dto.Event
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

    @POST("posts/{id}/likes")
    suspend fun likePostById(@Path("id") postId: Long): Response<Post>

    @DELETE("posts/{id}/likes")
    suspend fun dislikePostById(@Path("id") postId: Long): Response<Post>

    @DELETE("posts/{id}")
    suspend fun deletePost(@Path("id") postId: Long): Response<Unit>

    @GET("events")
    suspend fun getAllEvents() : Response<List<Event>>

    @GET("events/{id}")
    suspend fun getEventById(@Path("id")id: Long) : Response<Event>

    @POST("events")
    suspend fun createEvent(@Body event: Event) : Response<Event>

    @POST("events/{id}/likes")
    suspend fun likeEventById(@Path("id") eventId: Long): Response<Event>

    @DELETE("events/{id}/likes")
    suspend fun dislikeEventById(@Path("id") eventId: Long): Response<Event>

    @POST("events/{id}/participants")
    suspend fun participateEventById(@Path("id") eventId: Long): Response<Event>

    @DELETE("events/{id}/participants")
    suspend fun unparticipateEventById(@Path("id") eventId: Long): Response<Event>

    @DELETE("event/{id}")
    suspend fun deleteEvent(@Path("id") eventId: Long): Response<Unit>

}