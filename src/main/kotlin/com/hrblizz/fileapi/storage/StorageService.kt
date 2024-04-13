package com.hrblizz.fileapi.storage

import com.hrblizz.fileapi.library.log.Logger
import io.minio.GetObjectArgs
import io.minio.MinioClient
import io.minio.PutObjectArgs
import io.minio.RemoveObjectArgs
import io.minio.errors.MinioException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.IOException

@Service
class StorageService(
    @Value("\${minio.bucket-name}") private val bucketName: String,
    private val minioClient: MinioClient,
    private val logger: Logger
) {

    fun uploadFile(file: MultipartFile, fileName: String) {
        logger.info("Uploading file: $fileName")
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
            logger.critical("Error uploading file: $fileName")
            throw RuntimeException("Error uploading file to minio: $fileName", e)
        } catch (e: IOException) {
            logger.critical("Error reading file: ${e.message}")
            throw RuntimeException("Error reading file", e)
        }
    }

    fun downloadFile(fileName: String): ByteArray {
        logger.info("Downloading file: $fileName")
        try {
            val response = minioClient.getObject(
                GetObjectArgs.builder()
                    .bucket(bucketName)
                    .`object`(fileName)
                    .build()
            )
            return response.readAllBytes()
        } catch (e: MinioException) {
            logger.error("Error downloading file: $fileName")
            throw RuntimeException("Error downloading file from minio: $fileName", e)
        } catch (e: IOException) {
            logger.error("Error reading file: ${e.message}")
            throw RuntimeException("Error reading file", e)
        }
    }

    fun deleteFile(fileName: String) {
        logger.info("Deleting file: $fileName")
        try {
            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .`object`(fileName)
                    .build()
            )
        } catch (e: MinioException) {
            logger.error("Error deleting file: $fileName")
            throw RuntimeException("Error deleting file from minio: $fileName", e)
        }
    }
}
