import com.hrblizz.fileapi.rest.ErrorMessage

data class ResponseEntity<T>(
    val data: T? = null,
    val errors: List<ErrorMessage>? = null,
    val status: Int
) {
    companion object {
        operator fun <T> invoke(status: Int): ResponseEntity<T> {
            return ResponseEntity(null, null, status)
        }

        operator fun <T> invoke(data: T?, status: Int): ResponseEntity<T> {
            return ResponseEntity(data, null, status)
        }

        operator fun <T> invoke(data: T?, errors: List<ErrorMessage>?, status: Int): ResponseEntity<T> {
            return ResponseEntity(data, errors, status)
        }
    }
}

data class ErrorMessage(
    val code: String,
    val message: String
)
