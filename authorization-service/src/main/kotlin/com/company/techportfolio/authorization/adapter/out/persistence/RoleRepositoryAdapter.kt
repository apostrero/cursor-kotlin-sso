package com.company.techportfolio.authorization.adapter.out.persistence

import com.company.techportfolio.authorization.domain.port.RoleRepository
import com.company.techportfolio.shared.domain.model.Role
import com.company.techportfolio.shared.domain.model.Permission
import com.company.techportfolio.authorization.adapter.out.persistence.repository.RoleJpaRepository
import org.springframework.stereotype.Component

/**
 * Role Repository Adapter - Persistence Layer Implementation
 *
 * This adapter implements the RoleRepository domain port to provide persistence operations
 * for roles in the authorization service. It follows the hexagonal architecture pattern by
 * acting as a bridge between the domain layer and the JPA persistence infrastructure.
 *
 * ## Key Responsibilities:
 * - Implements CRUD operations for roles
 * - Handles entity-to-domain model mapping and vice versa
 * - Manages user-role relationship queries
 * - Provides role-permission relationship resolution
 *
 * ## Architecture:
 * - **Domain Port**: RoleRepository (implemented interface)
 * - **Infrastructure**: RoleJpaRepository (JPA repository dependency)
 * - **Pattern**: Adapter pattern for infrastructure abstraction
 *
 * ## Role-Permission Model:
 * Roles serve as containers for permissions, implementing the RBAC (Role-Based Access Control)
 * pattern where users are assigned roles, and roles contain permissions.
 *
 * ## Usage Example:
 * ```kotlin
 * val role = roleAdapter.findByName("ADMIN")
 * val userRoles = roleAdapter.findRolesByUser("john.doe")
 * val rolePermissions = roleAdapter.findPermissionsByRole("PORTFOLIO_MANAGER")
 * ```
 *
 * @param roleJpaRepository JPA repository for role entity operations
 * @author Technology Portfolio Team
 * @version 1.0
 * @since 1.0
 */
@Component
class RoleRepositoryAdapter(
    private val roleJpaRepository: RoleJpaRepository
) : RoleRepository {

    /**
     * Finds a role by its unique identifier.
     *
     * @param id The unique identifier of the role
     * @return The role domain model if found, null otherwise
     */
    override fun findById(id: Long): Role? {
        return roleJpaRepository.findById(id).orElse(null)?.toDomain()
    }

    /**
     * Finds a role by its name.
     *
     * Role names are unique identifiers within the system and are used for
     * role-based authorization decisions.
     *
     * @param name The unique name of the role (e.g., "ADMIN", "USER", "PORTFOLIO_MANAGER")
     * @return The role domain model if found, null otherwise
     */
    override fun findByName(name: String): Role? {
        return roleJpaRepository.findByName(name)?.toDomain()
    }

    /**
     * Retrieves all roles from the database.
     *
     * @return List of all role domain models
     */
    override fun findAll(): List<Role> {
        return roleJpaRepository.findAll().map { it.toDomain() }
    }

    /**
     * Saves a role to the database.
     *
     * This method handles both creation of new roles and updates to existing ones.
     * The JPA repository determines whether to insert or update based on the entity's ID.
     *
     * Note: The current implementation has a limitation in the toEntity() method where
     * permissions are set to emptySet(). In a production environment, this would need
     * proper handling of the role-permission relationship.
     *
     * @param role The role domain model to save
     * @return The saved role domain model with updated metadata
     */
    override fun save(role: Role): Role {
        val entity = role.toEntity()
        val savedEntity = roleJpaRepository.save(entity)
        return savedEntity.toDomain()
    }

    /**
     * Deletes a role by its unique identifier.
     *
     * @param id The unique identifier of the role to delete
     * @return true if the role was deleted, false if it didn't exist
     */
    override fun delete(id: Long): Boolean {
        return if (roleJpaRepository.existsById(id)) {
            roleJpaRepository.deleteById(id)
            true
        } else {
            false
        }
    }

    /**
     * Finds all roles assigned to a specific user.
     *
     * This method retrieves roles through the user-role relationship, which is
     * essential for determining a user's authorization scope.
     *
     * @param username The username to find roles for
     * @return List of role domain models assigned to the user
     */
    override fun findRolesByUser(username: String): List<Role> {
        return roleJpaRepository.findRolesByUsername(username)
            .map { it.toDomain() }
    }

    /**
     * Finds all permissions associated with a specific role.
     *
     * This method resolves the role-permission relationship to determine what
     * permissions are granted by a particular role. This is useful for role
     * management and authorization decision making.
     *
     * @param roleName The name of the role to find permissions for
     * @return List of permission domain models associated with the role
     */
    override fun findPermissionsByRole(roleName: String): List<Permission> {
        val role = roleJpaRepository.findByName(roleName)
        return role?.permissions?.map { it.toDomain() } ?: emptyList()
    }

    /**
     * Converts a role entity to a domain model.
     *
     * This extension function handles the mapping from JPA entity to domain model,
     * including the conversion of associated permissions. It ensures proper separation
     * between persistence and domain layers.
     *
     * @return The corresponding role domain model with all permissions
     */
    private fun com.company.techportfolio.authorization.adapter.out.persistence.entity.RoleEntity.toDomain(): Role {
        return Role(
            id = this.id,
            name = this.name,
            description = this.description,
            isActive = this.isActive,
            createdAt = this.createdAt,
            permissions = this.permissions.map { it.toDomain() }.toSet()
        )
    }

    /**
     * Converts a permission entity to a domain model.
     *
     * This extension function is used within role conversion to properly map
     * associated permissions from entities to domain models.
     *
     * @return The corresponding permission domain model
     */
    private fun com.company.techportfolio.authorization.adapter.out.persistence.entity.PermissionEntity.toDomain(): Permission {
        return Permission(
            id = this.id,
            name = this.name,
            description = this.description,
            resource = this.resource,
            action = this.action,
            isActive = this.isActive,
            createdAt = this.createdAt
        )
    }

    /**
     * Converts a role domain model to an entity.
     *
     * This extension function handles the mapping from domain model to JPA entity,
     * preparing the data for persistence operations.
     *
     * **Note**: The current implementation sets permissions to emptySet() which is
     * a limitation that would need to be addressed in a production environment.
     * Proper role-permission relationship handling would require additional logic
     * to manage the bidirectional relationship correctly.
     *
     * @return The corresponding role entity
     */
    private fun Role.toEntity(): com.company.techportfolio.authorization.adapter.out.persistence.entity.RoleEntity {
        return com.company.techportfolio.authorization.adapter.out.persistence.entity.RoleEntity(
            id = this.id,
            name = this.name,
            description = this.description,
            isActive = this.isActive,
            createdAt = this.createdAt,
            permissions = emptySet() // This would need to be handled properly in a real implementation
        )
    }
} 