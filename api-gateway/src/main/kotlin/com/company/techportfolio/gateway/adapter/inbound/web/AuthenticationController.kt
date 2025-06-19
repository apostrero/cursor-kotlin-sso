package com.company.techportfolio.gateway.adapter.inbound.web

import com.company.techportfolio.gateway.domain.model.AuthenticationResult
import com.company.techportfolio.gateway.domain.model.TokenValidationResult
import com.company.techportfolio.gateway.domain.port.AuthorizationResult
import com.company.techportfolio.gateway.domain.service.AuthenticationService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

/**
 * REST controller for authentication and authorization operations in the API Gateway.
 * 
 * This controller provides HTTP endpoints for user authentication, token validation,
 * token refresh, and authorization checks. It serves as the inbound adapter in the
 * hexagonal architecture, translating HTTP requests into domain service calls.
 * 
 * Key endpoints:
 * - POST /api/auth/authenticate - User authentication
 * - POST /api/auth/validate - JWT token validation
 * - POST /api/auth/refresh - JWT token refresh
 * - POST /api/auth/authorize - User authorization checks
 * - GET /api/auth/health - Service health check
 * 
 * @property authenticationService Domain service for authentication operations
 * 
 * @author Technology Portfolio Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/auth")
class AuthenticationController(
    private val authenticationService: AuthenticationService
) {

    /**
     * Authenticates a user and returns authentication result with JWT token.
     * 
     * This endpoint processes authentication requests, typically from SAML SSO flows,
     * and returns a comprehensive authentication result including user details and JWT token.
     * 
     * @param authentication Spring Security Authentication object containing user credentials
     * @return ResponseEntity with AuthenticationResult (200 OK if successful, 400 Bad Request if failed)
     */
    @PostMapping("/authenticate")
    fun authenticateUser(authentication: Authentication): ResponseEntity<AuthenticationResult> {
        val result = authenticationService.authenticateUser(authentication)
        return if (result.isAuthenticated) {
            ResponseEntity.ok(result)
        } else {
            ResponseEntity.badRequest().body(result)
        }
    }

    /**
     * Validates a JWT token and returns validation result with user information.
     * 
     * This endpoint accepts a Bearer token in the Authorization header and performs
     * comprehensive token validation including signature verification and expiration checking.
     * 
     * @param authorization Authorization header containing "Bearer {token}"
     * @return ResponseEntity with TokenValidationResult (200 OK if valid, 401 Unauthorized if invalid)
     */
    @PostMapping("/validate")
    fun validateToken(@RequestHeader("Authorization") authorization: String): ResponseEntity<TokenValidationResult> {
        val token = authorization.removePrefix("Bearer ")
        val result = authenticationService.validateToken(token)
        return if (result.isValid) {
            ResponseEntity.ok(result)
        } else {
            ResponseEntity.status(401).body(result)
        }
    }

    /**
     * Refreshes an existing JWT token and returns a new token with extended expiration.
     * 
     * This endpoint accepts a Bearer token in the Authorization header and attempts to
     * refresh it if valid. Returns a new token with extended expiration time.
     * 
     * @param authorization Authorization header containing "Bearer {token}"
     * @return ResponseEntity with new token (200 OK if successful, 401 Unauthorized if failed)
     */
    @PostMapping("/refresh")
    fun refreshToken(@RequestHeader("Authorization") authorization: String): ResponseEntity<Map<String, String>> {
        val token = authorization.removePrefix("Bearer ")
        val refreshedToken = authenticationService.refreshToken(token)
        return if (refreshedToken != null) {
            ResponseEntity.ok(mapOf("token" to refreshedToken))
        } else {
            ResponseEntity.status(401).body(mapOf("error" to "Token refresh failed"))
        }
    }

    /**
     * Authorizes a user to perform a specific action on a resource.
     * 
     * This endpoint performs authorization checks to determine if a user has permission
     * to perform a specific action on a given resource based on their roles and permissions.
     * 
     * @param username The username of the user requesting access
     * @param resource The resource being accessed (e.g., "portfolios", "users")
     * @param action The action being performed (e.g., "read", "write", "delete")
     * @return ResponseEntity with AuthorizationResult (200 OK if authorized, 403 Forbidden if not)
     */
    @PostMapping("/authorize")
    fun authorizeUser(
        @RequestParam username: String,
        @RequestParam resource: String,
        @RequestParam action: String
    ): ResponseEntity<AuthorizationResult> {
        val result = authenticationService.authorizeUser(username, resource, action)
        return if (result.isAuthorized) {
            ResponseEntity.ok(result)
        } else {
            ResponseEntity.status(403).body(result)
        }
    }

    /**
     * Health check endpoint for the authentication service.
     * 
     * This endpoint provides a simple health check to verify that the authentication
     * service is running and responsive. Used by monitoring systems and load balancers.
     * 
     * @return ResponseEntity with health status (200 OK with status information)
     */
    @GetMapping("/health")
    fun health(): ResponseEntity<Map<String, String>> {
        return ResponseEntity.ok(mapOf("status" to "UP", "service" to "authentication"))
    }
} 