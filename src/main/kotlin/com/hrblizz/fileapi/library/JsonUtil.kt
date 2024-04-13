package com.hrblizz.fileapi.library

import com.fasterxml.jackson.databind.ObjectMapper
import com.hrblizz.fileapi.config.ObjectMapperConfig.Companion.OBJECT_MAPPER
import java.text.SimpleDateFormat
import java.util.*

object JsonUtil {
    private const val DATE_FORMAT_PATTERN = "yyyy-MM-dd HH:mm a z"
    private val objectMapper: ObjectMapper = OBJECT_MAPPER.copy()
    private val dateFormat: SimpleDateFormat = SimpleDateFormat(DATE_FORMAT_PATTERN, Locale.getDefault())

    fun toJson(obj: Any, usePrettyWriter: Boolean = false, formatDates: Boolean = false): String {
        return runCatching {
            val writer = when {
                usePrettyWriter -> objectMapper.writerWithDefaultPrettyPrinter()
                else -> objectMapper.writer()
            }
            if (formatDates) objectMapper.dateFormat = dateFormat
            writer.writeValueAsString(obj)
        }.getOrDefault("")
    }

    fun <T> fromJson(json: String, clazz: Class<T>): T {
        return objectMapper.readValue(json, clazz)
    }

    inline fun <reified T> fromJson(json: String): T {
        return fromJson(json, T::class.java)
    }
}
