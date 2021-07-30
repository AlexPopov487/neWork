package com.example.netologydiploma.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.netologydiploma.entity.PostEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PostDao {

    // use Flow instead of LiveData to property check user auth in PostViewModel data map{} operation
    @Query("SELECT * FROM PostEntity ORDER BY id DESC")
    fun getAllPosts() : Flow<List<PostEntity>>

    @Query("SELECT * FROM PostEntity WHERE id = :id ")
    suspend fun getPostById(id: Long) : PostEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun createPosts(posts: List<PostEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun createPost(post: PostEntity)

    @Query("DELETE FROM PostEntity WHERE id = :id")
    suspend fun deletePost(id: Long)
}