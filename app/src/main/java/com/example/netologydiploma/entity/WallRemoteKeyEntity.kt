package com.example.netologydiploma.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class WallRemoteKeyEntity(

    @PrimaryKey
    val type: KeyType,
    val id: Long,
) {

    enum class KeyType {
        AFTER, BEFORE
    }
}