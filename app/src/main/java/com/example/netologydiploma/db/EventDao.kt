package com.example.netologydiploma.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.netologydiploma.entity.EventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {

    @Query("SELECT * FROM EventEntity ORDER BY id DESC")
    fun getEventPagingSource(): PagingSource<Int, EventEntity>

    // use Flow instead of LiveData to property check user auth in PostViewModel data map{} operation
    @Query("SELECT * FROM EventEntity ORDER BY id DESC")
    fun getAllEvents() : Flow<List<EventEntity>>

    @Query("SELECT * FROM EventEntity WHERE id = :id ")
    suspend fun getEventById(id: Long) : EventEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<EventEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventEntity)

    @Query("DELETE FROM EventEntity WHERE id = :id")
    suspend fun deleteEvent(id: Long)

    @Query("DELETE FROM EventEntity")
    suspend fun clearEventTable()
}