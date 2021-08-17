package com.example.netologydiploma.entity

import androidx.annotation.Nullable
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.netologydiploma.dto.AttachmentType
import com.example.netologydiploma.dto.MediaAttachment
import com.example.netologydiploma.dto.Post
import com.example.netologydiploma.dto.Coords

@Entity
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val authorId: Long,
    val author: String,
    @Nullable val authorAvatar: String?,
    val content: String,
    val published: Long,
    val isLikedByMe: Boolean,
    val likeCount: Int,
    @Embedded
    var coords: CoordsEmbeddable?,
    @Embedded
    var attachment: MediaAttachmentEmbeddable?,
) {
    fun toDto() = Post(
        id = id,
        authorId = authorId,
        author = author,
        authorAvatar = authorAvatar,
        content = content,
        published = published,
        likedByMe = isLikedByMe,
        likeCount = likeCount,
        coords = coords?.toDto(),
        attachment = attachment?.toDto()
    )

    companion object {
        fun fromDto(postDto: Post) =
            PostEntity(
                id = postDto.id,
                authorId = postDto.authorId,
                author = postDto.author,
                authorAvatar = postDto.authorAvatar,
                content = postDto.content,
                published = postDto.published,
                isLikedByMe = postDto.likedByMe,
                likeCount = postDto.likeCount,
                coords = CoordsEmbeddable.fromDto(postDto.coords),
                attachment = MediaAttachmentEmbeddable.fromDto(postDto.attachment)
            )
    }

}

data class MediaAttachmentEmbeddable(
    val url: String,
    val type: AttachmentType,
) {
    fun toDto() = MediaAttachment(url, type)

    companion object {
        fun fromDto(dto: MediaAttachment?) = dto?.let {
            MediaAttachmentEmbeddable(it.url, it.type) ?: null
        }
    }
}

data class CoordsEmbeddable(
    val lat: Double = 0.0,
    val lng: Double = 0.0,
) {
    fun toDto() = Coords(lat, lng)

    companion object {
        fun fromDto(dto: Coords?) = dto?.let {
            CoordsEmbeddable(it.lat, it.lng)
        }
    }
}

fun List<PostEntity>.toDto(): List<Post> = map(PostEntity::toDto)
fun List<Post>.toEntity(): List<PostEntity> = map(PostEntity::fromDto)