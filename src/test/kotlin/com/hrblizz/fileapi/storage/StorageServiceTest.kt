package com.hrblizz.fileapi.storage

import com.hrblizz.fileapi.IntegrationTest
import jakarta.annotation.Resource
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.Test
import org.springframework.core.io.ClassPathResource
import org.springframework.mock.web.MockMultipartFile

@IntegrationTest
class StorageServiceTest {
    @Resource
    private lateinit var storageService: StorageService

    private val validFileName = "test-file.txt"
    private val invalidFileName = "non-existent-file.txt"

    @Test
    fun `upload and download a valid file`() {
        val file = loadFileFromClasspath("__files/$validFileName")

        storageService.uploadFile(file, validFileName)
        val downloadedFile = storageService.downloadFile(validFileName)

        assertThat(downloadedFile).isEqualTo(file.bytes)
        assertThat(String(downloadedFile)).isEqualTo("test content\n")
    }

    @Test
    fun `download a non-existent file`() {
        val thrown = catchThrowable { storageService.downloadFile(invalidFileName) }

        assertThat(thrown).isInstanceOf(RuntimeException::class.java)
            .hasMessageContaining("Error downloading file from minio")
    }

    @Test
    fun `upload an empty file`() {
        val emptyFileName = "empty-file.txt"
        val emptyFile = MockMultipartFile(
            emptyFileName,
            emptyFileName,
            "text/plain",
            ByteArray(0)
        )

        val thrown = catchThrowable { storageService.uploadFile(emptyFile, emptyFileName) }

        assertThat(thrown).isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Cannot upload an empty file")
    }

    @Test
    fun `upload with empty file name`() {
        val file = loadFileFromClasspath("__files/$validFileName")

        val thrown = catchThrowable { storageService.uploadFile(file, "") }

        assertThat(thrown).isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("File name cannot be empty")
    }

    @Test
    fun `delete an existing file`() {
        val file = loadFileFromClasspath("__files/$validFileName")
        storageService.uploadFile(file, validFileName)
        storageService.deleteFile(validFileName)

        val thrown = catchThrowable { storageService.downloadFile(validFileName) }

        assertThat(thrown).isInstanceOf(RuntimeException::class.java)
            .hasMessageContaining("Error downloading file from minio")
    }

    @Test
    fun `delete a non-existent file`() {
        val thrown = catchThrowable { storageService.deleteFile(invalidFileName) }

        assertThat(thrown).isNull()
    }

    private fun loadFileFromClasspath(fileName: String): MockMultipartFile {
        val resource = ClassPathResource(fileName)
        val fileContent = resource.inputStream.readBytes()
        return MockMultipartFile("file", fileName, null, fileContent)
    }
}
