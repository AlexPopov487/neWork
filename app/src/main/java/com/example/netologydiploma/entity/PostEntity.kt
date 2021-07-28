package com.example.netologydiploma.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.netologydiploma.dto.MediaAttachment
import com.example.netologydiploma.dto.Post
import com.example.netologydiploma.dto.PostCoords

@Entity
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val authorId: Long,
    val author: String,
    val authorAvatar: String,
    val content: String,
    val published: Long,
    val isLiked: Boolean,
    val likeCount: Int,
    @Embedded
    val coords: PostCoordsEmbeddable?,
    @Embedded
    val attachment: MediaAttachmentEmbeddable?
) {
    fun toDto() = Post(
        id,
        authorId,
        author,
        authorAvatar,
        content,
        published,
        isLiked,
        likeCount,
        coords?.toDto(),
        attachment?.toDto()
    )

    companion object {
        fun fromDto(postDto: Post) =
            PostEntity(
                postDto.id,
                postDto.authorId,
                postDto.author,
                postDto.authorAvatar,
                postDto.content, postDto.published,
                postDto.isLiked,
                postDto.likeCount,
                PostCoordsEmbeddable.fromDto(postDto.coords),
                MediaAttachmentEmbeddable.fromDto(postDto.attachment)
            )
    }

}

data class MediaAttachmentEmbeddable(
    val url: String = "",
    val type: String = "",
) {
    fun toDto() = MediaAttachment(url, type)

    companion object {
        fun fromDto(dto: MediaAttachment?) = dto?.let {
            MediaAttachmentEmbeddable(it.url, it.type)
        }
    }
}

data class PostCoordsEmbeddable(
    val lat: Double = 0.0,
    val lng: Double = 0.0,
) {
    fun toDto() = PostCoords(lat, lng)

    companion object {
        fun fromDto(dto: PostCoords?) = dto?.let {
            PostCoordsEmbeddable(it.lat, it.lng)
        }
    }
}

fun List<PostEntity>.toDto(): List<Post> = map(PostEntity::toDto)
fun List<Post>.toEntity(): List<PostEntity> = map(PostEntity::fromDto)