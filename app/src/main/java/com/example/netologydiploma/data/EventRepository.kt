package com.example.netologydiploma.data

import androidx.room.withTransaction
import com.example.netologydiploma.api.ApiService
import com.example.netologydiploma.db.AppDb
import com.example.netologydiploma.db.EventDao
import com.example.netologydiploma.dto.Event
import com.example.netologydiploma.entity.EventEntity
import com.example.netologydiploma.entity.toDto
import com.example.netologydiploma.entity.toEntity
import com.example.netologydiploma.error.ApiError
import com.example.netologydiploma.error.DbError
import com.example.netologydiploma.error.NetworkError
import com.example.netologydiploma.error.UndefinedError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.sql.SQLException
import javax.inject.Inject

class EventRepository @Inject constructor(
    private val appDb: AppDb,
    private val apiService: ApiService,
    private val eventDao: EventDao
) {

    fun getAllEvents(): Flow<List<Event>> = eventDao.getAllEvents().map {
        it.toDto()
    }

    suspend fun loadEventsFromWeb() {
        try {
            val response = apiService.getAllEvents()

            if (!response.isSuccessful) {
                throw ApiError(response.code())
            }

            val body = response.body()?.map {
                it.copy(
                    likeCount = it.likeOwnerIds.size,
                    participantsCount = it.participantsIds.size
                )
            } ?: throw ApiError(response.code())

            appDb.withTransaction {
                eventDao.clearEventTable()
                eventDao.insertEvents(body.toEntity())
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: SQLException) {
            throw  DbError
        } catch (e: Exception) {
            throw UndefinedError
        }
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
            throw NetworkError
        } catch (e: SQLException) {
            throw  DbError
        } catch (e: Exception) {
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