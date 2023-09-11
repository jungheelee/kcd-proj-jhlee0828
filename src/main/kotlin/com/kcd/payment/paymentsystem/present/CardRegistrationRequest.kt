package com.kcd.payment.paymentsystem.present

data class CardRegistrationRequest (
    var cardNumber: String,
    var expirationDate: String,
    var cvv: String,
    val password2Digit: String,
    val birthOrBizNo: String,
    var cardHolderName: String,
    var isAutoPayEnabled: Boolean,
    var phoneNumber: String,
)