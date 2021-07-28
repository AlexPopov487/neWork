package com.example.netologydiploma.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.netologydiploma.data.PostRepository
import com.example.netologydiploma.db.AppDb
import com.example.netologydiploma.dto.Post
import kotlinx.coroutines.launch

class PostViewModel(application: Application): AndroidViewModel(application) {
    private val repository = PostRepository(AppDb.getInstance(application).postDao())

    init {
        loadPostsFromWeb()
    }

    private fun loadPostsFromWeb() {
        viewModelScope.launch {
            repository.loadPostsFromWeb()
        }
    }

    val postList = repository.getAllPosts()

    fun savePost(post: Post) {
        viewModelScope.launch {
            repository.createPost(post)
        }
    }


    fun deletePost(postId: Long) {
        viewModelScope.launch {
            repository.deletePost(postId)
        }
    }

}


