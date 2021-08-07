package com.example.netologydiploma.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.netologydiploma.entity.EventEntity
import com.example.netologydiploma.entity.PostEntity

@Database(entities = [PostEntity::class, EventEntity::class], version = 3)
abstract class AppDb: RoomDatabase() {
    abstract fun postDao() : PostDao
    abstract fun eventDao() : EventDao

}