package com.example.netologydiploma.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.netologydiploma.data.PostRepository
import com.example.netologydiploma.db.AppDb
import com.example.netologydiploma.db.PostEntity
import kotlinx.coroutines.launch

class PostViewModel(application: Application): AndroidViewModel(application) {
    private val repository = PostRepository(AppDb.getInstance(application).postDao())


    val postList = repository.getAllPosts()

    fun savePost(post: PostEntity) {
        viewModelScope.launch {
            repository.createPost(post)

        }
    }

}