package com.hrblizz.fileapi.security

import com.hrblizz.fileapi.library.JsonUtil
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus.UNAUTHORIZED
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component
import com.hrblizz.fileapi.rest.ErrorMessage as RestErrorMessage

@Component
class ApiAuthenticationEntryPoint : AuthenticationEntryPoint {

    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authEx: AuthenticationException
    ) {
        response.status = UNAUTHORIZED.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE

        val responseEntity = ResponseEntity(listOf(RestErrorMessage(authEx.message)), UNAUTHORIZED)

        response.writer.use { writer ->
            writer.println(JsonUtil.toJson(responseEntity))
            writer.flush()
        }
    }
}
