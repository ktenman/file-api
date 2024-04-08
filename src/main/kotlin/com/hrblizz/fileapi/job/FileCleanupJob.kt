package com.hrblizz.fileapi.job

import com.hrblizz.fileapi.library.log.Logger
import com.hrblizz.fileapi.service.FileService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class FileCleanupJob(
    private val fileService: FileService,
    private val logger: Logger
) {
    @Scheduled(cron = "0 0 0 * * *") // Run every day at midnight
    fun cleanupExpiredFiles() {
        logger.info("Running file cleanup job")
        val currentTime = Instant.now()
        fileService.removeExpiredFiles(currentTime)
        logger.info("File cleanup job completed")
    }
}
