package com.myfintech.payments.controller

import com.myfintech.payments.dto.AccountCreationRequest
import com.myfintech.payments.dto.AccountDetailsResponse
import com.myfintech.payments.entity.Account
import com.myfintech.payments.service.AccountService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/accounts")
class AccountController(
    private val accountService: AccountService
) {

    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun createAccount(@RequestBody request: AccountCreationRequest): ResponseEntity<AccountDetailsResponse> {
        val accountCreated = accountService.createAccount(request)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(convertToDto(accountCreated))
    }

    @GetMapping("/{accountId}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getAccountById(@PathVariable accountId: Long): ResponseEntity<AccountDetailsResponse> {
        val accountRetrieved = accountService.getAccountById(accountId)
        return ResponseEntity.status(HttpStatus.OK)
            .body(convertToDto(accountRetrieved))
    }

    private fun convertToDto(account: Account): AccountDetailsResponse {
        val accountId = account.accountId ?: throw IllegalStateException("Account id shouldn't be empty")
        val balance = account.balance ?: throw IllegalStateException("Account balance shouldn't be empty")
        return AccountDetailsResponse(accountId, balance)
    }
}