package com.hrblizz.fileapi.job

import com.hrblizz.fileapi.IntegrationTest
import com.hrblizz.fileapi.data.entities.FileMetadata
import com.hrblizz.fileapi.storage.StorageService
import jakarta.annotation.Resource
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.groups.Tuple
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.dropCollection
import java.time.Clock
import java.time.Instant

@IntegrationTest
class FileCleanupJobIntegrationTest {

    @MockBean
    private lateinit var clock: Clock

    @SpyBean
    private lateinit var storageService: StorageService

    @Resource
    private lateinit var fileCleanupJob: FileCleanupJob

    @Resource
    private lateinit var mongoTemplate: MongoTemplate

    @BeforeEach
    fun setup() {
        whenever(clock.instant()).thenReturn(Instant.parse("2025-01-01T00:00:00Z"))
        whenever(clock.zone).thenReturn(Clock.systemUTC().zone)
        mongoTemplate.dropCollection<FileMetadata>()
    }

    @Test
    fun `cleanupExpiredFiles should remove expired files and metadata`() {
        val expiredTime = Instant.parse("2024-01-01T00:00:00Z")
        val validTime = Instant.parse("2026-01-01T00:00:00Z")
        val expiredFileMetadata = FileMetadata(
            token = "expired_token",
            name = "expired_file.txt",
            contentType = "text/plain",
            meta = "{}",
            source = "test",
            expireTime = expiredTime
        )
        val validFileMetadata = FileMetadata(
            token = "valid_token",
            name = "valid_file.txt",
            contentType = "text/plain",
            meta = "\"{\"creatorEmployeeId\": 123}\"",
            source = "test",
            expireTime = validTime
        )
        mongoTemplate.save(expiredFileMetadata)
        mongoTemplate.save(validFileMetadata)

        fileCleanupJob.cleanupExpiredFiles()

        assertThat(mongoTemplate.findAll(FileMetadata::class.java))
            .extracting(FileMetadata::token)
            .contains(Tuple.tuple("valid_token"))
        verify(storageService, times(1)).deleteFile("expired_token")
        verify(storageService, never()).deleteFile("valid_token")
    }
}
