package com.myfintech.payments.service

import com.myfintech.payments.dto.AccountCreationRequest
import com.myfintech.payments.dto.AccountDetailsResponse
import com.myfintech.payments.dto.TransactionDetailsResponse
import com.myfintech.payments.entity.Account
import com.myfintech.payments.entity.Transaction
import com.myfintech.payments.exception.AccountNotFoundException
import com.myfintech.payments.exception.InsufficientFundsException
import com.myfintech.payments.repository.AccountRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
class AccountService(
    private val accountRepository: AccountRepository
) {

    @Transactional
    fun getAccountById(accountId: Long): Account {
        return accountRepository.findByAccountId(accountId) ?: throw AccountNotFoundException()
    }

    @Transactional
    fun updateAccount(updatedAccount: Account) {
        updatedAccount.balance?.let {
            if (it.signum() < 0) {
                throw InsufficientFundsException()
            }
            accountRepository.save(updatedAccount)
        } ?: IllegalStateException("Account balance shouldn't be empty")
    }

    @Transactional
    fun createAccount(accountCreationRequest: AccountCreationRequest): Account {
        return accountRepository.save(Account(accountCreationRequest.balance))
    }
}
