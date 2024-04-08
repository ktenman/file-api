package com.hrblizz.fileapi.library

import com.hrblizz.fileapi.controller.exception.BadRequestException
import com.hrblizz.fileapi.library.log.ExceptionLogItem
import com.hrblizz.fileapi.library.log.Logger
import com.hrblizz.fileapi.rest.ErrorMessage
import org.springframework.beans.TypeMismatchException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.lang.Nullable
import org.springframework.security.access.AccessDeniedException
import org.springframework.validation.BindException
import org.springframework.validation.BindingResult
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.context.request.WebRequest
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.multipart.support.MissingServletRequestPartException
import org.springframework.web.servlet.NoHandlerFoundException
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@ControllerAdvice
class RestExceptionHandler(
    private val log: Logger
) : ResponseEntityExceptionHandler() {

    override fun handleExceptionInternal(
        e: Exception,
        @Nullable body: Any?,
        headers: HttpHeaders,
        status: HttpStatus,
        request: WebRequest
    ): ResponseEntity<Any> {
        if (status.is5xxServerError) {
            log.critical(ExceptionLogItem("Internal exception: ${e.message}", e))
        }

        return when {
            body is ResponseEntity<*> -> ResponseEntity(body, headers, status)
            else -> createErrorResponseAny(status)
        }
    }

    override fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatus,
        request: WebRequest
    ): ResponseEntity<Any> = handleBindingErrors(ex, headers, HttpStatus.BAD_REQUEST, request)

    override fun handleBindException(
        ex: BindException,
        headers: HttpHeaders,
        status: HttpStatus,
        request: WebRequest
    ): ResponseEntity<Any> = handleBindingErrors(ex, headers, HttpStatus.BAD_REQUEST, request)

    private fun handleBindingErrors(
        ex: BindingResult,
        headers: HttpHeaders,
        status: HttpStatus,
        request: WebRequest
    ): ResponseEntity<Any> {
        val errors = ex.fieldErrors.map { ErrorMessage("${it.field}: ${it.defaultMessage}") } +
                ex.globalErrors.map { ErrorMessage("${it.objectName}: ${it.defaultMessage}") }

        val apiError = createErrorResponse(status, errors)
        val bindingException = BindingException()
        return handleExceptionInternal(bindingException, apiError, headers, status, request)
    }

    override fun handleTypeMismatch(
        ex: TypeMismatchException,
        headers: HttpHeaders,
        status: HttpStatus,
        request: WebRequest
    ): ResponseEntity<Any> = handleTypeMismatchException(ex, headers, HttpStatus.BAD_REQUEST, request)

    override fun handleMissingServletRequestPart(
        ex: MissingServletRequestPartException,
        headers: HttpHeaders,
        status: HttpStatus,
        request: WebRequest
    ): ResponseEntity<Any> = handleMissingPart(ex, headers, HttpStatus.BAD_REQUEST, request)

    override fun handleMissingServletRequestParameter(
        ex: MissingServletRequestParameterException,
        headers: HttpHeaders,
        status: HttpStatus,
        request: WebRequest
    ): ResponseEntity<Any> = handleMissingParameter(ex, headers, HttpStatus.BAD_REQUEST, request)

    private fun handleTypeMismatchException(
        ex: TypeMismatchException,
        headers: HttpHeaders,
        status: HttpStatus,
        request: WebRequest
    ): ResponseEntity<Any> {
        val apiError = createErrorResponse(
            status,
            listOf(ErrorMessage("${ex.value} value for ${ex.propertyName} should be of type ${ex.requiredType}"))
        )
        return handleExceptionInternal(ex, apiError, headers, status, request)
    }

    private fun handleMissingPart(
        ex: MissingServletRequestPartException,
        headers: HttpHeaders,
        status: HttpStatus,
        request: WebRequest
    ): ResponseEntity<Any> {
        val apiError = createErrorResponse(status, listOf(ErrorMessage("${ex.requestPartName} part is missing")))
        return handleExceptionInternal(ex, apiError, headers, status, request)
    }

    private fun handleMissingParameter(
        ex: MissingServletRequestParameterException,
        headers: HttpHeaders,
        status: HttpStatus,
        request: WebRequest
    ): ResponseEntity<Any> {
        val apiError = createErrorResponse(status, listOf(ErrorMessage("${ex.parameterName} parameter is missing")))
        return handleExceptionInternal(ex, apiError, headers, status, request)
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleMethodArgumentTypeMismatch(
        ex: MethodArgumentTypeMismatchException,
        request: WebRequest
    ): ResponseEntity<Any> {
        val errorStatus = HttpStatus.BAD_REQUEST
        val apiError = createErrorResponse(
            errorStatus,
            listOf(ErrorMessage("${ex.name} should be of type ${ex.requiredType?.name}"))
        )
        return ResponseEntity(apiError, HttpHeaders(), errorStatus)
    }

    override fun handleNoHandlerFoundException(
        ex: NoHandlerFoundException,
        headers: HttpHeaders,
        status: HttpStatus,
        request: WebRequest
    ): ResponseEntity<Any> = createErrorResponseEntity(ex, headers, HttpStatus.NOT_FOUND, request)

    override fun handleHttpRequestMethodNotSupported(
        ex: HttpRequestMethodNotSupportedException,
        headers: HttpHeaders,
        status: HttpStatus,
        request: WebRequest
    ): ResponseEntity<Any> = createErrorResponseEntity(ex, headers, HttpStatus.METHOD_NOT_ALLOWED, request)

    override fun handleHttpMediaTypeNotSupported(
        ex: HttpMediaTypeNotSupportedException,
        headers: HttpHeaders,
        status: HttpStatus,
        request: WebRequest
    ): ResponseEntity<Any> = createErrorResponseEntity(ex, headers, HttpStatus.UNSUPPORTED_MEDIA_TYPE, request)

    private fun createErrorResponseEntity(
        ex: Exception,
        headers: HttpHeaders,
        status: HttpStatus,
        request: WebRequest
    ): ResponseEntity<Any> {
        val apiError = createErrorResponse(status, listOf(ErrorMessage(status.reasonPhrase)))
        return handleExceptionInternal(ex, apiError, headers, status, request)
    }

    @ExceptionHandler(BadRequestException::class)
    fun handleBadRequest(ex: BadRequestException): ResponseEntity<Any> {
        val status = getResponseStatus(ex.javaClass) ?: HttpStatus.BAD_REQUEST
        val apiError = createErrorResponse(status, listOf(ErrorMessage(ex.message)))
        return ResponseEntity(apiError, HttpHeaders(), status)
    }

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDenied(ex: AccessDeniedException): ResponseEntity<Any> {
        val errorStatus = HttpStatus.UNAUTHORIZED
        val apiError = createErrorResponse(errorStatus, listOf(ErrorMessage(ex.message)))
        return ResponseEntity(apiError, HttpHeaders(), errorStatus)
    }

    @ExceptionHandler(Exception::class)
    fun handleAll(ex: Exception, request: WebRequest): ResponseEntity<Any> {
        log.error(ExceptionLogItem("Unhandled exception: ${ex.localizedMessage}", ex))

        val errorStatus = HttpStatus.INTERNAL_SERVER_ERROR
        val apiError = createErrorResponse(errorStatus, listOf(ErrorMessage("Unknown error occurred")))
        return ResponseEntity(apiError, HttpHeaders(), errorStatus)
    }

    private fun <T> getResponseStatus(ex: Class<T>?): HttpStatus? {
        if (ex == null) return null

        val responseStatus = ex.getAnnotation(ResponseStatus::class.java)
        return responseStatus?.value ?: getResponseStatus(ex.superclass)
    }

    data class ErrorResponse(val errors: List<ErrorMessage>)

    private fun createErrorResponse(
        status: HttpStatus,
        errors: List<ErrorMessage> = listOf(ErrorMessage(status.reasonPhrase))
    ): ResponseEntity<ErrorResponse> {
        return ResponseEntity(ErrorResponse(errors), HttpHeaders(), status)
    }

    private fun createErrorResponseAny(
        status: HttpStatus,
        errors: List<ErrorMessage> = listOf(ErrorMessage(status.reasonPhrase))
    ): ResponseEntity<Any> {
        val errorResponse = ErrorResponse(errors)
        return ResponseEntity(errorResponse, HttpHeaders(), status)
    }

    class BindingException : RuntimeException()
}
