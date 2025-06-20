package com.company.techportfolio.authorization.adapter.out.persistence.repository

import com.company.techportfolio.authorization.adapter.out.persistence.entity.PermissionEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * Permission JPA Repository - Permission Entity Data Access Layer
 *
 * This repository interface provides data access operations for permission entities in the
 * authorization service. It includes complex queries for permission resolution through
 * user-role-permission relationships, which are critical for authorization decisions.
 *
 * ## Key Features:
 * - Permission lookup by resource and action combinations
 * - User permission resolution through role relationships
 * - Optimized permission checking for authorization decisions
 * - Support for active status filtering across the permission hierarchy
 *
 * ## Custom Query Methods:
 * - **findByResourceAndAction**: Locates permissions by resource-action pairs
 * - **findByResourceAndActionAndIsActiveTrue**: Finds only active permissions
 * - **findPermissionsByUsername**: Resolves all user permissions through roles
 * - **hasPermission**: Optimized boolean check for specific permission authorization
 *
 * ## Authorization Flow:
 * The permission resolution follows this path:
 * User -> Roles -> Permissions, with active status filtering at each level.
 *
 * ## Usage Example:
 * ```kotlin
 * val permission = permissionRepository.findByResourceAndActionAndIsActiveTrue("portfolio", "read")
 * val userPermissions = permissionRepository.findPermissionsByUsername("john.doe")
 * val hasAccess = permissionRepository.hasPermission("john.doe", "portfolio", "write")
 * ```
 *
 * @author Technology Portfolio Team
 * @version 1.0
 * @since 1.0
 */
@Repository
interface PermissionJpaRepository : JpaRepository<PermissionEntity, Long> {
    
    /**
     * Finds a permission by resource and action combination.
     *
     * @param resource The resource identifier (e.g., "portfolio", "user", "admin")
     * @param action The action identifier (e.g., "read", "write", "delete")
     * @return The permission entity if found, null otherwise
     */
    fun findByResourceAndAction(resource: String, action: String): PermissionEntity?
    
    /**
     * Finds an active permission by resource and action combination.
     *
     * This method filters results to only return permissions where isActive is true,
     * ensuring that deactivated permissions are not included in authorization decisions.
     *
     * @param resource The resource identifier (e.g., "portfolio", "user", "admin")
     * @param action The action identifier (e.g., "read", "write", "delete")
     * @return The active permission entity if found, null otherwise
     */
    fun findByResourceAndActionAndIsActiveTrue(resource: String, action: String): PermissionEntity?
    
    /**
     * Finds all permissions assigned to a user through their roles.
     *
     * This complex query joins across the user-role-permission relationship to resolve
     * all permissions a user has been granted. It includes active status filtering at
     * all levels to ensure only currently valid permissions are returned.
     *
     * The query path is: User -> Roles -> Permissions, with constraints:
     * - User must exist
     * - Roles must be active (isActive = true)
     * - Permissions must be active (isActive = true)
     *
     * @param username The username to find permissions for
     * @return List of active permission entities accessible to the user
     */
    @Query("SELECT DISTINCT p FROM PermissionEntity p " +
           "JOIN p.roles r " +
           "JOIN r.users u " +
           "WHERE u.username = :username AND p.isActive = true AND r.isActive = true")
    fun findPermissionsByUsername(@Param("username") username: String): List<PermissionEntity>
    
    /**
     * Checks if a user has a specific permission for a resource and action.
     *
     * This is an optimized query that returns a boolean result instead of loading
     * full permission entities. It's designed for high-performance authorization
     * checks where only the access decision is needed.
     *
     * The query performs the same user-role-permission resolution as findPermissionsByUsername
     * but returns COUNT(p) > 0 for better performance in authorization scenarios.
     *
     * @param username The username to check permissions for
     * @param resource The resource identifier to check access to
     * @param action The action identifier to check authorization for
     * @return true if the user has the specified permission, false otherwise
     */
    @Query("SELECT COUNT(p) > 0 FROM PermissionEntity p " +
           "JOIN p.roles r " +
           "JOIN r.users u " +
           "WHERE u.username = :username AND p.resource = :resource AND p.action = :action " +
           "AND p.isActive = true AND r.isActive = true")
    fun hasPermission(
        @Param("username") username: String,
        @Param("resource") resource: String,
        @Param("action") action: String
    ): Boolean
} 