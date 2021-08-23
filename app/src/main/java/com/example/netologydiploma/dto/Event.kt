package com.example.netologydiploma.dto

import java.time.Instant

enum class EventType {
    OFFLINE, ONLINE
}

data class Event(
    val id: Long = 0L,
    val authorId: Long = 0L,
    val author: String = "",
    val authorAvatar: String?= null,
    val content: String = "",

    val published: Instant = Instant.now(),
    val datetime: Instant = Instant.now(),

    val type: EventType = EventType.ONLINE,
    val likedByMe: Boolean = false,
    val likeCount: Int = 0,
    val likeOwnerIds: Set<Int> = emptySet<Int>(),
    val speakersIds: Set<Int> = emptySet(),
    val participantsIds: Set<Long> = emptySet(),
    val participatedByMe: Boolean = false,
    val participantsCount: Int = 0,
    val coords: Coords? = null,
    val attachment: MediaAttachment? = null,
    val ownedByMe: Boolean = false,


    )

