package com.hrblizz.fileapi.lock

import com.hrblizz.fileapi.controller.exception.LockAcquisitionException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.anyString
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.ValueOperations
import java.time.Clock
import java.util.concurrent.TimeUnit

@ExtendWith(MockitoExtension::class)
class LockServiceTest {

    private val defaultLockIdentifier = "testLock"

    @Mock
    private lateinit var redisTemplate: StringRedisTemplate

    @Mock
    private lateinit var valueOperations: ValueOperations<String, String>

    @Mock
    private lateinit var clock: Clock

    @InjectMocks
    private lateinit var lockService: LockService

    @Test
    fun `acquireLock should acquire lock successfully`() {
        whenever(redisTemplate.opsForValue()).thenReturn(valueOperations)
        whenever(
            valueOperations.setIfAbsent(
                eq("lock:$defaultLockIdentifier"),
                eq("locked"),
                eq(60_000L),
                eq(TimeUnit.MILLISECONDS)
            )
        )
            .thenReturn(true)

        lockService.acquireLock(defaultLockIdentifier, 60_000)

        verify(valueOperations, times(1)).setIfAbsent(anyString(), anyString(), anyLong(), any())
    }

    @Test
    fun `acquireLock should throw exception when lock is already acquired`() {
        whenever(clock.millis())
            .thenReturn(0L)
            .thenReturn(5000L)

        val thrown = catchThrowable { lockService.acquireLock(defaultLockIdentifier, 60_000) }

        assertThat(thrown)
            .isInstanceOf(LockAcquisitionException::class.java)
            .hasMessageContaining("Unable to acquire lock for identifier: $defaultLockIdentifier")
    }

    @Test
    fun `releaseLock should release lock successfully`() {
        lockService.releaseLock(defaultLockIdentifier)

        verify(redisTemplate, times(1)).delete("lock:$defaultLockIdentifier")
    }

    @Test
    fun `acquireLock should retry acquiring lock with fibonacci backoff`() {
        whenever(redisTemplate.opsForValue()).thenReturn(valueOperations)
        whenever(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any()))
            .thenReturn(false)
            .thenReturn(false)
            .thenReturn(false)
            .thenReturn(true)
        whenever(clock.millis())
            .thenReturn(0L)   // Start time
            .thenReturn(30L)  // Retry 1
            .thenReturn(60L)  // Retry 2
            .thenReturn(90L)  // Retry 3
            .thenReturn(120L) // Lock acquired

        lockService.acquireLock(defaultLockIdentifier, 60_000)

        verify(valueOperations, times(4)).setIfAbsent(anyString(), anyString(), anyLong(), any())
        verify(clock, times(8)).millis()
    }

}
