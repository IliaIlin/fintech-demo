package com.myfintech.payments.controller

import com.myfintech.payments.dto.ErrorResponse
import com.myfintech.payments.exception.AccountNotFoundException
import com.myfintech.payments.exception.InsufficientFundsException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(Exception::class)
    fun handleException(ex: Exception): ResponseEntity<ErrorResponse> {
        log.error(ex.message, ex)
        return when (ex) {
            is AccountNotFoundException -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse(ACCOUNT_NOT_FOUND_ERROR_MESSAGE))

            is HttpMessageNotReadableException -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse(BAD_REQUEST_ERROR_MESSAGE))

            is InsufficientFundsException -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse(INSUFFICIENT_FUNDS_ERROR_MESSAGE))

            else -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse(INTERNAL_SERVER_ERROR_MESSAGE))
        }
    }

    companion object {
        val log: Logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

        const val ACCOUNT_NOT_FOUND_ERROR_MESSAGE = "Account not found"
        const val BAD_REQUEST_ERROR_MESSAGE = "Bad request, please check correctness of the input and try again"
        const val INSUFFICIENT_FUNDS_ERROR_MESSAGE = "Insufficient funds, transaction can't be completed"
        const val INTERNAL_SERVER_ERROR_MESSAGE = "Internal server error"
    }
}