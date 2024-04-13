package com.hrblizz.fileapi.library

import com.hrblizz.fileapi.controller.exception.BadRequestException
import com.hrblizz.fileapi.controller.exception.NotFoundException
import com.hrblizz.fileapi.library.log.ExceptionLogItem
import com.hrblizz.fileapi.library.log.Logger
import com.hrblizz.fileapi.rest.ErrorMessage
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.support.WebExchangeBindException
import org.springframework.web.multipart.support.MissingServletRequestPartException

@ControllerAdvice
class RestExceptionHandler(private val logger: Logger) {

    @ExceptionHandler(
        NotFoundException::class,
        BadRequestException::class,
        IllegalStateException::class,
        MissingServletRequestPartException::class,
        WebExchangeBindException::class,
        MethodArgumentNotValidException::class,
        MissingServletRequestParameterException::class,
        Exception::class
    )
    fun handleException(exception: Exception): ResponseEntity<ErrorMessage> {
        val (code, message, status) = when (exception) {
            is NotFoundException -> Triple(
                "not_found",
                exception.message ?: "Not found",
                HttpStatus.NOT_FOUND
            )

            is BadRequestException -> Triple(
                "bad_request",
                exception.message ?: "Bad request",
                HttpStatus.BAD_REQUEST
            )

            is IllegalStateException -> Triple(
                "illegal_state",
                exception.message ?: "Illegal state",
                HttpStatus.INTERNAL_SERVER_ERROR
            )

            is MissingServletRequestPartException -> Triple(
                "missing_request_part",
                "Required part '${exception.requestPartName}' is not present.",
                HttpStatus.BAD_REQUEST
            )

            is WebExchangeBindException, is MethodArgumentNotValidException -> {
                extractErrors(exception)
                Triple("validation_error", "Validation error", HttpStatus.BAD_REQUEST)
            }

            is MissingServletRequestParameterException -> Triple(
                "missing_request_parameter",
                "Required request parameter '${exception.parameterName}' is missing.",
                HttpStatus.BAD_REQUEST
            )

            else -> Triple(
                "internal_server_error",
                "An internal server error occurred",
                HttpStatus.SERVICE_UNAVAILABLE
            )
        }

        logException(exception, "$code exception occurred")

        return ResponseEntity(
            ErrorMessage(
                code = code,
                message = message,
                errors = extractErrors(exception).values.toList()
            ),
            status
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
        logger.error(ExceptionLogItem(message, exception))
    }
}
