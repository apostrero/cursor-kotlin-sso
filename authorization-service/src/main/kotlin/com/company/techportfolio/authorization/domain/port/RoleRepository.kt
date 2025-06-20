package com.company.techportfolio.authorization.domain.port

import com.company.techportfolio.shared.domain.model.Role
import com.company.techportfolio.shared.domain.model.Permission

/**
 * Repository port for role data access operations.
 * 
 * This interface defines the contract for managing roles within the authorization
 * system. Roles represent collections of permissions that can be assigned to users,
 * providing a hierarchical approach to authorization management.
 * 
 * The repository handles CRUD operations for roles as well as specialized queries
 * for role-permission relationships and user-role assignments. This abstraction
 * allows the domain layer to work with roles without being coupled to specific
 * persistence technologies.
 * 
 * Key responsibilities:
 * - Role lifecycle management (create, read, update, delete)
 * - Role-permission relationship queries
 * - User-role assignment queries
 * - Role-based authorization support
 * 
 * @author Technology Portfolio Team
 * @since 1.0.0
 */
interface RoleRepository {
    
    /**
     * Finds a role by its unique identifier.
     * 
     * @param id The unique role identifier
     * @return Role object if found, null otherwise
     */
    fun findById(id: Long): Role?
    
    /**
     * Finds a role by its name.
     * 
     * Role names should be unique within the system to avoid ambiguity
     * in role assignments and permission resolution.
     * 
     * @param name The role name to search for
     * @return Role object if found, null otherwise
     */
    fun findByName(name: String): Role?
    
    /**
     * Retrieves all roles in the system.
     * 
     * This method is typically used for administrative purposes or
     * when building role selection interfaces.
     * 
     * @return List of all roles, empty if no roles exist
     */
    fun findAll(): List<Role>
    
    /**
     * Persists a role to the repository.
     * 
     * This method handles both creation of new roles and updates to
     * existing roles based on the presence of an ID.
     * 
     * @param role The role to save
     * @return The saved role with any generated identifiers populated
     */
    fun save(role: Role): Role
    
    /**
     * Deletes a role from the repository.
     * 
     * This operation should also handle cleanup of role assignments
     * to maintain referential integrity.
     * 
     * @param id The ID of the role to delete
     * @return true if the role was successfully deleted, false otherwise
     */
    fun delete(id: Long): Boolean
    
    /**
     * Retrieves all roles assigned to a specific user.
     * 
     * This method is essential for authorization decisions as it determines
     * which roles (and their associated permissions) apply to a user.
     * 
     * @param username The username to query roles for
     * @return List of roles assigned to the user, empty if no roles assigned
     */
    fun findRolesByUser(username: String): List<Role>
    
    /**
     * Retrieves all permissions associated with a specific role.
     * 
     * This method resolves the many-to-many relationship between roles
     * and permissions, providing the complete set of permissions granted
     * by a particular role.
     * 
     * @param roleName The name of the role to query permissions for
     * @return List of permissions granted by the role, empty if no permissions
     */
    fun findPermissionsByRole(roleName: String): List<Permission>
} 