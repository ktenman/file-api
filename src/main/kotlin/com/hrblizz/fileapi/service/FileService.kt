package com.hrblizz.fileapi.service

import com.hrblizz.fileapi.controller.exception.NotFoundException
import com.hrblizz.fileapi.data.entities.FileMetadata
import com.hrblizz.fileapi.data.repository.FileMetadataRepository
import com.hrblizz.fileapi.rest.FileData
import com.hrblizz.fileapi.rest.FileUploadMetadata
import com.hrblizz.fileapi.storage.StorageService
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.time.Instant
import java.util.*

@Service
class FileService(
    private val fileMetadataRepository: FileMetadataRepository,
    private val storageService: StorageService
) {

    fun uploadFile(metadata: FileUploadMetadata, file: MultipartFile): String {
        val token = UUID.randomUUID().toString()
        storageService.uploadFile(file, token)
        val fileMetadata = FileMetadata(
            token = token,
            name = metadata.name,
            contentType = metadata.contentType,
            meta = metadata.meta,
            source = metadata.source,
            expireTime = metadata.expireTime
        )
        fileMetadataRepository.save(fileMetadata)
        return token
    }

    fun getFilesByMetadata(tokens: List<String>): Map<String, FileMetadata> {
        val files = fileMetadataRepository.findAllById(tokens)
        return files.associateBy { it.token }
    }

    fun downloadFile(token: String): FileData {
        val fileMetadata = fileMetadataRepository.findById(token)
            .orElseThrow { NotFoundException("File not found with token: $token") }
        val fileContent = storageService.downloadFile(token)
        return FileData(
            name = fileMetadata.name,
            size = fileContent.size.toLong(),
            contentType = fileMetadata.contentType,
            createTime = Instant.ofEpochMilli(fileMetadata.createTime),
            content = fileContent
        )
    }

}
