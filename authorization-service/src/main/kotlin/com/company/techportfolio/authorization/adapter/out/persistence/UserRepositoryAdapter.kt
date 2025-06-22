package com.company.techportfolio.authorization.adapter.out.persistence

import com.company.techportfolio.authorization.adapter.out.persistence.repository.PermissionJpaRepository
import com.company.techportfolio.authorization.adapter.out.persistence.repository.RoleJpaRepository
import com.company.techportfolio.authorization.adapter.out.persistence.repository.UserJpaRepository
import com.company.techportfolio.authorization.domain.port.UserRepository
import com.company.techportfolio.shared.domain.model.User
import org.springframework.stereotype.Component

/**
 * Persistence adapter implementation for user repository operations.
 *
 * This adapter implements the UserRepository port by delegating to JPA repositories
 * and handling the mapping between domain models and persistence entities. It follows
 * the hexagonal architecture pattern by isolating persistence concerns from the domain layer.
 *
 * The adapter coordinates between multiple JPA repositories to provide comprehensive
 * user data access capabilities, including user information, roles, permissions,
 * and organizational context. It also handles the complex mapping between JPA entities
 * and domain models.
 *
 * Key responsibilities:
 * - User data retrieval and mapping
 * - Role and permission resolution for users
 * - Entity-to-domain model transformation
 * - Coordination between multiple JPA repositories
 * - User status and organization queries
 *
 * @property userJpaRepository JPA repository for user entity operations
 * @property roleJpaRepository JPA repository for role entity operations
 * @property permissionJpaRepository JPA repository for permission entity operations
 *
 * @author Technology Portfolio Team
 * @since 1.0.0
 */
@Component
class UserRepositoryAdapter(
    /** JPA repository for user entity operations */
    private val userJpaRepository: UserJpaRepository,
    /** JPA repository for role entity operations */
    private val roleJpaRepository: RoleJpaRepository,
    /** JPA repository for permission entity operations */
    private val permissionJpaRepository: PermissionJpaRepository
) : UserRepository {

    /**
     * Finds a user by their unique identifier.
     *
     * This method delegates to the username-based lookup since the domain
     * uses username as the primary identifier for users.
     *
     * @param username The unique username identifier
     * @return User domain object if found, null otherwise
     */
    override fun findById(username: String): User? {
        return userJpaRepository.findByUsername(username)?.toDomain()
    }

    /**
     * Finds a user by their username.
     *
     * This method retrieves a user entity from the database and converts
     * it to the domain model representation.
     *
     * @param username The username to search for
     * @return User domain object if found, null otherwise
     */
    override fun findByUsername(username: String): User? {
        return userJpaRepository.findByUsername(username)?.toDomain()
    }

    /**
     * Finds a user with their complete roles and permissions loaded.
     *
     * Currently delegates to the standard user lookup. In a more complex
     * implementation, this could use eager fetching or custom queries
     * to optimize the loading of role and permission data.
     *
     * @param username The username to search for
     * @return User domain object with roles and permissions loaded, null if not found
     */
    override fun findUserWithRolesAndPermissions(username: String): User? {
        return userJpaRepository.findByUsername(username)?.toDomain()
    }

    /**
     * Retrieves all permissions assigned to a specific user.
     *
     * This method queries permissions through the permission repository and
     * converts them to permission strings in the format "resource:action".
     *
     * @param username The username to query permissions for
     * @return List of permission strings, empty if user has no permissions
     */
    override fun findUserPermissions(username: String): List<String> {
        return permissionJpaRepository.findPermissionsByUsername(username)
            .map { it.getPermissionString() }
    }

    /**
     * Retrieves all roles assigned to a specific user.
     *
     * This method queries roles through the role repository and extracts
     * the role names for easy consumption by the domain layer.
     *
     * @param username The username to query roles for
     * @return List of role names, empty if user has no roles
     */
    override fun findUserRoles(username: String): List<String> {
        return roleJpaRepository.findRolesByUsername(username)
            .map { it.name }
    }

    /**
     * Checks if a user account is currently active.
     *
     * This method delegates to the JPA repository for user status checking,
     * returning false if the user doesn't exist or is inactive.
     *
     * @param username The username to check status for
     * @return true if the user is active, false otherwise
     */
    override fun isUserActive(username: String): Boolean {
        return userJpaRepository.isUserActive(username) ?: false
    }

    /**
     * Retrieves the organization ID associated with a user.
     *
     * This method queries the user's organizational affiliation through
     * the JPA repository.
     *
     * @param username The username to query organization for
     * @return Organization ID if user belongs to an organization, null otherwise
     */
    override fun findUserOrganization(username: String): Long? {
        return userJpaRepository.findOrganizationIdByUsername(username)
    }

    /**
     * Extension function for mapping UserEntity to User domain model.
     *
     * This private extension function handles the transformation from
     * JPA entity to domain model, including the mapping of nested
     * role and permission entities.
     *
     * @return User domain object
     */
    private fun com.company.techportfolio.authorization.adapter.out.persistence.entity.UserEntity.toDomain(): User {
        return User(
            id = this.id,
            username = this.username,
            email = this.email,
            firstName = this.firstName,
            lastName = this.lastName,
            isActive = this.isActive,
            isEnabled = this.isEnabled,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
            lastLoginAt = this.lastLoginAt,
            organizationId = this.organizationId,
            roles = this.roles.map { it.toDomain() }.toSet()
        )
    }

    /**
     * Extension function for mapping RoleEntity to Role domain model.
     *
     * This private extension function handles the transformation from
     * JPA role entity to domain model, including the mapping of
     * associated permission entities.
     *
     * @return Role domain object
     */
    private fun com.company.techportfolio.authorization.adapter.out.persistence.entity.RoleEntity.toDomain(): com.company.techportfolio.shared.domain.model.Role {
        return com.company.techportfolio.shared.domain.model.Role(
            id = this.id,
            name = this.name,
            description = this.description,
            isActive = this.isActive,
            createdAt = this.createdAt,
            permissions = this.permissions.map { it.toDomain() }.toSet()
        )
    }

    /**
     * Extension function for mapping PermissionEntity to Permission domain model.
     *
     * This private extension function handles the transformation from
     * JPA permission entity to domain model.
     *
     * @return Permission domain object
     */
    private fun com.company.techportfolio.authorization.adapter.out.persistence.entity.PermissionEntity.toDomain(): com.company.techportfolio.shared.domain.model.Permission {
        return com.company.techportfolio.shared.domain.model.Permission(
            id = this.id,
            name = this.name,
            description = this.description,
            resource = this.resource,
            action = this.action,
            isActive = this.isActive,
            createdAt = this.createdAt
        )
    }
} 