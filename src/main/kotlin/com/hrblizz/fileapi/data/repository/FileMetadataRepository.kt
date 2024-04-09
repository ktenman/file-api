package com.hrblizz.fileapi.data.repository

import com.hrblizz.fileapi.data.entities.FileMetadata
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository
import java.time.Instant

interface FileMetadataRepository : MongoRepository<FileMetadata, ObjectId> {
    fun findByToken(token: String): FileMetadata?
    fun findByExpireTimeBefore(expirationTime: Instant): List<FileMetadata>
    fun findAllByTokenInAndExpireTimeGreaterThanOrExpireTimeIsNull(
        tokens: List<String>,
        currentTime: Instant?
    ): List<FileMetadata>
}
