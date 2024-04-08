package com.hrblizz.fileapi.data.entities

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "file_metadata")
data class FileMetadata(
    @Id
    val token: String,
    val name: String,
    val contentType: String,
    val meta: String,
    val source: String,
    val createTime: Long = Instant.now().toEpochMilli(),
    val expireTime: Long?
)
