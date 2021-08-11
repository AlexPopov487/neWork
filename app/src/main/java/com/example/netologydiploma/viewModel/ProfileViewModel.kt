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

    private val _dataState = MutableLiveData(FeedStateModel())
    val dataState: LiveData<FeedStateModel>
        get() = _dataState

    fun invalidateDataState() {
        _dataState.value = FeedStateModel()
    }


    @ExperimentalCoroutinesApi
    @ExperimentalPagingApi
    fun getWallPosts(authorId: Long): Flow<PagingData<Post>> = appAuth.authStateFlow
        .flatMapLatest { (myId, _) ->
            repository.getAllPosts(authorId)
                .map { postList ->
                    postList.map {
                        it.copy(
                            ownedByMe = it.authorId == myId
                        )
                    }
                }
        }
        .cachedIn(viewModelScope)

    fun setAuthorId(authorId: Long): Long {
        return if (authorId == -1L) appAuth.authStateFlow.value.id
        else authorId
    }

    fun setAuthorName(authorName: String?): String {
        // если authorName = null, значит пользователь зашел в свой профиль через вкладку
        // в меню. Тогда берем не имя польщователя, а логин из authStateFlow.
        return authorName ?: appAuth.authStateFlow.value.login ?: "Your profile"
    }


    suspend fun loadJobsFromServer(authorId: Long) {
        try {
            _dataState.value = FeedStateModel(isLoading = true)
            repository.loadJobsFromServer(authorId)
            _dataState.value = FeedStateModel(isLoading = false)
        } catch (e: Exception) {
            _dataState.value = FeedStateModel(
                hasError = true,
                errorMessage = AppError.getMessage(e)
            )
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

    fun onSignOut() {
        appAuth.removeAuth()
    }
}