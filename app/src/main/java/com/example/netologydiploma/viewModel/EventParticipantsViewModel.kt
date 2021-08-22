package com.example.netologydiploma.viewModel

import androidx.lifecycle.*
import com.example.netologydiploma.auth.AppAuth
import com.example.netologydiploma.data.UserRepository
import com.example.netologydiploma.error.AppError
import com.example.netologydiploma.model.FeedStateModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EventParticipantsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val auth: AppAuth
) :
    ViewModel() {

    private val _dataState = MutableLiveData(FeedStateModel())
    val dataState: LiveData<FeedStateModel>
        get() = _dataState

    suspend fun getParticipants(eventId: Long) = auth.authStateFlow.flatMapLatest { (myId, _) ->
            userRepository.getEventParticipants(eventId).map { userList ->
                userList.map { it.copy(isItMe = it.id == myId) }
            }
        }


    init {
        loadAllUsers()
    }


    private fun loadAllUsers() {
        viewModelScope.launch {
            try {
                _dataState.value = FeedStateModel(isLoading = true)
                userRepository.loadAllUsers()
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