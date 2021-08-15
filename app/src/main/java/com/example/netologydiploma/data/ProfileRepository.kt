package com.example.netologydiploma.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.paging.*
import com.example.netologydiploma.api.ApiService
import com.example.netologydiploma.db.AppDb
import com.example.netologydiploma.db.JobDao
import com.example.netologydiploma.db.WallPostDao
import com.example.netologydiploma.db.WallRemoteKeyDao
import com.example.netologydiploma.dto.Job
import com.example.netologydiploma.dto.Post
import com.example.netologydiploma.entity.JobEntity
import com.example.netologydiploma.entity.WallPostEntity
import com.example.netologydiploma.entity.fromDto
import com.example.netologydiploma.entity.toWallPostEntity
import com.example.netologydiploma.error.ApiError
import com.example.netologydiploma.error.DbError
import com.example.netologydiploma.error.NetworkError
import com.example.netologydiploma.error.UndefinedError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.sql.SQLException
import javax.inject.Inject

class ProfileRepository @Inject constructor(
    private val apiService: ApiService,
    private val appDb: AppDb,
    private val wallPostDao: WallPostDao,
    private val wallRemoteKeyDao: WallRemoteKeyDao,
    private val jobDao: JobDao,
) {

    @ExperimentalPagingApi
    fun getAllPosts(authorId: Long): Flow<PagingData<Post>> = Pager(
        config = PagingConfig(pageSize = DEFAULT_WALL_PAGE_SIZE, enablePlaceholders = false),
        remoteMediator = WallRemoteMediator(
            apiService,
            appDb,
            wallPostDao,
            wallRemoteKeyDao,
            authorId
        ),
        pagingSourceFactory = { wallPostDao.getWallPagingSource() }
    ).flow.map { postList ->
        postList.map { it.toDto() }
    }

    fun getAllJobs(): LiveData<List<Job>> = jobDao.getAllJobs().map { jobList ->
        jobList.map {
            it.toDto()
        }
    }

    suspend fun getLatestWallPosts(authorId: Long) {
        try {
            wallPostDao.clearPostTable()
            val response = apiService.getLatestWallPosts(authorId, 10)

            if (!response.isSuccessful) throw ApiError(response.code())

            val body = response.body() ?: throw ApiError(response.code())

            wallPostDao.insertPosts(body.toWallPostEntity())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: SQLException) {
            throw  DbError
        } catch (e: Exception) {
            throw UndefinedError
        }
    }


    suspend fun likePost(post: Post) {
        try {
            // like in db
            val likedPost = post.copy(
                likeCount = if (post.likedByMe) post.likeCount.dec()
                else post.likeCount.inc(),
                likedByMe = !post.likedByMe
            )
            wallPostDao.insertPost(WallPostEntity.fromDto(likedPost))

            // like on server
            val response = if (post.likedByMe) apiService.dislikeWallPostById(post.id)
            else apiService.likeWallPostById(post.id)

            if (!response.isSuccessful)
                throw ApiError(response.code())
        } catch (e: IOException) {
            // revert changes to init state
            wallPostDao.insertPost(WallPostEntity.fromDto(post))
            throw NetworkError
        } catch (e: SQLException) {
            // revert changes to init state
            wallPostDao.insertPost(WallPostEntity.fromDto(post))
            throw  DbError
        } catch (e: Exception) {
            // revert changes to init state
            wallPostDao.insertPost(WallPostEntity.fromDto(post))
            throw UndefinedError
        }
    }

    suspend fun loadJobsFromServer(authorId: Long) {
        try {
            jobDao.removeAllJobs()
            val response = apiService.getAllUserJobs(authorId)

            if (!response.isSuccessful) throw ApiError(response.code())

            val body = response.body() ?: throw ApiError(response.code())

            jobDao.insertJobs(body.fromDto())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: SQLException) {
            throw  DbError
        } catch (e: Exception) {
            throw UndefinedError
        }
    }

    suspend fun createJob(job: Job) {
        try {
            val response = apiService.saveJob(job)

            if (!response.isSuccessful) throw ApiError(response.code())

            val body = response.body() ?: throw ApiError(response.code())

            jobDao.insertJob(JobEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: SQLException) {
            throw  DbError
        } catch (e: Exception) {
            throw UndefinedError
        }
    }


    suspend fun deleteJobById(id: Long) {
        val jobToDelete = jobDao.getJobById(id)
        try {
            jobDao.removeJobById(id)

            val response = apiService.removeJobById(id)
            if (!response.isSuccessful) {
                jobDao.insertJob(jobToDelete)
                throw ApiError(response.code())
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: SQLException) {
            throw  DbError
        } catch (e: Exception) {
            throw UndefinedError
        }
    }
}
