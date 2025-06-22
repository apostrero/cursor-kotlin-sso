package com.company.techportfolio.authorization.adapter.inbound.web

import com.company.techportfolio.authorization.domain.model.AuthorizationRequest
import com.company.techportfolio.authorization.domain.model.AuthorizationResponse
import com.company.techportfolio.authorization.domain.model.UserPermissions
import com.company.techportfolio.authorization.domain.service.AuthorizationService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

/**
 * REST controller for authorization and permission management operations (REACTIVE).
 *
 * This controller provides HTTP endpoints for authorization decisions, permission
 * queries, and role verification within the technology portfolio system using
 * reactive programming patterns. It serves as the primary inbound adapter for
 * the authorization service, translating HTTP requests into domain service calls
 * using reactive streams.
 *
 * The controller follows RESTful design principles and provides both synchronous
 * authorization checks and user permission queries. All endpoints return appropriate
 * HTTP status codes and structured responses for easy integration with client applications.
 * All methods return Mono<ResponseEntity<T>> for non-blocking I/O operations.
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
 * - Reactive error handling with onErrorMap and onErrorResume
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
     * **Reactive**: Returns Mono<ResponseEntity<AuthorizationResponse>>
     *
     * @param request The authorization request containing user, resource, action, and context
     * @return Mono<ResponseEntity<AuthorizationResponse>> with AuthorizationResponse and appropriate HTTP status
     */
    @PostMapping("/check")
    fun checkAuthorization(@RequestBody @Valid request: AuthorizationRequest): Mono<ResponseEntity<AuthorizationResponse>> {
        return Mono.fromCallable<AuthorizationResponse> { authorizationService.authorizeUser(request) }
            .map { response ->
                if (response.isAuthorized) {
                    ResponseEntity.ok(response)
                } else {
                    ResponseEntity.status(403).body(response)
                }
            }
            .onErrorReturn(ResponseEntity.internalServerError().build())
    }

    /**
     * Retrieves comprehensive permission information for a user.
     *
     * This endpoint provides a complete view of a user's permissions, roles,
     * and authorization context. It's useful for building user dashboards,
     * permission summaries, or caching authorization data.
     *
     * **Reactive**: Returns Mono<ResponseEntity<UserPermissions>>
     *
     * @param username The username to retrieve permissions for
     * @return Mono<ResponseEntity<UserPermissions>> containing UserPermissions object
     */
    @GetMapping("/permissions")
    fun getUserPermissions(@RequestParam username: String): Mono<ResponseEntity<UserPermissions>> {
        return Mono.fromCallable<UserPermissions> { authorizationService.getUserPermissions(username) }
            .map { permissions -> ResponseEntity.ok(permissions) }
            .onErrorReturn(ResponseEntity.notFound().build())
    }

    /**
     * Checks if a user has a specific role assigned.
     *
     * This endpoint provides a simple boolean check for role membership.
     * It's useful for role-based access control scenarios where specific
     * roles are required for certain operations.
     *
     * **Reactive**: Returns Mono<ResponseEntity<Boolean>>
     *
     * @param username The username to check roles for
     * @param role The role name to verify
     * @return Mono<ResponseEntity<Boolean>> containing boolean result
     */
    @GetMapping("/has-role")
    fun hasRole(
        @RequestParam username: String,
        @RequestParam role: String
    ): Mono<ResponseEntity<Boolean>> {
        return Mono.fromCallable<Boolean> { authorizationService.hasRole(username, role) }
            .map { hasRole -> ResponseEntity.ok(hasRole) }
            .onErrorReturn(ResponseEntity.internalServerError().build())
    }

    /**
     * Checks if a user has any of the specified roles assigned.
     *
     * This endpoint accepts a list of roles and returns true if the user
     * has at least one of them. It's useful for scenarios where multiple
     * roles can provide the same level of access.
     *
     * **Reactive**: Returns Mono<ResponseEntity<Boolean>>
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
     * @return Mono<ResponseEntity<Boolean>> containing boolean result
     */
    @PostMapping("/has-any-role")
    fun hasAnyRole(
        @RequestBody request: Map<String, Any>
    ): Mono<ResponseEntity<Boolean>> {
        val username = request["username"] as String
        @Suppress("UNCHECKED_CAST")
        val roles = request["roles"] as List<String>

        return Mono.fromCallable<Boolean> { authorizationService.hasAnyRole(username, roles) }
            .map { hasAnyRole -> ResponseEntity.ok(hasAnyRole) }
            .onErrorReturn(ResponseEntity.internalServerError().build())
    }

    /**
     * Checks if a user has permission to perform a specific action on a resource.
     *
     * This endpoint provides fine-grained permission checking for specific
     * resource-action combinations. It considers both direct permissions and
     * permissions inherited through role assignments.
     *
     * **Reactive**: Returns Mono<ResponseEntity<Boolean>>
     *
     * @param username The username to check permissions for
     * @param resource The resource being accessed (e.g., "portfolio", "technology")
     * @param action The action being performed (e.g., "READ", "WRITE", "DELETE")
     * @return Mono<ResponseEntity<Boolean>> containing boolean result
     */
    @GetMapping("/has-permission")
    fun hasPermission(
        @RequestParam username: String,
        @RequestParam resource: String,
        @RequestParam action: String
    ): Mono<ResponseEntity<Boolean>> {
        return Mono.fromCallable<Boolean> { authorizationService.hasPermission(username, resource, action) }
            .map { hasPermission -> ResponseEntity.ok(hasPermission) }
            .onErrorReturn(ResponseEntity.internalServerError().build())
    }

    /**
     * Health check endpoint for service monitoring.
     *
     * This endpoint provides a simple health check for the authorization service,
     * returning service status and identification information. It's used by
     * monitoring systems, load balancers, and service discovery mechanisms.
     *
     * **Reactive**: Returns Mono<ResponseEntity<Map<String, String>>>
     *
     * @return Mono<ResponseEntity<Map<String, String>>> containing health status information
     */
    @GetMapping("/health")
    fun health(): Mono<ResponseEntity<Map<String, String>>> {
        return Mono.just(ResponseEntity.ok(mapOf("status" to "UP", "service" to "authorization")))
    }
} 