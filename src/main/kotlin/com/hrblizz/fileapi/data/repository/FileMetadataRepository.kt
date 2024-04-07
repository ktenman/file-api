package com.hrblizz.fileapi.data.repository

import com.hrblizz.fileapi.data.entities.FileMetadata
import org.springframework.data.mongodb.repository.MongoRepository

interface FileMetadataRepository : MongoRepository<FileMetadata, String>
