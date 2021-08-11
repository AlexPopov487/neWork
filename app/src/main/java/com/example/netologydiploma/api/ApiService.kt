package com.example.netologydiploma.api

import com.example.netologydiploma.dto.Event
import com.example.netologydiploma.dto.Job
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
    ): Response<AuthJsonModel>

    @GET("posts")
    suspend fun getAllPosts(): Response<List<Post>>

    @GET("events/latest")
    suspend fun getLatestEvents(@Query("count") count: Int): Response<List<Event>>

    @GET("events/{id}/before")
    suspend fun getEventsBefore(
        @Path("id") id: Long,
        @Query("count") count: Int
    ): Response<List<Event>>

    @GET("events/{id}/after")
    suspend fun getEventsAfter(
        @Path("id") id: Long,
        @Query("count") count: Int
    ): Response<List<Event>>

    @GET("posts/latest")
    suspend fun getLatestPosts(@Query("count") count: Int): Response<List<Post>>

    @GET("posts/{id}/before")
    suspend fun getPostsBefore(
        @Path("id") id: Long,
        @Query("count") count: Int
    ): Response<List<Post>>

    @GET("posts/{id}/after")
    suspend fun getPostsAfter(
        @Path("id") id: Long,
        @Query("count") count: Int
    ): Response<List<Post>>

    @GET("posts/{id}")
    suspend fun getPostById(@Path("id") id: Long): Response<Post>

    @POST("posts")
    suspend fun createPost(@Body post: Post): Response<Post>

    @POST("posts/{id}/likes")
    suspend fun likePostById(@Path("id") postId: Long): Response<Post>

    @DELETE("posts/{id}/likes")
    suspend fun dislikePostById(@Path("id") postId: Long): Response<Post>

    @DELETE("posts/{id}")
    suspend fun deletePost(@Path("id") postId: Long): Response<Unit>

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


    /** wall interaction */

    @GET("{authorId}/wall/latest")
    suspend fun getLatestWallPosts(
        @Path("authorId") authorId: Long,
        @Query("count") count: Int
    ): Response<List<Post>>

    @GET("{authorId}/wall/{id}/before")
    suspend fun getWallPostsBefore(
        @Path("id") id: Long,
        @Path("authorId") authorId: Long,
        @Query("count") count: Int
    ): Response<List<Post>>

    @GET("{authorId}/wall/{id}/after")
    suspend fun getWallPostsAfter(
        @Path("id") id: Long,
        @Path("authorId") authorId: Long,
        @Query("count") count: Int
    ): Response<List<Post>>


    /** job interaction */


    @GET("{userId}/jobs")
    suspend fun getAllUserJobs(@Path("userId")authorId: Long) : Response<List<Job>>

    @POST("my/jobs")
    suspend fun saveJob(@Body job: Job): Response<Job>

    @DELETE("my/jobs/{id}")
    suspend fun removeJobById(@Path("id") id: Long): Response<Unit>
}