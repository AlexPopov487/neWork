package com.example.netologydiploma.db

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.jetbrains.annotations.NotNull

@Entity
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val authorId: Long = 0L,
    val author: String = "",
    val authorAvatar: String = "",
    @NotNull val content: String,
    val published : Long = 0L,
    val isLiked: Boolean = false,
    val likeCount: Int = 0,
    @Embedded
    val coords: PostCoords =  PostCoords(
        lat = 0.0,
        lng = 0.0
    ),
    @Embedded
    val attachment: MediaAttachment = MediaAttachment(
        url = "netology.jpg",
        type = "IMAGE"
    )
) {

}

data class MediaAttachment(
    val url: String = "",
    val type: String = "",
) {

}

data class PostCoords(
    val lat: Double = 0.0,
    val lng: Double = 0.0,
) {

}
