package com.example.netologydiploma.viewModel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.example.netologydiploma.auth.AppAuth
import com.example.netologydiploma.data.PostRepository
import com.example.netologydiploma.dto.AttachmentType
import com.example.netologydiploma.dto.MediaUpload
import com.example.netologydiploma.dto.Post
import com.example.netologydiploma.error.AppError
import com.example.netologydiploma.model.FeedStateModel
import com.example.netologydiploma.model.MediaModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@ExperimentalPagingApi
@HiltViewModel
class PostViewModel @Inject constructor(
    private val repository: PostRepository,
    appAuth: AppAuth
) : ViewModel() {

    private val noMedia = MediaModel()


    private val _dataState = MutableLiveData(FeedStateModel())
    val dataState: LiveData<FeedStateModel>
        get() = _dataState

    fun invalidateDataState() {
        _dataState.value = FeedStateModel()
    }

    private val _editedPost = MutableLiveData<Post?>(null)
    val editedPost: LiveData<Post?>
        get() = _editedPost

    private val _media = MutableLiveData(noMedia)
    val media: LiveData<MediaModel>
        get() = _media


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
                when (_media.value) {
                    noMedia -> repository.createPost(post)
                    else -> {
                        when (_media.value?.type) {
                            AttachmentType.IMAGE -> {
                                _media.value?.file?.let { file ->
                                    repository.saveWithAttachment(
                                        post,
                                        MediaUpload(file),
                                        AttachmentType.IMAGE
                                    )
                                }
                            }
                            AttachmentType.VIDEO -> {
                                _media.value?.file?.let { file ->
                                    repository.saveWithAttachment(
                                        post,
                                        MediaUpload(file),
                                        AttachmentType.VIDEO
                                    )
                                }
                            }
                            AttachmentType.AUDIO -> {
                                _media.value?.file?.let { file ->
                                    repository.saveWithAttachment(
                                        post,
                                        MediaUpload(file),
                                        AttachmentType.AUDIO
                                    )
                                }
                            }
                            null -> repository.createPost(post)
                        }
                    }


                }

                _dataState.value = FeedStateModel()
            } catch (e: Exception) {
                _dataState.value = (FeedStateModel(
                    hasError = true,
                    errorMessage = AppError.getMessage(e)
                ))
            } finally {
                invalidateEditPost()
                _media.value = noMedia
            }
        }
    }


    fun likePost(post: Post) {
        viewModelScope.launch {
            try {
                _dataState.value = FeedStateModel()
                repository.likePost(post)
            } catch (e: Exception) {
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

    fun changeMedia(uri: Uri?, file: File?, type: AttachmentType?) {
        _media.value = MediaModel(uri, file, type)
    }


}


