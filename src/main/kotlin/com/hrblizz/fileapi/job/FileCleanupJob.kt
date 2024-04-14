package com.hrblizz.fileapi.job

import com.hrblizz.fileapi.library.log.Logger
import com.hrblizz.fileapi.lock.Lock
import com.hrblizz.fileapi.service.FileService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.Instant

@Component
class FileCleanupJob(
    private val fileService: FileService,
    private val logger: Logger,
    private val clock: Clock
) {
    @Scheduled(cron = "0 0 0 * * *") // Run every day at midnight
    @Lock(key = "file-cleanup-job", retry = false)
    fun cleanupExpiredFiles() {
        logger.info("Running file cleanup job")
        val currentTime = Instant.now(clock)
        fileService.removeExpiredFiles(currentTime)
        logger.info("File cleanup job completed")
    }
}
