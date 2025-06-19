package com.company.techportfolio.gateway.domain.model

import java.time.LocalDateTime

/**
 * Represents the result of an authentication attempt in the API Gateway.
 * 
 * This data class encapsulates all information related to user authentication,
 * including success/failure status, user details, security tokens, and session information.
 * It serves as the primary contract between authentication adapters and the domain service.
 * 
 * @property isAuthenticated Indicates whether the authentication attempt was successful
 * @property username The authenticated user's username, null if authentication failed
 * @property authorities List of granted authorities/roles for the authenticated user
 * @property token The generated JWT token for authenticated sessions, null if authentication failed
 * @property sessionIndex Unique session identifier for SAML SSO sessions, may be null for other auth methods
 * @property expiresAt Timestamp when the authentication token expires, null if authentication failed
 * @property errorMessage Descriptive error message when authentication fails, null on success
 * 
 * @author Technology Portfolio Team
 * @since 1.0.0
 */
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
        /**
         * Creates a successful authentication result with all required user information.
         * 
         * @param username The authenticated user's username
         * @param authorities List of user authorities/roles
         * @param token Generated JWT token for the session
         * @param sessionIndex Unique session identifier, may be null
         * @param expiresAt Token expiration timestamp
         * @return AuthenticationResult indicating successful authentication
         */
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

        /**
         * Creates a failed authentication result with an error message.
         * 
         * @param errorMessage Descriptive message explaining the authentication failure
         * @return AuthenticationResult indicating failed authentication
         */
        fun failure(errorMessage: String): AuthenticationResult = AuthenticationResult(
            isAuthenticated = false,
            errorMessage = errorMessage
        )

        /**
         * Creates an authentication result for a user who is not authenticated.
         * 
         * This is typically used when no authentication credentials are provided
         * or when the user session has expired.
         * 
         * @return AuthenticationResult indicating no authentication
         */
        fun notAuthenticated(): AuthenticationResult = AuthenticationResult(
            isAuthenticated = false
        )
    }
} 