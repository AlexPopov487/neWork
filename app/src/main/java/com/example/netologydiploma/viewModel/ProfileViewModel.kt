package com.example.netologydiploma.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.example.netologydiploma.auth.AppAuth
import com.example.netologydiploma.data.ProfileRepository
import com.example.netologydiploma.dto.Job
import com.example.netologydiploma.dto.Post
import com.example.netologydiploma.dto.User
import com.example.netologydiploma.error.AppError
import com.example.netologydiploma.model.FeedStateModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val appAuth: AppAuth,
    private val repository: ProfileRepository
) : ViewModel() {

    val myId: Long = appAuth.authStateFlow.value.id

    private val _currentUser = MutableLiveData(User())
    val currentUser: LiveData<User>
        get() = _currentUser

    private val _dataState = MutableLiveData(FeedStateModel())
    val dataState: LiveData<FeedStateModel>
        get() = _dataState

    fun invalidateDataState() {
        _dataState.value = FeedStateModel()
    }

    private val _profileUserId = MutableLiveData<Long?>()
    val profileUserId: LiveData<Long?>
        get() = _profileUserId


    @ExperimentalCoroutinesApi
    @ExperimentalPagingApi
    fun getWallPosts(): Flow<PagingData<Post>> = appAuth.authStateFlow
        .flatMapLatest { (myId, _) ->
            repository.getAllPosts(_profileUserId.value!!)
                .map { postList ->
                    postList.map {
                        it.copy(
                            ownedByMe = it.authorId == myId
                        )
                    }
                }
        }
        .cachedIn(viewModelScope)

    fun setAuthorId(authorId: Long) {
        // если никакого id не передали (т.е. id = -1L),то пользователь зашел из меню к себе в профиль
        _profileUserId.value = if (authorId == -1L) appAuth.authStateFlow.value.id
        else authorId
    }

    fun getUserById() {
        viewModelScope.launch {
            try {
                _dataState.value = FeedStateModel(isLoading = true)
                val fetchedUser = repository.getUserById(_profileUserId.value!!)
                _currentUser.value = fetchedUser
                _dataState.value = FeedStateModel()
            } catch (e: Exception) {
                _dataState.value = FeedStateModel(
                    hasError = true,
                    errorMessage = AppError.getMessage(e)
                )
            }
        }
    }

    fun loadJobsFromServer() {
        viewModelScope.launch {
            try {
                _dataState.value = FeedStateModel(isLoading = true)
                repository.loadJobsFromServer(_profileUserId.value!!)
                _dataState.value = FeedStateModel()
            } catch (e: Exception) {
                _dataState.value = FeedStateModel(
                    hasError = true,
                    errorMessage = AppError.getMessage(e)
                )
            }
        }
    }



    fun getLatestWallPosts(){
        viewModelScope.launch {
            try {
                _dataState.value = FeedStateModel(isLoading = true)
                repository.getLatestWallPosts(_profileUserId.value!!)
                _dataState.value = FeedStateModel()
            } catch (e: Exception) {
                _dataState.value = FeedStateModel(
                    hasError = true,
                    errorMessage = AppError.getMessage(e)
                )
            }
        }
    }

    fun refreshLatestWallPosts(){
        viewModelScope.launch {
            try {
                _dataState.value = FeedStateModel(isRefreshing = true)
                repository.getLatestWallPosts(_profileUserId.value!!)
                _dataState.value = FeedStateModel()
            } catch (e: Exception) {
                _dataState.value = FeedStateModel(
                    hasError = true,
                    errorMessage = AppError.getMessage(e)
                )
            }
        }
    }


    fun likeWallPostById(post: Post) {
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

    fun getAllJobs(): LiveData<List<Job>> = repository.getAllJobs()

    fun createNewJob(job: Job) {
        viewModelScope.launch {
            try {
                _dataState.value = FeedStateModel(isLoading = true)
                repository.createJob(job)
                _dataState.value = FeedStateModel(isLoading = false)
            } catch (e: Exception) {
                _dataState.value = FeedStateModel(
                    hasError = true,
                    errorMessage = AppError.getMessage(e)
                )
            }
        }
    }

    fun deleteJobById(id: Long) {
        viewModelScope.launch {
            try {
                _dataState.value = FeedStateModel(isLoading = true)
                repository.deleteJobById(id)
                _dataState.value = FeedStateModel(isLoading = false)
            } catch (e: Exception) {
                _dataState.value = FeedStateModel(
                    hasError = true,
                    errorMessage = AppError.getMessage(e)
                )
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

    fun onSignOut() {
        appAuth.removeAuth()
    }
}