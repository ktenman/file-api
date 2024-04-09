package com.hrblizz.fileapi.library.log

import java.time.LocalDateTime

open class LogItem(
    open val message: String
) {
    val dateTime: LocalDateTime = LocalDateTime.now()

    open var correlationId: String? = null
    var type: String? = null

    override fun toString(): String {
        return "[$dateTime] [$correlationId] $message"
    }
}
