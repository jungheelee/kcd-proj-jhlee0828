package com.kcd.payment.paymentsystem.exception

abstract class CardException(val errorCode: String, override val message: String, cause: Throwable? = null) : RuntimeException(message, cause)
class CardAlreadyRegisteredException : CardException("CARD_ALREADY_REGISTERED", "The card is already registered.")
class CardNotFoundException(customerKey: String) : CardException("CARD_NOT_FOUND", "The card is not found. ccId: $customerKey")
class IssueBillingKeyFailedException(customerKey: String, message: String, cause: Throwable? = null) : CardException("ISSUE_BILLING_KEY_FAILED", "Failed to issue a billing key. customerKey: $customerKey, message: $message", cause)