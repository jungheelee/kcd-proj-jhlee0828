package com.kcd.payment.paymentsystem.service

/***
 * https://developers.portone.io/docs/ko/api/non-authenticated-payment-api/again-api
 *
 * code * integer 응답코드 0이면 정상적인 조회, 0 이 아닌 값이면 message를 확인해봐야 합니다
 * message * string 응답메세지 code 값이 0이 아닐 때, ‘존재하지 않는 결제정보입니다’와 같은 오류 메세지를 포함합니다
 * response (PaymentAnnotation, optional) 응답객체입니다
 *
 * */
data class PortOneResponse(val code: Int, val message: String, val response: PaymentAnnotation) {

    fun isSuccess(): Boolean {
        return code == 0
    }
}

data class PaymentAnnotation (
    val amount: Int,
    val apply_num: String,
    val bank_code: String,
    val bank_name: String,
    val buyer_addr: String,
    val buyer_email: String,
    val buyer_name: String,
    val buyer_postcode: String,
    val buyer_tel: String,
    val cancel_amount: Int,
    val cancel_history: List<CancelHistory>,
    val cancel_reason: String,
    val cancel_receipt_urls: List<String>,
    val cancelled_at: Int,
    val card_code: String,
    val card_name: String,
    val card_number: String,
    val card_quota: Int,
    val card_type: String,
    val cash_receipt_issued: Boolean,
    val channel: String,
    val currency: String,
    val custom_data: String,
    val customer_uid: String,
    val customer_uid_usage: String,
    val emb_pg_provider: String,
    val escrow: Boolean,
    val fail_reason: String,
    val failed_at: Int,
    val imp_uid: String,
    val merchant_uid: String,
    val name: String,
    val paid_at: Int,
    val pay_method: String,
    val pg_id: String,
    val pg_provider: String,
    val pg_tid: String,
    val receipt_url: String,
    val started_at: Int,
    val status: String,
    val user_agent: String,
    val vbank_code: String,
    val vbank_date: Int,
    val vbank_holder: String,
    val vbank_issued_at: Int,
    val vbank_name: String,
    val vbank_num: String
)

data class CancelHistory(
    val amount: Int,
    val cancelled_at: Int,
    val pg_tid: String,
    val reason: String,
    val receipt_url: String
)

data class PortOneRequest(
    val customer_uid: String, // 빌링키
    val merchant_uid: String, // 가맹점 주문번호
    val amount: Int,          // 결제금액
    val name: String,         // 상품명
//    val card_number: String,
//    val secret_key: String,
//    val transaction_key: String
)