package com.hrblizz.fileapi.service

import com.hrblizz.fileapi.controller.exception.NotFoundException
import com.hrblizz.fileapi.data.entities.FileMetadata
import com.hrblizz.fileapi.data.repository.FileMetadataRepository
import com.hrblizz.fileapi.library.JsonUtil
import com.hrblizz.fileapi.rest.FileData
import com.hrblizz.fileapi.rest.FileUploadMetadata
import com.hrblizz.fileapi.storage.StorageService
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class FileService(
    private val fileMetadataRepository: FileMetadataRepository,
    private val storageService: StorageService
) {

    fun uploadFile(metadata: FileUploadMetadata, file: MultipartFile): String {
        val metaJson = JsonUtil.toJson(metadata.meta)
        val fileMetadata = FileMetadata(
            name = metadata.name,
            contentType = metadata.contentType,
            meta = metaJson,
            source = metadata.source,
            expireTime = metadata.expireTime
        )
        storageService.uploadFile(file, fileMetadata.token)
        fileMetadataRepository.save(fileMetadata)
        return fileMetadata.token
    }

    fun getFilesByMetadata(tokens: List<String>): Map<String, FileMetadata> {
        val files = fileMetadataRepository.findAllByTokenIn(tokens)
        return files.associateBy { it.token }
    }

    fun downloadFile(token: String): FileData {
        val fileMetadata = fileMetadataRepository.findByToken(token)
            .orElseThrow { NotFoundException("File not found with token: $token") }
        val fileContent = storageService.downloadFile(token)
        return FileData(
            name = fileMetadata.name,
            size = fileContent.size.toLong(),
            contentType = fileMetadata.contentType,
            createTime = fileMetadata.createTime,
            content = fileContent
        )
    }

}
