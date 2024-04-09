package com.hrblizz.fileapi.controller

import com.hrblizz.fileapi.IntegrationTest
import com.hrblizz.fileapi.IntegrationTest.Companion.DEFAULT_ROLE
import com.hrblizz.fileapi.IntegrationTest.Companion.PASSWORD
import com.hrblizz.fileapi.IntegrationTest.Companion.USERNAME
import com.hrblizz.fileapi.library.JsonUtil
import com.hrblizz.fileapi.rest.FileUploadMetadata
import com.hrblizz.fileapi.service.FileService
import com.hrblizz.fileapi.storage.StorageService
import jakarta.annotation.Resource
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.mock.web.MockMultipartFile
import org.springframework.mock.web.MockPart
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@IntegrationTest
class FileControllerInternalServerErrorIntegrationTest {

    @Resource
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var fileService: FileService

    @MockBean
    private lateinit var storageService: StorageService

    private val testFile = MockMultipartFile(
        "file",
        "test.txt",
        "text/plain",
        "Test content".toByteArray()
    )

    @Test
    @WithMockUser(username = USERNAME, password = PASSWORD, roles = [DEFAULT_ROLE])
    fun `should return 503 when an uncaught exception occurs during file upload`() {
        val metadata = FileUploadMetadata(
            name = "test.txt",
            contentType = "text/plain",
            meta = mapOf("creatorEmployeeId" to 1),
            source = "test",
            expireTime = null
        )
        val metadataPart = MockPart("metadata", JsonUtil.toJson(metadata).toByteArray())
        metadataPart.headers.contentType = APPLICATION_JSON

        whenever(storageService.uploadFile(any(), any()))
            .thenThrow(RuntimeException("Minio connection error"))

        mockMvc.perform(multipart("/files").file(testFile).part(metadataPart))
            .andExpect(status().isServiceUnavailable)
            .andExpect(jsonPath("$.code").value("internal_server_error"))
            .andExpect(jsonPath("$.message").value("An internal server error occurred"))
    }

    @Test
    @WithMockUser(username = USERNAME, password = PASSWORD, roles = [DEFAULT_ROLE])
    fun `should return 503 when an uncaught exception occurs during file download`() {
        val token = "some-token"

        whenever(fileService.downloadFile(token))
            .thenThrow(RuntimeException("Minio connection error"))

        mockMvc.perform(get("/files/$token/content"))
            .andExpect(status().isServiceUnavailable)
            .andExpect(jsonPath("$.code").value("internal_server_error"))
            .andExpect(jsonPath("$.message").value("An internal server error occurred"))
    }
}
