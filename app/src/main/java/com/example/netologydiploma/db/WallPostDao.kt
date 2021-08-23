package com.example.netologydiploma.db

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.netologydiploma.entity.PostEntity
import com.example.netologydiploma.entity.WallPostEntity

@Dao
interface WallPostDao {
    @Query("SELECT * FROM WallPostEntity ORDER BY id DESC")
    fun getWallPagingSource(): PagingSource<Int, WallPostEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<WallPostEntity>)

    @Query("DELETE FROM WallPostEntity")
    suspend fun clearPostTable()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: WallPostEntity)

    @Query("DELETE FROM WallPostEntity WHERE id = :id")
    suspend fun deletePost(id: Long)
}