package com.example.netologydiploma.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.netologydiploma.dto.Event
import com.example.netologydiploma.dto.EventType

@Entity
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val authorId: Long,
    val author: String,
    val authorAvatar: String,
    val content: String,

    val published: Long,
    val dateTime: Long,

    @ColumnInfo(name = "event_type")
    val type: EventType,
    val isLikedByMe: Boolean,
    val likeCount: Int,
    val participatedByMe: Boolean,
    val participantsCount: Int,
    @Embedded
    val coords: CoordsEmbeddable?,
    @Embedded
    val attachment: MediaAttachmentEmbeddable?,
    val ownedByMe: Boolean = false,
) {

    fun toDto() = Event(
        id = id,
        authorId = authorId,
        author = author,
        authorAvatar = authorAvatar,
        content = content,
        published = published,
        datetime = dateTime,
        type = type,
        likedByMe = isLikedByMe,
        likeCount = likeCount,
        participantsCount = participantsCount,
        participatedByMe = participatedByMe,
        coords = coords?.toDto(),
        attachment = attachment?.toDto(),

        )

    companion object {
        fun fromDto(eventDto: Event) =
            EventEntity(
                id = eventDto.id,
                authorId = eventDto.authorId,
                author = eventDto.author,
                authorAvatar = eventDto.authorAvatar,
                content = eventDto.content,
                published = eventDto.published,
                dateTime = eventDto.datetime,
                type = eventDto.type,
                isLikedByMe = eventDto.likedByMe,
                likeCount = eventDto.likeCount,
                participatedByMe = eventDto.participatedByMe,
                participantsCount = eventDto.participantsCount,
                coords = CoordsEmbeddable.fromDto(eventDto.coords),
                attachment = MediaAttachmentEmbeddable.fromDto(eventDto.attachment),
            )
    }

}

fun List<EventEntity>.toDto(): List<Event> = map(EventEntity::toDto)
fun List<Event>.toEntity(): List<EventEntity> = map(EventEntity::fromDto)