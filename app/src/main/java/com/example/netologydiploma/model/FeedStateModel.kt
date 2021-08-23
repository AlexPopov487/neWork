package com.example.netologydiploma.model

data class FeedStateModel(
    val isLoading: Boolean = false,
    val hasError: Boolean = false,
    val isRefreshing: Boolean = false,

    val errorMessage: Int? = null,
)