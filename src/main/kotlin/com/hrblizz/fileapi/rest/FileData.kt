package com.hrblizz.fileapi.rest

import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

data class FileData(
    @Schema(description = "File name")
    val name: String,
    @Schema(description = "File size in bytes")
    val size: Long,
    @Schema(description = "File content type")
    val contentType: String,
    @Schema(description = "File creation time")
    val createTime: Instant,
    @Schema(description = "File content")
    val content: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FileData

        if (name != other.name) return false
        if (size != other.size) return false
        if (contentType != other.contentType) return false
        if (createTime != other.createTime) return false
        if (!content.contentEquals(other.content)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + size.hashCode()
        result = 31 * result + contentType.hashCode()
        result = 31 * result + createTime.hashCode()
        result = 31 * result + content.contentHashCode()
        return result
    }
}
