package com.company.techportfolio.authorization.adapter.out.persistence.entity

import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * JPA entity representing a permission in the authorization system.
 * 
 * This entity maps to the "permissions" database table and represents a specific
 * permission that defines what actions can be performed on what resources.
 * Permissions are the atomic units of authorization control.
 * 
 * Each permission is defined by a resource-action combination, allowing for
 * fine-grained access control. The entity includes helper methods for working
 * with permission strings in the format "resource:action".
 * 
 * Database mapping:
 * - Table: permissions
 * - Primary key: id (auto-generated)
 * - Unique constraints: name
 * - Relationships: Many-to-many with roles
 * 
 * @property id Unique identifier for the permission (auto-generated)
 * @property name Unique name of the permission
 * @property description Optional description of the permission's purpose
 * @property resource The resource type this permission applies to
 * @property action The action this permission allows
 * @property isActive Whether the permission is currently active
 * @property createdAt Timestamp when the permission was created
 * @property roles Set of roles that include this permission
 * 
 * @author Technology Portfolio Team
 * @since 1.0.0
 */
@Entity
@Table(name = "permissions")
data class PermissionEntity(
    /** Unique identifier for the permission (auto-generated) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    /** Unique name of the permission */
    @Column(unique = true, nullable = false)
    val name: String,
    
    /** Optional description of the permission's purpose */
    @Column
    val description: String? = null,
    
    /** The resource type this permission applies to */
    @Column(nullable = false)
    val resource: String,
    
    /** The action this permission allows */
    @Column(nullable = false)
    val action: String,
    
    /** Whether the permission is currently active */
    @Column(name = "is_active", nullable = false)
    val isActive: Boolean = true,
    
    /** Timestamp when the permission was created */
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    /** Set of roles that include this permission */
    @ManyToMany(mappedBy = "permissions", fetch = FetchType.LAZY)
    val roles: Set<RoleEntity> = emptySet()
) {
    /**
     * Generates a permission string in the format "resource:action".
     * 
     * This method provides a standardized way to represent permissions
     * as strings, which is useful for authorization checks and logging.
     * 
     * @return Permission string in the format "resource:action"
     */
    fun getPermissionString(): String = "$resource:$action"
} 