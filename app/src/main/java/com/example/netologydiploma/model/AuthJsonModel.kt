package com.example.netologydiploma.model

import com.google.gson.annotations.SerializedName

data class AuthJsonModel(
    @SerializedName("id")
    var userId: Long?,
    @SerializedName("token")
    var token: String?
) {
}