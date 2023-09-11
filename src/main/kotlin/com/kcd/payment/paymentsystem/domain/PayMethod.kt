package com.kcd.payment.paymentsystem.domain

/***
    결제 수단

    APPLY NOW: 카드결제
    TOBE: 간편결제(네이버페이, 카카오페이, 토스페이 등), 가상계좌, 휴대폰결제, 예치금
 ***/
enum class PayMethod(s: String) {
    CARD("카드")
}
