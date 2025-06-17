package com.company.techportfolio.gateway.domain.service

import com.company.techportfolio.gateway.domain.model.AuthenticationResult
import com.company.techportfolio.gateway.domain.model.TokenValidationResult
import com.company.techportfolio.gateway.domain.port.AuditPort
import com.company.techportfolio.gateway.domain.port.AuthorizationPort
import com.company.techportfolio.gateway.domain.port.AuthenticationPort
import com.company.techportfolio.gateway.domain.port.AuthenticationEvent
import com.company.techportfolio.gateway.domain.port.AuthenticationEventType
import com.company.techportfolio.gateway.domain.port.TokenEvent
import com.company.techportfolio.gateway.domain.port.TokenEventType
import com.company.techportfolio.gateway.domain.port.AuthorizationEvent
import org.springframework.security.core.Authentication
import org.springframework.security.saml2.provider.service.authentication.Saml2Authentication
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class AuthenticationService(
    private val authenticationPort: AuthenticationPort,
    private val authorizationPort: AuthorizationPort,
    private val auditPort: AuditPort
) {

    fun authenticateUser(authentication: Authentication): AuthenticationResult {
        return try {
            val samlAuth = authentication as Saml2Authentication
            val username = samlAuth.principal as String
            val authorities = samlAuth.authorities.map { it.authority }
            val sessionIndex = samlAuth.credentials?.toString()

            // Generate JWT token
            val token = authenticationPort.generateToken(username, authorities, sessionIndex)
            
            // Get user permissions for authorization
            val permissions = authorizationPort.getUserPermissions(username)
            
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

    fun refreshToken(token: String): String? {
        return try {
            val username = authenticationPort.extractUsernameFromToken(token)
            val sessionIndex = authenticationPort.extractAuthoritiesFromToken(token)?.firstOrNull()
            
            val refreshedToken = authenticationPort.refreshToken(token)
            
            if (refreshedToken != null && username != null) {
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

    fun authorizeUser(username: String, resource: String, action: String): com.company.techportfolio.gateway.domain.port.AuthorizationResult {
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

            com.company.techportfolio.gateway.domain.port.AuthorizationResult.unauthorized(username, resource, action, errorMessage)
        }
    }
} 