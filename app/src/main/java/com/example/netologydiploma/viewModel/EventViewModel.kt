package com.example.netologydiploma.viewModel

import androidx.lifecycle.*
import com.example.netologydiploma.auth.AppAuth
import com.example.netologydiploma.data.EventRepository
import com.example.netologydiploma.dto.Event
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
class EventViewModel @Inject constructor(
    private val repository: EventRepository,
    appAuth: AppAuth
) : ViewModel() {

    private val _dataState = MutableLiveData(FeedStateModel())
    val dataState: LiveData<FeedStateModel>
        get() = _dataState

    fun invalidateDataState() {
        _dataState.value = FeedStateModel()
    }

    private val _editedEvent = MutableLiveData<Event?>(null)
    val editedEvent: LiveData<Event?>
        get() = _editedEvent

    init {
        loadEventsFromWeb()
    }

    @ExperimentalCoroutinesApi
    val eventList: LiveData<List<Event>> = appAuth
        .authStateFlow
        .flatMapLatest { (myId, _) ->
            repository.getAllEvents().map { eventsList ->
                eventsList.map {
                    it.copy(ownedByMe = it.authorId == myId)
                }
            }
        }.asLiveData(Dispatchers.Default)

    private fun loadEventsFromWeb() {
        viewModelScope.launch {
            try {
                _dataState.value = (FeedStateModel(isLoading = true))
                repository.loadEventsFromWeb()
                _dataState.value = (FeedStateModel(isLoading = false))
            } catch (e: Exception) {
                _dataState.value = (FeedStateModel(
                    hasError = true,
                    errorMessage = AppError.getMessage(e)
                ))
            }
        }
    }

    fun updateEvents() {
        viewModelScope.launch {
            try {
                _dataState.value = (FeedStateModel(isRefreshing = true))
                repository.loadEventsFromWeb()
                _dataState.value = (FeedStateModel(isRefreshing = false))
            } catch (e: Exception) {
                _dataState.value = (FeedStateModel(
                    hasError = true,
                    errorMessage = AppError.getMessage(e)
                ))
            }
        }
    }

    fun editEvent(editedEvent: Event) {
        _editedEvent.value = editedEvent
    }

    fun invalidateEditedEvent() {
        _editedEvent.value = null
    }

    fun saveEvent(event: Event) {
        viewModelScope.launch {
            try {
                _dataState.value = (FeedStateModel(isLoading = true))
                repository.createEvent(event)
                _dataState.value = (FeedStateModel(isLoading = false))
            } catch (e: Exception) {
                _dataState.value = (FeedStateModel(
                    hasError = true,
                    errorMessage = AppError.getMessage(e)
                ))
            } finally {
                invalidateEditedEvent()
            }
        }
    }

    fun likeEvent(event: Event) {
        viewModelScope.launch {
            try {
                _dataState.value = FeedStateModel()
                repository.likeEvent(event)
            } catch (e: Exception) {
                _dataState.value = (FeedStateModel(
                    hasError = true,
                    errorMessage = AppError.getMessage(e)
                ))
            }
        }
    }

    fun participateInEvent(event: Event) {
        viewModelScope.launch {
            try {
                _dataState.value = FeedStateModel(isLoading = true)
                repository.participateInEvent(event)
                _dataState.value = FeedStateModel(isLoading = false)
            } catch (e: Exception) {
                _dataState.value = (FeedStateModel(
                    hasError = true,
                    errorMessage = AppError.getMessage(e)
                ))
            }
        }
    }

    fun deleteEvent(eventId: Long) {
        viewModelScope.launch {
            try {
                _dataState.value = (FeedStateModel(isLoading = true))
                repository.deleteEvent(eventId)
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