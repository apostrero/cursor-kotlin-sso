package com.company.techportfolio.authorization.adapter.out.persistence.repository

import com.company.techportfolio.authorization.adapter.out.persistence.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * User JPA Repository - User Entity Data Access Layer
 *
 * This repository interface provides data access operations for user entities in the
 * authorization service. It extends Spring Data JPA's JpaRepository to inherit standard
 * CRUD operations and defines custom query methods for user-specific operations.
 *
 * ## Key Features:
 * - Standard CRUD operations through JpaRepository inheritance
 * - Username-based user lookup with optional active status filtering
 * - Organization ID retrieval for user context
 * - User activation status checking
 *
 * ## Custom Query Methods:
 * - **findByUsername**: Locates users by their unique username
 * - **findByUsernameAndIsActiveTrue**: Finds only active users by username
 * - **findOrganizationIdByUsername**: Retrieves user's organization context
 * - **isUserActive**: Checks if a specific user is currently active
 *
 * ## Usage Example:
 * ```kotlin
 * val user = userRepository.findByUsernameAndIsActiveTrue("john.doe")
 * val orgId = userRepository.findOrganizationIdByUsername("john.doe")
 * val isActive = userRepository.isUserActive("john.doe")
 * ```
 *
 * @author Technology Portfolio Team
 * @version 1.0
 * @since 1.0
 */
@Repository
interface UserJpaRepository : JpaRepository<UserEntity, Long> {

    /**
     * Finds a user by their username.
     *
     * @param username The unique username to search for
     * @return The user entity if found, null otherwise
     */
    fun findByUsername(username: String): UserEntity?

    /**
     * Finds an active user by their username.
     *
     * This method filters results to only return users where isActive is true,
     * ensuring that deactivated users are not included in authorization decisions.
     *
     * @param username The unique username to search for
     * @return The active user entity if found, null otherwise
     */
    fun findByUsernameAndIsActiveTrue(username: String): UserEntity?

    /**
     * Retrieves the organization ID for a specific user.
     *
     * This method is useful for organization-scoped authorization decisions
     * where permissions may be limited to specific organizational contexts.
     *
     * @param username The username to find the organization ID for
     * @return The organization ID if the user exists, null otherwise
     */
    @Query("SELECT u.organizationId FROM UserEntity u WHERE u.username = :username")
    fun findOrganizationIdByUsername(@Param("username") username: String): Long?

    /**
     * Checks if a user is currently active.
     *
     * This is a lightweight method for quickly determining user activation status
     * without loading the full user entity.
     *
     * @param username The username to check activation status for
     * @return true if the user is active, false if inactive, null if user doesn't exist
     */
    @Query("SELECT u.isActive FROM UserEntity u WHERE u.username = :username")
    fun isUserActive(@Param("username") username: String): Boolean?
} 