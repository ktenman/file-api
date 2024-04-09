package com.hrblizz.fileapi.library

import com.hrblizz.fileapi.controller.exception.BadRequestException
import com.hrblizz.fileapi.controller.exception.NotFoundException
import com.hrblizz.fileapi.library.log.ExceptionLogItem
import com.hrblizz.fileapi.library.log.Logger
import com.hrblizz.fileapi.rest.ErrorMessage
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.support.WebExchangeBindException
import org.springframework.web.multipart.support.MissingServletRequestPartException

@ControllerAdvice
class RestExceptionHandler(
    private val logger: Logger
) {

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFoundException(exception: NotFoundException): ResponseEntity<ErrorMessage> {
        logException(exception, "Not found exception occurred")
        return ResponseEntity(
            ErrorMessage(
                code = "not_found",
                message = exception.message ?: "Not found"
            ),
            HttpStatus.NOT_FOUND
        )
    }

    @ExceptionHandler(BadRequestException::class)
    fun handleBadRequestException(exception: BadRequestException): ResponseEntity<ErrorMessage> {
        logException(exception, "Bad request exception occurred")
        return ResponseEntity(
            ErrorMessage(
                code = "bad_request",
                message = exception.message ?: "Bad request"
            ),
            HttpStatus.BAD_REQUEST
        )
    }

    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalStateException(exception: IllegalStateException): ResponseEntity<ErrorMessage> {
        logException(exception, "Illegal state exception occurred")
        return ResponseEntity(
            ErrorMessage(
                code = "illegal_state",
                message = exception.message ?: "Illegal state"
            ),
            HttpStatus.INTERNAL_SERVER_ERROR
        )
    }

    @ExceptionHandler(MissingServletRequestPartException::class)
    fun handleMissingRequestPartException(exception: MissingServletRequestPartException): ResponseEntity<ErrorMessage> {
        logException(exception, "Missing request part exception occurred")
        return ResponseEntity(
            ErrorMessage(
                code = "missing_request_part",
                message = "Required part '${exception.requestPartName}' is not present."
            ),
            HttpStatus.BAD_REQUEST
        )
    }

    @ExceptionHandler(
        WebExchangeBindException::class,
        MethodArgumentNotValidException::class
    )
    fun handleValidationExceptions(exception: Exception): ResponseEntity<ErrorMessage> {
        logException(exception, "Validation exception occurred")
        return handleValidationException(exception)
    }

    @ExceptionHandler(Exception::class)
    fun handleInternalException(exception: Exception): ResponseEntity<ErrorMessage> {
        logException(exception, "Internal server error occurred")
        return ResponseEntity(
            ErrorMessage(
                code = "internal_server_error",
                message = "An internal server error occurred"
            ),
            HttpStatus.SERVICE_UNAVAILABLE
        )
    }

    private fun handleValidationException(exception: Exception): ResponseEntity<ErrorMessage> {
        val errors = extractErrors(exception)

        return ResponseEntity(
            ErrorMessage(
                code = "validation_error",
                message = "Validation error",
                errors = errors.values.toList()
            ),
            HttpStatus.BAD_REQUEST
        )
    }

    private fun extractErrors(exception: Exception): Map<String, String> {
        val bindingResult = when (exception) {
            is MethodArgumentNotValidException -> exception.bindingResult
            is WebExchangeBindException -> exception.bindingResult
            else -> return emptyMap()
        }
        return bindingResult.fieldErrors.associate {
            it.field to (it.defaultMessage ?: "Validation error")
        }
    }

    private fun logException(exception: Exception, message: String) {
        this.logger.error(ExceptionLogItem(message, exception))
    }
}
