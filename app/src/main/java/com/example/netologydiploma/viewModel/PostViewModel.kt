package com.example.netologydiploma.viewModel

import androidx.lifecycle.*
import com.example.netologydiploma.auth.AppAuth
import com.example.netologydiploma.data.PostRepository
import com.example.netologydiploma.dto.Post
import com.example.netologydiploma.error.AppError
import com.example.netologydiploma.model.FeedStateModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostViewModel @Inject constructor(
    private val repository: PostRepository,
    appAuth: AppAuth
) : ViewModel() {

    private val _dataState = MutableLiveData(FeedStateModel())
    val dataState: LiveData<FeedStateModel>
        get() = _dataState

    fun invalidateDataState() {
        _dataState.value = FeedStateModel()
    }


    private val _editedPost = MutableLiveData<Post?>(null)
    val editedPost: LiveData<Post?>
        get() = _editedPost


    init {
        loadPostsFromWeb()
    }

    fun loadPostsFromWeb() {
        viewModelScope.launch {
            try {
                _dataState.value = (FeedStateModel(isLoading = true))
                repository.loadPostsFromWeb()
                _dataState.value = (FeedStateModel(isLoading = false))
            } catch (e: Exception) {
                _dataState.value = (FeedStateModel(
                    hasError = true,
                    errorMessage = AppError.getMessage(e)
                ))
            }
        }
    }

    fun editPost(editedPost: Post) {
        _editedPost.value = editedPost
    }

    fun invalidateEditPost() {
        _editedPost.value = null
    }

    @ExperimentalCoroutinesApi
    val postList: LiveData<List<Post>> = appAuth
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
            try {
                _dataState.value = (FeedStateModel(isLoading = true))
                repository.createPost(post)
                _dataState.value = (FeedStateModel(isLoading = false))
            } catch (e: Exception) {
                _dataState.value = (FeedStateModel(
                    hasError = true,
                    errorMessage = AppError.getMessage(e)
                ))
            } finally {
                invalidateEditPost()            }
        }
    }


    fun likePost(post: Post){
        viewModelScope.launch {
          try{
              _dataState.value = FeedStateModel()
              repository.likePost(post)
          } catch (e : Exception) {
              _dataState.value = (FeedStateModel(
                  hasError = true,
                  errorMessage = AppError.getMessage(e)
              ))
          }
        }
    }

    fun deletePost(postId: Long) {
        viewModelScope.launch {
            try {
                _dataState.value = (FeedStateModel(isLoading = true))
                repository.deletePost(postId)
                _dataState.value = (FeedStateModel(isLoading = false))
            } catch (e: Exception) {
                _dataState.value = (FeedStateModel(
                    hasError = true,
                    errorMessage = AppError.getMessage(e)
                ))
            }
        }
    }

}


