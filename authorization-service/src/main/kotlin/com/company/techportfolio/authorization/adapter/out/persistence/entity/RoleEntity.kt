package com.company.techportfolio.authorization.adapter.out.persistence.entity

import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * JPA entity representing a role in the authorization system.
 *
 * This entity maps to the "roles" database table and represents a role that
 * can be assigned to users. Roles are collections of permissions that provide
 * a hierarchical approach to authorization management.
 *
 * The entity includes bidirectional relationships with both users and permissions,
 * allowing for efficient role-based access control queries and permission resolution.
 *
 * Database mapping:
 * - Table: roles
 * - Primary key: id (auto-generated)
 * - Unique constraints: name
 * - Relationships: Many-to-many with users and permissions
 *
 * @property id Unique identifier for the role (auto-generated)
 * @property name Unique name of the role
 * @property description Optional description of the role's purpose
 * @property isActive Whether the role is currently active
 * @property createdAt Timestamp when the role was created
 * @property permissions Set of permissions granted by this role
 * @property users Set of users assigned to this role
 *
 * @author Technology Portfolio Team
 * @since 1.0.0
 */
@Entity
@Table(name = "roles")
data class RoleEntity(
    /** Unique identifier for the role (auto-generated) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    /** Unique name of the role */
    @Column(unique = true, nullable = false)
    val name: String,

    /** Optional description of the role's purpose */
    @Column
    val description: String? = null,

    /** Whether the role is currently active */
    @Column(name = "is_active", nullable = false)
    val isActive: Boolean = true,

    /** Timestamp when the role was created */
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    /** Set of permissions granted by this role */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "role_permissions",
        joinColumns = [JoinColumn(name = "role_id")],
        inverseJoinColumns = [JoinColumn(name = "permission_id")]
    )
    val permissions: Set<PermissionEntity> = emptySet(),

    /** Set of users assigned to this role */
    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    val users: Set<UserEntity> = emptySet()
) 