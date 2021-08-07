package com.example.netologydiploma.data

import androidx.room.withTransaction
import com.example.netologydiploma.api.ApiService
import com.example.netologydiploma.db.AppDb
import com.example.netologydiploma.db.PostDao
import com.example.netologydiploma.dto.Event
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
    private val postApi: ApiService,
    private val db: AppDb
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
            val body = response.body()?.map {
                it.copy(likeCount = it.likeOwnerIds.size)
            } ?: throw ApiError(response.code())
            db.withTransaction {
                postDao.clearPostTable()
                postDao.insertPosts(body.toEntity())
            }
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

            postDao.insertPost(PostEntity.fromDto(getPostBody))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: SQLException) {
            throw  DbError
        } catch (e: Exception) {
            throw UndefinedError
        }
    }

    suspend fun likePost(post: Post) {
        try {
            // like in db
            val likedPost = post.copy(
                likeCount = if (post.likedByMe) post.likeCount.dec()
                else post.likeCount.inc(),
                likedByMe = !post.likedByMe
            )
            postDao.insertPost(PostEntity.fromDto(likedPost))

            // like on server
            val response = if (post.likedByMe) postApi.dislikePostById(post.id)
            else postApi.likePostById(post.id)

            if (!response.isSuccessful)
                throw ApiError(response.code())
        } catch (e: IOException) {
            // revert changes to init state
            postDao.insertPost(PostEntity.fromDto(post))
            throw NetworkError
        } catch (e: SQLException) {
            // revert changes to init state
            postDao.insertPost(PostEntity.fromDto(post))
            throw  DbError
        } catch (e: Exception) {
            // revert changes to init state
            postDao.insertPost(PostEntity.fromDto(post))
            throw UndefinedError
        }
    }



    suspend fun deletePost(postId: Long) {
        val postToDelete = postDao.getPostById(postId)
        try {
            postDao.deletePost(postId)

            val response = postApi.deletePost(postId)
            if (!response.isSuccessful) {
                postDao.insertPost(postToDelete)
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

