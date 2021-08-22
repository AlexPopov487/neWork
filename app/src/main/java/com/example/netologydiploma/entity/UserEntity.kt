package com.example.netologydiploma.entity

import androidx.annotation.Nullable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.netologydiploma.dto.Post
import com.example.netologydiploma.dto.User

@Entity
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val login: String,
    val name: String,
    @Nullable
    val avatar: String?,
)  {
    fun toDto() = User(
        id = id,
        login = login,
        name = name,
        avatar = avatar,

    )

    companion object {
        fun fromDto(userDto: User) =
            UserEntity(
                id = userDto.id,
                login = userDto.login,
                name = userDto.name,
                avatar = userDto.avatar,
            )
    }
}

fun List<UserEntity>.toDto(): List<User> = map(UserEntity::toDto)
fun List<User>.toEntity(): List<UserEntity> = map(UserEntity::fromDto)