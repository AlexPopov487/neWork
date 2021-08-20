package com.example.netologydiploma.entity

import androidx.annotation.Nullable
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.netologydiploma.db.InstantDateConverter
import com.example.netologydiploma.dto.Post
import java.time.Instant

@Entity
data class WallPostEntity (
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val authorId: Long,
    val author: String,
    @Nullable val authorAvatar: String?,
    val content: String,
    @TypeConverters(InstantDateConverter::class)
    val published: Instant,
    val isLikedByMe: Boolean,
    val likeCount: Int,
    @Embedded
    val coords: CoordsEmbeddable?,
    @Embedded
    val attachment: MediaAttachmentEmbeddable?
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
            WallPostEntity(
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


fun List<WallPostEntity>.toDto(): List<Post> = map(WallPostEntity::toDto)
fun List<Post>.toWallPostEntity(): List<WallPostEntity> = map(WallPostEntity::fromDto)