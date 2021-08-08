package com.example.netologydiploma.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.netologydiploma.entity.EventRemoteKeyEntity

@Dao
interface EventRemoteKeyDao {
    @Query("SELECT COUNT(*) == 0 FROM EventRemoteKeyEntity")
    suspend fun isEmpty(): Boolean

    @Query("SELECT MIN(id) FROM EventRemoteKeyEntity")
    suspend fun getMinKey(): Long?

    @Query("SELECT MAX(id) FROM EventRemoteKeyEntity")
    suspend fun getMaxKey(): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKey(key: EventRemoteKeyEntity)

    @Query("DELETE FROM EventRemoteKeyEntity")
    suspend fun removeAll()

}