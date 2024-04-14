package com.hrblizz.fileapi.lock

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Aspect
@Component
class LockAspect(private val lockService: LockService) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Around("@annotation(lock)")
    fun aroundLockedMethod(joinPoint: ProceedingJoinPoint, lock: Lock) {
        require(lock.key.isNotBlank()) { "Lock key cannot be empty" }
        val timeoutMillis = lock.timeoutMillis
        if (lock.retry) {
            lockService.acquireLock(lock.key, timeoutMillis)
        } else {
            val lockAcquired = lockService.tryAcquireLock(lock.key, timeoutMillis)
            check(lockAcquired) { "Unable to acquire lock for key: ${lock.key}" }
        }
        log.debug("Lock acquired for key ${lock.key}")
        try {
            joinPoint.proceed()
        } finally {
            lockService.releaseLock(lock.key)
            log.debug("Lock released for key ${lock.key}")
        }
    }
}
