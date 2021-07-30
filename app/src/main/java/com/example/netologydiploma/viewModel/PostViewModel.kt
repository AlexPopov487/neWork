package com.example.netologydiploma.viewModel

import android.app.Application
import androidx.lifecycle.*
import com.example.netologydiploma.auth.AppAuth
import com.example.netologydiploma.data.PostRepository
import com.example.netologydiploma.db.AppDb
import com.example.netologydiploma.dto.Post
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import okhttp3.Dispatcher

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

    @ExperimentalCoroutinesApi
    val postList: LiveData<List<Post>> = AppAuth.getInstance()
        .authStateFlow
        .flatMapLatest { (myId, _) ->
            repository.getAllPosts()
                .map { postList ->
                    postList.map {
                        it.copy(ownedByMe = myId == it.authorId)
                    }
                }
        }.asLiveData(Dispatchers.Default)

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


