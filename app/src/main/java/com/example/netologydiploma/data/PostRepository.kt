package com.example.netologydiploma.data

import com.example.netologydiploma.api.ApiService
import com.example.netologydiploma.db.PostDao
import com.example.netologydiploma.dto.Post
import com.example.netologydiploma.entity.PostEntity
import com.example.netologydiploma.entity.toDto
import com.example.netologydiploma.entity.toEntity
import com.example.netologydiploma.error.ApiError
import com.example.netologydiploma.error.DbError
import com.example.netologydiploma.error.NetworkError
import com.example.netologydiploma.error.UndefinedError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.sql.SQLException
import javax.inject.Inject

class PostRepository @Inject constructor(
    private val postDao: PostDao,
    private val postApi: ApiService
) {

    fun getAllPosts(): Flow<List<Post>> {
        return postDao.getAllPosts().map { it.toDto() }
    }

    suspend fun loadPostsFromWeb() {
        try {
            val response = postApi.getAllPosts()
            if (!response.isSuccessful) {
                throw ApiError(response.code())
            }
            val body = response.body() ?: throw ApiError(response.code())
            postDao.createPosts(body.toEntity())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: SQLException) {
            throw  DbError
        } catch (e: Exception) {
            throw UndefinedError
        }
    }

    suspend fun createPost(post: Post) {
        try {
            val createPostResponse = postApi.createPost(post)
            if (!createPostResponse.isSuccessful) {
                throw ApiError(createPostResponse.code())
            }
            val createPostBody = createPostResponse.body() ?: throw ApiError(
                createPostResponse.code())

            // additional network call to get the created post is required
            // because createPostBody doesn't have authorName set (it is set via backend),
            // so we cannot pass createPostBody to db and prefer to get the newly created
            // post explicitly
            val getPostResponse = postApi.getPostById(createPostBody.id)
            if (!getPostResponse.isSuccessful) {
                throw ApiError(getPostResponse.code())
            }
            val getPostBody = getPostResponse.body() ?: throw ApiError(
                getPostResponse.code())

            postDao.createPost(PostEntity.fromDto(getPostBody))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: SQLException) {
            throw  DbError
        } catch (e: Exception) {
            throw UndefinedError
        }
    }

    suspend fun deletePost(postId: Long) {
        val postToDelete = postDao.getPostById(postId)
        try {
            postDao.deletePost(postId)

            val response = postApi.deletePost(postId)
            if (!response.isSuccessful) {
                postDao.createPost(postToDelete)
                throw ApiError(response.code())
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: SQLException) {
            throw  DbError
        } catch (e: Exception) {
            throw UndefinedError
        }
    }
}

