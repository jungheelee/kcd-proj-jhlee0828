package com.kcd.payment.paymentsystem.service

import com.kcd.payment.paymentsystem.ApiResponse
import com.kcd.payment.paymentsystem.domain.*
import com.kcd.payment.paymentsystem.exception.*
import com.kcd.payment.paymentsystem.present.PaymentExecutionRequest
import com.kcd.payment.paymentsystem.present.PaymentExecutionResponse
import com.kcd.payment.paymentsystem.present.PaymentInfo
import com.kcd.payment.paymentsystem.present.PaymentInitRequest
import com.kcd.payment.paymentsystem.repository.CardRepository
import com.kcd.payment.paymentsystem.repository.PaymentHistoryRepository
import com.kcd.payment.paymentsystem.repository.PaymentRepository
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class PaymentService(
    private val paymentRepository: PaymentRepository,
    private val paymentHistoryRepository: PaymentHistoryRepository,
    private val cardRepository: CardRepository,
    private val portOneService: PortOneService,
    private val webhookService: WebhookService,
) {

    @PersistenceContext
    private lateinit var entityManager: EntityManager
    private val logger = LoggerFactory.getLogger(PaymentService::class.java)

    @Transactional
    fun initiatePayment(paymentRequest: PaymentInitRequest): ApiResponse<PaymentInfo> {
        return with(paymentRequest) {
            val card = paymentRequest.customerKey?.let {
                cardRepository.findByCustomerKey(it) ?: throw CardNotFoundException(customerKey!!)
            }
            val payment = PaymentEntity(
                customerKey = card!!.customerKey,
                cardId = card.id!!,
                amount = amount!!,
                payMethod = PayMethod.CARD
            ).also { paymentRepository.save(it) }

            payment.id?.let {
                paymentHistoryRepository.save(
                    PaymentHistoryEntity(
                        paymentId = it,
                        transactionKey = paymentRequest.transactionKey,
                        paymentStatus = PaymentStatus.READY
                    )
                )
            }
            ApiResponse.success(PaymentInfo(transactionKey = payment.transactionKey))
        }
    }

    @Transactional
    fun executePayment(request: PaymentExecutionRequest): ApiResponse<PaymentExecutionResponse> {
        logger.info("Executing payment for transactionKey: ${request.transactionKey}")
        val payment = validateAndGetPayment(request.transactionKey)
        val card = validAndGetCard(payment)

        return try {
            executeAndFinalizePayment(payment, card, request)
        } catch (e: PortOneException) {
            handlePortOneException(e, payment, request)
        } catch (e: PaymentException) {
            handlePaymentFailure(e, payment, request)
        } catch (e: Exception) {
            handleUnexpectedFailure(e, payment, request)
        }
    }

    private fun validAndGetCard(payment: PaymentEntity): CardEntity {
        return cardRepository.findById(payment.cardId)
            .orElseThrow { CardNotFoundException(payment.customerKey) }
    }

    private fun executeAndFinalizePayment(
        payment: PaymentEntity,
        card: CardEntity,
        request: PaymentExecutionRequest
    ): ApiResponse<PaymentExecutionResponse> {
        savePaymentStatusWithHistory(payment, PaymentStatus.IN_PROGRESS)

        val portOneResponse = portOneService.issuePayment(payment, card, request)
        val finalStatus = determineFinalStatus(portOneResponse)
        savePaymentStatusWithHistory(payment, finalStatus)

        return ApiResponse.success(
            PaymentExecutionResponse(
                transactionKey = request.transactionKey,
                paymentStatus = finalStatus
            )
        )
    }

    private fun handlePortOneException(
        e: PortOneException,
        payment: PaymentEntity,
        request: PaymentExecutionRequest
    ): ApiResponse<PaymentExecutionResponse> {
        savePaymentStatusWithHistory(payment, PaymentStatus.ABORTED)

        sendPaymentFailureNotification(request, payment, e)

        return ApiResponse.fail(
            message = "PortOneException occurred",
            errorCode = e.errorCode,
            errorMessage = e.message
        )
    }

    // 결제 실패 시
    private fun handlePaymentFailure(
        e: PaymentException,
        payment: PaymentEntity,
        request: PaymentExecutionRequest
    ): ApiResponse<PaymentExecutionResponse> {
        savePaymentStatusWithHistory(payment, PaymentStatus.ABORTED)
        sendPaymentFailureNotification(request, payment, e)

        return ApiResponse.fail(
            message = "Payment Failed",
            errorCode = "PAYMENT_FAILED",
            errorMessage = e.message
        )
    }

    private fun handleUnexpectedFailure(
        e: Exception,
        payment: PaymentEntity,
        request: PaymentExecutionRequest
    ): ApiResponse<PaymentExecutionResponse> {
        savePaymentStatusWithHistory(payment, PaymentStatus.UNKNOWN)
        logger.error(
            "Unexpected failure: transactionKey=${payment.transactionKey} customerKey=${request.customerKey} errorCode=${PaymentStatus.UNKNOWN}, message=${e.message}",
            e
        )
        sendPaymentFailureNotification(request, payment, e)

        return ApiResponse.fail(
            message = "Unexpected Failure",
            errorCode = "UNEXPECTED_FAILURE",
            errorMessage = e.message
        )
    }

    private fun determineFinalStatus(portOneResponse: PortOneResponse): PaymentStatus {
        return when (portOneResponse.code) {
            0 -> PaymentStatus.DONE
            1 -> PaymentStatus.REJECTED
            2 -> PaymentStatus.ABORTED
            else -> PaymentStatus.ABORTED
        }
    }

    private fun validateAndGetPayment(transactionKey: String): PaymentEntity {
        val payment = paymentRepository.findByTransactionKey(transactionKey)

        if (payment == null) {
            logger.warn("Transaction not found: $transactionKey")
            throw TransactionKeyNotFoundException(transactionKey)
        }

        if (payment.paymentStatus != PaymentStatus.READY) {
            logger.warn("Payment is already processed: $transactionKey")
            throw PaymentFailedException("Payment is already processed")
        }

        if (payment.createdAt.plusMinutes(30).isBefore(LocalDateTime.now())) {
            logger.warn("The transaction is expired: $transactionKey")
            throw ExpiredTransactionException(transactionKey)
        }

        return payment
    }

    private fun savePaymentStatusWithHistory(payment: PaymentEntity, status: PaymentStatus) {
        // 상태 업데이트
        payment.updateStatus(status)

        // 히스토리 저장
        paymentHistoryRepository.save(
            PaymentHistoryEntity(
                paymentId = payment.id ?: 0,
                transactionKey = payment.transactionKey,
                paymentStatus = status
            )
        )
    }

    // 오류 발생 시 알림을 줄 훅 정보가 있다면 해당 URL을 호출한다.
    private fun sendPaymentFailureNotification(request: PaymentExecutionRequest, payment: PaymentEntity, e: Exception) {
        request.webhookUrl?.let {
            webhookService.callPaymentFailedWebhook(
                webhookUrl = it,
                transactionKey = request.transactionKey,
                cardKey = payment.customerKey,
                reason = e.message ?: "",
            )
        }
    }
}
