package com.kcd.payment.paymentsystem.repository

import com.kcd.payment.paymentsystem.domain.PaymentHistoryEntity
import org.springframework.data.jpa.repository.JpaRepository

interface PaymentHistoryRepository : JpaRepository<PaymentHistoryEntity, Long> {
    fun findByTransactionKey(transactionKey: String): PaymentHistoryEntity?
}