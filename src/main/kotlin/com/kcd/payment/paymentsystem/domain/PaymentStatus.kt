package com.kcd.payment.paymentsystem.domain

enum class PaymentStatus (s: String) {
    READY("결제 생성"),
    IN_PROGRESS("결제 진행 중"),
    DONE("승인 완료"),
    CANCELED("승인된 결제 취소"),
    REJECTED("승인 거절"),
    ABORTED("승인 실패"),
//    REFUNDED("환불"),
    EXPIRED("요청 만료") // 요청된 결제의 유효 시간 30분이 지나 거래가 취소된 상태입니다. IN_PROGRESS 상태에서 결제 승인 API를 호출하지 않으면 EXPIRED가 됩니다.
}
