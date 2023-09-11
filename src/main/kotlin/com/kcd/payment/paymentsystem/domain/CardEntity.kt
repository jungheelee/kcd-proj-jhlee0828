package com.kcd.payment.paymentsystem.domain

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

/**
 * 암호화 생략
 * 포트원 빌링키 doc https://chaifinance.notion.site/642f8184eaf74465b824a309509d43f9
 */
@Entity
class CardEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    val cardNumber: String,

    @Enumerated(EnumType.STRING)
    var cardIssuer: CardIssuer = CardIssuer.identify(cardNumber),

    @Column(nullable = false)
    val expirationDate: String, // 카드 유효기간 YYYY-MM

    @Column(nullable = false)
    val cvv: String,            // 카드 인증번호

    @Column(nullable = false)
    val password2Digit: String, // 비밀번호 앞 2자리

    @Column(nullable = false)
    val birthOrBizNo: String, // 생년월일(6자리) 혹은 사업자번호 10자리

    @Column(nullable = false)
    val phoneNumber: String,  // 사용자 휴대폰 번호 = 식별자

    @Column(nullable = false)
    val cardHolderName: String, // 카드 소유자명

    val isAutoPayEnabled: Boolean = false, // 자동결제 여부
) {
    @Column(unique = true, nullable = false)
    val customerKey: String = generateCustomerKey()

    @Column(columnDefinition = "datetime(6)")
    var createdAt: LocalDateTime = LocalDateTime.now()

    @Column(columnDefinition = "datetime(6)")
    var updatedAt: LocalDateTime = LocalDateTime.now()

    @PrePersist
    fun prePersist() {
        createdAt = LocalDateTime.now()
    }

    @PreUpdate
    fun preUpdate() {
        updatedAt = LocalDateTime.now()
    }

    fun getMaskedCardNumber(): String {
        return cardNumber.take(6) + "******" + cardNumber.takeLast(3)
    }

    fun getMaskedCardHolderName(): String {
        return cardHolderName.take(1) + "**"
    }

    fun getMaskedBirthOrBizNo(): String {
        return birthOrBizNo.take(6) + "******"
    }

    fun getMaskedPassword2Digit(): String {
        return "**"
    }

    fun getMaskedCvv(): String {
        return cvv.take(1) + "**"
    }

    fun getMaskedExpirationDate(): String {
        return expirationDate.take(2) + "**"
    }

    fun getMaskedPhoneNumber(): String {
        return phoneNumber.take(3) + "****" + phoneNumber.takeLast(4)
    }

    fun getMaskedCardInfo(): String {
        return getMaskedCardNumber() + " " + getMaskedCardHolderName() + " " + getMaskedBirthOrBizNo() + " " + getMaskedPassword2Digit() + " " + getMaskedCvv() + " " + getMaskedExpirationDate()
    }

    private fun generateCustomerKey(): String {
        val uuid = UUID.randomUUID().toString()
        val base = "ccid_${uuid}"
        return if (base.length > 30) {
            base.take(30)
        } else {
            base
        }
    }
}