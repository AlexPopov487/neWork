package com.example.netologydiploma.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.netologydiploma.entity.*

@Database(
    entities = [PostEntity::class,
        EventEntity::class,
        EventRemoteKeyEntity::class,
        PostRemoteKeyEntity::class,
        WallRemoteKeyEntity::class,
        WallPostEntity::class,
        JobEntity::class], version = 3
)
abstract class AppDb : RoomDatabase() {
    abstract fun postDao(): PostDao
    abstract fun eventDao(): EventDao
    abstract fun eventRemoteKeyDao(): EventRemoteKeyDao
    abstract fun postRemoteKeyDao(): PostRemoteKeyDao
    abstract fun wallRemoteKeyDao(): WallRemoteKeyDao
    abstract fun wallPostDao(): WallPostDao
    abstract fun jobDao(): JobDao
}