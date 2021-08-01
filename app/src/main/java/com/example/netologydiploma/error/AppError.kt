package com.example.netologydiploma.error

sealed class AppError : RuntimeException() {

    companion object {
        fun getMessage(e: Throwable): String =
            when (e) {
                is ApiError -> "Server error. Please, try again. Code - ${e.status}"
                is AuthenticationError -> "Failed to sign in. Ensure your login and password are correct and try again"
                is DbError -> "Internal database error, try again."
                is NetworkError -> "Connection error. Please try again later."
                is  RegistrationError -> "Failed to sign up. Please try again!"
                is  UndefinedError -> "Unknown error, please try again later."
                else -> "Unknown error, please try again later."
            }
    }


}

class ApiError (val status: Int): AppError()
object NetworkError : AppError()
object DbError : AppError()
object RegistrationError: AppError()
object AuthenticationError: AppError()
object UndefinedError : AppError()

