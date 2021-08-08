package com.example.netologydiploma.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.netologydiploma.entity.EventEntity
import com.example.netologydiploma.entity.EventRemoteKeyEntity
import com.example.netologydiploma.entity.PostEntity
import com.example.netologydiploma.entity.PostRemoteKeyEntity

@Database(
    entities = [PostEntity::class,
        EventEntity::class,
        EventRemoteKeyEntity::class,
        PostRemoteKeyEntity::class], version = 3
)
abstract class AppDb: RoomDatabase() {
    abstract fun postDao() : PostDao
    abstract fun eventDao(): EventDao
    abstract fun eventRemoteKeyDao(): EventRemoteKeyDao
    abstract fun postRemoteKeyDao(): PostRemoteKeyDao
}