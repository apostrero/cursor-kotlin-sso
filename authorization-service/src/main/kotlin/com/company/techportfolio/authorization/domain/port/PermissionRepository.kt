package com.company.techportfolio.authorization.domain.port

import com.company.techportfolio.shared.domain.model.Permission

/**
 * Repository port for permission data access operations.
 *
 * This interface defines the contract for managing permissions within the
 * authorization system. Permissions represent specific actions that can be
 * performed on resources, forming the atomic units of authorization control.
 *
 * The repository provides both basic CRUD operations and specialized queries
 * for authorization decisions. It abstracts the persistence layer concerns
 * from the domain logic, allowing for flexible implementation strategies.
 *
 * Key responsibilities:
 * - Permission lifecycle management (create, read, update, delete)
 * - Resource-action based permission queries
 * - User permission resolution
 * - Authorization decision support
 *
 * @author Technology Portfolio Team
 * @since 1.0.0
 */
interface PermissionRepository {

    /**
     * Finds a permission by its unique identifier.
     *
     * @param id The unique permission identifier
     * @return Permission object if found, null otherwise
     */
    fun findById(id: Long): Permission?

    /**
     * Finds a permission by its name.
     *
     * Permission names should be unique and descriptive, typically following
     * a naming convention like "READ_PORTFOLIO" or "DELETE_TECHNOLOGY".
     *
     * @param name The permission name to search for
     * @return Permission object if found, null otherwise
     */
    fun findByName(name: String): Permission?

    /**
     * Retrieves all permissions in the system.
     *
     * This method is typically used for administrative purposes or
     * when building permission management interfaces.
     *
     * @return List of all permissions, empty if no permissions exist
     */
    fun findAll(): List<Permission>

    /**
     * Persists a permission to the repository.
     *
     * This method handles both creation of new permissions and updates to
     * existing permissions based on the presence of an ID.
     *
     * @param permission The permission to save
     * @return The saved permission with any generated identifiers populated
     */
    fun save(permission: Permission): Permission

    /**
     * Deletes a permission from the repository.
     *
     * This operation should also handle cleanup of role-permission assignments
     * to maintain referential integrity.
     *
     * @param id The ID of the permission to delete
     * @return true if the permission was successfully deleted, false otherwise
     */
    fun delete(id: Long): Boolean

    /**
     * Finds a permission by resource and action combination.
     *
     * This method supports fine-grained permission queries based on the
     * specific resource type and action being performed.
     *
     * @param resource The resource type (e.g., "portfolio", "technology")
     * @param action The action being performed (e.g., "READ", "WRITE", "DELETE")
     * @return Permission object if found, null otherwise
     */
    fun findByResourceAndAction(resource: String, action: String): Permission?

    /**
     * Retrieves all permissions assigned to a specific user.
     *
     * This includes both direct permissions and permissions inherited through
     * role assignments. The result provides a complete view of what a user
     * is authorized to do.
     *
     * @param username The username to query permissions for
     * @return List of permissions assigned to the user, empty if no permissions
     */
    fun findPermissionsByUser(username: String): List<Permission>

    /**
     * Checks if a user has a specific permission for a resource and action.
     *
     * This is a convenience method for authorization decisions that returns
     * a boolean result rather than requiring permission object comparisons.
     *
     * @param username The username to check permissions for
     * @param resource The resource being accessed
     * @param action The action being performed
     * @return true if the user has the required permission, false otherwise
     */
    fun hasPermission(username: String, resource: String, action: String): Boolean
} 