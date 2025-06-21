package com.company.techportfolio.authorization.domain.service

import com.company.techportfolio.authorization.domain.model.AuthorizationRequest
import com.company.techportfolio.authorization.domain.model.AuthorizationResponse
import com.company.techportfolio.authorization.domain.model.UserPermissions
import com.company.techportfolio.authorization.domain.port.PermissionRepository
import com.company.techportfolio.authorization.domain.port.RoleRepository
import com.company.techportfolio.authorization.domain.port.UserRepository
import org.springframework.stereotype.Service

/**
 * Core domain service for authorization and permission management.
 *
 * This service implements the business logic for making authorization decisions
 * within the technology portfolio system. It coordinates between various repository
 * ports to evaluate user permissions, roles, and access rights for specific
 * resources and actions.
 *
 * The service follows domain-driven design principles and implements the hexagonal
 * architecture pattern by depending on repository ports rather than concrete
 * implementations. This allows for flexible persistence strategies while maintaining
 * clean separation of concerns.
 *
 * Key responsibilities:
 * - Authorization decision making based on user permissions and roles
 * - User permission and role queries
 * - Resource access validation
 * - Security context evaluation
 * - Error handling and graceful degradation
 *
 * Authorization flow:
 * 1. Validate user existence and active status
 * 2. Evaluate specific permissions for resource/action combinations
 * 3. Consider role-based permissions and inheritance
 * 4. Apply organizational context if applicable
 * 5. Return comprehensive authorization response
 *
 * @property userRepository Repository for user data access operations
 * @property roleRepository Repository for role data access operations
 * @property permissionRepository Repository for permission data access operations
 *
 * @author Technology Portfolio Team
 * @since 1.0.0
 */
@Service
class AuthorizationService(
    /** Repository for user data access operations */
    private val userRepository: UserRepository,
    /** Repository for role data access operations */
    private val roleRepository: RoleRepository,
    /** Repository for permission data access operations */
    private val permissionRepository: PermissionRepository
) {

    /**
     * Evaluates an authorization request and returns a comprehensive response.
     *
     * This is the primary method for making authorization decisions. It evaluates
     * whether a user has permission to perform a specific action on a given resource,
     * considering the user's active status, assigned permissions, roles, and any
     * contextual information provided in the request.
     *
     * The method follows a fail-safe approach where any errors or exceptions
     * result in denial of access to maintain security.
     *
     * @param request The authorization request containing user, resource, action, and context
     * @return AuthorizationResponse indicating whether access is granted and providing user details
     */
    fun authorizeUser(request: AuthorizationRequest): AuthorizationResponse {
        return try {
            val username = request.username
            val resource = request.resource
            val action = request.action

            // Check if user exists and is active
            if (!userRepository.isUserActive(username)) {
                return AuthorizationResponse.unauthorized(
                    username = username,
                    resource = resource,
                    action = action,
                    errorMessage = "User is not active or does not exist"
                )
            }

            // Check if user has the required permission
            val hasPermission = permissionRepository.hasPermission(username, resource, action)

            if (hasPermission) {
                val permissions = userRepository.findUserPermissions(username)
                val roles = userRepository.findUserRoles(username)
                val organizationId = userRepository.findUserOrganization(username)

                AuthorizationResponse.authorized(
                    username = username,
                    resource = resource,
                    action = action,
                    permissions = permissions,
                    roles = roles,
                    organizationId = organizationId
                )
            } else {
                AuthorizationResponse.unauthorized(
                    username = username,
                    resource = resource,
                    action = action,
                    errorMessage = "User does not have permission for $resource:$action"
                )
            }
        } catch (e: Exception) {
            AuthorizationResponse.unauthorized(
                username = request.username,
                resource = request.resource,
                action = request.action,
                errorMessage = "Authorization failed: ${e.message}"
            )
        }
    }

    /**
     * Retrieves comprehensive permission information for a user.
     *
     * This method provides a complete view of a user's authorization capabilities,
     * including all assigned permissions, roles, organizational context, and
     * account status. It's useful for caching authorization data or building
     * user permission summaries.
     *
     * @param username The username to retrieve permissions for
     * @return UserPermissions object containing complete authorization information
     */
    fun getUserPermissions(username: String): UserPermissions {
        return try {
            if (!userRepository.isUserActive(username)) {
                return UserPermissions(
                    username = username,
                    permissions = emptyList(),
                    roles = emptyList(),
                    isActive = false
                )
            }

            val permissions = userRepository.findUserPermissions(username)
            val roles = userRepository.findUserRoles(username)
            val organizationId = userRepository.findUserOrganization(username)

            UserPermissions(
                username = username,
                permissions = permissions,
                roles = roles,
                organizationId = organizationId,
                isActive = true
            )
        } catch (e: Exception) {
            UserPermissions(
                username = username,
                permissions = emptyList(),
                roles = emptyList(),
                isActive = false
            )
        }
    }

    /**
     * Checks if a user has a specific role assigned.
     *
     * This method performs a simple role check for a user, considering only
     * direct role assignments. It's useful for role-based access control
     * scenarios where specific roles are required.
     *
     * @param username The username to check roles for
     * @param role The role name to verify
     * @return true if the user has the specified role, false otherwise
     */
    fun hasRole(username: String, role: String): Boolean {
        return try {
            if (!userRepository.isUserActive(username)) {
                return false
            }

            val userRoles = userRepository.findUserRoles(username)
            userRoles.contains(role)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Checks if a user has any of the specified roles assigned.
     *
     * This method performs an OR operation across multiple roles, returning
     * true if the user has at least one of the specified roles. It's useful
     * for scenarios where multiple roles can provide the same level of access.
     *
     * @param username The username to check roles for
     * @param roles List of role names to check against
     * @return true if the user has any of the specified roles, false otherwise
     */
    fun hasAnyRole(username: String, roles: List<String>): Boolean {
        return try {
            if (!userRepository.isUserActive(username)) {
                return false
            }

            val userRoles = userRepository.findUserRoles(username)
            userRoles.any { it in roles }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Checks if a user has permission to perform a specific action on a resource.
     *
     * This method evaluates fine-grained permissions for specific resource-action
     * combinations. It considers both direct permissions and permissions inherited
     * through role assignments.
     *
     * @param username The username to check permissions for
     * @param resource The resource being accessed
     * @param action The action being performed
     * @return true if the user has the required permission, false otherwise
     */
    fun hasPermission(username: String, resource: String, action: String): Boolean {
        return try {
            if (!userRepository.isUserActive(username)) {
                return false
            }

            permissionRepository.hasPermission(username, resource, action)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Checks if a user has permission to perform any of the specified actions on a resource.
     *
     * This method performs an OR operation across multiple actions for a single resource,
     * returning true if the user has permission for at least one of the actions.
     * It's useful for scenarios where multiple actions can satisfy an access requirement.
     *
     * @param username The username to check permissions for
     * @param resource The resource being accessed
     * @param actions List of actions to check permission for
     * @return true if the user has permission for any of the specified actions, false otherwise
     */
    fun hasAnyPermission(username: String, resource: String, actions: List<String>): Boolean {
        return try {
            if (!userRepository.isUserActive(username)) {
                return false
            }

            actions.any { action -> permissionRepository.hasPermission(username, resource, action) }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Retrieves detailed user information including roles and organizational context.
     *
     * This method provides user details in the form of an authorization response,
     * including role assignments and organizational affiliation. It's primarily
     * used for user profile and administrative purposes.
     *
     * @param username The username to retrieve details for
     * @return AuthorizationResponse containing user details or error information
     */
    fun getUserDetails(username: String): AuthorizationResponse {
        return try {
            if (!userRepository.isUserActive(username)) {
                return AuthorizationResponse.unauthorized(
                    username = username,
                    resource = null,
                    action = null,
                    errorMessage = "User is not active or does not exist"
                )
            }

            val user = userRepository.findById(username)
            if (user == null) {
                return AuthorizationResponse.unauthorized(
                    username = username,
                    resource = null,
                    action = null,
                    errorMessage = "User not found"
                )
            }

            val roles = userRepository.findUserRoles(username)

            AuthorizationResponse.authorized(
                username = username,
                resource = null,
                action = null,
                permissions = emptyList(),
                roles = roles,
                organizationId = user.organizationId
            )
        } catch (e: Exception) {
            AuthorizationResponse.unauthorized(
                username = username,
                resource = null,
                action = null,
                errorMessage = "Authorization failed: ${e.message}"
            )
        }
    }
} 