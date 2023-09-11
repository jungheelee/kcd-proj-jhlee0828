package com.kcd.payment.paymentsystem.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

@Service
class WebhookService {
    private val webClient: WebClient = WebClient.builder().build();

    private val logger = LoggerFactory.getLogger(WebhookService::class.java)

    fun callPaymentFailedWebhook(transactionKey: String, cardKey: String, reason: String, webhookUrl: String) {
        val payload = mapOf(
            "event" to "payment_failed",
            "data" to mapOf(
                "transactionId" to transactionKey,
                "cardKey" to cardKey,
                "reason" to reason,
                "timestamp" to java.time.Instant.now().toString()
            )
        )

        webClient.post()
            .uri(webhookUrl)
            .bodyValue(payload)
            .retrieve()
            .bodyToMono(Void::class.java)
            .retry(1)  // 재시도 1회
            .doOnError { throwable ->  // 웹훅이 실패하면 로깅
                logger.error("Failed to call webhook after retries. url: $webhookUrl, payload: $payload", throwable)
            }
            .subscribe()
    }
}