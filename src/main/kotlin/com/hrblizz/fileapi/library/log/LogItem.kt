package com.hrblizz.fileapi.library.log

open class LogItem(
    open val message: String
) {

    open var transactionId: String? = null
    var type: String? = null

    override fun toString(): String {
        return message
    }
}
