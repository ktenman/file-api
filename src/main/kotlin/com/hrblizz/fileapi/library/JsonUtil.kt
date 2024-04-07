package com.hrblizz.fileapi.library

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.text.SimpleDateFormat
import java.util.*

object JsonUtil {
    private const val DATE_FORMAT_PATTERN = "yyyy-MM-dd HH:mm a z"

    private val objectMapper: ObjectMapper = ObjectMapper()
        .registerModule(JavaTimeModule())
        .registerKotlinModule()
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)

    private val dateFormat: SimpleDateFormat = SimpleDateFormat(DATE_FORMAT_PATTERN, Locale.getDefault())

    /**
     * Safely writes the input object into a JSON string.
     *
     * @param obj The object to be serialized.
     * @param usePrettyWriter Whether to use pretty printing for the JSON output.
     * @param formatDates Whether to format dates using the specified date format.
     * @return The JSON string representation of the object, or an empty string if serialization fails.
     */
    fun toJson(obj: Any, usePrettyWriter: Boolean = false, formatDates: Boolean = false): String {
        return try {
            val writer = if (usePrettyWriter) {
                objectMapper.writerWithDefaultPrettyPrinter()
            } else {
                objectMapper.writer()
            }

            if (formatDates) {
                objectMapper.dateFormat = dateFormat
            }

            writer.writeValueAsString(obj)
        } catch (e: Exception) {
            // Return an empty string if serialization fails
            ""
        }
    }
}
