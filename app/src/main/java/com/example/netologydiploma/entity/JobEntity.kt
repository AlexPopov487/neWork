package com.example.netologydiploma.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.netologydiploma.dto.Job

@Entity
data class JobEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String = "",
    val position: String = "",
    val start: Long = 0L,
    val finish: Long? = null,
    val link: String? = null,
) {

    fun toDto(): Job = Job(
        id = id,
        name = name,
        position = position,
        start = start,
        finish = finish,
        link = link
    )

    companion object {
        fun fromDto(jobDto: Job): JobEntity {
            return JobEntity(
                jobDto.id,
                jobDto.name,
                jobDto.position,
                jobDto.start,
                jobDto.finish,
                jobDto.link,
            )
        }
    }
}


fun List<JobEntity>.toDto(): List<Job> = map(JobEntity::toDto)
fun List<Job>.fromDto(): List<JobEntity> = map(JobEntity::fromDto)