package com.hrblizz.fileapi.data.repository

import com.hrblizz.fileapi.data.entities.FileMetadata
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository
import java.time.Instant
import java.util.*

interface FileMetadataRepository : MongoRepository<FileMetadata, ObjectId> {
    fun findAllByTokenIn(tokens: List<String>): List<FileMetadata>
    fun findByToken(token: String): Optional<FileMetadata>
    fun findByExpireTimeBefore(expirationTime: Instant): List<FileMetadata>
    fun findAllByTokenInAndExpireTimeGreaterThanOrExpireTimeIsNull(
        tokens: List<String>,
        currentTime: Instant?
    ): List<FileMetadata>
}
