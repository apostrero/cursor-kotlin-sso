package com.company.techportfolio.gateway.adapter.out.authorization

import com.company.techportfolio.gateway.domain.port.AuthorizationPort
import com.company.techportfolio.gateway.domain.port.AuthorizationResult
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

/**
 * REST client adapter for authorization service communication.
 *
 * This adapter implements the AuthorizationPort interface as an outbound adapter
 * in the hexagonal architecture, providing communication with the external
 * authorization microservice via HTTP REST API calls.
 *
 * Key features:
 * - User authorization checks via REST API
 * - Permission retrieval and validation
 * - Role-based access control (RBAC) operations
 * - Error handling and fallback responses
 * - Configurable service endpoint
 *
 * Configuration properties:
 * - services.authorization.url: Base URL of the authorization service
 *
 * @property restTemplate HTTP client for making REST API calls
 * @property authorizationServiceUrl Base URL of the authorization service
 *
 * @author Technology Portfolio Team
 * @since 1.0.0
 */
@Component
class AuthorizationServiceAdapter(
    private val restTemplate: RestTemplate
) : AuthorizationPort {

    @Value("\${services.authorization.url:http://localhost:8081}")
    private lateinit var authorizationServiceUrl: String

    /**
     * Authorizes a user to perform a specific action on a resource via REST API.
     *
     * Makes an HTTP POST request to the authorization service to check if a user
     * has permission to perform a specific action on a given resource. Handles
     * service communication errors gracefully.
     *
     * @param username The username of the user requesting access
     * @param resource The resource being accessed (e.g., "portfolios", "users")
     * @param action The action being performed (e.g., "read", "write", "delete")
     * @return AuthorizationResult containing authorization decision and details
     */
    override fun authorizeUser(username: String, resource: String, action: String): AuthorizationResult {
        return try {
            val url = "$authorizationServiceUrl/api/authorization/check"
            val request = mapOf(
                "username" to username,
                "resource" to resource,
                "action" to action
            )

            val response = restTemplate.postForObject(url, request, AuthorizationResult::class.java)
            response ?: AuthorizationResult.unauthorized(username, resource, action, "No response from authorization service")
        } catch (e: Exception) {
            AuthorizationResult.unauthorized(username, resource, action, "Authorization service error: ${e.message}")
        }
    }

    /**
     * Retrieves all permissions granted to a specific user via REST API.
     *
     * Makes an HTTP GET request to the authorization service to fetch the complete
     * list of permissions for a user. Returns empty list if service is unavailable
     * or user has no permissions.
     *
     * @param username The username to retrieve permissions for
     * @return List of permission strings granted to the user
     */
    override fun getUserPermissions(username: String): List<String> {
        return try {
            val url = "$authorizationServiceUrl/api/authorization/permissions?username=$username"
            val permissions = restTemplate.getForObject(url, Array<String>::class.java)
            permissions?.toList() ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Checks if a user has a specific role via REST API.
     *
     * Makes an HTTP GET request to the authorization service to verify if a user
     * has a specific role. Returns false if service is unavailable or user
     * doesn't have the role.
     *
     * @param username The username to check
     * @param role The role name to verify (e.g., "ADMIN", "USER", "MANAGER")
     * @return true if user has the specified role, false otherwise
     */
    override fun hasRole(username: String, role: String): Boolean {
        return try {
            val url = "$authorizationServiceUrl/api/authorization/has-role?username=$username&role=$role"
            val hasRole = restTemplate.getForObject(url, Boolean::class.java)
            hasRole ?: false
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Checks if a user has any of the specified roles via REST API.
     *
     * Makes an HTTP POST request to the authorization service to verify if a user
     * has any of the provided roles. Returns false if service is unavailable or
     * user doesn't have any of the specified roles.
     *
     * @param username The username to check
     * @param roles List of role names to verify against
     * @return true if user has any of the specified roles, false otherwise
     */
    override fun hasAnyRole(username: String, roles: List<String>): Boolean {
        return try {
            val url = "$authorizationServiceUrl/api/authorization/has-any-role"
            val request = mapOf(
                "username" to username,
                "roles" to roles
            )

            val hasAnyRole = restTemplate.postForObject(url, request, Boolean::class.java)
            hasAnyRole ?: false
        } catch (e: Exception) {
            false
        }
    }
} 