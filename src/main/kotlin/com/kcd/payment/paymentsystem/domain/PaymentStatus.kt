package com.kcd.payment.paymentsystem.domain

enum class PaymentStatus (s: String) {
    READY("결제 생성"),
    IN_PROGRESS("결제 진행 중"),
    DONE("승인 완료"),
    CANCELED("승인된 결제 취소"),
    REJECTED("승인 거절"),
    ABORTED("승인 실패"),
    EXPIRED("요청 만료"),
    UNKNOWN("알 수 없음")
}
