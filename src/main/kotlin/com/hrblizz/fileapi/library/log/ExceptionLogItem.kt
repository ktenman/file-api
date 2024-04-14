package com.hrblizz.fileapi.library.log

import com.fasterxml.jackson.annotation.JsonIgnore
import java.io.PrintWriter
import java.io.StringWriter

data class ExceptionLogItem(
    override val message: String,
    @JsonIgnore val exception: Exception,
    override var transactionId: String? = null
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
        return "$message \n $stacktrace"
    }
}
