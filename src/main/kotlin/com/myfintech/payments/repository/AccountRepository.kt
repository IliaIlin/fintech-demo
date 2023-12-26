package com.myfintech.payments.repository

import com.myfintech.payments.entity.Account
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface AccountRepository : JpaRepository<Account, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findByAccountId(accountId: Long): Account?

//    @Modifying
//    @Query("UPDATE Account a SET a.balance = :balance WHERE a.accountId = :accountId")
//    fun updateBalance(@Param("accountId") accountId: Long, @Param("balance") balance: BigDecimal)

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun save(account: Account): Account
}
