package com.example.netologydiploma.data

import com.example.netologydiploma.api.PostApi
import com.example.netologydiploma.db.PostDao
import com.example.netologydiploma.dto.Post
import com.example.netologydiploma.entity.PostEntity
import com.example.netologydiploma.entity.toDto
import com.example.netologydiploma.entity.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.IOException

class PostRepository(private val postDao: PostDao) {

    fun getAllPosts(): Flow<List<Post>> {
        return postDao.getAllPosts().map { it.toDto() }
    }

    suspend fun loadPostsFromWeb() {
        try {
            val response = PostApi.retrofitService.getAllPosts()
            if (!response.isSuccessful){
                throw Error()
                // TODO create a comprehensive wrapper for all app errors
            }
            // TODO create a comprehensive wrapper for all app errors
            val body = response.body() ?: throw Error()
            postDao.createPosts(body.toEntity())
        } catch (e: IOException) {
            e.printStackTrace()
            // TODO create a comprehensive wrapper for all app errors
        } catch (e: Exception) {
            e.printStackTrace()
            // TODO create a comprehensive wrapper for all app errors
        }

    }

    suspend fun createPost(post: Post) {
        try {
            val createPostResponse = PostApi.retrofitService.createPost(post)
            if (!createPostResponse.isSuccessful) {
                Error().printStackTrace()
                throw Error()
                // TODO create a comprehensive wrapper for all app errors
            }
            // TODO create a comprehensive wrapper for all app errors
            val createPostBody = createPostResponse.body() ?: throw Error()

            // additional network call to get the created post is required
            // because createPostBody doesn't have authorName set (it is set via backend),
            // so we cannot pass createPostBody to db and prefer to get the newly created
            // post explicitly
            val getPostResponse = PostApi.retrofitService.getPostById(createPostBody.id)
            val getPostBody = getPostResponse.body() ?: throw Error()

            postDao.createPost(PostEntity.fromDto(getPostBody))
        } catch (e: IOException) {
            e.printStackTrace()
            // TODO create a comprehensive wrapper for all app errors
        } catch (e: Exception) {
            e.printStackTrace()
            // TODO create a comprehensive wrapper for all app errors
        }
    }

    suspend fun deletePost(postId: Long) {
        val postToDelete = postDao.getPostById(postId)
        try {
            postDao.deletePost(postId)

            val response = PostApi.retrofitService.deletePost(postId)
            if (!response.isSuccessful) {
                postDao.createPost(postToDelete)
                throw Error()
                // TODO create a comprehensive wrapper for all app errors
            }
        } catch (e: IOException) {
            e.printStackTrace()
            // TODO create a comprehensive wrapper for all app errors
        } catch (e: Exception) {
            e.printStackTrace()
            // TODO create a comprehensive wrapper for all app errors
        }
    }
}

