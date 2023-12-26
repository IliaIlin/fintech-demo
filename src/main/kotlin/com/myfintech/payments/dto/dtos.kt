package com.myfintech.payments.dto

import java.math.BigDecimal

data class AccountCreationRequest(
    val balance: BigDecimal
)

data class AccountDetailsResponse(
    val accountId: Long,
    val balance: BigDecimal
)

data class TransactionCreationRequest(
    val amount: BigDecimal,
    val fromAccountId: Long,
    val toAccountId: Long
)

data class TransactionDetailsResponse(
    val transactionId: Long,
    val amount: BigDecimal,
    val fromAccountId: Long,
    val toAccountId: Long
)

data class ErrorResponse(
    val errorMessage: String
)

