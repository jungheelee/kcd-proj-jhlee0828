package com.kcd.payment.paymentsystem.service

import com.kcd.payment.paymentsystem.domain.CardEntity
import com.kcd.payment.paymentsystem.domain.PaymentEntity
import com.kcd.payment.paymentsystem.exception.IssueBillingKeyFailedException
import com.kcd.payment.paymentsystem.exception.PaymentFailedException
import com.kcd.payment.paymentsystem.exception.PortOneException
import com.kcd.payment.paymentsystem.present.PaymentExecutionRequest
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class PortOneService(
    private val restTemplate: RestTemplate,
) {
    @Value("\${pgmodule.imp_key}")
    lateinit var IMP_KEY: String

    @Value("\${pgmodule.imp_secret}")
    lateinit var SECRET_KEY: String

    private val logger = LoggerFactory.getLogger(PortOneService::class.java)

    /**
     *  빌링키 발급 요청
     */
    fun issueBillingKey(card: CardEntity): PortOneResponse {
        val accessToken = getToken(card.customerKey)

        val headers = HttpHeaders().apply {
            set("Content-Type", "application/json")
            set("Authorization", "Bearer $accessToken")
        }

        val impPayload = mapOf(
            "customer_uid" to card.customerKey,
            "card_number" to card.cardNumber,
            "expiry" to card.expirationDate,
            "birth" to card.birthOrBizNo,
            "pwd_2digit" to card.password2Digit
        )

        val entity = HttpEntity(impPayload, headers)
        val url = "https://api.iamport.kr/subscription/issue-billing"

        try {
            val issueKeyResponse = restTemplate.exchange(url, HttpMethod.POST, entity, Map::class.java)

            val code = issueKeyResponse.body?.get("code") as Int
            val message = issueKeyResponse.body?.get("message") as? String

            if (code != 0) {
                throw IssueBillingKeyFailedException(card.customerKey, message ?: "")
            }

            return PortOneResponse(
                code = code,
                message = message ?: "",
                response = issueKeyResponse.body?.get("response") as PaymentAnnotation
            )
        } catch (e: Exception) {
            throw IssueBillingKeyFailedException(card.customerKey, e.message ?: "Unknown Error", e)
        }
    }


    /**
     * 저장된 빌링키(customer_uid)로 결제를 요청
     * https://developers.portone.io/docs/ko/api/non-authenticated-payment-api/again-api
     */
    fun issuePayment(payment: PaymentEntity, card: CardEntity, request: PaymentExecutionRequest): PortOneResponse {
        val accessToken = getToken(card.customerKey)
        try {
            val paymentHeaders = HttpHeaders().apply {
                set("Content-Type", "application/json")
                set("Authorization", "Bearer $accessToken")
            }

            val payload = mapOf(
                "customer_uid" to card.customerKey,
                "merchant_uid" to payment.transactionKey,
                "amount" to payment.amount,
                "transaction_key" to payment.transactionKey
            )

            val paymentEntity = HttpEntity(payload, paymentHeaders)

            val paymentResponse = restTemplate.exchange(
                "https://api.iamport.kr/subscribe/payments/again",
                HttpMethod.POST,
                paymentEntity,
                Map::class.java
            )

            val code = paymentResponse.body?.get("code") as Int
            val message = paymentResponse.body?.get("message") as? String
            if (code != 0) {
                throw PortOneException(portOneErrorCode = code, customerKey = card.customerKey, portOneMessage = message ?: "")
            }

            return PortOneResponse(
                code = paymentResponse.body?.get("code") as Int,
                message = message ?: "",
                response = paymentResponse.body?.get("response") as PaymentAnnotation
            )
        } catch (e: PortOneException) {
            throw e
        }  catch (e: Exception) {
            throw PaymentFailedException(e.message ?: "customerKey: ${card.customerKey} | transactionKey: ${payment.transactionKey} | ${e.message}")
        }

    }

    /**
     * 아임포트 API 사용용 토큰 발급
     */
    fun getToken(customerKey: String): String = run {
        val headers = HttpHeaders()
        headers.set("Content-Type", "application/json")

        val impPayload = mapOf(
            "imp_key" to IMP_KEY,
            "imp_secret" to SECRET_KEY
        )
        val entity = HttpEntity(impPayload, headers)

        val signInResponse = restTemplate.exchange(
            "https://api.iamport.kr/users/getToken",
            HttpMethod.POST,
            entity,
            Map::class.java
        )

        val code = signInResponse.body?.get("code") as Int
        val message = signInResponse.body?.get("message") as? String
        val responseMap = signInResponse.body?.get("response") as? Map<String, Any>
        val token = responseMap?.get("access_token") as? String

        if (code != 0) {
            throw PortOneException(portOneErrorCode = code, customerKey = customerKey, portOneMessage = message ?: "")
        }

        return token!!;

    }
}
