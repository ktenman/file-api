package com.hrblizz.fileapi.controller

import com.hrblizz.fileapi.IntegrationTest
import com.hrblizz.fileapi.IntegrationTest.Companion.DEFAULT_PASSWORD
import com.hrblizz.fileapi.IntegrationTest.Companion.DEFAULT_ROLE
import com.hrblizz.fileapi.IntegrationTest.Companion.DEFAULT_USERNAME
import com.hrblizz.fileapi.data.entities.FileMetadata
import com.hrblizz.fileapi.library.JsonUtil
import com.hrblizz.fileapi.rest.FileMetaRequest
import com.hrblizz.fileapi.rest.FileMetaResponse
import com.hrblizz.fileapi.rest.FileUploadMetadata
import com.hrblizz.fileapi.storage.StorageService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.dropCollection
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.mock.web.MockMultipartFile
import org.springframework.mock.web.MockPart
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.*
import javax.annotation.Resource

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
    @DisplayName("POST /files")
    internal inner class PostFiles {
        @Test
        @WithMockUser(username = DEFAULT_USERNAME, password = DEFAULT_PASSWORD, roles = [DEFAULT_ROLE])
        fun `should upload file and persist metadata`() {
            val metadata = FileUploadMetadata(
                name = "test.txt",
                contentType = "text/plain",
                meta = "Test file",
                source = "test",
                expireTime = null
            )
            val metadataPart = MockPart("metadata", JsonUtil.toJson(metadata).toByteArray())
            metadataPart.headers.contentType = APPLICATION_JSON

            mockMvc.perform(multipart("/files").file(file).part(metadataPart)).andExpect(status().isCreated)

            val fileMetadataList = mongoTemplate.findAll(FileMetadata::class.java)
            assertThat(fileMetadataList).isNotNull.hasSize(1)
            val fileMetadata = fileMetadataList.first()
            assertThat(fileMetadata.name).isEqualTo("test.txt")
            assertThat(fileMetadata.contentType).isEqualTo("text/plain")
            assertThat(fileMetadata.meta).isEqualTo("Test file")
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
                meta = "Test file",
                source = "test",
                expireTime = null
            )
            val metadataPart = MockPart("metadata", JsonUtil.toJson(metadata).toByteArray())
            metadataPart.headers.contentType = APPLICATION_JSON

            mockMvc.perform(multipart("/files").file(file).part(metadataPart))
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.errors[0].message").value("name: Name is required"))
        }

        @Test
        @WithMockUser(username = DEFAULT_USERNAME, password = DEFAULT_PASSWORD, roles = [DEFAULT_ROLE])
        fun `should return bad request when content type is missing`() {
            val metadata = FileUploadMetadata(
                name = "test.txt",
                contentType = "",
                meta = "Test file",
                source = "test",
                expireTime = null
            )
            val metadataPart = MockPart("metadata", JsonUtil.toJson(metadata).toByteArray())
            metadataPart.headers.contentType = APPLICATION_JSON

            mockMvc.perform(multipart("/files").file(file).part(metadataPart))
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.errors[0].message").value("contentType: Content type is required"))
        }

        @Test
        @WithMockUser(username = DEFAULT_USERNAME, password = DEFAULT_PASSWORD, roles = [DEFAULT_ROLE])
        fun `should return bad request when meta is missing`() {
            val metadata = FileUploadMetadata(
                name = "test.txt",
                contentType = "text/plain",
                meta = "",
                source = "test",
                expireTime = null
            )
            val metadataPart = MockPart("metadata", JsonUtil.toJson(metadata).toByteArray())
            metadataPart.headers.contentType = APPLICATION_JSON

            mockMvc.perform(multipart("/files").file(file).part(metadataPart))
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.errors[0].message").value("meta: Meta is required"))
        }

        @Test
        @WithMockUser(username = DEFAULT_USERNAME, password = DEFAULT_PASSWORD, roles = [DEFAULT_ROLE])
        fun `should return bad request when source is missing`() {
            val metadata = FileUploadMetadata(
                name = "test.txt",
                contentType = "text/plain",
                meta = "Test file",
                source = "",
                expireTime = null
            )
            val metadataPart = MockPart("metadata", JsonUtil.toJson(metadata).toByteArray())
            metadataPart.headers.contentType = APPLICATION_JSON

            mockMvc.perform(multipart("/files").file(file).part(metadataPart))
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.errors[0].message").value("source: Source is required"))
        }

        @Test
        fun `should return unauthorized when user is not authenticated`() {
            val metadata = FileUploadMetadata(
                name = "test.txt",
                contentType = "text/plain",
                meta = "Test file",
                source = "test",
                expireTime = null
            )
            val metadataPart = MockPart("metadata", JsonUtil.toJson(metadata).toByteArray())
            metadataPart.headers.contentType = APPLICATION_JSON

            mockMvc.perform(multipart("/files").file(file).part(metadataPart))
                .andExpect(status().isUnauthorized)
        }

        @Test
        @WithMockUser(username = DEFAULT_USERNAME, password = DEFAULT_PASSWORD, roles = [DEFAULT_ROLE])
        fun `should return bad request when file is missing`() {
            val metadata = FileUploadMetadata(
                name = "test.txt",
                contentType = "text/plain",
                meta = "Test file",
                source = "test",
                expireTime = null
            )
            val metadataPart = MockPart("metadata", JsonUtil.toJson(metadata).toByteArray())
            metadataPart.headers.contentType = APPLICATION_JSON

            mockMvc.perform(multipart("/files").part(metadataPart))
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.errors[0].message").value("file part is missing"))
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
                meta = "Test file 1",
                source = "test",
                expireTime = null
            )
            val metadata2 = FileMetadata(
                token = UUID.randomUUID().toString(),
                name = "test2.txt",
                contentType = "text/plain",
                meta = "Test file 2",
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
            assertThat(response.files).hasSize(2)
                .containsExactlyInAnyOrderEntriesOf(
                    mapOf(
                        metadata1.token to metadata1,
                        metadata2.token to metadata2
                    )
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
}
