package com.hrblizz.fileapi.rest

import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType

data class FileDownloadResponse(val fileData: FileData) : HttpEntity<ByteArray>(
    fileData.content,
    HttpHeaders().apply {
        this["X-Filename"] = fileData.name
        this["X-Filesize"] = fileData.size.toString()
        this["X-CreateTime"] = fileData.createTime.toString()
        contentType = MediaType.parseMediaType(fileData.contentType)
    }
)
