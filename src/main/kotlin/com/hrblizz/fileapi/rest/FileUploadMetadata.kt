package com.hrblizz.fileapi.rest

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import java.time.Instant

data class FileUploadMetadata(
    @field:NotBlank(message = "Name is required")
    @Schema(
        description = "Name of the file",
        example = "example.txt"
    )
    val name: String,

    @field:NotBlank(message = "Content type is required")
    @Schema(
        description = "MIME type of the file",
        example = "plain/text"
    )
    val contentType: String,

    @field:NotEmpty(message = "Metadata is required")
    @Schema(
        description = "JSON object of additional metadata",
        example = "{\"creatorEmployeeId\": 1, \"department\": \"HR\"}"
    )
    val meta: Map<String, Any>,

    @field:NotBlank(message = "Source is required")
    @Schema(
        description = "Source of the file",
        example = "HR system"
    )
    val source: String,

    @Schema(
        description = "Expiration time of the file",
        example = "2022-12-31T23:59:59Z"
    )
    val expireTime: Instant?
)
