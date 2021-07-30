package com.example.netologydiploma.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.netologydiploma.auth.AppAuth
import com.example.netologydiploma.data.SignInUpRepository
import kotlinx.coroutines.launch


class SignInUpViewModel: ViewModel() {
    private val repository = SignInUpRepository()

    private val _isSignedIn = MutableLiveData(false)
    val isSignedIn : LiveData<Boolean>
    get() = _isSignedIn

    fun invalidateSignedInState() {
        _isSignedIn.value = false
    }

    fun onSignIn(login:String, password: String){
        viewModelScope.launch {

            val idAndToken = repository.onSignIn(login, password)
            val id = idAndToken.userId ?: 0L
            val token = idAndToken.token ?: "N/A"
            AppAuth.getInstance().setAuth(id= id, token = token)
            _isSignedIn.value = true
        }
    }

    fun onSignUp(login: String, password: String, userName: String) {
        viewModelScope.launch {
            repository.onSignUp(login, password, userName)
        }
    }

}