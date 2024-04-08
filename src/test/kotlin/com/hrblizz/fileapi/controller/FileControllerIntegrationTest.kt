package com.hrblizz.fileapi.controller

import com.hrblizz.fileapi.IntegrationTest
import com.hrblizz.fileapi.IntegrationTest.Companion.DEFAULT_PASSWORD
import com.hrblizz.fileapi.IntegrationTest.Companion.DEFAULT_ROLE
import com.hrblizz.fileapi.IntegrationTest.Companion.DEFAULT_USERNAME
import com.hrblizz.fileapi.data.entities.FileMetadata
import com.hrblizz.fileapi.library.JsonUtil
import com.hrblizz.fileapi.rest.FileMetaDataResponse
import com.hrblizz.fileapi.rest.FileMetaRequest
import com.hrblizz.fileapi.rest.FileMetaResponse
import com.hrblizz.fileapi.rest.FileUploadMetadata
import com.hrblizz.fileapi.storage.StorageService
import jakarta.annotation.Resource
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.dropCollection
import org.springframework.http.MediaType
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.mock.web.MockMultipartFile
import org.springframework.mock.web.MockPart
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.*

@IntegrationTest
internal class FileControllerIntegrationTest {

    @Resource
    private lateinit var mockMvc: MockMvc

    @Resource
    private lateinit var mongoTemplate: MongoTemplate

    @Resource
    private lateinit var storageService: StorageService
    val file = MockMultipartFile("file", "test.txt", "text/plain", "Test content".toByteArray())

    @BeforeEach
    fun setUp() {
        mongoTemplate.dropCollection<FileMetadata>()
    }

    @Nested
    @DisplayName("POST /files/upload")
    internal inner class PostFiles {
        @Test
        @WithMockUser(username = DEFAULT_USERNAME, password = DEFAULT_PASSWORD, roles = [DEFAULT_ROLE])
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

            mockMvc.perform(multipart("/files/upload").file(file).part(metadataPart)).andExpect(status().isCreated)

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
        @WithMockUser(username = DEFAULT_USERNAME, password = DEFAULT_PASSWORD, roles = [DEFAULT_ROLE])
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

            mockMvc.perform(multipart("/files/upload").file(file).part(metadataPart))
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.errors[0]").value("Name is required"))
        }

        @Test
        @WithMockUser(username = DEFAULT_USERNAME, password = DEFAULT_PASSWORD, roles = [DEFAULT_ROLE])
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

            mockMvc.perform(multipart("/files/upload").file(file).part(metadataPart))
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.errors[0]").value("Content type is required"))
        }

        @Test
        @WithMockUser(username = DEFAULT_USERNAME, password = DEFAULT_PASSWORD, roles = [DEFAULT_ROLE])
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

            mockMvc.perform(multipart("/files/upload").file(file).part(metadataPart))
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.errors[0]").value("Metadata is required"))
        }

        @Test
        @WithMockUser(username = DEFAULT_USERNAME, password = DEFAULT_PASSWORD, roles = [DEFAULT_ROLE])
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

            mockMvc.perform(multipart("/files/upload").file(file).part(metadataPart))
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

            mockMvc.perform(multipart("/files/upload").file(file).part(metadataPart))
                .andExpect(status().isUnauthorized)
        }

        @Test
        @WithMockUser(username = DEFAULT_USERNAME, password = DEFAULT_PASSWORD, roles = [DEFAULT_ROLE])
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

            mockMvc.perform(multipart("/files/upload").part(metadataPart))
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.message").value("Required part 'file' is not present."))
        }
    }

    @Nested
    @DisplayName("POST /files/metas")
    inner class GetFilesByMetadata {
        @Test
        @WithMockUser(username = DEFAULT_USERNAME, password = DEFAULT_PASSWORD, roles = [DEFAULT_ROLE])
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
                expireTime = null
            )
            mongoTemplate.save(metadata1)
            mongoTemplate.save(metadata2)
            val request = FileMetaRequest(listOf(metadata1.token, metadata2.token))

            val result = mockMvc.perform(
                post("/files/metas")
                    .contentType(APPLICATION_JSON)
                    .content(JsonUtil.toJson(request))
            )
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
        @WithMockUser(username = DEFAULT_USERNAME, password = DEFAULT_PASSWORD, roles = [DEFAULT_ROLE])
        fun `should return empty response when no tokens are provided`() {
            val request = FileMetaRequest(emptyList())

            mockMvc.perform(
                post("/files/metas").contentType(APPLICATION_JSON).content(JsonUtil.toJson(request))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.files").isEmpty)
        }

        @Test
        fun `should return unauthorized when user is not authenticated`() {
            val request = FileMetaRequest(listOf("token1"))

            mockMvc.perform(
                post("/files/metas")
                    .contentType(APPLICATION_JSON)
                    .content(JsonUtil.toJson(request))
            ).andExpect(status().isUnauthorized)
        }
    }

    @Nested
    @DisplayName("GET /files/{token}")
    inner class GetFileByToken {
        @Test
        @WithMockUser(username = DEFAULT_USERNAME, password = DEFAULT_PASSWORD, roles = [DEFAULT_ROLE])
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
            storageService.uploadFile(file, metadata.token)

            val result = mockMvc.perform(get("/files/${metadata.token}"))
                .andExpect(status().isOk)
                .andExpect(header().string("X-Filename", "test.txt"))
                .andExpect(header().string("X-Filesize", file.size.toString()))
                .andExpect(header().exists("X-CreateTime"))
                .andExpect(header().string("Content-Type", MediaType.TEXT_PLAIN_VALUE))
                .andReturn()

            val downloadedContent = result.response.contentAsString
            assertThat(downloadedContent).isEqualTo("Test content")
        }

        @Test
        @WithMockUser(username = DEFAULT_USERNAME, password = DEFAULT_PASSWORD, roles = [DEFAULT_ROLE])
        fun `should return not found when file does not exist`() {
            val token = "non-existent-token"

            mockMvc.perform(get("/files/$token"))
                .andExpect(status().isNotFound)
        }

        @Test
        fun `should return unauthorized when user is not authenticated`() {
            val token = "some-token"

            mockMvc.perform(get("/files/$token"))
                .andExpect(status().isUnauthorized)
        }
    }
}
