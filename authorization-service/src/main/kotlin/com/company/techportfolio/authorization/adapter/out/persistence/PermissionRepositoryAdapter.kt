package com.company.techportfolio.authorization.adapter.out.persistence

import com.company.techportfolio.authorization.adapter.out.persistence.repository.PermissionJpaRepository
import com.company.techportfolio.authorization.domain.port.PermissionRepository
import com.company.techportfolio.shared.domain.model.Permission
import org.springframework.stereotype.Component

/**
 * Permission Repository Adapter - Persistence Layer Implementation
 *
 * This adapter implements the PermissionRepository domain port to provide persistence operations
 * for permissions in the authorization service. It follows the hexagonal architecture pattern by
 * acting as a bridge between the domain layer and the JPA persistence infrastructure.
 *
 * ## Key Responsibilities:
 * - Implements CRUD operations for permissions
 * - Handles entity-to-domain model mapping and vice versa
 * - Provides permission-based authorization queries
 * - Manages user-permission relationship queries
 *
 * ## Architecture:
 * - **Domain Port**: PermissionRepository (implemented interface)
 * - **Infrastructure**: PermissionJpaRepository (JPA repository dependency)
 * - **Pattern**: Adapter pattern for infrastructure abstraction
 *
 * ## Usage Example:
 * ```kotlin
 * val permission = permissionAdapter.findByResourceAndAction("portfolio", "read")
 * val userPermissions = permissionAdapter.findPermissionsByUser("john.doe")
 * val hasAccess = permissionAdapter.hasPermission("john.doe", "portfolio", "write")
 * ```
 *
 * @param permissionJpaRepository JPA repository for permission entity operations
 * @author Technology Portfolio Team
 * @version 1.0
 * @since 1.0
 */
@Component
class PermissionRepositoryAdapter(
    private val permissionJpaRepository: PermissionJpaRepository
) : PermissionRepository {

    /**
     * Finds a permission by its unique identifier.
     *
     * @param id The unique identifier of the permission
     * @return The permission domain model if found, null otherwise
     */
    override fun findById(id: Long): Permission? {
        return permissionJpaRepository.findById(id).orElse(null)?.toDomain()
    }

    /**
     * Finds a permission by its name.
     *
     * Note: This method currently returns null as the findByName query is not implemented
     * in the JPA repository. This is a placeholder for future implementation.
     *
     * @param name The name of the permission to find
     * @return The permission domain model if found, null otherwise
     */
    override fun findByName(name: String): Permission? {
        // This method doesn't exist in the JPA repository, so we'll need to implement it
        // For now, return null as it's not used in the current implementation
        return null
    }

    /**
     * Retrieves all permissions from the database.
     *
     * @return List of all permission domain models
     */
    override fun findAll(): List<Permission> {
        return permissionJpaRepository.findAll().map { it.toDomain() }
    }

    /**
     * Saves a permission to the database.
     *
     * This method handles both creation of new permissions and updates to existing ones.
     * The JPA repository determines whether to insert or update based on the entity's ID.
     *
     * @param permission The permission domain model to save
     * @return The saved permission domain model with updated metadata
     */
    override fun save(permission: Permission): Permission {
        val entity = permission.toEntity()
        val savedEntity = permissionJpaRepository.save(entity)
        return savedEntity.toDomain()
    }

    /**
     * Deletes a permission by its unique identifier.
     *
     * @param id The unique identifier of the permission to delete
     * @return true if the permission was deleted, false if it didn't exist
     */
    override fun delete(id: Long): Boolean {
        return if (permissionJpaRepository.existsById(id)) {
            permissionJpaRepository.deleteById(id)
            true
        } else {
            false
        }
    }

    /**
     * Finds a permission by resource and action combination.
     *
     * This method is used to locate specific permissions based on the resource they protect
     * and the action they authorize (e.g., "portfolio" + "read").
     *
     * @param resource The resource identifier (e.g., "portfolio", "user", "admin")
     * @param action The action identifier (e.g., "read", "write", "delete")
     * @return The permission domain model if found, null otherwise
     */
    override fun findByResourceAndAction(resource: String, action: String): Permission? {
        return permissionJpaRepository.findByResourceAndAction(resource, action)?.toDomain()
    }

    /**
     * Finds all permissions assigned to a specific user.
     *
     * This method retrieves permissions through the user's role assignments, following
     * the user -> roles -> permissions relationship chain.
     *
     * @param username The username to find permissions for
     * @return List of permission domain models assigned to the user
     */
    override fun findPermissionsByUser(username: String): List<Permission> {
        return permissionJpaRepository.findPermissionsByUsername(username)
            .map { it.toDomain() }
    }

    /**
     * Checks if a user has a specific permission for a resource and action.
     *
     * This is a convenience method for authorization decisions that returns a boolean
     * instead of the full permission object. It's optimized for permission checking
     * scenarios where only the authorization result is needed.
     *
     * @param username The username to check permissions for
     * @param resource The resource identifier to check access to
     * @param action The action identifier to check authorization for
     * @return true if the user has the specified permission, false otherwise
     */
    override fun hasPermission(username: String, resource: String, action: String): Boolean {
        return permissionJpaRepository.hasPermission(username, resource, action)
    }

    /**
     * Converts a permission entity to a domain model.
     *
     * This extension function handles the mapping from JPA entity to domain model,
     * ensuring proper separation between persistence and domain layers.
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
     * Converts a permission domain model to an entity.
     *
     * This extension function handles the mapping from domain model to JPA entity,
     * preparing the data for persistence operations.
     *
     * @return The corresponding permission entity
     */
    private fun Permission.toEntity(): com.company.techportfolio.authorization.adapter.out.persistence.entity.PermissionEntity {
        return com.company.techportfolio.authorization.adapter.out.persistence.entity.PermissionEntity(
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