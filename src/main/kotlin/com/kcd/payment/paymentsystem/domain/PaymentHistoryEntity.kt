package com.kcd.payment.paymentsystem.domain

import jakarta.persistence.*

@Entity
class PaymentHistoryEntity (
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    val paymentId: Long,
    val transactionKey: String? = null,
    @Enumerated(EnumType.STRING)
    var paymentStatus: PaymentStatus? = PaymentStatus.READY
)