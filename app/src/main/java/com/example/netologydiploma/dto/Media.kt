package com.example.netologydiploma.dto

import java.io.File


data class MediaDownload(val url: String)

data class MediaUpload(val file: File)

enum class AttachmentType{
    IMAGE, VIDEO, AUDIO
}