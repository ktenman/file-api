package com.hrblizz.fileapi.library.log

import com.fasterxml.jackson.annotation.JsonIgnore
import java.io.PrintWriter
import java.io.StringWriter

data class ExceptionLogItem(
    override val message: String,
    @JsonIgnore val exception: Exception,
    override var correlationId: String? = null
) : LogItem(message) {

    @JsonIgnore
    val stacktrace: String

    init {
        type = "exception"
        val stringWriter = StringWriter()
        exception.printStackTrace(PrintWriter(stringWriter))
        stacktrace = stringWriter.toString()
    }

    override fun toString(): String {
        return "[$dateTime] [$correlationId] $message \n $stacktrace"
    }
}
