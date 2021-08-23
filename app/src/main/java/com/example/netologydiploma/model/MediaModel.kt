package com.example.netologydiploma.model

import android.net.Uri
import com.example.netologydiploma.dto.AttachmentType
import java.io.File

data class MediaModel(
    val uri: Uri? = null,
    val file: File? = null,
    val type: AttachmentType? = null
)