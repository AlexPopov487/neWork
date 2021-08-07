package com.example.netologydiploma.dto

enum class EventType {
    OFFLINE, ONLINE
}

data class Event(
    val id: Long = 0L,
    val authorId: Long = 0L,
    val author: String = "",
    val authorAvatar: String = "",
    val content: String = "",

    val published: Long = 0L,
    val datetime: Long = 0L,

    val type: EventType = EventType.ONLINE,
    val likedByMe: Boolean = false,
    val likeCount: Int = 0,
    val likeOwnerIds: Set<Int> = emptySet<Int>(),
    val speakersIds: Set<Int> = emptySet(),
    val participantsIds: Set<Int> = emptySet(),
    val participatedByMe: Boolean = false,
    val participantsCount: Int = 0,
    val coords: Coords? = Coords(
        lat = 0.0,
        lng = 0.0
    ),
    val attachment: MediaAttachment? = MediaAttachment(
        url = "netology.jpg",
        type = "IMAGE"
    ),
    val ownedByMe: Boolean = false,


    ) {
}

