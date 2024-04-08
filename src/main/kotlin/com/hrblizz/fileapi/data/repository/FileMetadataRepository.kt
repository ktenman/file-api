package com.hrblizz.fileapi.data.repository

import com.hrblizz.fileapi.data.entities.FileMetadata
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository
import java.util.*

interface FileMetadataRepository : MongoRepository<FileMetadata, ObjectId> {
    fun findAllByTokenIn(tokens: List<String>): List<FileMetadata>
    fun findByToken(token: String): Optional<FileMetadata>
}
