package com.hrblizz.fileapi.library.log

import com.hrblizz.fileapi.library.JsonUtil
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.slf4j.MDC
import org.springframework.stereotype.Component
import java.util.*

const val CORRELATION_ID = "correlationId"

@Aspect
@Component
class LoggingAspect(private val logger: Logger) {

    @Around("@annotation(Loggable)")
    fun logMethod(joinPoint: ProceedingJoinPoint) {
        val signature = joinPoint.signature as MethodSignature
        val method = signature.method
        when (method.returnType) {
            Void.TYPE -> {
                logger.warn("Loggable annotation should not be used on methods without return type: ${method.name}")
                joinPoint.proceed()
            }

            else -> logInvocation(joinPoint)
        }
    }

    private fun logInvocation(joinPoint: ProceedingJoinPoint): Any {
        MDC.put(CORRELATION_ID, UUID.randomUUID().toString())
        logEntry(joinPoint)
        val result = joinPoint.proceed()
        logExit(joinPoint, result)
        return result
    }

    private fun logEntry(joinPoint: ProceedingJoinPoint) {
        val argsJson = JsonUtil.toJson(joinPoint.args)
        logger.info("${joinPoint.signature.toShortString()} entered with arguments: $argsJson")
    }

    private fun logExit(joinPoint: ProceedingJoinPoint, result: Any) {
        val resultJson = JsonUtil.toJson(result)
        logger.info("${joinPoint.signature.toShortString()} exited with result: $resultJson")
    }
}
