package com.hrblizz.fileapi.rest

data class ResponseEntity<T>(
    val data: T? = null,
    val errors: List<ErrorMessage>? = null,
    val status: Int
)
