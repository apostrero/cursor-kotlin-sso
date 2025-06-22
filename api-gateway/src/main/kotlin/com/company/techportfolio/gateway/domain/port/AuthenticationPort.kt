package com.company.techportfolio.gateway.domain.port

import com.company.techportfolio.gateway.domain.model.AuthenticationResult
import com.company.techportfolio.gateway.domain.model.TokenValidationResult
import org.springframework.security.core.Authentication

/**
 * Port interface for authentication operations in the API Gateway domain.
 *
 * This interface defines the contract for authentication-related operations including
 * user authentication, token validation, token generation, and token management.
 * It follows the hexagonal architecture pattern by defining the domain's requirements
 * for authentication services without depending on specific implementations.
 *
 * Implementations of this interface handle:
 * - JWT token operations (generation, validation, refresh)
 * - User authentication via various methods (SAML, mock, etc.)
 * - Token claim extraction and validation
 *
 * @author Technology Portfolio Team
 * @since 1.0.0
 */
interface AuthenticationPort {

    /**
     * Authenticates a user based on the provided Spring Security Authentication object.
     *
     * This method processes authentication requests from various sources (SAML, form-based, etc.)
     * and returns a comprehensive authentication result including tokens and user details.
     *
     * @param authentication Spring Security Authentication object containing user credentials
     * @return AuthenticationResult containing authentication status, user details, and JWT token
     */
    fun authenticateUser(authentication: Authentication): AuthenticationResult

    /**
     * Validates a JWT token and extracts user information.
     *
     * Performs comprehensive token validation including signature verification,
     * expiration checking, and claim extraction.
     *
     * @param token JWT token string to validate
     * @return TokenValidationResult containing validation status and extracted user information
     */
    fun validateToken(token: String): TokenValidationResult

    /**
     * Refreshes an existing JWT token if it's valid and not expired.
     *
     * Creates a new token with extended expiration time while preserving
     * the original user claims and authorities.
     *
     * @param token Current JWT token to refresh
     * @return New JWT token string, or null if refresh fails
     */
    fun refreshToken(token: String): String?

    /**
     * Generates a new JWT token for the specified user.
     *
     * Creates a signed JWT token containing user information, authorities,
     * and session details with appropriate expiration time.
     *
     * @param username User's username to include in the token
     * @param authorities List of user authorities/roles
     * @param sessionIndex Optional session identifier for SAML sessions
     * @return Generated JWT token string
     */
    fun generateToken(username: String, authorities: List<String>, sessionIndex: String?): String

    /**
     * Extracts the username from a JWT token without full validation.
     *
     * Performs basic token parsing to extract the username claim.
     * This method is typically used for logging or auditing purposes.
     *
     * @param token JWT token string
     * @return Username extracted from token, or null if extraction fails
     */
    fun extractUsernameFromToken(token: String): String?

    /**
     * Extracts the authorities/roles from a JWT token without full validation.
     *
     * Performs basic token parsing to extract the authorities claim.
     * This method is typically used for authorization checks.
     *
     * @param token JWT token string
     * @return List of authorities extracted from token, or null if extraction fails
     */
    fun extractAuthoritiesFromToken(token: String): List<String>?

    /**
     * Checks if a JWT token has expired based on its expiration claim.
     *
     * Compares the token's expiration time with the current system time.
     * This is a lightweight check that doesn't perform full token validation.
     *
     * @param token JWT token string to check
     * @return true if token is expired, false otherwise
     */
    fun isTokenExpired(token: String): Boolean
} 