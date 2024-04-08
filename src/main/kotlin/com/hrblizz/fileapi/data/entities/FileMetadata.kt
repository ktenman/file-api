package com.hrblizz.fileapi.data.entities

import com.fasterxml.jackson.annotation.JsonIgnore
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.data.mongodb.core.mapping.FieldType
import java.time.Instant
import java.util.*

@Document(collection = "file_metadata")
data class FileMetadata(
    @Id
    @JsonIgnore
    val id: ObjectId? = null,
    val token: String = UUID.randomUUID().toString(),
    val name: String,
    val contentType: String,
    @Field(targetType = FieldType.STRING)
    val meta: String,
    val source: String,
    val createTime: Instant = Instant.now(),
    val expireTime: Instant?
)
