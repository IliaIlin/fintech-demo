package com.myfintech.payments.controller

import com.myfintech.payments.dto.TransactionCreationRequest
import com.myfintech.payments.dto.TransactionDetailsResponse
import com.myfintech.payments.entity.Transaction
import com.myfintech.payments.service.TransactionService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/transactions")
class TransactionController(
    private val transactionService: TransactionService
) {

    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun createTransaction(@RequestBody request: TransactionCreationRequest): ResponseEntity<TransactionDetailsResponse> {
        val transactionCreated = transactionService.createTransactionAndPerformTransfer(request)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(convertToDto(transactionCreated))
    }

    private fun convertToDto(transaction: Transaction): TransactionDetailsResponse {
        val transactionId = transaction.txId ?: throw IllegalStateException("Transaction id shouldn't be empty")
        val amount = transaction.amount ?: throw IllegalStateException("Amount shouldn't be empty")
        val fromAccountId =
            transaction.fromAccount?.accountId ?: throw IllegalStateException("From account shouldn't be empty")
        val toAccountId =
            transaction.toAccount?.accountId ?: throw IllegalStateException("To account shouldn't be empty")
        return TransactionDetailsResponse(transactionId, amount, fromAccountId, toAccountId)
    }
}