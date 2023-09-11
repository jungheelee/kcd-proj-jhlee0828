package com.kcd.payment.paymentsystem.present

import com.kcd.payment.paymentsystem.domain.PayMethod
import com.kcd.payment.paymentsystem.domain.PaymentStatus
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

class PaymentExecutionRequest (
    val paymentStatus: PaymentStatus? = PaymentStatus.READY,

    // 유니크한 데이터 식별자들
    var mId: String? = null,
    var customerKey: String? = null,
    var transactionKey: String,

    // 결제 이행 주문 정보
    var orderId: String? = null,
    var orderName: String? = null,
    var payMethod: PayMethod? = PayMethod.CARD,
    var transactionAt: LocalDateTime = LocalDateTime.now(),
    var currency: Currency = Currency.getInstance("KRW"),
    var amount: BigDecimal? = BigDecimal.ZERO.setScale(currency.defaultFractionDigits),

    // 오류 발생 시 웹훅을 통해 알림을 받을 URL
    var webhookUrl: String? = null,
)
