package com.hrblizz.fileapi.library.log

import com.hrblizz.fileapi.library.CORRELATION_ID
import org.slf4j.MDC
import org.springframework.stereotype.Component

@Component
class Logger {
    fun info(logItem: LogItem) {
        write("info", logItem)
    }

    fun info(string: String) {
        info(LogItem(string))
    }

    fun error(logItem: LogItem) {
        write("error", logItem)
    }

    fun error(string: String) {
        error(LogItem(string))
    }

    fun critical(logItem: LogItem) {
        write("critical", logItem)
    }

    fun critical(string: String) {
        critical(LogItem(string))
    }

    private fun write(logLevel: String, logItem: LogItem) {
        logItem.correlationId = MDC.get(CORRELATION_ID)
        println("$logLevel: $logItem")
    }
}
