package com.example.netologydiploma.model

import com.google.gson.annotations.SerializedName

data class AuthJsonModel(
    @SerializedName("id")
    val userId: Long?,
    @SerializedName("token")
    val token: String?
)