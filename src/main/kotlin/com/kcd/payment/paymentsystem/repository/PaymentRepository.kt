package com.kcd.payment.paymentsystem.repository

import com.kcd.payment.paymentsystem.domain.PaymentEntity
import org.springframework.data.jpa.repository.JpaRepository

interface PaymentRepository : JpaRepository<PaymentEntity, Long> {
    fun findByTransactionKey(transactionKey: String): PaymentEntity?
}