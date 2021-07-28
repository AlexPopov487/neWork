package com.example.netologydiploma.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.example.netologydiploma.api.PostApi
import com.example.netologydiploma.db.PostDao
import com.example.netologydiploma.dto.Post
import com.example.netologydiploma.entity.PostEntity
import com.example.netologydiploma.entity.toDto
import com.example.netologydiploma.entity.toEntity
import java.io.IOException

class PostRepository(private val postDao: PostDao) {

    fun getAllPosts(): LiveData<List<Post>> {
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
            val response = PostApi.retrofitService.createPost(post)
            if (!response.isSuccessful) {
                Error().printStackTrace()
                throw Error()
                // TODO create a comprehensive wrapper for all app errors
            }
            // TODO create a comprehensive wrapper for all app errors
            val body = response.body() ?: throw Error()
            postDao.createPost(PostEntity.fromDto(body))
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

