package com.example.netologydiploma.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PostDao {

    @Query("SELECT * FROM PostEntity ORDER BY id DESC")
    fun getAllPosts() : LiveData<List<PostEntity>>

    @Insert
    suspend fun createPost(post: PostEntity)


}