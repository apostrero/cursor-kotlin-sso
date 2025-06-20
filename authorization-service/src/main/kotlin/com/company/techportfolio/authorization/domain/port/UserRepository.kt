package com.company.techportfolio.authorization.domain.port

import com.company.techportfolio.shared.domain.model.User

/**
 * Repository port for user data access operations.
 * 
 * This interface defines the contract for accessing user information required
 * for authorization decisions. It follows the hexagonal architecture pattern
 * by providing a domain-focused abstraction over the actual data persistence
 * mechanism.
 * 
 * The implementation of this interface handles the complexity of retrieving
 * user data from various sources (database, cache, external services) while
 * providing a clean, domain-focused API for the authorization service.
 * 
 * Key responsibilities:
 * - User lookup and retrieval operations
 * - User permission and role queries
 * - User status and organization information
 * - Efficient data access for authorization decisions
 * 
 * @author Technology Portfolio Team
 * @since 1.0.0
 */
interface UserRepository {
    
    /**
     * Finds a user by their unique identifier.
     * 
     * @param username The unique username identifier
     * @return User object if found, null otherwise
     */
    fun findById(username: String): User?
    
    /**
     * Finds a user by their username.
     * 
     * @param username The username to search for
     * @return User object if found, null otherwise
     */
    fun findByUsername(username: String): User?
    
    /**
     * Finds a user with their complete roles and permissions loaded.
     * 
     * This method performs an eager fetch of the user's role and permission
     * information to avoid multiple database queries during authorization.
     * 
     * @param username The username to search for
     * @return User object with roles and permissions loaded, null if not found
     */
    fun findUserWithRolesAndPermissions(username: String): User?
    
    /**
     * Retrieves all permissions assigned to a specific user.
     * 
     * This includes both direct permissions and permissions inherited
     * through role assignments.
     * 
     * @param username The username to query permissions for
     * @return List of permission strings, empty if user has no permissions
     */
    fun findUserPermissions(username: String): List<String>
    
    /**
     * Retrieves all roles assigned to a specific user.
     * 
     * @param username The username to query roles for
     * @return List of role names, empty if user has no roles
     */
    fun findUserRoles(username: String): List<String>
    
    /**
     * Checks if a user account is currently active.
     * 
     * Inactive users should be denied authorization regardless of their
     * permissions and roles.
     * 
     * @param username The username to check status for
     * @return true if the user is active, false otherwise
     */
    fun isUserActive(username: String): Boolean
    
    /**
     * Retrieves the organization ID associated with a user.
     * 
     * Organization context may be used for authorization decisions
     * in multi-tenant scenarios.
     * 
     * @param username The username to query organization for
     * @return Organization ID if user belongs to an organization, null otherwise
     */
    fun findUserOrganization(username: String): Long?
} 
 