package com.hrblizz.fileapi.lock

import com.hrblizz.fileapi.controller.exception.LockAcquisitionException
import java.time.Clock

interface LockService {
    val clock: Clock

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

    fun tryAcquireLock(identifier: String, timeoutMillis: Long): Boolean
    fun releaseLock(identifier: String)

    companion object {
        const val DEFAULT_LOCK_WAIT_MILLIS = 5000L
        const val DEFAULT_LOCK_RETRY_INTERVAL_MILLIS = 30L
    }
}
