package com.example.netologydiploma.viewModel

import androidx.lifecycle.*
import com.example.netologydiploma.auth.AppAuth
import com.example.netologydiploma.data.UserRepository
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
class UsersViewModel @Inject constructor(
    private val repository: UserRepository,
    auth: AppAuth
) : ViewModel() {

    private val _dataState = MutableLiveData(FeedStateModel())
    val dataState: LiveData<FeedStateModel>
        get() = _dataState

    init {
        loadAllUsers()
    }

    private val users = repository.getAllUsers()

    @ExperimentalCoroutinesApi
    val userList = auth.authStateFlow.flatMapLatest { (myId, _) ->
        users.map { list ->
            list.map { userItem ->
                userItem.copy(isItMe = myId == userItem.id)
            }
        }
    }.asLiveData(Dispatchers.Default)


    private fun loadAllUsers() {
        viewModelScope.launch {
            try {
                _dataState.value = FeedStateModel(isLoading = true)
                repository.loadAllUsers()
                _dataState.value = FeedStateModel()
            } catch (e: Exception) {
                _dataState.value = FeedStateModel(
                    hasError = true,
                    errorMessage = AppError.getMessage(e)
                )
            }
        }
    }

    fun refreshUsers() {
        viewModelScope.launch {
            try {
                _dataState.value = FeedStateModel(isRefreshing = true)
                repository.loadAllUsers()
                _dataState.value = FeedStateModel()
            } catch (e: Exception) {
                _dataState.value = FeedStateModel(
                    hasError = true,
                    errorMessage = AppError.getMessage(e)
                )
            }
        }
    }

    fun invalidateDataState() {
        _dataState.value = FeedStateModel()
    }
}