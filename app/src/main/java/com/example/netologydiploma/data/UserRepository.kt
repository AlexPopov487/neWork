package com.example.netologydiploma.data

import com.example.netologydiploma.api.ApiService
import com.example.netologydiploma.db.UserDao
import com.example.netologydiploma.dto.Event
import com.example.netologydiploma.dto.User
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

class UserRepository @Inject constructor(
    private val apiService: ApiService,
    private val userDao: UserDao,
) {

    fun getAllUsers() = userDao.getAllUsers().map { userList ->
        userList.map { it.toDto() }
    }

    suspend fun loadAllUsers() {
        try {
            userDao.removeAllUsers()
            val response = apiService.getAllUsers()

            if (!response.isSuccessful) throw ApiError(response.code())

            val body = response.body() ?: throw ApiError(response.code())

            userDao.insertUsers(body.toEntity())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: SQLException) {
            throw  DbError
        } catch (e: Exception) {
            throw UndefinedError
        }
    }

    suspend fun getParticipatedEvent(id: Long) : Event {
        try {
            val response = apiService.getEventById(id)

            if (!response.isSuccessful) throw ApiError(response.code())

            return response.body() ?: throw ApiError(response.code())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: SQLException) {
            throw  DbError
        } catch (e: Exception) {
            throw UndefinedError
        }
    }

   suspend fun getEventParticipants(eventId: Long): Flow<List<User>> {
       val event = getParticipatedEvent(eventId)
       return userDao.getEventParticipants(event.participantsIds).map { participantsList ->
           participantsList.map { it.toDto() }
       }
   }
}