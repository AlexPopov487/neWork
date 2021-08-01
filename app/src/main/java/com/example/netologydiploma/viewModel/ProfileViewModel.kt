package com.example.netologydiploma.viewModel

import androidx.lifecycle.ViewModel
import com.example.netologydiploma.auth.AppAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(private val appAuth: AppAuth)
    : ViewModel() {

    fun onSignOut() {
        appAuth.removeAuth()
    }
}