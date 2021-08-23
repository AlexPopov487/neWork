package com.example.netologydiploma.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.netologydiploma.dto.User
import kotlinx.coroutines.flow.Flow
import com.example.netologydiploma.entity.UserEntity

@Dao
interface UserDao {
    @Query("SELECT * FROM UserEntity ORDER BY name")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    @Query("DELETE FROM UserEntity")
    suspend fun removeAllUsers()

    @Query("SELECT * FROM UserEntity WHERE id IN (:participants)")
    fun getEventParticipants(participants: Set<Long>): Flow<List<UserEntity>>
}