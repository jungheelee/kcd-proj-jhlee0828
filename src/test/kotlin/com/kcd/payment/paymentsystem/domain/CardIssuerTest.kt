package com.kcd.payment.paymentsystem.domain

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

class CardIssuerTest {

    @Test
    fun `카드번호 입력값에 따라 적합한 카드 타입을 찾는지 확인`() {
        val visaCard = "4111111111111111"
        val masterCard = "5111111111111118"
        val amexCard = "341111111111111"

        assertEquals(CardIssuer.VISA, CardIssuer.identify(visaCard))
        assertEquals(CardIssuer.MASTERCARD, CardIssuer.identify(masterCard))
        assertEquals(CardIssuer.AMERICAN_EXPRESS, CardIssuer.identify(amexCard))
    }

    @Test
    fun `카드발급사가 정의한 패턴에 따라 적절한 포멧팅이 되는지 확인`() {
        val cardNumber = "4123456789012345"
        val formattedCard = CardIssuer.identifyFormattedCard(cardNumber)
        val formattedNumber = formattedCard.formatCardNumber()
        assertEquals("4123 4567 8901 2345", formattedNumber)
    }
}