package com.hrblizz.fileapi.lock

import com.hrblizz.fileapi.library.log.Logger
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.expression.ExpressionParser
import org.springframework.expression.spel.standard.SpelExpressionParser
import org.springframework.expression.spel.support.StandardEvaluationContext
import org.springframework.stereotype.Component

@Aspect
@Component
class LockAspect(
    private val lockService: LockService,
    private val logger: Logger,
) {

    private val parser: ExpressionParser = SpelExpressionParser()

    @Around("@annotation(lock)")
    fun aroundLockedMethod(joinPoint: ProceedingJoinPoint, lock: Lock) {
        require(lock.key.isNotBlank()) { "Lock key cannot be empty" }
        val lockKey = getKey(lock.key, joinPoint)
        val timeoutMillis = lock.timeoutMillis
        if (lock.retry) {
            lockService.acquireLock(lockKey, timeoutMillis)
        } else {
            val lockAcquired = lockService.tryAcquireLock(lockKey, timeoutMillis)
            check(lockAcquired) { "Unable to acquire lock for identifier: $lockKey" }
        }
        logger.debug("Lock acquired for key ${lock.key} with lock key $lockKey")
        try {
            joinPoint.proceed()
        } finally {
            lockService.releaseLock(lockKey)
            logger.debug("Lock released for key ${lock.key} with lock key $lockKey")
        }
    }

    private fun getKey(keyExpression: String, joinPoint: ProceedingJoinPoint): String {
        val cleanedKeyExpression = when {
            keyExpression.startsWith("'") && keyExpression.endsWith("'") -> {
                return keyExpression.substring(1, keyExpression.length - 1)
            }

            keyExpression.startsWith("#") -> keyExpression.substring(1)
            else -> keyExpression
        }

        val keys = cleanedKeyExpression.split(".").filter { it.isNotEmpty() }
        require(keys.size >= 2) { "No nested key found in key expression" }

        val args = joinPoint.args
        val nestedKey = keys[1]

        val context = StandardEvaluationContext(args[0])
        val expression = parser.parseExpression(nestedKey)

        return expression.getValue(context, String::class.java)
            ?: throw IllegalArgumentException("Key expression evaluation resulted in null")
    }
}
