package com.kcd.payment.paymentsystem.domain

import com.kcd.payment.paymentsystem.domain.PaymentStatus.*
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*


@Entity
class PaymentEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    val cardId: Long,
    val customerKey: String,

    @Enumerated(EnumType.STRING)
    val type: Type? = Type.NORMAL,
    @Enumerated(EnumType.STRING)
    var paymentStatus: PaymentStatus = READY,

    var amount: BigDecimal,

    var orderId: String? = null,
    var orderName: String? = null,
    var payMethod: PayMethod? = PayMethod.CARD,

    @Column(columnDefinition = "datetime(6)")
    var startAt: LocalDateTime? = null,
    @Column(columnDefinition = "datetime(6)")
    var paidAt: LocalDateTime? = null,
    @Column(columnDefinition = "datetime(6)")
    var expiredAt: LocalDateTime? = null,
    @Column(columnDefinition = "datetime(6)")
    var canceledAt: LocalDateTime? = null,
) {
    @Column(unique = true)
    val transactionKey: String = generateTransactionKey(customerKey)

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

    private fun generateTransactionKey(customerKey: String): String {
        val uuid = UUID.randomUUID().toString()
        val base = "txn_${customerKey}_${uuid}"
        return if (base.length > 70) {
            base.take(70)
        } else {
            base
        }
    }

    fun updateStatus(newStatus: PaymentStatus) {
        this.paymentStatus = newStatus
        this.updatedAt = LocalDateTime.now()

        when(newStatus) {
            DONE -> this.paidAt = LocalDateTime.now()
            CANCELED -> this.canceledAt = LocalDateTime.now()
            IN_PROGRESS -> this.startAt = LocalDateTime.now()
            EXPIRED -> this.expiredAt = LocalDateTime.now()
            READY -> {}
            REJECTED -> {}
            ABORTED -> {}
        }
    }
}
