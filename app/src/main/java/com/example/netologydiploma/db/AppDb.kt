package com.example.netologydiploma.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.netologydiploma.entity.PostEntity

@Database(entities = [PostEntity::class], version = 1)
abstract class AppDb: RoomDatabase() {
    abstract fun postDao() : PostDao


}