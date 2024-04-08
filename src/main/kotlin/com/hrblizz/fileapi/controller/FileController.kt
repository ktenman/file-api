package com.hrblizz.fileapi.controller

import com.hrblizz.fileapi.rest.FileMetaRequest
import com.hrblizz.fileapi.rest.FileMetaResponse
import com.hrblizz.fileapi.rest.FileUploadMetadata
import com.hrblizz.fileapi.rest.FileUploadResponse
import com.hrblizz.fileapi.service.FileService
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
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
import javax.validation.Valid

@RestController
@RequestMapping("/files")
class FileController(
    private val fileService: FileService
) {

    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @ResponseStatus(HttpStatus.CREATED)
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
    fun downloadFile(@PathVariable token: String): ResponseEntity<ByteArray> {
        val fileData = fileService.downloadFile(token)
        val headers = HttpHeaders()
        headers["X-Filename"] = fileData.name
        headers["X-Filesize"] = fileData.size.toString()
        headers["X-CreateTime"] = fileData.createTime.toString()
        headers.contentType = MediaType.parseMediaType(fileData.contentType)
        return ResponseEntity(fileData.content, headers, HttpStatus.OK)
    }
}
