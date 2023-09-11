package com.kcd.payment.paymentsystem.present

import com.kcd.payment.paymentsystem.domain.PaymentStatus

class PaymentExecutionResponse(
    val transactionKey: String,
    var paymentStatus: PaymentStatus
)
