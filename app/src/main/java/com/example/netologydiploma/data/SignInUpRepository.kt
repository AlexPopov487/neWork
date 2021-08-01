package com.example.netologydiploma.data


import com.example.netologydiploma.api.ApiService
import com.example.netologydiploma.error.ApiError
import com.example.netologydiploma.error.NetworkError
import com.example.netologydiploma.error.UndefinedError
import com.example.netologydiploma.model.AuthJsonModel
import java.io.IOException
import javax.inject.Inject

class SignInUpRepository @Inject constructor(
    private val postApi: ApiService
) {

    suspend fun onSignIn(login: String, password: String): AuthJsonModel {
        try {
            val response = postApi.signIn(login, password)
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
            val response = postApi.signUp(login, password, userName)
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