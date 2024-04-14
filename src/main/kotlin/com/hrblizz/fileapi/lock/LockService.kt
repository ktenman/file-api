package com.hrblizz.fileapi.lock

import com.hrblizz.fileapi.controller.exception.LockAcquisitionException
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.lang.Boolean.TRUE
import java.time.Clock
import java.util.concurrent.TimeUnit

@Service
class LockService(
    private val redisTemplate: RedisTemplate<String, String>,
    private val clock: Clock
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun acquireLock(identifier: String, timeoutMillis: Long) {
        val startTime = clock.millis()
        val maxEndTime = startTime + DEFAULT_LOCK_WAIT_MILLIS
        var retryIntervalMillis = DEFAULT_LOCK_RETRY_INTERVAL_MILLIS

        while (clock.millis() < maxEndTime) {
            if (tryAcquireLock(identifier, timeoutMillis)) return
            Thread.sleep(retryIntervalMillis)
            retryIntervalMillis = (retryIntervalMillis * 2).coerceAtMost(maxEndTime - clock.millis())
        }
        throw LockAcquisitionException("Unable to acquire lock for identifier: $identifier")
    }

    fun tryAcquireLock(identifier: String, timeoutMillis: Long): Boolean = try {
        redisTemplate.opsForValue()
            .setIfAbsent(identifier.lockKey(), "locked", timeoutMillis, TimeUnit.MILLISECONDS) == TRUE
    } catch (e: Exception) {
        false
    }

    fun releaseLock(identifier: String) {
        try {
            redisTemplate.delete(identifier.lockKey())
        } catch (e: Exception) {
            log.error("Failed to release lock for identifier: $identifier", e)
        }
    }

    private fun String.lockKey() = "$LOCK_PREFIX$this"

    companion object {
        const val LOCK_PREFIX = "lock:"
        const val DEFAULT_LOCK_WAIT_MILLIS = 5000L
        const val DEFAULT_LOCK_RETRY_INTERVAL_MILLIS = 30L
    }
}
