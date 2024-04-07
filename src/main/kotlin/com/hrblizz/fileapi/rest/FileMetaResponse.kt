package com.hrblizz.fileapi.rest

import com.hrblizz.fileapi.data.entities.FileMetadata

data class FileMetaResponse(
    val files: Map<String, FileMetadata>
)
