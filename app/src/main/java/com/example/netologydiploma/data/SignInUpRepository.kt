package com.example.netologydiploma.data


import com.example.netologydiploma.api.ApiService
import com.example.netologydiploma.dto.MediaUpload
import com.example.netologydiploma.error.ApiError
import com.example.netologydiploma.error.NetworkError
import com.example.netologydiploma.error.UndefinedError
import com.example.netologydiploma.model.AuthJsonModel
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.IOException
import javax.inject.Inject

class SignInUpRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun onSignIn(login: String, password: String): AuthJsonModel {
        try {
            val response = apiService.signIn(login, password)
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
            val response = apiService.signUp(login, password, userName)
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

    suspend fun onSignUpWithAttachment(
        login: String,
        password: String,
        userName: String,
        mediaUpload: MediaUpload
    ): AuthJsonModel {
        try {

            val loginPart = MultipartBody.Part.createFormData("login", login)
            val passPart = MultipartBody.Part.createFormData("pass", password)
            val namePart = MultipartBody.Part.createFormData("name", userName)
            val avatarPart = MultipartBody.Part.createFormData(
                "file", mediaUpload.file.name,
                mediaUpload.file.asRequestBody()
            )

            val response = apiService.signUpWithAvatar(loginPart, passPart, namePart, avatarPart)
            if (!response.isSuccessful) {
                throw ApiError(response.code())
            }
            return response.body() ?: throw ApiError(response.code())
        } catch (e: IOException) {
            e.printStackTrace()
            throw NetworkError
        } catch (e: Exception) {
            e.printStackTrace()
            throw UndefinedError
        }


    }
}
