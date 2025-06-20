package com.company.techportfolio.gateway.domain.port

import java.time.LocalDateTime

/**
 * Port interface for audit logging operations in the API Gateway domain.
 * 
 * This interface defines the contract for audit-related operations including
 * logging authentication events, authorization decisions, and token operations.
 * It follows the hexagonal architecture pattern by defining the domain's requirements
 * for audit services without depending on specific implementations.
 * 
 * Implementations of this interface handle:
 * - Authentication event logging for compliance and security monitoring
 * - Authorization decision logging for access control auditing
 * - Token lifecycle event logging for security tracking
 * 
 * @author Technology Portfolio Team
 * @since 1.0.0
 */
interface AuditPort {
    
    /**
     * Logs an authentication-related event.
     * 
     * Records authentication attempts, successes, failures, and logout events
     * for security monitoring and compliance purposes.
     * 
     * @param event AuthenticationEvent containing details of the authentication event
     */
    fun logAuthenticationEvent(event: AuthenticationEvent)
    
    /**
     * Logs an authorization-related event.
     * 
     * Records authorization decisions including granted and denied access attempts
     * for security auditing and compliance tracking.
     * 
     * @param event AuthorizationEvent containing details of the authorization decision
     */
    fun logAuthorizationEvent(event: AuthorizationEvent)
    
    /**
     * Logs a token-related event.
     * 
     * Records token lifecycle events including generation, validation, refresh,
     * and expiration for security monitoring and troubleshooting.
     * 
     * @param event TokenEvent containing details of the token operation
     */
    fun logTokenEvent(event: TokenEvent)
}

/**
 * Represents an authentication-related audit event.
 * 
 * This data class captures comprehensive information about authentication attempts,
 * including user details, session information, client details, and outcome.
 * Used for security monitoring, compliance reporting, and troubleshooting.
 * 
 * @property username The username involved in the authentication event
 * @property eventType The type of authentication event that occurred
 * @property timestamp When the authentication event occurred (defaults to current time)
 * @property sessionIndex Unique session identifier, particularly for SAML sessions
 * @property ipAddress Client IP address from which the authentication was attempted
 * @property userAgent Client user agent string for device/browser identification
 * @property success Whether the authentication attempt was successful
 * @property errorMessage Error details when authentication fails, null on success
 * 
 * @author Technology Portfolio Team
 * @since 1.0.0
 */
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

/**
 * Enumeration of authentication event types for audit logging.
 * 
 * Defines the various types of authentication-related events that can occur
 * in the system and need to be audited for security and compliance purposes.
 * 
 * @author Technology Portfolio Team
 * @since 1.0.0
 */
enum class AuthenticationEventType {
    /** User successfully logged in */
    LOGIN_SUCCESS,
    
    /** User login attempt failed */
    LOGIN_FAILURE,
    
    /** User logged out */
    LOGOUT,
    
    /** User token was refreshed */
    TOKEN_REFRESH,
    
    /** User token expired */
    TOKEN_EXPIRED
}

/**
 * Represents an authorization-related audit event.
 * 
 * This data class captures comprehensive information about authorization decisions,
 * including user details, resource access attempts, permissions, and outcomes.
 * Used for access control auditing, compliance reporting, and security monitoring.
 * 
 * @property username The username for whom authorization was checked
 * @property resource The resource that was being accessed
 * @property action The action that was being performed on the resource
 * @property timestamp When the authorization check occurred (defaults to current time)
 * @property authorized Whether the user was authorized to perform the action
 * @property permissions List of permissions that were considered in the decision
 * @property errorMessage Error details when authorization fails, null on success
 * 
 * @author Technology Portfolio Team
 * @since 1.0.0
 */
data class AuthorizationEvent(
    val username: String,
    val resource: String,
    val action: String,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val authorized: Boolean,
    val permissions: List<String> = emptyList(),
    val errorMessage: String? = null
)

/**
 * Represents a token-related audit event.
 * 
 * This data class captures comprehensive information about JWT token operations,
 * including generation, validation, refresh, and expiration events.
 * Used for security monitoring, troubleshooting, and compliance tracking.
 * 
 * @property username The username associated with the token
 * @property eventType The type of token event that occurred
 * @property timestamp When the token event occurred (defaults to current time)
 * @property tokenId Unique identifier for the token, if available
 * @property sessionIndex Session identifier associated with the token
 * 
 * @author Technology Portfolio Team
 * @since 1.0.0
 */
data class TokenEvent(
    val username: String,
    val eventType: TokenEventType,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val tokenId: String? = null,
    val sessionIndex: String? = null
)

/**
 * Enumeration of token event types for audit logging.
 * 
 * Defines the various types of token-related events that can occur
 * in the system and need to be audited for security monitoring and troubleshooting.
 * 
 * @author Technology Portfolio Team
 * @since 1.0.0
 */
enum class TokenEventType {
    /** New token was generated */
    TOKEN_GENERATED,
    
    /** Token was validated successfully */
    TOKEN_VALIDATED,
    
    /** Token was refreshed */
    TOKEN_REFRESHED,
    
    /** Token expired */
    TOKEN_EXPIRED,
    
    /** Token validation failed */
    TOKEN_INVALID
} 