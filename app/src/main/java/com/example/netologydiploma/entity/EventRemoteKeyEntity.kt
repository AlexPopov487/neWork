package com.example.netologydiploma.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class EventRemoteKeyEntity(
    @PrimaryKey
    val type: KeyType,
    val id: Long,
) {

    enum class KeyType{
        AFTER, BEFORE
    }
}