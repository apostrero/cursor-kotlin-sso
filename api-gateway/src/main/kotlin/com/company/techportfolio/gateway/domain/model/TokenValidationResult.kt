package com.company.techportfolio.gateway.domain.model

import java.time.LocalDateTime

data class TokenValidationResult(
    val isValid: Boolean,
    val username: String? = null,
    val authorities: List<String> = emptyList(),
    val sessionIndex: String? = null,
    val issuedAt: LocalDateTime? = null,
    val expiresAt: LocalDateTime? = null,
    val isExpired: Boolean = false,
    val errorMessage: String? = null
) {
    companion object {
        fun valid(
            username: String,
            authorities: List<String>,
            sessionIndex: String?,
            issuedAt: LocalDateTime,
            expiresAt: LocalDateTime
        ): TokenValidationResult = TokenValidationResult(
            isValid = true,
            username = username,
            authorities = authorities,
            sessionIndex = sessionIndex,
            issuedAt = issuedAt,
            expiresAt = expiresAt,
            isExpired = LocalDateTime.now().isAfter(expiresAt)
        )

        fun invalid(errorMessage: String): TokenValidationResult = TokenValidationResult(
            isValid = false,
            errorMessage = errorMessage
        )

        fun expired(username: String, authorities: List<String>, sessionIndex: String?): TokenValidationResult = TokenValidationResult(
            isValid = false,
            username = username,
            authorities = authorities,
            sessionIndex = sessionIndex,
            isExpired = true,
            errorMessage = "Token has expired"
        )
    }
} 