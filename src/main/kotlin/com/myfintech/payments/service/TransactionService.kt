package com.myfintech.payments.service

import com.myfintech.payments.dto.TransactionCreationRequest
import com.myfintech.payments.dto.TransactionDetailsResponse
import com.myfintech.payments.entity.Transaction
import com.myfintech.payments.repository.TransactionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
class TransactionService(
    private val transactionRepository: TransactionRepository,
    private val accountService: AccountService
) {

    @Transactional
    fun createTransactionAndPerformTransfer(transactionRequest: TransactionCreationRequest): Transaction {
        val fromAccount = accountService.getAccountById(transactionRequest.fromAccountId)
        val toAccount = accountService.getAccountById(transactionRequest.toAccountId)

        fromAccount.balance = fromAccount.balance?.minus(transactionRequest.amount)
        toAccount.balance = toAccount.balance?.plus(transactionRequest.amount)
        accountService.updateAccount(fromAccount)
        accountService.updateAccount(toAccount)

        return transactionRepository.save(Transaction(transactionRequest.amount, fromAccount, toAccount))
    }
}
