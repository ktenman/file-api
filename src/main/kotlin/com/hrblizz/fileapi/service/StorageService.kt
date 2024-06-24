package com.hrblizz.fileapi.service

import io.minio.GetObjectArgs
import io.minio.MinioClient
import io.minio.PutObjectArgs
import io.minio.RemoveObjectArgs
import io.minio.errors.MinioException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.IOException

@Service
class StorageService(
    @Value("\${minio.bucket-name}") private val bucketName: String,
    private val minioClient: MinioClient
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun uploadFile(file: MultipartFile, fileName: String) {
        log.info("Uploading file: $fileName")
        try {
            require(!file.isEmpty && file.size > 0) { "Cannot upload an empty file" }
            require(fileName != "") { "File name cannot be empty" }

            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .`object`(fileName)
                    .stream(file.inputStream, file.size, -1)
                    .build()
            )
        } catch (e: MinioException) {
            log.error("Error uploading file: $fileName")
            throw RuntimeException("Error uploading file to minio: $fileName", e)
        } catch (e: IOException) {
            log.error("Error reading file: ${e.message}")
            throw RuntimeException("Error reading file", e)
        }
    }

    fun downloadFile(fileName: String): ByteArray {
        log.info("Downloading file: $fileName")
        try {
            val response = minioClient.getObject(
                GetObjectArgs.builder()
                    .bucket(bucketName)
                    .`object`(fileName)
                    .build()
            )
            return response.readAllBytes()
        } catch (e: MinioException) {
            log.error("Error downloading file: $fileName")
            throw RuntimeException("Error downloading file from minio: $fileName", e)
        } catch (e: IOException) {
            log.error("Error reading file: ${e.message}")
            throw RuntimeException("Error reading file", e)
        }
    }

    fun deleteFile(fileName: String) {
        log.info("Deleting file: $fileName")
        try {
            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .`object`(fileName)
                    .build()
            )
        } catch (e: MinioException) {
            log.error("Error deleting file: $fileName")
            throw RuntimeException("Error deleting file from minio: $fileName", e)
        }
    }
}
