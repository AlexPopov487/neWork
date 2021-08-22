package com.example.netologydiploma.api

import com.example.netologydiploma.dto.*
import com.example.netologydiploma.model.AuthJsonModel
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*


interface ApiService {

    @POST("users/push-tokens")
    suspend fun saveToken(@Body pushToken: PushToken): Response<Unit>

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

    @Multipart
    @POST("users/registration")
    suspend fun signUpWithAvatar(
        @Part login: MultipartBody.Part,
        @Part pass: MultipartBody.Part,
        @Part name: MultipartBody.Part,
        @Part file: MultipartBody.Part) : Response<AuthJsonModel>


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

    @DELETE("events/{id}")
    suspend fun deleteEvent(@Path("id") id: Long): Response<Unit>


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


    @POST("{authorId}/wall/{id}/likes")
    suspend fun likeWallPostById(@Path("id") postId: Long): Response<Post>

    @DELETE("{authorId}/wall/{id}/likes")
    suspend fun dislikeWallPostById(@Path("id") postId: Long): Response<Post>

    /** job interaction */

    @GET("{userId}/jobs")
    suspend fun getAllUserJobs(@Path("userId")authorId: Long) : Response<List<Job>>

    @POST("my/jobs")
    suspend fun saveJob(@Body job: Job): Response<Job>

    @DELETE("my/jobs/{id}")
    suspend fun removeJobById(@Path("id") id: Long): Response<Unit>

    /** HANDLING MEDIA REQUESTS */
    @Multipart
    @POST("media")
    suspend fun saveMediaFile(@Part file: MultipartBody.Part) : Response<MediaDownload>

    /** USER INTERACTION */
    @GET("users/{id}")
    suspend fun getUserById(@Path("id") userId: Long) : Response<User>

    @GET("users/")
    suspend fun getAllUsers() : Response<List<User>>
}