package com.example.netologydiploma.data


import com.example.netologydiploma.api.PostApi
import com.example.netologydiploma.error.ApiError
import com.example.netologydiploma.error.NetworkError
import com.example.netologydiploma.error.UndefinedError
import com.example.netologydiploma.model.AuthJsonModel
import java.io.IOException

class SignInUpRepository {

    suspend fun onSignIn(login: String, password: String): AuthJsonModel {
        try {
            val response = PostApi.retrofitService.signIn(login, password)
            if (!response.isSuccessful) {
                throw ApiError(response.code())
            }
            return response.body() ?: throw ApiError(response.code())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UndefinedError
        }
    }

    suspend fun onSignUp(login: String, password: String, userName: String): AuthJsonModel {
        try {
            val response = PostApi.retrofitService.signUp(login, password, userName)
            if (!response.isSuccessful) {
                throw ApiError(response.code())
            }
            return response.body() ?: throw ApiError(response.code())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UndefinedError
        }
    }
}