package com.kcd.payment.paymentsystem.exception

abstract class PaymentException(val errorCode: String, override val message: String, cause: Throwable? = null) :
    RuntimeException(message, cause)

class PaymentFailedException(reason: String) : PaymentException("PAYMENT_FAILED", "Payment failed due to: $reason")

class TransactionKeyNotFoundException(val transactionKey: String) :
    PaymentException("TRANSACTION_KEY_NOT_FOUND", "The transactionKey is not found. transactionKey: $transactionKey")

class ExpiredTransactionException(val transactionKey: String) :
    PaymentException("EXPIRED_TRANSACTION", "The transaction is expired. transactionKey: $transactionKey")

class PortOneException(val portOneErrorCode: Int, val customerKey: String, val portOneMessage: String) : PaymentException(
    errorCode = portOneErrorCode.toString(),
    message = "PortOne error customerKey: $customerKey PortOne errror code: $portOneErrorCode PortOne error message: $portOneMessage"
)

// 더 다양한 상태에 대한 익셉션도 구체적으로 명시해서 상황에 따라 사용..
class PaymentNotFoundException(val transactionKey: String) : PaymentException("PAYMENT_NOT_FOUND", "The payment is not found. transactionKey: $transactionKey")
class PaymentAlreadyProcessedException(val transactionKey: String) : PaymentException("PAYMENT_ALREADY_PROCESSED", "The payment is already processed. transactionKey: $transactionKey")
class PaymentAlreadyCancelledException(val transactionKey: String) : PaymentException("PAYMENT_ALREADY_CANCELLED", "The payment is already cancelled. transactionKey: $transactionKey")
class PaymentAlreadyRefundedException(val transactionKey: String) : PaymentException("PAYMENT_ALREADY_REFUNDED", "The payment is already refunded. transactionKey: $transactionKey")
class PaymentAlreadyExpiredException(val transactionKey: String) : PaymentException("PAYMENT_ALREADY_EXPIRED", "The payment is already expired. transactionKey: $transactionKey")
class PaymentAlreadyRejectedException(val transactionKey: String) : PaymentException("PAYMENT_ALREADY_REJECTED", "The payment is already rejected. transactionKey: $transactionKey")
class PaymentAlreadyDoneException(val transactionKey: String) : PaymentException("PAYMENT_ALREADY_DONE", "The payment is already done. transactionKey: $transactionKey")