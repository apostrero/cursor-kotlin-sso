package com.company.techportfolio.gateway.domain.port

/**
 * Port interface for authorization operations in the API Gateway domain.
 * 
 * This interface defines the contract for authorization-related operations including
 * user authorization checks, permission management, and role-based access control.
 * It follows the hexagonal architecture pattern by defining the domain's requirements
 * for authorization services without depending on specific implementations.
 * 
 * Implementations of this interface handle:
 * - Resource-based authorization checks
 * - User permission retrieval and validation
 * - Role-based access control (RBAC)
 * 
 * @author Technology Portfolio Team
 * @since 1.0.0
 */
interface AuthorizationPort {
    
    /**
     * Authorizes a user to perform a specific action on a resource.
     * 
     * Performs comprehensive authorization check considering user permissions,
     * roles, and resource-specific access rules.
     * 
     * @param username The username of the user requesting access
     * @param resource The resource being accessed (e.g., "portfolios", "users")
     * @param action The action being performed (e.g., "read", "write", "delete")
     * @return AuthorizationResult containing authorization decision and details
     */
    fun authorizeUser(username: String, resource: String, action: String): AuthorizationResult
    
    /**
     * Retrieves all permissions granted to a specific user.
     * 
     * Returns a comprehensive list of permissions that includes both
     * direct permissions and permissions inherited from roles.
     * 
     * @param username The username to retrieve permissions for
     * @return List of permission strings granted to the user
     */
    fun getUserPermissions(username: String): List<String>
    
    /**
     * Checks if a user has a specific role.
     * 
     * Performs role membership check for the specified user and role.
     * 
     * @param username The username to check
     * @param role The role name to verify (e.g., "ADMIN", "USER", "MANAGER")
     * @return true if user has the specified role, false otherwise
     */
    fun hasRole(username: String, role: String): Boolean
    
    /**
     * Checks if a user has any of the specified roles.
     * 
     * Performs role membership check against multiple roles,
     * returning true if the user has at least one of them.
     * 
     * @param username The username to check
     * @param roles List of role names to verify against
     * @return true if user has any of the specified roles, false otherwise
     */
    fun hasAnyRole(username: String, roles: List<String>): Boolean
}

/**
 * Represents the result of an authorization check in the API Gateway.
 * 
 * This data class encapsulates all information related to authorization decisions,
 * including the authorization status, user details, resource information, and
 * relevant permissions. It serves as the contract between authorization adapters
 * and the domain service.
 * 
 * @property isAuthorized Indicates whether the user is authorized to perform the action
 * @property username The username of the user for whom authorization was checked
 * @property resource The resource that was being accessed
 * @property action The action that was being performed on the resource
 * @property permissions List of permissions that were considered in the authorization decision
 * @property errorMessage Descriptive error message when authorization fails, null on success
 * 
 * @author Technology Portfolio Team
 * @since 1.0.0
 */
data class AuthorizationResult(
    val isAuthorized: Boolean,
    val username: String,
    val resource: String,
    val action: String,
    val permissions: List<String> = emptyList(),
    val errorMessage: String? = null
) {
    companion object {
        /**
         * Creates an authorized result with user permissions.
         * 
         * @param username The authorized user's username
         * @param resource The resource being accessed
         * @param action The action being performed
         * @param permissions List of permissions that granted access
         * @return AuthorizationResult indicating successful authorization
         */
        fun authorized(username: String, resource: String, action: String, permissions: List<String>): AuthorizationResult =
            AuthorizationResult(
                isAuthorized = true,
                username = username,
                resource = resource,
                action = action,
                permissions = permissions
            )

        /**
         * Creates an unauthorized result with optional error message.
         * 
         * @param username The user's username who was denied access
         * @param resource The resource that was being accessed
         * @param action The action that was denied
         * @param errorMessage Optional descriptive error message
         * @return AuthorizationResult indicating denied authorization
         */
        fun unauthorized(username: String, resource: String, action: String, errorMessage: String? = null): AuthorizationResult =
            AuthorizationResult(
                isAuthorized = false,
                username = username,
                resource = resource,
                action = action,
                errorMessage = errorMessage
            )
    }
} 
 