package com.kcd.payment.paymentsystem.present

import com.kcd.payment.paymentsystem.domain.PayMethod
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

class PaymentInitRequest(
    var cardId: Long? = null,
    var customerKey: String? = null,
    var transactionKey: String? = null,

    var payMethod: PayMethod? = PayMethod.CARD,

    var transactionAt: LocalDateTime = LocalDateTime.now(),
    var currency: Currency = Currency.getInstance("KRW"),
    var amount: BigDecimal? = BigDecimal.ZERO.setScale(currency.defaultFractionDigits),

    // 실제 주문 정보가 없으므로 임의의 값으로 설정
    var storeId: String? = "store_id_1",
    var orderId: String? = null,
    var orderName: String? = null,
)