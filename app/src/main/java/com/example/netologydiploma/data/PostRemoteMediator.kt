package com.example.netologydiploma.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.netologydiploma.api.ApiService
import com.example.netologydiploma.db.AppDb
import com.example.netologydiploma.db.PostDao
import com.example.netologydiploma.db.PostRemoteKeyDao
import com.example.netologydiploma.dto.Post
import com.example.netologydiploma.entity.PostEntity
import com.example.netologydiploma.entity.PostRemoteKeyEntity
import com.example.netologydiploma.entity.toEntity
import com.example.netologydiploma.error.ApiError

const val DEFAULT_POST_PAGE_SIZE = 10

@ExperimentalPagingApi
class PostRemoteMediator(
    private val apiService: ApiService,
    private val appDb: AppDb,
    private val postDao: PostDao,
    private val postRemoteKeyDao: PostRemoteKeyDao
) : RemoteMediator<Int, PostEntity>() {
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PostEntity>
    ): MediatorResult {
        try {
            val response = when (loadType) {
                LoadType.REFRESH -> {
                    apiService.getLatestPosts(state.config.initialLoadSize)
                }
                LoadType.PREPEND -> {
                    val maxKey = postRemoteKeyDao.getMaxKey() ?: return MediatorResult.Success(
                        endOfPaginationReached = false
                    )
                    apiService.getPostsAfter(maxKey, state.config.pageSize)
                }
                LoadType.APPEND -> {
                    val minKey = postRemoteKeyDao.getMinKey() ?: return MediatorResult.Success(
                        endOfPaginationReached = false
                    )
                    apiService.getPostsBefore(minKey, state.config.pageSize)
                }
            }

            if (!response.isSuccessful) throw ApiError(response.code())
            val receivedBody = response.body() ?: throw ApiError(response.code())

            if (receivedBody.isEmpty()) return MediatorResult.Success(
                endOfPaginationReached = true
            )

            appDb.withTransaction {
                when (loadType) {
                    LoadType.REFRESH -> {
                        postRemoteKeyDao.removeAll()
                        insertMaxKey(receivedBody)
                        insertMinKey(receivedBody)
                        postDao.clearPostTable()
                    }
                    LoadType.PREPEND -> insertMaxKey(receivedBody)
                    LoadType.APPEND -> insertMinKey(receivedBody)
                }
                postDao.insertPosts(receivedBody.toEntity())
            }
            return MediatorResult.Success(endOfPaginationReached = receivedBody.isEmpty())
        } catch (e: Exception) {
            return MediatorResult.Error(e)
        }

    }


    private suspend fun insertMaxKey(receivedBody: List<Post>) {
        postRemoteKeyDao.insertKey(
            PostRemoteKeyEntity(
                type = PostRemoteKeyEntity.KeyType.AFTER,
                id = receivedBody.first().id
            )
        )
    }

    private suspend fun insertMinKey(receivedBody: List<Post>) {
        postRemoteKeyDao.insertKey(
            PostRemoteKeyEntity(
                type = PostRemoteKeyEntity.KeyType.BEFORE,
                id = receivedBody.last().id,
            )
        )
    }
}