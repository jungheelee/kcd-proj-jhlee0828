package com.kcd.payment.paymentsystem.service

import com.kcd.payment.paymentsystem.ApiResponse
import com.kcd.payment.paymentsystem.domain.CardEntity
import com.kcd.payment.paymentsystem.exception.IssueBillingKeyFailedException
import com.kcd.payment.paymentsystem.present.CardInfo
import com.kcd.payment.paymentsystem.present.CardInfoList
import com.kcd.payment.paymentsystem.present.CardRegistrationRequest
import com.kcd.payment.paymentsystem.repository.CardRepository
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class CardManageService (
    private val cardRepository: CardRepository,
    private val portOneService: PortOneService
) {
    private val logger = LoggerFactory.getLogger(CardManageService::class.java)

    @Transactional
    fun registerCardAndIssueBillingKey(req: CardRegistrationRequest): ApiResponse<CardInfo> {
        try {
            // 휴대폰 번호 + 카드 번호 조합으로 이미 있는지 확인
            val sanitizedPhoneNumber = req.phoneNumber.filter { it.isDigit() }
            val sanitizedCardNumber = req.cardNumber.filter { it.isDigit() }

            val existingCard = cardRepository.findByPhoneNumberAndCardNumber(
                sanitizedPhoneNumber,
                sanitizedCardNumber
            )

            existingCard?.let {
                throw IllegalArgumentException("Combination of phone number and card number already exists")
            }

            val card = cardRepository.save(
                CardEntity(
                    cardNumber = req.cardNumber.filter { it.isDigit() },
                    cvv = req.cvv,
                    expirationDate = req.expirationDate,
                    password2Digit = req.password2Digit,
                    birthOrBizNo = req.birthOrBizNo,
                    cardHolderName = req.cardHolderName,
                    isAutoPayEnabled = req.isAutoPayEnabled,
                    phoneNumber = req.phoneNumber.filter { it.isDigit() }
                )
            )

            portOneService.issueBillingKey(card)

            return ApiResponse.success(
                CardInfo(
                    cardId = card.id!!,
                    cardNumber = card.getMaskedCardNumber(),
                    cvv = card.getMaskedCvv(),
                    customerKey = card.customerKey,
                    cardIssuer = card.cardIssuer.cardKorName,
                    expirationDate = card.getMaskedExpirationDate(),
                    cardHolderName = card.getMaskedCardHolderName(),
                    isAutoPayEnabled = card.isAutoPayEnabled,
                    password2Digit = card.getMaskedPassword2Digit(),
                    birthOrBizNo = card.getMaskedBirthOrBizNo(),
                    phoneNumber = card.getMaskedPhoneNumber()
                )
            )
        } catch (e: IssueBillingKeyFailedException) {
            throw e
        } catch (e: Exception) {
            logger.error("Failed to register card", e)
            throw e
        }
    }

    @Transactional
    fun getCards(phoneNumber: String): ApiResponse<CardInfoList> {
        val findAllByPhoneNumber = cardRepository.findAllByPhoneNumber(phoneNumber.filter { it.isDigit() })

        val cardInfos = findAllByPhoneNumber.map { cardEntity ->
            CardInfo(
                cardId = cardEntity.id!!,
                cardNumber = cardEntity.getMaskedCardNumber(),
                cvv = cardEntity.getMaskedCvv(),
                customerKey = cardEntity.customerKey,
                cardIssuer = cardEntity.cardIssuer.cardKorName,
                expirationDate = cardEntity.getMaskedExpirationDate(),
                cardHolderName = cardEntity.getMaskedCardHolderName(),
                isAutoPayEnabled = cardEntity.isAutoPayEnabled,
                password2Digit = cardEntity.getMaskedPassword2Digit(),
                birthOrBizNo = cardEntity.getMaskedBirthOrBizNo(),
                phoneNumber = cardEntity.getMaskedPhoneNumber()
            )
        }

        return ApiResponse.success(CardInfoList(cardInfos))
    }
}
