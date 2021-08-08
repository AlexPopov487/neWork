package com.example.netologydiploma.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.netologydiploma.entity.PostRemoteKeyEntity

@Dao
interface PostRemoteKeyDao {
    @Query("SELECT COUNT(*) == 0 FROM PostRemoteKeyEntity")
    suspend fun isEmpty(): Boolean

    @Query("SELECT MIN(id) FROM PostRemoteKeyEntity")
    suspend fun getMinKey(): Long?

    @Query("SELECT MAX(id) FROM PostRemoteKeyEntity")
    suspend fun getMaxKey(): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKey(key: PostRemoteKeyEntity)

    @Query("DELETE FROM PostRemoteKeyEntity")
    suspend fun removeAll()
}