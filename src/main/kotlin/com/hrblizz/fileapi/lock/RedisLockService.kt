package com.hrblizz.fileapi.lock

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.lang.Boolean.TRUE
import java.time.Clock
import java.util.concurrent.TimeUnit

@Service
@ConditionalOnProperty(name = ["lock.service.provider"], havingValue = "redis")
class RedisLockService(
    override val clock: Clock,
    private val redisTemplate: RedisTemplate<String, String>
) : LockService {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun tryAcquireLock(identifier: String, timeoutMillis: Long): Boolean = try {
        redisTemplate.opsForValue()
            .setIfAbsent(identifier.lockKey(), "locked", timeoutMillis, TimeUnit.MILLISECONDS) == TRUE
    } catch (e: Exception) {
        log.error("Failed to acquire lock for identifier: $identifier", e)
        false
    }

    override fun releaseLock(identifier: String) {
        try {
            redisTemplate.delete(identifier.lockKey())
        } catch (e: Exception) {
            log.error("Failed to release lock for identifier: $identifier", e)
        }
    }

    private fun String.lockKey() = "$LOCK_PREFIX$this"

    companion object {
        const val LOCK_PREFIX = "lock:"
    }
}
