package com.kcd.payment.paymentsystem.domain

/**
 * 카드번호 패턴 https://en.wikipedia.org/wiki/Payment_card_number
 */
enum class CardIssuer(val cardKorName: String, val prefix: String, val format: String, val separator: Char) {
    VISA("비자카드", "4","#### #### #### ####", ' '),
    MASTERCARD("마스터카드", "5", "#### #### #### ####", ' '),
    AMERICAN_EXPRESS("아메리칸 익스프레스", "34,37", "#### ###### #####", '-'),
    UNKNOWN("알 수 없는 카드", "", "", ' ');

    companion object {
        fun identify(cardNumber: String): CardIssuer {
            val filteredNumber = cardNumber.filter { it.isDigit() }
            return values().firstOrNull { issuer ->
                issuer.prefix.split(",").any { prefix ->
                    filteredNumber.startsWith(prefix)
                }
            } ?: UNKNOWN
        }

        fun identifyFormattedCard(cardNumber: String): FormattedCard {
            val issuer = identify(cardNumber)
            return FormattedCard(issuer, cardNumber)
        }
    }
}

data class FormattedCard(val issuer: CardIssuer, val cardNumber: String) {
    fun formatCardNumber(): String {
        val formattedNumber = StringBuilder()
        var index = 0
        for (char in issuer.format) {
            if (char == '#' && index < cardNumber.length) {
                formattedNumber.append(cardNumber[index])
                index++
            } else {
                formattedNumber.append(issuer.separator)
            }
        }
        return formattedNumber.toString()
    }
}
