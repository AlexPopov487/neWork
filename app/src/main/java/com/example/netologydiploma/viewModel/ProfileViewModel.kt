package com.example.netologydiploma.viewModel

import androidx.lifecycle.ViewModel
import com.example.netologydiploma.auth.AppAuth

class ProfileViewModel: ViewModel() {

    fun onSignOut() {
        AppAuth.getInstance().removeAuth()
    }
}