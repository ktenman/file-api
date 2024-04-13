package com.hrblizz.fileapi.rest

import com.fasterxml.jackson.annotation.JsonFormat
import com.hrblizz.fileapi.data.entities.FileMetadata
import com.hrblizz.fileapi.library.JsonUtil
import java.time.Instant
import java.util.*

data class FileMetaResponse(
    val files: Map<String, FileMetaDataResponse>
) {
    constructor(fileMetadata: List<FileMetadata>) : this(
        files = fileMetadata.associateBy(
            { it.token },
            { FileMetaDataResponse(it) }
        )
    )

    data class FileMetaDataResponse(
        val token: String = UUID.randomUUID().toString(),
        val name: String,
        val contentType: String,
        val meta: Map<String, Any>,
        val source: String,
        @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'",
            timezone = "UTC"
        )
        val createTime: Instant = Instant.now(),
        @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'",
            timezone = "UTC"
        )
        val expireTime: Instant?
    ) {
        constructor(fileMetadata: FileMetadata) : this(
            token = fileMetadata.token,
            name = fileMetadata.name,
            contentType = fileMetadata.contentType,
            meta = JsonUtil.fromJson(fileMetadata.meta),
            source = fileMetadata.source,
            createTime = fileMetadata.createTime,
            expireTime = fileMetadata.expireTime
        )
    }

}
