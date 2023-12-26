package com.myfintech.payments.entity

import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "Transaction")
class Transaction() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "txId")
    var txId: Long? = null

    @Column(name = "amount", nullable = false, scale = 4, precision = 12)
    var amount: BigDecimal? = null

    @ManyToOne
    @JoinColumn(name = "fromAccountId")
    var fromAccount: Account? = null

    @ManyToOne
    @JoinColumn(name = "toAccountId")
    var toAccount: Account? = null

    constructor(amount: BigDecimal, fromAccount: Account, toAccount: Account) : this() {
        this.amount = amount
        this.fromAccount = fromAccount
        this.toAccount = toAccount
    }
}

