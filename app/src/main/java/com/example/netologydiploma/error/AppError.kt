package com.example.netologydiploma.error

import com.example.netologydiploma.R


sealed class AppError : RuntimeException() {

    companion object {
        fun getMessage(e: Throwable): Int =
            when (e) {
                is ApiError -> R.string.app_error_api_message
                is AuthenticationError -> R.string.app_error_auth_message
                is DbError -> R.string.app_error_db_message
                is NetworkError -> R.string.app_error_network_message
                is  RegistrationError -> R.string.app_error_registration_message
                is  UndefinedError -> R.string.app_error_undefined_message
                else -> R.string.app_error_undefined_message
            }
    }


}

class ApiError (val status: Int): AppError()
object NetworkError : AppError()
object DbError : AppError()
object RegistrationError: AppError()
object AuthenticationError: AppError()
object UndefinedError : AppError()

