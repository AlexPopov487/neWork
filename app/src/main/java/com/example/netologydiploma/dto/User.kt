package com.example.netologydiploma.dto

data class User(
    val id: Long = 0L,
    val login: String = "",
    val name: String = "",
    val avatar: String? = null,
    val isItMe: Boolean = false,
)