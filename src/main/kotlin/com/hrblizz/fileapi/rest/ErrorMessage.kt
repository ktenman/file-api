package com.hrblizz.fileapi.rest

data class ErrorMessage(
    val code: String? = null,
    val message: String? = null,
    val errors: List<String>? = null
)
