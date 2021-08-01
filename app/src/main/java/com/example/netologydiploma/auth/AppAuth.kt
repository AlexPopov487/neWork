package com.example.netologydiploma.auth

import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


class AppAuth (private val prefs: SharedPreferences) {
    // store token and id for user auth validation
    private val idKey = "id"
    private val tokenKey = "token"

    private val _authStateFlow = MutableStateFlow(AuthState())
    val authStateFlow: StateFlow<AuthState> = _authStateFlow.asStateFlow()



    init {
        val id = prefs.getLong(idKey, 0L)
        val token = prefs.getString(tokenKey, null)

        if (id == 0L || token == null) {
            _authStateFlow.value = AuthState()
            prefs.edit()
                .clear()
                .apply()
        } else {
            _authStateFlow.value = AuthState(id, token)
        }
    }

    @Synchronized
    fun setAuth(id: Long, token: String) {
        _authStateFlow.value = AuthState(id, token)
        with(prefs.edit()) {
            putLong(idKey, id)
            putString(tokenKey, token)
            apply()
        }
    }

    @Synchronized
    fun removeAuth() {
        _authStateFlow.value = AuthState()
        with(prefs.edit()) {
            clear()
            commit()
        }
    }
}

data class AuthState(val id: Long = 0, val token: String? = null)
