package com.kcd.payment.paymentsystem.domain

import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.Currency

//@Entity
class PaymentTransaction (
    @Id @GeneratedValue var id: Long? = null,
    val paymentStatus: PaymentStatus? = PaymentStatus.READY,
    var mId: String,
    var customerKey: String,
    var transactionKey: String,
    var paymentKey: String,

    var orderId: String,
    var payMethod: PayMethod,

    var transactionAt: LocalDateTime = LocalDateTime.now(),
    var currency: Currency = Currency.getInstance(""),
    var amount: BigDecimal,
) {
}