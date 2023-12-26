package com.myfintech.payments.repository

import com.myfintech.payments.entity.Transaction
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock

interface TransactionRepository : JpaRepository<Transaction, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun save(transaction: Transaction): Transaction
}