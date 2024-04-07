package com.hrblizz.fileapi.library

import com.hrblizz.fileapi.library.log.CORRELATION_ID
import com.hrblizz.fileapi.library.log.Logger
import com.hrblizz.fileapi.library.log.TraceLogItem
import org.slf4j.MDC
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class LoggerRequestInterceptor(
    private val logger: Logger
) : HandlerInterceptor {

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ): Boolean {
        request.setAttribute("start_time", System.nanoTime())
        return true
    }

    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: Exception?
    ) {
        val logItem = TraceLogItem(
            request.method,
            request.requestURL.toString(),
            response.status.toLong(),
            durationInMillis(request.getAttribute("start_time") as Long),
        )
        logItem.correlationId = MDC.get(CORRELATION_ID)
        this.logger.info(logItem)
        MDC.remove(CORRELATION_ID)
    }

    fun durationInMillis(startTime: Long): Long {
        val duration = (System.nanoTime() - startTime) / 1000000.0
        return duration.toLong()
    }
}
