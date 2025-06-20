package com.company.techportfolio.authorization.adapter.inbound.web

import com.company.techportfolio.authorization.domain.model.AuthorizationRequest
import com.company.techportfolio.authorization.domain.model.AuthorizationResponse
import com.company.techportfolio.authorization.domain.model.UserPermissions
import com.company.techportfolio.authorization.domain.service.AuthorizationService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import jakarta.validation.Valid

/**
 * REST controller for authorization and permission management operations.
 * 
 * This controller provides HTTP endpoints for authorization decisions, permission
 * queries, and role verification within the technology portfolio system. It serves
 * as the primary inbound adapter for the authorization service, translating HTTP
 * requests into domain service calls.
 * 
 * The controller follows RESTful design principles and provides both synchronous
 * authorization checks and user permission queries. All endpoints return appropriate
 * HTTP status codes and structured responses for easy integration with client applications.
 * 
 * Key endpoints:
 * - POST /api/authorization/check - Primary authorization decision endpoint
 * - GET /api/authorization/permissions - User permission queries
 * - GET /api/authorization/has-role - Role verification
 * - POST /api/authorization/has-any-role - Multiple role verification
 * - GET /api/authorization/has-permission - Permission verification
 * - GET /api/authorization/health - Service health check
 * 
 * Security considerations:
 * - All endpoints should be protected by authentication middleware
 * - Sensitive authorization data is only returned for authorized requests
 * - Error responses avoid leaking sensitive system information
 * 
 * @property authorizationService Domain service for authorization operations
 * 
 * @author Technology Portfolio Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/authorization")
class AuthorizationController(
    /** Domain service for authorization operations */
    private val authorizationService: AuthorizationService
) {

    /**
     * Evaluates an authorization request and returns the decision.
     * 
     * This is the primary endpoint for authorization decisions. It accepts a
     * structured authorization request containing user, resource, action, and
     * context information, and returns a comprehensive authorization response.
     * 
     * The endpoint returns HTTP 200 for authorized requests and HTTP 403 for
     * unauthorized requests, with detailed response information in both cases.
     * 
     * @param request The authorization request containing user, resource, action, and context
     * @return ResponseEntity with AuthorizationResponse and appropriate HTTP status
     */
    @PostMapping("/check")
    fun checkAuthorization(@RequestBody @Valid request: AuthorizationRequest): ResponseEntity<AuthorizationResponse> {
        val response = authorizationService.authorizeUser(request)
        return if (response.isAuthorized) {
            ResponseEntity.ok(response)
        } else {
            ResponseEntity.status(403).body(response)
        }
    }

    /**
     * Retrieves comprehensive permission information for a user.
     * 
     * This endpoint provides a complete view of a user's permissions, roles,
     * and authorization context. It's useful for building user dashboards,
     * permission summaries, or caching authorization data.
     * 
     * @param username The username to retrieve permissions for
     * @return ResponseEntity containing UserPermissions object
     */
    @GetMapping("/permissions")
    fun getUserPermissions(@RequestParam username: String): ResponseEntity<UserPermissions> {
        val permissions = authorizationService.getUserPermissions(username)
        return ResponseEntity.ok(permissions)
    }

    /**
     * Checks if a user has a specific role assigned.
     * 
     * This endpoint provides a simple boolean check for role membership.
     * It's useful for role-based access control scenarios where specific
     * roles are required for certain operations.
     * 
     * @param username The username to check roles for
     * @param role The role name to verify
     * @return ResponseEntity containing boolean result
     */
    @GetMapping("/has-role")
    fun hasRole(
        @RequestParam username: String,
        @RequestParam role: String
    ): ResponseEntity<Boolean> {
        val hasRole = authorizationService.hasRole(username, role)
        return ResponseEntity.ok(hasRole)
    }

    /**
     * Checks if a user has any of the specified roles assigned.
     * 
     * This endpoint accepts a list of roles and returns true if the user
     * has at least one of them. It's useful for scenarios where multiple
     * roles can provide the same level of access.
     * 
     * Request body format:
     * ```json
     * {
     *   "username": "john.doe",
     *   "roles": ["ADMIN", "MANAGER", "LEAD"]
     * }
     * ```
     * 
     * @param request Map containing username and list of roles to check
     * @return ResponseEntity containing boolean result
     */
    @PostMapping("/has-any-role")
    fun hasAnyRole(
        @RequestBody request: Map<String, Any>
    ): ResponseEntity<Boolean> {
        val username = request["username"] as String
        val roles = request["roles"] as List<String>
        val hasAnyRole = authorizationService.hasAnyRole(username, roles)
        return ResponseEntity.ok(hasAnyRole)
    }

    /**
     * Checks if a user has permission to perform a specific action on a resource.
     * 
     * This endpoint provides fine-grained permission checking for specific
     * resource-action combinations. It considers both direct permissions and
     * permissions inherited through role assignments.
     * 
     * @param username The username to check permissions for
     * @param resource The resource being accessed (e.g., "portfolio", "technology")
     * @param action The action being performed (e.g., "READ", "WRITE", "DELETE")
     * @return ResponseEntity containing boolean result
     */
    @GetMapping("/has-permission")
    fun hasPermission(
        @RequestParam username: String,
        @RequestParam resource: String,
        @RequestParam action: String
    ): ResponseEntity<Boolean> {
        val hasPermission = authorizationService.hasPermission(username, resource, action)
        return ResponseEntity.ok(hasPermission)
    }

    /**
     * Health check endpoint for service monitoring.
     * 
     * This endpoint provides a simple health check for the authorization service,
     * returning service status and identification information. It's used by
     * monitoring systems, load balancers, and service discovery mechanisms.
     * 
     * @return ResponseEntity containing health status information
     */
    @GetMapping("/health")
    fun health(): ResponseEntity<Map<String, String>> {
        return ResponseEntity.ok(mapOf("status" to "UP", "service" to "authorization"))
    }
} 