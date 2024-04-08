package com.hrblizz.fileapi.rest

import jakarta.validation.constraints.NotBlank

data class FileUploadMetadata(
    @field:NotBlank(message = "Name is required")
    val name: String,

    @field:NotBlank(message = "Content type is required")
    val contentType: String,

    @field:NotBlank(message = "Meta is required")
    val meta: String,

    @field:NotBlank(message = "Source is required")
    val source: String,

    val expireTime: Long?
)
