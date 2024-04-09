package com.hrblizz.fileapi.controller

import com.hrblizz.fileapi.IntegrationTest
import com.hrblizz.fileapi.IntegrationTest.Companion.DEFAULT_ROLE
import com.hrblizz.fileapi.IntegrationTest.Companion.PASSWORD
import com.hrblizz.fileapi.IntegrationTest.Companion.USERNAME
import com.hrblizz.fileapi.data.entities.FileMetadata
import com.hrblizz.fileapi.library.JsonUtil
import com.hrblizz.fileapi.rest.FileMetaDataResponse
import com.hrblizz.fileapi.rest.FileMetaResponse
import com.hrblizz.fileapi.rest.FileUploadMetadata
import com.hrblizz.fileapi.storage.StorageService
import jakarta.annotation.Resource
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.dropCollection
import org.springframework.http.MediaType
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.mock.web.MockMultipartFile
import org.springframework.mock.web.MockPart
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Clock
import java.time.Instant
import java.util.*


@IntegrationTest
class FileControllerIntegrationTest {

    @MockBean
    private lateinit var clock: Clock

    @Resource
    private lateinit var mockMvc: MockMvc

    @Resource
    private lateinit var mongoTemplate: MongoTemplate

    @Resource
    private lateinit var storageService: StorageService

    private val testFile = MockMultipartFile(
        "file",
        "test.txt",
        "text/plain",
        "Test content".toByteArray()
    )
    @BeforeEach
    fun setUp() {
        whenever(clock.instant()).thenReturn(Instant.parse("2025-01-01T00:00:00Z"))
        whenever(clock.zone).thenReturn(Clock.systemUTC().zone)
        mongoTemplate.dropCollection<FileMetadata>()
    }

    @Nested
    @DisplayName("POST /files")
    internal inner class PostFiles {
        private val urlTemplate = "/files"

        @Test
        @WithMockUser(username = USERNAME, password = PASSWORD, roles = [DEFAULT_ROLE])
        fun `should upload file and persist metadata`() {
            val metadata = FileUploadMetadata(
                name = "test.txt",
                contentType = "text/plain",
                meta = mapOf("creatorEmployeeId" to 1),
                source = "test",
                expireTime = null
            )
            val metadataPart = MockPart("metadata", JsonUtil.toJson(metadata).toByteArray())
            metadataPart.headers.contentType = APPLICATION_JSON

            mockMvc.perform(multipart(urlTemplate).file(testFile).part(metadataPart))
                .andExpect(status().isCreated)

            val fileMetadataList = mongoTemplate.findAll(FileMetadata::class.java)
            assertThat(fileMetadataList).isNotNull.hasSize(1)
            val fileMetadata = fileMetadataList.first()
            assertThat(fileMetadata.name).isEqualTo("test.txt")
            assertThat(fileMetadata.contentType).isEqualTo("text/plain")
            assertThat(fileMetadata.meta).isEqualTo("{\"creatorEmployeeId\":1}")
            assertThat(fileMetadata.source).isEqualTo("test")
            val downloadedBytes = storageService.downloadFile(fileMetadata.token)
            assertThat(downloadedBytes).isNotNull
            val downloadedContent = String(downloadedBytes)
            assertThat(downloadedContent).isEqualTo("Test content")
        }

        @Test
        @WithMockUser(username = USERNAME, password = PASSWORD, roles = [DEFAULT_ROLE])
        fun `should return bad request when name is missing`() {
            val metadata = FileUploadMetadata(
                name = "",
                contentType = "text/plain",
                meta = mapOf("creatorEmployeeId" to 1),
                source = "test",
                expireTime = null
            )
            val metadataPart = MockPart("metadata", JsonUtil.toJson(metadata).toByteArray())
            metadataPart.headers.contentType = APPLICATION_JSON

            mockMvc.perform(multipart(urlTemplate).file(testFile).part(metadataPart))
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.errors[0]").value("Name is required"))
        }

        @Test
        @WithMockUser(username = USERNAME, password = PASSWORD, roles = [DEFAULT_ROLE])
        fun `should return bad request when content type is missing`() {
            val metadata = FileUploadMetadata(
                name = "test.txt",
                contentType = "",
                meta = mapOf("creatorEmployeeId" to 1),
                source = "test",
                expireTime = null
            )
            val metadataPart = MockPart("metadata", JsonUtil.toJson(metadata).toByteArray())
            metadataPart.headers.contentType = APPLICATION_JSON

            mockMvc.perform(multipart(urlTemplate).file(testFile).part(metadataPart))
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.errors[0]").value("Content type is required"))
        }

        @Test
        @WithMockUser(username = USERNAME, password = PASSWORD, roles = [DEFAULT_ROLE])
        fun `should return bad request when meta is missing`() {
            val metadata = FileUploadMetadata(
                name = "test.txt",
                contentType = "text/plain",
                meta = emptyMap(),
                source = "test",
                expireTime = null
            )
            val metadataPart = MockPart("metadata", JsonUtil.toJson(metadata).toByteArray())
            metadataPart.headers.contentType = APPLICATION_JSON

            mockMvc.perform(multipart(urlTemplate).file(testFile).part(metadataPart))
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.errors[0]").value("Metadata is required"))
        }

        @Test
        @WithMockUser(username = USERNAME, password = PASSWORD, roles = [DEFAULT_ROLE])
        fun `should return bad request when source is missing`() {
            val metadata = FileUploadMetadata(
                name = "test.txt",
                contentType = "text/plain",
                meta = mapOf("creatorEmployeeId" to 1),
                source = "",
                expireTime = null
            )
            val metadataPart = MockPart("metadata", JsonUtil.toJson(metadata).toByteArray())
            metadataPart.headers.contentType = APPLICATION_JSON

            mockMvc.perform(multipart(urlTemplate).file(testFile).part(metadataPart))
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.errors[0]").value("Source is required"))
        }

        @Test
        fun `should return unauthorized when user is not authenticated`() {
            val metadata = FileUploadMetadata(
                name = "test.txt",
                contentType = "text/plain",
                meta = mapOf("creatorEmployeeId" to 1),
                source = "test",
                expireTime = null
            )
            val metadataPart = MockPart("metadata", JsonUtil.toJson(metadata).toByteArray())
            metadataPart.headers.contentType = APPLICATION_JSON

            mockMvc.perform(multipart("/files").file(testFile).part(metadataPart))
                .andExpect(status().isUnauthorized)
        }

        @Test
        @WithMockUser(username = USERNAME, password = PASSWORD, roles = [DEFAULT_ROLE])
        fun `should return bad request when file is missing`() {
            val metadata = FileUploadMetadata(
                name = "test.txt",
                contentType = "text/plain",
                meta = mapOf("creatorEmployeeId" to 1),
                source = "test",
                expireTime = null
            )
            val metadataPart = MockPart("metadata", JsonUtil.toJson(metadata).toByteArray())
            metadataPart.headers.contentType = APPLICATION_JSON

            mockMvc.perform(multipart("/files").part(metadataPart))
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.message").value("Required part 'file' is not present."))
        }
    }

    @Nested
    @DisplayName("GET /files?tokens={comma-separated-tokens}")
    inner class GetFilesByMetadata {
        @Test
        @WithMockUser(username = USERNAME, password = PASSWORD, roles = [DEFAULT_ROLE])
        fun `should return file metadata for given tokens`() {
            val metadata1 = FileMetadata(
                token = UUID.randomUUID().toString(),
                name = "test1.txt",
                contentType = "text/plain",
                meta = "{\"creatorEmployeeId\":1}",
                source = "test",
                expireTime = null
            )
            val metadata2 = FileMetadata(
                token = UUID.randomUUID().toString(),
                name = "test2.txt",
                contentType = "text/plain",
                meta = "{\"creatorEmployeeId\":1}",
                source = "test",
                expireTime = Instant.parse("2026-01-01T00:00:00Z")
            )
            val metadata3 = FileMetadata(
                token = UUID.randomUUID().toString(),
                name = "test2.txt",
                contentType = "text/plain",
                meta = "{\"creatorEmployeeId\":1}",
                source = "test",
                expireTime = Instant.parse("2019-01-01T00:00:00Z")
            )
            mongoTemplate.save(metadata1)
            mongoTemplate.save(metadata2)
            mongoTemplate.save(metadata3)
            val tokens = arrayOf(metadata1.token, metadata2.token, metadata3.token)

            val result = mockMvc.perform(get("/files").param("tokens", *tokens))
                .andExpect(status().isOk)
                .andReturn()

            val response: FileMetaResponse = JsonUtil.fromJson(result.response.contentAsString)
            assertThat(response.files)
                .hasSize(2)
                .containsKeys(metadata1.token, metadata2.token)
                .containsValues(
                    FileMetaDataResponse(metadata1.copy(createTime = response.files[metadata1.token]!!.createTime)),
                    FileMetaDataResponse(metadata2.copy(createTime = response.files[metadata2.token]!!.createTime))
                )
        }

        @Test
        @WithMockUser(username = USERNAME, password = PASSWORD, roles = [DEFAULT_ROLE])
        fun `should return empty response when no tokens are provided`() {
            mockMvc.perform(get("/files?tokens="))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.files").isEmpty)
        }

        @Test
        @WithMockUser(username = USERNAME, password = PASSWORD, roles = [DEFAULT_ROLE])
        fun `should return bad request when tokens are missing`() {
            mockMvc.perform(get("/files"))
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.message").value("Required request parameter 'tokens' is missing."))
        }

        @Test
        fun `should return unauthorized when user is not authenticated`() {
            mockMvc.perform(get("/files").param("tokens", "some-token"))
                .andExpect(status().isUnauthorized)
                .andReturn()
        }
    }

    @Nested
    @DisplayName("GET /files/{token}/meta")
    inner class GetFileMetadataByToken {
        @Test
        @WithMockUser(username = USERNAME, password = PASSWORD, roles = [DEFAULT_ROLE])
        fun `should return file metadata`() {
            val metadata = FileMetadata(
                token = UUID.randomUUID().toString(),
                name = "test.txt",
                contentType = MediaType.TEXT_PLAIN_VALUE,
                meta = "{\"creatorEmployeeId\":1}",
                source = "test",
                expireTime = null
            )
            mongoTemplate.save(metadata)

            val result = mockMvc.perform(get("/files/${metadata.token}/meta"))
                .andExpect(status().isOk)
                .andReturn()

            val response: FileMetaDataResponse = JsonUtil.fromJson(result.response.contentAsString)
            assertThat(response).isEqualTo(FileMetaDataResponse(metadata.copy(createTime = response.createTime)))
        }

        @Test
        @WithMockUser(username = USERNAME, password = PASSWORD, roles = [DEFAULT_ROLE])
        fun `should return not found when file is expired`() {
            val metadata = FileMetadata(
                token = UUID.randomUUID().toString(),
                name = "test.txt",
                contentType = MediaType.TEXT_PLAIN_VALUE,
                meta = "{\"creatorEmployeeId\":1}",
                source = "test",
                expireTime = Instant.parse("2023-01-01T00:00:00Z")
            )
            mongoTemplate.save(metadata)

            mockMvc.perform(get("/files/${metadata.token}/meta"))
                .andExpect(status().isNotFound)
        }

        @Test
        @WithMockUser(username = USERNAME, password = PASSWORD, roles = [DEFAULT_ROLE])
        fun `should return not found when file metadata does not exist`() {
            val token = "non-existent-token"

            mockMvc.perform(get("/files/$token/meta"))
                .andExpect(status().isNotFound)
        }

        @Test
        fun `should return unauthorized when user is not authenticated`() {
            val token = "some-token"

            mockMvc.perform(get("/files/$token/meta"))
                .andExpect(status().isUnauthorized)
        }
    }

    @Nested
    @DisplayName("GET /files/{token}/content")
    inner class GetFileByToken {
        @Test
        @WithMockUser(username = USERNAME, password = PASSWORD, roles = [DEFAULT_ROLE])
        fun `should download file with metadata`() {
            val metadata = FileMetadata(
                token = UUID.randomUUID().toString(),
                name = "test.txt",
                contentType = MediaType.TEXT_PLAIN_VALUE,
                meta = "Test file 1",
                source = "test",
                expireTime = null
            )
            mongoTemplate.save(metadata)
            storageService.uploadFile(testFile, metadata.token)

            val result = mockMvc.perform(get("/files/${metadata.token}/content"))
                .andExpect(status().isOk)
                .andExpect(header().string("X-Filename", "test.txt"))
                .andExpect(header().string("X-Filesize", testFile.size.toString()))
                .andExpect(header().exists("X-CreateTime"))
                .andExpect(header().string("Content-Type", MediaType.TEXT_PLAIN_VALUE))
                .andReturn()

            val downloadedContent = result.response.contentAsString
            assertThat(downloadedContent).isEqualTo("Test content")
        }

        @Test
        @WithMockUser(username = USERNAME, password = PASSWORD, roles = [DEFAULT_ROLE])
        fun `should return not found when file does not exist`() {
            val token = "non-existent-token"

            mockMvc.perform(get("/files/$token/content"))
                .andExpect(status().isNotFound)
        }

        @Test
        fun `should return unauthorized when user is not authenticated`() {
            val token = "some-token"

            mockMvc.perform(get("/files/$token/content"))
                .andExpect(status().isUnauthorized)
        }
    }

    @Nested
    @DisplayName("DELETE /files/{token}")
    inner class DeleteFileByToken {
        @Test
        @WithMockUser(username = USERNAME, password = PASSWORD, roles = [DEFAULT_ROLE])
        fun `should delete file and metadata`() {
            val metadata = FileMetadata(
                token = UUID.randomUUID().toString(),
                name = "test.txt",
                contentType = MediaType.TEXT_PLAIN_VALUE,
                meta = "{\"creatorEmployeeId\":1}",
                source = "test",
                expireTime = null
            )
            mongoTemplate.save(metadata)
            storageService.uploadFile(testFile, metadata.token)

            mockMvc.perform(delete("/files/${metadata.token}"))
                .andExpect(status().isNoContent)

            assertThat(mongoTemplate.findAll(FileMetadata::class.java)).isEmpty()
            assertThatThrownBy { storageService.downloadFile(metadata.token) }
                .isInstanceOf(RuntimeException::class.java)
                .hasMessage("Error downloading file from minio: ${metadata.token}")
        }

        @Test
        @WithMockUser(username = USERNAME, password = PASSWORD, roles = [DEFAULT_ROLE])
        fun `should return not found when file does not exist`() {
            val token = "non-existent-token"

            mockMvc.perform(delete("/files/$token"))
                .andExpect(status().isNotFound)
        }

        @Test
        fun `should return unauthorized when user is not authenticated`() {
            val token = "some-token"

            mockMvc.perform(delete("/files/$token"))
                .andExpect(status().isUnauthorized)
        }
    }

}
