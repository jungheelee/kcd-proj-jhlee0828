package com.kcd.payment.paymentsystem

import com.kcd.payment.paymentsystem.exception.*
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalControllerAdvice {
    private val logger = LoggerFactory.getLogger(GlobalControllerAdvice::class.java)

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(ex: IllegalArgumentException): ResponseEntity<ApiResponse<*>> {
        return ResponseEntity(
            ApiResponse.fail(
                message = "Invalid argument",
                errorCode = HttpStatus.BAD_REQUEST.name,
                errorMessage = ex.message,
                data = null
            ),
            HttpStatus.BAD_REQUEST
        )
    }

    @ExceptionHandler(IssueBillingKeyFailedException::class)
    fun handleIssueBillingKeyFailedException(e: IssueBillingKeyFailedException): ResponseEntity<Map<String, Any>> {
        logger.error("IssueBillingKeyFailedException occurred: ${e.message}", e)
        return buildResponse(e, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(CardNotFoundException::class)
    fun handleCardNotFoundException(e: CardNotFoundException): ResponseEntity<Map<String, Any>> {
        logger.error("CardNotFoundException occurred: ${e.message}", e)
        return buildResponse(e, HttpStatus.NOT_FOUND)
    }

    private fun buildResponse(e: CardException, status: HttpStatus): ResponseEntity<Map<String, Any>> {
        val responseBody = mapOf(
            "errorCode" to e.errorCode,
            "message" to e.message
        )
        return ResponseEntity(responseBody, status)
    }
}

data class ApiResponse<T>(
    val status: String,
    val message: String? = null,
    val errorCode: String? = null,
    val errorMessage: String? = null,
    val data: T? = null,
) {
    companion object {
        fun <T> success(data: T): ApiResponse<T> {
            return ApiResponse("SUCCESS", "OK", data = data)
        }

        fun <T> fail(
            message: String,
            errorCode: String? = null,
            errorMessage: String? = null,
            data: T? = null
        ): ApiResponse<T> {
            return ApiResponse(
                "FAIL",
                message = message,
                data = data,
                errorCode = errorCode,
                errorMessage = errorMessage
            )
        }
    }
}
