package com.kcd.payment.paymentsystem.repository

import com.kcd.payment.paymentsystem.domain.CardEntity
import org.springframework.data.jpa.repository.JpaRepository

interface CardRepository : JpaRepository<CardEntity, Long> {
    fun findByCustomerKey(customerKey: String): CardEntity?
    fun findAllByPhoneNumber(phoneNumber: String): List<CardEntity>
    fun findByPhoneNumberAndCardNumber(phoneNumber: String, cardNumber: String): CardEntity?
}