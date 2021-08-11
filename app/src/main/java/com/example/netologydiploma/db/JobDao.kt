package com.example.netologydiploma.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.netologydiploma.entity.JobEntity

@Dao
interface JobDao {

    @Query("SELECT * FROM JobEntity ORDER BY id DESC")
    fun getAllJobs(): LiveData<List<JobEntity>>

    @Query("SELECT * FROM JobEntity WHERE id = :jobId LIMIT 1")
    suspend fun getJobById(jobId: Long): JobEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJob(jobEntity: JobEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJobs(jobEntities: List<JobEntity>)

    @Query("DELETE FROM JobEntity WHERE id = :jobId")
    suspend fun removeJobById(jobId: Long)

    @Query("DELETE FROM JobEntity")
    suspend fun removeAllJobs()
}