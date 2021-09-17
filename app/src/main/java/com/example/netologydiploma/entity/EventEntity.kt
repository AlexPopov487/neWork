package com.example.netologydiploma.entity

import androidx.annotation.Nullable
import androidx.room.*
import com.example.netologydiploma.db.InstantDateConverter
import com.example.netologydiploma.db.LongSetDataConverter
import com.example.netologydiploma.dto.Event
import com.example.netologydiploma.dto.EventType
import java.time.Instant

@Entity
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val authorId: Long,
    val author: String,
    @Nullable
    val authorAvatar: String?,
    val content: String,

    @TypeConverters(InstantDateConverter::class)
    val published: Instant,
    @TypeConverters(InstantDateConverter::class)
    val dateTime: Instant,

    @ColumnInfo(name = "event_type")
    val type: EventType,
    val isLikedByMe: Boolean,
    val likeCount: Int,
    val participatedByMe: Boolean,
    val participantsCount: Int,
    @TypeConverters(LongSetDataConverter::class)
    val participantsIds: Set<Long>,
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
        participantsIds = participantsIds,
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
                participantsIds = eventDto.participantsIds,
                coords = CoordsEmbeddable.fromDto(eventDto.coords),
                attachment = MediaAttachmentEmbeddable.fromDto(eventDto.attachment),
            )
    }

}

fun List<EventEntity>.toDto(): List<Event> = map(EventEntity::toDto)
fun List<Event>.toEntity(): List<EventEntity> = map(EventEntity::fromDto)