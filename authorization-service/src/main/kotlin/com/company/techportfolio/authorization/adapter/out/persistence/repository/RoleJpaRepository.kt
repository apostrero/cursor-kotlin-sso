package com.company.techportfolio.authorization.adapter.out.persistence.repository

import com.company.techportfolio.authorization.adapter.out.persistence.entity.RoleEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * Role JPA Repository - Role Entity Data Access Layer
 *
 * This repository interface provides data access operations for role entities in the
 * authorization service. It supports role management operations and role-user relationship
 * queries essential for the authorization process.
 *
 * ## Key Features:
 * - Role lookup by name with optional active status filtering
 * - User-role relationship queries for authorization decisions
 * - Support for role hierarchy and permission inheritance
 *
 * ## Custom Query Methods:
 * - **findByName**: Locates roles by their unique name
 * - **findByNameAndIsActiveTrue**: Finds only active roles by name
 * - **findRolesByUsername**: Retrieves all active roles assigned to a user
 *
 * ## Usage Example:
 * ```kotlin
 * val role = roleRepository.findByNameAndIsActiveTrue("ADMIN")
 * val userRoles = roleRepository.findRolesByUsername("john.doe")
 * ```
 *
 * @author Technology Portfolio Team
 * @version 1.0
 * @since 1.0
 */
@Repository
interface RoleJpaRepository : JpaRepository<RoleEntity, Long> {
    
    /**
     * Finds a role by its name.
     *
     * @param name The unique role name to search for
     * @return The role entity if found, null otherwise
     */
    fun findByName(name: String): RoleEntity?
    
    /**
     * Finds an active role by its name.
     *
     * This method filters results to only return roles where isActive is true,
     * ensuring that deactivated roles are not included in authorization decisions.
     *
     * @param name The unique role name to search for
     * @return The active role entity if found, null otherwise
     */
    fun findByNameAndIsActiveTrue(name: String): RoleEntity?
    
    /**
     * Finds all active roles assigned to a specific user.
     *
     * This method joins user and role entities to retrieve the complete set of
     * active roles for a user, which is essential for authorization decisions.
     * Only active roles are returned to ensure deactivated roles don't grant permissions.
     *
     * @param username The username to find roles for
     * @return List of active role entities assigned to the user
     */
    @Query("SELECT r FROM RoleEntity r JOIN r.users u WHERE u.username = :username AND r.isActive = true")
    fun findRolesByUsername(@Param("username") username: String): List<RoleEntity>
} 