package com.hrblizz.fileapi.storage

import com.hrblizz.fileapi.library.log.Logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.core.exception.SdkException
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.net.URI

@Service
class StorageService(
    @Value("\${minio.url}") private val minioUrl: String,
    @Value("\${minio.access-key}") private val accessKey: String,
    @Value("\${minio.secret-key}") private val secretKey: String,
    @Value("\${minio.bucket-name}") private val bucketName: String,
    private val logger: Logger
) {
    private val s3Client = S3Client.builder()
        .endpointOverride(URI.create(minioUrl))
        .region(Region.EU_CENTRAL_1)
        .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
        .build()

    fun uploadFile(file: MultipartFile, fileName: String) {
        logger.info("Uploading file: $fileName")
        try {
            require(!file.isEmpty && file.size > 0) { "Cannot upload an empty file" }
            require(fileName != "") { "File name cannot be empty" }
            val putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build()
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.bytes))
        } catch (e: SdkException) {
            logger.critical("Error uploading file: ${e.message}")
            throw RuntimeException("Error uploading file to minio", e)
        }
    }

    fun downloadFile(fileName: String): ByteArray {
        logger.info("Downloading file: $fileName")
        try {
            val getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build()
            val response = s3Client.getObject(getObjectRequest)
            return response.readAllBytes()
        } catch (e: SdkException) {
            logger.error("Error downloading file: ${e.message}")
            throw RuntimeException("Error downloading file from minio", e)
        }
    }
}
