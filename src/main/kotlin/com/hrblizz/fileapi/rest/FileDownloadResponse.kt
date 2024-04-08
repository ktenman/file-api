package com.hrblizz.fileapi.rest

import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType

data class FileDownloadResponse(val fileData: FileData) : HttpEntity<ByteArray>() {
    companion object {
        private fun createHeaders(fileData: FileData): HttpHeaders {
            val headers = HttpHeaders()
            headers["X-Filename"] = fileData.name
            headers["X-Filesize"] = fileData.size.toString()
            headers["X-CreateTime"] = fileData.createTime.toString()
            headers.contentType = MediaType.parseMediaType(fileData.contentType)
            return headers
        }
    }

    override fun getHeaders(): HttpHeaders = createHeaders(fileData)
    override fun getBody(): ByteArray = fileData.content
}
