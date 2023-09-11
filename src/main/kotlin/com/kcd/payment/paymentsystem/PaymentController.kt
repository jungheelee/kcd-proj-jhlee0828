package com.kcd.payment.paymentsystem

import com.kcd.payment.paymentsystem.present.*
import com.kcd.payment.paymentsystem.service.CardManageService
import com.kcd.payment.paymentsystem.service.PaymentService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/payment")
class PaymentController(
    private val paymentService: PaymentService,
    private val cardManageService: CardManageService
) {
    @PostMapping("/card/register")
    fun 카드등록(@RequestBody req: CardRegistrationRequest): ApiResponse<CardInfo> {
        return cardManageService.registerCardAndIssueBillingKey(req)
    }

    @GetMapping("/card")
    fun 카드조회(@RequestParam phoneNumber: String): ApiResponse<CardInfoList> {
        return cardManageService.getCards(phoneNumber)
    }

    @GetMapping("/card/me")
    fun 사용가능한_카드목록(@RequestParam phoneNumber: String): ApiResponse<CardInfoList> {
        return cardManageService.getCards(phoneNumber)
    }

    @PostMapping("/start")
    fun 결제시작(@RequestBody req: PaymentInitRequest): ApiResponse<PaymentInfo> {
        return paymentService.initiatePayment(req);
    }

    @PostMapping("/execute")
    fun 결제실행(@RequestBody req: PaymentExecutionRequest): ApiResponse<PaymentExecutionResponse> {
        return paymentService.executePayment(req)
    }

    @PostMapping("/regular/execute")
    fun 정기결제() {
        TODO()
    }

    @PostMapping("/regular/cancel")
    fun 정기결제_취소() {
        TODO()
    }

}
