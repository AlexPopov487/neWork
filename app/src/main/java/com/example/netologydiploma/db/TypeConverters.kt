package com.example.netologydiploma.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.Instant

class InstantDateConverter {

    @TypeConverter
    fun fromInstantToMillis(instant: Instant): Long =
        instant.toEpochMilli()

    @TypeConverter
    fun fromMillisToInstant(milis: Long): Instant =
        Instant.ofEpochMilli(milis)
}


class LongSetDataConverter {
    @TypeConverter
    fun fromLongSet(set: Set<Long>): String {
        val gson = Gson()
        val objectType = object : TypeToken<Set<Long>>() {}.type
        return gson.toJson(set, objectType)
    }


    @TypeConverter
    fun toLongSet(value: String): Set<Long> {
        val gson = Gson()
        val objectType = object : TypeToken<Set<Long>>() {}.type
        return gson.fromJson(value, objectType)
    }
}