package com.example.netologydiploma.viewModel

import androidx.lifecycle.*
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.example.netologydiploma.auth.AppAuth
import com.example.netologydiploma.data.PostRepository
import com.example.netologydiploma.dto.Post
import com.example.netologydiploma.error.AppError
import com.example.netologydiploma.model.FeedStateModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalPagingApi
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


    private val cached = repository.getAllPosts().cachedIn(viewModelScope)

    @ExperimentalCoroutinesApi
    val postList: Flow<PagingData<Post>> = appAuth
        .authStateFlow
        .flatMapLatest { (myId, _) ->
            cached.map { postList ->
                postList.map { it.copy(ownedByMe = it.authorId == myId) }
            }
        }


            fun editPost(editedPost: Post) {
                _editedPost.value = editedPost
            }

            fun invalidateEditPost() {
                _editedPost.value = null
            }


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
                        invalidateEditPost()
                    }
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


