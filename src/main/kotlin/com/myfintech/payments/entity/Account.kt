package com.myfintech.payments.entity

import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "Account")
class Account() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "accountId")
    var accountId: Long? = null

    @Column(name = "balance", nullable = false, scale = 4, precision = 12)
    var balance: BigDecimal? = null

    constructor(balance: BigDecimal) : this() {
        this.balance = balance
    }
}