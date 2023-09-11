package com.kcd.payment.paymentsystem.service

import com.kcd.payment.paymentsystem.ApiResponse
import com.kcd.payment.paymentsystem.present.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.postForEntity
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.*
import org.springframework.web.util.UriComponentsBuilder
import java.math.BigDecimal

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CardRegistrationE2ETest(@Autowired val restTemplate: TestRestTemplate) {

    @Test
    fun `같은 카드번호 + 전화번호 조합으로 여러번 카드 등록은 안 됨`() {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        // 첫 번째 카드 등록
        val request1 = CardRegistrationRequest(
            cardNumber = "1234-567812-345678",
            cvv = "123",
            expirationDate = "2025-12",
            password2Digit = "12",
            birthOrBizNo = "860101",
            cardHolderName = "John Doe",
            isAutoPayEnabled = false,
            phoneNumber = "010-1234-5678"
        )

        val response1 = restTemplate.postForEntity<ApiResponse<String>>(
            "/v1/payment/card/register", request1, ApiResponse::class.java
        )
        assertEquals(HttpStatus.OK, response1.statusCode)

        // 동일한 정보로 두 번째 카드 등록
        val request2 = CardRegistrationRequest(
            cardNumber = "1234-567812-345678",
            cvv = "123",
            expirationDate = "2025-12",
            password2Digit = "12",
            birthOrBizNo = "860101",
            cardHolderName = "may lee",
            isAutoPayEnabled = false,
            phoneNumber = "01012345678"
        )

        val response2 = restTemplate.postForEntity<ApiResponse<String>>(
            "/v1/payment/card/register", request2, ApiResponse::class.java
        )

        // 두 번째 호출에서는 실패해야 함
        assertEquals(HttpStatus.BAD_REQUEST, response2.statusCode)
    }

    @Test
    fun `여러개의 카드를 등록해두고 하나 골라서 사용할 수 있음`() {
        // 1. 같은 연락처, 다른 카드 번호로 카드 2개 등록
        val card1 = createCard("1234-567812-345678")
        val card2 = createCard("4234-567812-345678")

        assertEquals(HttpStatus.OK, card1.statusCode)
        assertEquals(HttpStatus.OK, card2.statusCode)
        assertEquals("SUCCESS", card1.body?.status)
        assertEquals("SUCCESS", card2.body?.status)

        // 2. 사용 가능한 카드 목록 조회
        val phoneNumber = "010-0000-5678"
        val uri = UriComponentsBuilder.fromUriString("/v1/payment/card/me")
            .queryParam("phoneNumber", phoneNumber)
            .build()
            .toUri()

        val cardListResponse = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            null,
            object : ParameterizedTypeReference<ApiResponse<CardInfoList>>() {}
        )

        assertEquals(HttpStatus.OK, cardListResponse.statusCode)
        assertEquals("SUCCESS", cardListResponse.body?.status)

        val cardInfoList: CardInfoList? = cardListResponse.body?.data
        val cardList: List<CardInfo>? = cardInfoList?.cardInfos

        assertNotNull(cardList)
        assertEquals(2, cardList!!.size)
    }

    @Test
    fun `카드 등록에서 결제까지 해피케이스 테스트`() {
        // 1. 카드 등록
        val cardRegistrationRequest = CardRegistrationRequest(
            cardNumber = "4134-567812-345678",
            cvv = "123",
            expirationDate = "2025-12",
            password2Digit = "12",
            birthOrBizNo = "860101",
            cardHolderName = "may lee",
            isAutoPayEnabled = false,
            phoneNumber = "010-0000-5678"
        )

        val cardResponse = restTemplate.exchange(
            "/v1/payment/card/register",
            HttpMethod.POST,
            HttpEntity(cardRegistrationRequest, HttpHeaders().apply {
                set("Content-Type", "application/json")
            }),
            object : ParameterizedTypeReference<ApiResponse<CardInfo>>() {}
        )

        assertEquals(HttpStatus.OK, cardResponse.statusCode)
        assertEquals("SUCCESS", cardResponse.body?.status)

        val cardInfo = cardResponse.body?.data!!
        assertNotNull(cardInfo.customerKey)
//        println("customerKey: ${cardInfo.customerKey}")

        // 2. 사용 가능한 카드 목록 조회
        val phoneNumber = "010-0000-5678"

        val uri = UriComponentsBuilder.fromUriString("/v1/payment/card/me")
            .queryParam("phoneNumber", phoneNumber)
            .build()
            .toUri()

        val cardListResponse = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            null,
            object : ParameterizedTypeReference<ApiResponse<CardInfoList>>() {}
        )

        assertEquals(HttpStatus.OK, cardListResponse.statusCode)
        assertEquals("SUCCESS", cardListResponse.body?.status)


        // 3. 등록한 카드의 빌링키를 통해 결제 시작
        val paymentInitRequest = PaymentInitRequest(
            customerKey = cardInfo.customerKey,
            amount = BigDecimal(1000)
        )

        val paymentInitResponse = restTemplate.exchange(
            "/v1/payment/start",
            HttpMethod.POST,
            HttpEntity(paymentInitRequest, HttpHeaders().apply {
                set("Content-Type", "application/json")
            }),
            object : ParameterizedTypeReference<ApiResponse<Map<String, Any>>>() {}
        )

        println("paymentInitResponse: ${paymentInitResponse.body}")

        assertEquals(HttpStatus.OK, paymentInitResponse.statusCode)
        assertEquals("SUCCESS", paymentInitResponse.body?.status)

        val paymentInfoMap = paymentInitResponse.body?.data!!

        val paymentInfo = PaymentInfo(
            transactionKey = paymentInfoMap["transactionKey"] as String,
            // 여기에 나머지 필드들을 맵핑하세요.
        )

        assertNotNull(paymentInfo.transactionKey)


        // 4. 결제 실행
        val paymentExecutionRequest = PaymentExecutionRequest(
            transactionKey = paymentInfo.transactionKey
        )

        val paymentExecutionResponse = restTemplate.postForEntity<ApiResponse<PaymentExecutionResponse>>(
            "/v1/payment/execute",
            HttpEntity(paymentExecutionRequest, HttpHeaders().apply {
                set("Content-Type", "application/json")
            }),
            object : ParameterizedTypeReference<ApiResponse<PaymentExecutionResponse>>() {}
        )

        assertEquals(HttpStatus.OK, paymentExecutionResponse.statusCode)
        println("paymentExecutionResponse.body:  ${paymentExecutionResponse.body}")
        assertEquals("SUCCESS", paymentExecutionResponse.body?.status)
    }

    private fun createCard(cardNumber: String): ResponseEntity<ApiResponse<String>> {
        val cardRegistrationRequest = CardRegistrationRequest(
            cardNumber = cardNumber,
            cvv = "123",
            expirationDate = "2025-12",
            password2Digit = "12",
            birthOrBizNo = "860101",
            cardHolderName = "may lee",
            isAutoPayEnabled = false,
            phoneNumber = "010-0000-5678"
        )

        return restTemplate.postForEntity(
            "/v1/payment/card/register",
            HttpEntity(cardRegistrationRequest, HttpHeaders().apply {
                set("Content-Type", "application/json")
            }),
            object : ParameterizedTypeReference<ApiResponse<String>>() {}
        )
    }

}
