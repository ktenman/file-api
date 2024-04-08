package com.hrblizz.fileapi.rest

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import java.time.Instant

data class FileUploadMetadata(
    @field:NotBlank(message = "Name is required")
    val name: String,

    @field:NotBlank(message = "Content type is required")
    val contentType: String,

    @field:NotEmpty(message = "Metadata is required")
    val meta: Map<String, Any>,

    @field:NotBlank(message = "Source is required")
    val source: String,

    val expireTime: Instant?
)
