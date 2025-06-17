package com.company.techportfolio.gateway.domain.model

import java.time.LocalDateTime

data class AuthenticationResult(
    val isAuthenticated: Boolean,
    val username: String? = null,
    val authorities: List<String> = emptyList(),
    val token: String? = null,
    val sessionIndex: String? = null,
    val expiresAt: LocalDateTime? = null,
    val errorMessage: String? = null
) {
    companion object {
        fun success(
            username: String,
            authorities: List<String>,
            token: String,
            sessionIndex: String?,
            expiresAt: LocalDateTime
        ): AuthenticationResult = AuthenticationResult(
            isAuthenticated = true,
            username = username,
            authorities = authorities,
            token = token,
            sessionIndex = sessionIndex,
            expiresAt = expiresAt
        )

        fun failure(errorMessage: String): AuthenticationResult = AuthenticationResult(
            isAuthenticated = false,
            errorMessage = errorMessage
        )

        fun notAuthenticated(): AuthenticationResult = AuthenticationResult(
            isAuthenticated = false
        )
    }
} 