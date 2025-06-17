package com.company.techportfolio.gateway.domain.port

import java.time.LocalDateTime

interface AuditPort {
    fun logAuthenticationEvent(event: AuthenticationEvent)
    fun logAuthorizationEvent(event: AuthorizationEvent)
    fun logTokenEvent(event: TokenEvent)
}

data class AuthenticationEvent(
    val username: String,
    val eventType: AuthenticationEventType,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val sessionIndex: String? = null,
    val ipAddress: String? = null,
    val userAgent: String? = null,
    val success: Boolean,
    val errorMessage: String? = null
)

enum class AuthenticationEventType {
    LOGIN_SUCCESS, LOGIN_FAILURE, LOGOUT, TOKEN_REFRESH, TOKEN_EXPIRED
}

data class AuthorizationEvent(
    val username: String,
    val resource: String,
    val action: String,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val authorized: Boolean,
    val permissions: List<String> = emptyList(),
    val errorMessage: String? = null
)

data class TokenEvent(
    val username: String,
    val eventType: TokenEventType,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val tokenId: String? = null,
    val sessionIndex: String? = null
)

enum class TokenEventType {
    TOKEN_GENERATED, TOKEN_VALIDATED, TOKEN_REFRESHED, TOKEN_EXPIRED, TOKEN_INVALID
} 