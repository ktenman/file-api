package com.hrblizz.fileapi.controller

import com.hrblizz.fileapi.rest.FileDownloadResponse
import com.hrblizz.fileapi.rest.FileMetaDataResponse
import com.hrblizz.fileapi.rest.FileMetaRequest
import com.hrblizz.fileapi.rest.FileMetaResponse
import com.hrblizz.fileapi.rest.FileUploadMetadata
import com.hrblizz.fileapi.rest.FileUploadResponse
import com.hrblizz.fileapi.service.FileService
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Encoding
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/files")
class FileController(
    private val fileService: FileService
) {

    @PostMapping(
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE],
        path = ["/upload"]
    )
    @ResponseStatus(HttpStatus.CREATED)
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        content = [Content(
            encoding = [Encoding(
                contentType = MediaType.APPLICATION_JSON_VALUE,
                name = "metadata"
            )]
        )]
    )
    fun uploadFile(
        @Valid @RequestPart("metadata") request: FileUploadMetadata,
        @RequestParam("file") file: MultipartFile
    ): FileUploadResponse {
        val token = fileService.uploadFile(request, file)
        return FileUploadResponse(token)
    }

    @PostMapping("/metas")
    fun getFilesByMetadata(@RequestBody request: FileMetaRequest): FileMetaResponse {
        val files = fileService.getFilesByMetadata(request.tokens)
        return FileMetaResponse(files)
    }

    @GetMapping("/{token}")
    fun downloadFile(@PathVariable token: String): FileDownloadResponse {
        val fileData = fileService.downloadFile(token)
        return FileDownloadResponse(fileData)
    }

    @DeleteMapping("/{token}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteFile(@PathVariable token: String) {
        fileService.deleteFile(token)
    }

    @GetMapping("/{token}/meta")
    fun getFileMetadata(@PathVariable token: String): FileMetaDataResponse {
        val fileMetadata = fileService.getFileMetadata(token)
        return FileMetaDataResponse(fileMetadata)
    }
}
