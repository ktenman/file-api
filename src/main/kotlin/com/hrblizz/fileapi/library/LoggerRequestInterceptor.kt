package com.hrblizz.fileapi.library

import com.hrblizz.fileapi.library.log.TraceLogItem
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import java.util.*

@Component
class LoggerRequestInterceptor : HandlerInterceptor {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val transactionId = UUID.randomUUID().toString()
        MDC.put(TRANSACTION_ID, transactionId)
        request.setAttribute("start_time", System.nanoTime())
        return true
    }

    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        exception: Exception?
    ) {
        val logItem = TraceLogItem(
            request.method,
            request.requestURL.toString(),
            response.status.toLong(),
            durationInMillis(request.getAttribute("start_time") as Long),
        )
        logItem.transactionId = MDC.get(TRANSACTION_ID)
        log.info(logItem.toString())
        MDC.remove(TRANSACTION_ID)
    }

    fun durationInMillis(startTime: Long): Long {
        val duration = (System.nanoTime() - startTime) / 1000000.0
        return duration.toLong()
    }

    companion object {
        const val TRANSACTION_ID = "transactionId"
    }
}
