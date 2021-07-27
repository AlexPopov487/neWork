package com.example.netologydiploma.data

import androidx.lifecycle.LiveData
import com.example.netologydiploma.db.PostDao
import com.example.netologydiploma.db.PostEntity

class PostRepository(private val postDao: PostDao) {

    fun getAllPosts(): LiveData<List<PostEntity>> {
       return postDao.getAllPosts()
    }

    suspend fun createPost(post: PostEntity) {
        postDao.createPost(post)
    }
}