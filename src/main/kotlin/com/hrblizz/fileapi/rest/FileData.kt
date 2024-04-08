package com.hrblizz.fileapi.rest

import java.time.Instant

data class FileData(
    val name: String,
    val size: Long,
    val contentType: String,
    val createTime: Instant,
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
