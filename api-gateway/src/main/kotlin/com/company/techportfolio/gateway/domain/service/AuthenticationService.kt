package com.company.techportfolio.gateway.domain.service

import com.company.techportfolio.gateway.domain.model.AuthenticationResult
import com.company.techportfolio.gateway.domain.model.TokenValidationResult
import com.company.techportfolio.gateway.domain.port.*
import org.springframework.security.core.Authentication
import org.springframework.security.saml2.provider.service.authentication.Saml2Authentication
import org.springframework.stereotype.Service
import java.time.LocalDateTime

/**
 * Domain service for authentication and authorization operations in the API Gateway.
 *
 * This service orchestrates authentication workflows, token management, and authorization
 * decisions while ensuring comprehensive audit logging. It serves as the core business
 * logic layer that coordinates between various ports (authentication, authorization, audit)
 * to provide secure access control for the microservices architecture.
 *
 * Key responsibilities:
 * - User authentication via SAML and other methods
 * - JWT token generation, validation, and refresh
 * - Authorization decisions based on user permissions
 * - Comprehensive audit logging for security compliance
 * - Error handling and security event logging
 *
 * @property authenticationPort Port for authentication operations (JWT, SAML)
 * @property authorizationPort Port for authorization and permission checks
 * @property auditPort Port for security audit logging
 *
 * @author Technology Portfolio Team
 * @since 1.0.0
 */
@Service
class AuthenticationService(
    private val authenticationPort: AuthenticationPort,
    private val authorizationPort: AuthorizationPort,
    private val auditPort: AuditPort
) {

    /**
     * Authenticates a user based on the provided Spring Security Authentication object.
     *
     * This method processes SAML authentication responses, generates JWT tokens,
     * retrieves user permissions, and logs all authentication events for audit purposes.
     * It handles the complete authentication workflow from SAML assertion processing
     * to JWT token generation.
     *
     * @param authentication Spring Security Authentication object, typically Saml2Authentication
     * @return AuthenticationResult containing success/failure status, user details, and JWT token
     *
     * @throws Exception when authentication processing fails
     */
    fun authenticateUser(authentication: Authentication): AuthenticationResult {
        return try {
            val samlAuth = authentication as Saml2Authentication
            val username = samlAuth.principal as String
            val authorities = samlAuth.authorities.map { it.authority }
            val sessionIndex = samlAuth.credentials?.toString()

            // Generate JWT token
            val token = authenticationPort.generateToken(username, authorities, sessionIndex)

            // Log successful authentication
            auditPort.logAuthenticationEvent(
                AuthenticationEvent(
                    username = username,
                    eventType = AuthenticationEventType.LOGIN_SUCCESS,
                    sessionIndex = sessionIndex,
                    success = true
                )
            )

            // Log token generation
            auditPort.logTokenEvent(
                TokenEvent(
                    username = username,
                    eventType = TokenEventType.TOKEN_GENERATED,
                    sessionIndex = sessionIndex
                )
            )

            AuthenticationResult.success(
                username = username,
                authorities = authorities,
                token = token,
                sessionIndex = sessionIndex,
                expiresAt = LocalDateTime.now().plusHours(1) // 1 hour expiration
            )
        } catch (e: Exception) {
            val errorMessage = "Authentication failed: ${e.message}"

            // Log failed authentication
            auditPort.logAuthenticationEvent(
                AuthenticationEvent(
                    username = "unknown",
                    eventType = AuthenticationEventType.LOGIN_FAILURE,
                    success = false,
                    errorMessage = errorMessage
                )
            )

            AuthenticationResult.failure(errorMessage)
        }
    }

    /**
     * Validates a JWT token and extracts user information.
     *
     * This method performs comprehensive token validation including signature verification,
     * expiration checking, and claim extraction. All validation attempts are logged for
     * security monitoring and audit purposes.
     *
     * @param token JWT token string to validate
     * @return TokenValidationResult containing validation status and extracted user information
     *
     * @throws Exception when token validation processing fails
     */
    fun validateToken(token: String): TokenValidationResult {
        return try {
            val validationResult = authenticationPort.validateToken(token)

            if (validationResult.isValid) {
                // Log token validation
                auditPort.logTokenEvent(
                    TokenEvent(
                        username = validationResult.username ?: "unknown",
                        eventType = TokenEventType.TOKEN_VALIDATED,
                        sessionIndex = validationResult.sessionIndex
                    )
                )
            } else {
                // Log invalid token
                auditPort.logTokenEvent(
                    TokenEvent(
                        username = "unknown",
                        eventType = TokenEventType.TOKEN_INVALID
                    )
                )
            }

            validationResult
        } catch (e: Exception) {
            val errorMessage = "Token validation failed: ${e.message}"

            // Log token validation error
            auditPort.logTokenEvent(
                TokenEvent(
                    username = "unknown",
                    eventType = TokenEventType.TOKEN_INVALID
                )
            )

            TokenValidationResult.invalid(errorMessage)
        }
    }

    /**
     * Refreshes an existing JWT token if it's valid and not expired.
     *
     * This method creates a new token with extended expiration time while preserving
     * the original user claims and authorities. Token refresh events are logged for
     * security monitoring and session management.
     *
     * @param token Current JWT token to refresh
     * @return New JWT token string, or null if refresh fails
     *
     * @throws Exception when token refresh processing fails
     */
    fun refreshToken(token: String): String? {
        return try {
            val username = authenticationPort.extractUsernameFromToken(token)
            val sessionIndex = authenticationPort.extractAuthoritiesFromToken(token)?.firstOrNull()

            // If we can't extract username, the token is not valid for refresh
            if (username == null) {
                return null
            }

            val refreshedToken = authenticationPort.refreshToken(token)

            if (refreshedToken != null) {
                // Log token refresh
                auditPort.logTokenEvent(
                    TokenEvent(
                        username = username,
                        eventType = TokenEventType.TOKEN_REFRESHED,
                        sessionIndex = sessionIndex
                    )
                )
            }

            refreshedToken
        } catch (e: Exception) {
            // Log token refresh failure
            auditPort.logTokenEvent(
                TokenEvent(
                    username = "unknown",
                    eventType = TokenEventType.TOKEN_INVALID
                )
            )
            null
        }
    }

    /**
     * Authorizes a user to perform a specific action on a resource.
     *
     * This method performs comprehensive authorization checks considering user permissions,
     * roles, and resource-specific access rules. All authorization decisions are logged
     * for compliance and security monitoring purposes.
     *
     * @param username The username of the user requesting access
     * @param resource The resource being accessed (e.g., "portfolios", "users")
     * @param action The action being performed (e.g., "read", "write", "delete")
     * @return AuthorizationResult containing authorization decision and details
     *
     * @throws Exception when authorization processing fails
     */
    fun authorizeUser(
        username: String,
        resource: String,
        action: String
    ): AuthorizationResult {
        return try {
            val authorizationResult = authorizationPort.authorizeUser(username, resource, action)

            // Log authorization event
            auditPort.logAuthorizationEvent(
                AuthorizationEvent(
                    username = username,
                    resource = resource,
                    action = action,
                    authorized = authorizationResult.isAuthorized,
                    permissions = authorizationResult.permissions,
                    errorMessage = authorizationResult.errorMessage
                )
            )

            authorizationResult
        } catch (e: Exception) {
            val errorMessage = "Authorization failed: ${e.message}"

            // Log authorization failure
            auditPort.logAuthorizationEvent(
                AuthorizationEvent(
                    username = username,
                    resource = resource,
                    action = action,
                    authorized = false,
                    errorMessage = errorMessage
                )
            )

            AuthorizationResult.unauthorized(
                username,
                resource,
                action,
                errorMessage
            )
        }
    }
} 