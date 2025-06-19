package com.company.techportfolio.gateway.domain.model

import java.time.LocalDateTime

/**
 * Represents the result of JWT token validation in the API Gateway.
 * 
 * This data class encapsulates all information related to token validation,
 * including validity status, user details extracted from the token, expiration information,
 * and error details. It serves as the contract between JWT authentication adapters and services.
 * 
 * @property isValid Indicates whether the JWT token is valid and can be trusted
 * @property username The username extracted from the token, null if token is invalid
 * @property authorities List of authorities/roles extracted from the token
 * @property sessionIndex Unique session identifier from the token, may be null
 * @property issuedAt Timestamp when the token was originally issued, null if token is invalid
 * @property expiresAt Timestamp when the token expires, null if token is invalid
 * @property isExpired Indicates whether the token has expired based on current time
 * @property errorMessage Descriptive error message when validation fails, null on success
 * 
 * @author Technology Portfolio Team
 * @since 1.0.0
 */
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
        /**
         * Creates a valid token validation result with extracted user information.
         * 
         * Automatically calculates expiration status based on current time and token expiration.
         * 
         * @param username The username extracted from the token
         * @param authorities List of user authorities/roles from the token
         * @param sessionIndex Unique session identifier, may be null
         * @param issuedAt Timestamp when the token was issued
         * @param expiresAt Token expiration timestamp
         * @return TokenValidationResult indicating valid token with user details
         */
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

        /**
         * Creates an invalid token validation result with an error message.
         * 
         * Used when token parsing fails, signature is invalid, or other validation errors occur.
         * 
         * @param errorMessage Descriptive message explaining the validation failure
         * @return TokenValidationResult indicating invalid token
         */
        fun invalid(errorMessage: String): TokenValidationResult = TokenValidationResult(
            isValid = false,
            errorMessage = errorMessage
        )

        /**
         * Creates a token validation result for an expired token.
         * 
         * Used when the token is structurally valid but has exceeded its expiration time.
         * Includes user information that was extracted before expiration check.
         * 
         * @param username The username extracted from the expired token
         * @param authorities List of user authorities/roles from the expired token
         * @param sessionIndex Unique session identifier, may be null
         * @return TokenValidationResult indicating expired token
         */
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