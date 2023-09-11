package com.kcd.payment.paymentsystem.present

data class CardInfo (
    val cardId: Long,
    val cardNumber: String,
    val cvv: String,
    val customerKey: String,
    val cardIssuer: String,
    val expirationDate: String,
    val cardHolderName: String,
    val isAutoPayEnabled: Boolean,
    val password2Digit: String,
    val birthOrBizNo: String,
    val phoneNumber: String
)

data class CardInfoList(val cardInfos: List<CardInfo>)