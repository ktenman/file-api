package com.hrblizz.fileapi.job

import com.hrblizz.fileapi.lock.Lock
import com.hrblizz.fileapi.service.FileService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.Instant

@Component
class FileCleanupJob(
    private val fileService: FileService,
    private val clock: Clock
) {
    private val log = LoggerFactory.getLogger(javaClass)
    @Scheduled(cron = "0 0 0 * * *") // Run every day at midnight
    @Lock(key = "file-cleanup-job", retry = false)
    fun cleanupExpiredFiles() {
        log.info("Running file cleanup job")
        val currentTime = Instant.now(clock)
        fileService.removeExpiredFiles(currentTime)
        log.info("File cleanup job completed")
    }
}
