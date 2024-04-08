package com.hrblizz.fileapi.library

import com.hrblizz.fileapi.rest.ErrorMessage
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.support.WebExchangeBindException
import org.springframework.web.multipart.support.MissingServletRequestPartException

@ControllerAdvice
class RestExceptionHandler {

    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalStateException(exception: IllegalStateException): ResponseEntity<ErrorMessage> {
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
        return handleValidationException(exception)
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
        return bindingResult.fieldErrors.associate { it.field to (it.defaultMessage ?: "Validation error") }
    }

}
