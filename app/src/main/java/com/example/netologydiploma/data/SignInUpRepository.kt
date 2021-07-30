package com.example.netologydiploma.data


import com.example.netologydiploma.api.PostApi
import com.example.netologydiploma.model.AuthJsonModel
import java.io.IOException

class SignInUpRepository {

    suspend fun onSignIn(login: String, password: String): AuthJsonModel {
        try {
            val response = PostApi.retrofitService.signIn(login, password)
            if (!response.isSuccessful) {
                println("RESPONSE CODE IS ${response.code()}")
                println("RESPONSE MESSAGE : ${response.message()}")
                throw Error()
                // TODO create a comprehensive wrapper for all app errors
            }
            return response.body() ?: throw Error()
        } catch (e: IOException) {
            throw Error()
            // TODO create a comprehensive wrapper for all app errors
        } catch (e: Exception) {
            print(e.message)
            throw Error()
            // TODO create a comprehensive wrapper for all app errors
        }
    }

    suspend fun onSignUp(login: String, password: String, userName: String): AuthJsonModel {
        try {
            val response = PostApi.retrofitService.signUp(login, password, userName)
            if (!response.isSuccessful) {
                println("RESPONSE CODE IS ${response.code()}")
                println("RESPONSE MESSAGE : ${response.message()}")
                throw Error()
                // TODO create a comprehensive wrapper for all app errors
            }
            return response.body() ?: throw Error()
        } catch (e: IOException) {
            throw Error()
            // TODO create a comprehensive wrapper for all app errors
        } catch (e: Exception) {
            print(e.message)
            throw Error()
            // TODO create a comprehensive wrapper for all app errors
        }
    }
}