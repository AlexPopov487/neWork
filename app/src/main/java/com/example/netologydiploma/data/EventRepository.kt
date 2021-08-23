package com.example.netologydiploma.data

import androidx.paging.*
import com.example.netologydiploma.api.ApiService
import com.example.netologydiploma.db.AppDb
import com.example.netologydiploma.db.EventDao
import com.example.netologydiploma.db.EventRemoteKeyDao
import com.example.netologydiploma.dto.*
import com.example.netologydiploma.entity.EventEntity
import com.example.netologydiploma.error.ApiError
import com.example.netologydiploma.error.DbError
import com.example.netologydiploma.error.NetworkError
import com.example.netologydiploma.error.UndefinedError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.IOException
import java.sql.SQLException
import javax.inject.Inject

class EventRepository @Inject constructor(
    private val appDb: AppDb,
    private val apiService: ApiService,
    private val eventDao: EventDao,
    private val eventRemoteKeyDao: EventRemoteKeyDao

) {

    @ExperimentalPagingApi
    fun getAllEvents(): Flow<PagingData<Event>> = Pager(
        config = PagingConfig(pageSize = DEFAULT_EVENT_PAGE_SIZE, enablePlaceholders = false),
        remoteMediator = EventRemoteMediator(appDb, eventRemoteKeyDao, apiService, eventDao),
        pagingSourceFactory = { eventDao.getEventPagingSource() }
    ).flow
        .map { entityList ->
            entityList.map { it.toDto() }
        }


    suspend fun createEvent(event: Event) {
        try {
            val createEventResponse = apiService.createEvent(event)
            if (!createEventResponse.isSuccessful) {
                throw ApiError(createEventResponse.code())
            }
            val createEventBody =
                createEventResponse.body() ?: throw ApiError(createEventResponse.code())


            // additional network call to get the created event is required
            // because createEventBody doesn't have authorName set (it is set via backend),
            // so we cannot pass createEventBody to db and prefer to get the newly created
            // event explicitly

            val getEventResponse = apiService.getEventById(createEventBody.id)
            if (!getEventResponse.isSuccessful) throw ApiError(getEventResponse.code())
            val getEventBody = getEventResponse.body() ?: throw ApiError(getEventResponse.code())

            eventDao.insertEvent(EventEntity.fromDto(getEventBody))
        } catch (e: IOException) {
            e.printStackTrace()

            throw NetworkError
        } catch (e: SQLException) {
            e.printStackTrace()

            throw  DbError
        } catch (e: Exception) {
            e.printStackTrace()

            throw UndefinedError
        }
    }

    suspend fun saveWithAttachment(event: Event, mediaUpload: MediaUpload) {
        try {
            val uploadedMedia = uploadMedia(mediaUpload)

            val eventWithAttachment = event.copy(
                attachment = MediaAttachment(
                    url = uploadedMedia.url,
                    type = AttachmentType.IMAGE
                )
            )

            createEvent(eventWithAttachment)
        } catch (e: IOException) {
            e.printStackTrace()
            throw NetworkError
        } catch (e: SQLException) {
            e.printStackTrace()
            throw  DbError
        } catch (e: Exception) {
            e.printStackTrace()
            throw UndefinedError
        }
    }

    private suspend fun uploadMedia(mediaUpload: MediaUpload): MediaDownload {
        try {
            val mediaMultipart = MultipartBody.Part.createFormData(
                "file", mediaUpload.file.name,
                mediaUpload.file.asRequestBody()
            )
            val uploadMediaResponse = apiService.saveMediaFile(mediaMultipart)
            if (!uploadMediaResponse.isSuccessful) throw ApiError(uploadMediaResponse.code())
            return uploadMediaResponse.body() ?: throw ApiError(uploadMediaResponse.code())
        } catch (e: IOException) {
            e.printStackTrace()
            throw NetworkError
        } catch (e: Exception) {
            e.printStackTrace()
            throw UndefinedError
        }
    }

    suspend fun likeEvent(event: Event) {
        try {
            // like in db
            val likedEvent = event.copy(
                likeCount = if (event.likedByMe) event.likeCount.dec()
                else event.likeCount.inc(),
                likedByMe = !event.likedByMe
            )
            eventDao.insertEvent(EventEntity.fromDto(likedEvent))

            // like on server
            val response = if (event.likedByMe) apiService.dislikeEventById(event.id)
            else apiService.likeEventById(event.id)

            if (!response.isSuccessful) throw ApiError(response.code())
        } catch (e: IOException) {
            // revert changes to init state
            eventDao.insertEvent(EventEntity.fromDto(event))
            throw NetworkError
        } catch (e: SQLException) {
            // revert changes to init state
            eventDao.insertEvent(EventEntity.fromDto(event))
            throw  DbError
        } catch (e: Exception) {
            // revert changes to init state
            eventDao.insertEvent(EventEntity.fromDto(event))
            throw UndefinedError
        }
    }

    suspend fun participateInEvent(event: Event) {
        try {
            // participate in db
            val attendedEvent = event.copy(
                participantsCount = if (event.participatedByMe) event.participantsCount.dec()
                else event.participantsCount.inc(),
                participatedByMe = !event.participatedByMe
            )
            eventDao.insertEvent(EventEntity.fromDto(attendedEvent))

            //participate on server
            val response = if (event.participatedByMe) apiService.unparticipateEventById(event.id)
            else apiService.participateEventById(event.id)

            if (!response.isSuccessful) throw ApiError(response.code())
        } catch (e: IOException) {
            // revert changes to init state
            eventDao.insertEvent(EventEntity.fromDto(event))
            throw NetworkError
        } catch (e: SQLException) {
            // revert changes to init state
            eventDao.insertEvent(EventEntity.fromDto(event))
            throw  DbError
        } catch (e: Exception) {
            // revert changes to init state
            eventDao.insertEvent(EventEntity.fromDto(event))
            throw UndefinedError
        }
    }

    suspend fun deleteEvent(eventId: Long) {
        val eventToDelete = eventDao.getEventById(eventId)

        try {
            eventDao.deleteEvent(eventId)

            val response = apiService.deleteEvent(eventId)
            if (!response.isSuccessful) {
                eventDao.insertEvent(eventToDelete)
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