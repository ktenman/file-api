package com.hrblizz.fileapi.controller

import com.hrblizz.fileapi.IntegrationTest
import com.hrblizz.fileapi.data.repository.FileMetadataRepository
import com.hrblizz.fileapi.library.JsonUtil
import com.hrblizz.fileapi.rest.FileUploadMetadata
import com.hrblizz.fileapi.storage.StorageService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.mock.web.MockPart
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import javax.annotation.Resource

@IntegrationTest
internal class FileControllerIntegrationTest {

    @Resource
    private lateinit var mockMvc: MockMvc
    @Resource
    private lateinit var fileMetadataRepository: FileMetadataRepository
    @Resource
    private lateinit var storageService: StorageService
    val file = MockMultipartFile("file", "test.txt", "text/plain", "Test content".toByteArray())

    @Nested
    @DisplayName("POST /files")
    internal inner class PostFiles {
        @Test
        @WithMockUser(username = "admin", password = "hunter2", roles = ["USER"])
        fun `should upload file and persist metadata`() {
            val metadata = FileUploadMetadata(
                name = "test.txt",
                contentType = "text/plain",
                meta = "Test file",
                source = "test",
                expireTime = null
            )
            val metadataPart = MockPart("metadata", JsonUtil.toJson(metadata).toByteArray())
            metadataPart.headers.contentType = MediaType.APPLICATION_JSON

            mockMvc.perform(multipart("/files").file(file).part(metadataPart)).andExpect(status().isCreated)

            val fileMetadataList = fileMetadataRepository.findAll()
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
        @WithMockUser(username = "admin", password = "hunter2", roles = ["USER"])
        fun `should return bad request when name is missing`() {
            val metadata = FileUploadMetadata(
                name = "",
                contentType = "text/plain",
                meta = "Test file",
                source = "test",
                expireTime = null
            )
            val metadataPart = MockPart("metadata", JsonUtil.toJson(metadata).toByteArray())
            metadataPart.headers.contentType = MediaType.APPLICATION_JSON

            mockMvc.perform(multipart("/files").file(file).part(metadataPart))
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.errors[0].message").value("name: Name is required"))
        }

        @Test
        @WithMockUser(username = "admin", password = "hunter2", roles = ["USER"])
        fun `should return bad request when content type is missing`() {
            val metadata = FileUploadMetadata(
                name = "test.txt",
                contentType = "",
                meta = "Test file",
                source = "test",
                expireTime = null
            )
            val metadataPart = MockPart("metadata", JsonUtil.toJson(metadata).toByteArray())
            metadataPart.headers.contentType = MediaType.APPLICATION_JSON

            mockMvc.perform(multipart("/files").file(file).part(metadataPart))
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.errors[0].message").value("contentType: Content type is required"))
        }

        @Test
        @WithMockUser(username = "admin", password = "hunter2", roles = ["USER"])
        fun `should return bad request when meta is missing`() {
            val metadata = FileUploadMetadata(
                name = "test.txt",
                contentType = "text/plain",
                meta = "",
                source = "test",
                expireTime = null
            )
            val metadataPart = MockPart("metadata", JsonUtil.toJson(metadata).toByteArray())
            metadataPart.headers.contentType = MediaType.APPLICATION_JSON

            mockMvc.perform(multipart("/files").file(file).part(metadataPart))
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.errors[0].message").value("meta: Meta is required"))
        }

        @Test
        @WithMockUser(username = "admin", password = "hunter2", roles = ["USER"])
        fun `should return bad request when source is missing`() {
            val metadata = FileUploadMetadata(
                name = "test.txt",
                contentType = "text/plain",
                meta = "Test file",
                source = "",
                expireTime = null
            )
            val metadataPart = MockPart("metadata", JsonUtil.toJson(metadata).toByteArray())
            metadataPart.headers.contentType = MediaType.APPLICATION_JSON

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
            metadataPart.headers.contentType = MediaType.APPLICATION_JSON

            mockMvc.perform(multipart("/files").file(file).part(metadataPart))
                .andExpect(status().isUnauthorized)
        }

        @Test
        @WithMockUser(username = "admin", password = "hunter2", roles = ["USER"])
        fun `should return bad request when file is missing`() {
            val metadata = FileUploadMetadata(
                name = "test.txt",
                contentType = "text/plain",
                meta = "Test file",
                source = "test",
                expireTime = null
            )
            val metadataPart = MockPart("metadata", JsonUtil.toJson(metadata).toByteArray())
            metadataPart.headers.contentType = MediaType.APPLICATION_JSON

            mockMvc.perform(multipart("/files").part(metadataPart))
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.errors[0].message").value("file part is missing"))
        }
    }
}
