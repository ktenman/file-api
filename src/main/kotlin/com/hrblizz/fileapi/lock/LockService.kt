package com.hrblizz.fileapi.lock

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.lang.Boolean.TRUE
import java.time.Clock
import java.util.concurrent.TimeUnit
import kotlin.math.min

@Service
class LockService(
    private val redisTemplate: RedisTemplate<String, String>,
    private val clock: Clock
) {
    companion object {
        const val LOCK_PREFIX: String = "lock:"
        private const val DEFAULT_LOCK_WAIT_MILLIS: Long = 5000
        private const val DEFAULT_LOCK_RETRY_INTERVAL_MILLIS: Long = 30
    }

    fun acquireLock(identifier: String, timeoutMillis: Long) {
        var retryIntervalMillis = DEFAULT_LOCK_RETRY_INTERVAL_MILLIS
        val startTime = clock.millis()
        var previous: Long = 0
        var current: Long = 1

        while (clock.millis() - startTime < DEFAULT_LOCK_WAIT_MILLIS) {
            if (tryAcquireLock(identifier, timeoutMillis)) {
                return
            }
            sleep(retryIntervalMillis)
            val next = previous + current
            previous = current
            current = next
            retryIntervalMillis = calculateRetryInterval(current, retryIntervalMillis, startTime)
        }
        throw IllegalStateException("Unable to acquire lock for identifier: $identifier")
    }

    fun tryAcquireLock(identifier: String, timeoutMillis: Long): Boolean {
        val lockKey = LOCK_PREFIX + identifier
        return TRUE == redisTemplate.opsForValue()
            .setIfAbsent(lockKey, "locked", timeoutMillis, TimeUnit.MILLISECONDS)
    }

    private fun sleep(millis: Long) {
        try {
            TimeUnit.MILLISECONDS.sleep(millis)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            throw IllegalStateException("Lock acquisition interrupted", e)
        }
    }

    private fun calculateRetryInterval(
        current: Long,
        retryIntervalMillis: Long,
        startTime: Long,
    ): Long {
        return min(
            (current * retryIntervalMillis).toDouble(),
            (DEFAULT_LOCK_WAIT_MILLIS - (clock.millis() - startTime)).toDouble()
        ).toLong()
    }

    fun releaseLock(identifier: String) {
        val lockKey = LOCK_PREFIX + identifier
        redisTemplate.delete(lockKey)
    }
}
