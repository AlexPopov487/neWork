package com.example.netologydiploma.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.example.netologydiploma.auth.AppAuth
import com.example.netologydiploma.auth.AuthState
import kotlinx.coroutines.Dispatchers

class AuthViewModel : ViewModel() {
    val authState: LiveData<AuthState> = AppAuth.getInstance()
        .authStateFlow
        .asLiveData(Dispatchers.Default)

    val isAuthenticated: Boolean
        get() = AppAuth.getInstance().authStateFlow.value.id != 0L
}