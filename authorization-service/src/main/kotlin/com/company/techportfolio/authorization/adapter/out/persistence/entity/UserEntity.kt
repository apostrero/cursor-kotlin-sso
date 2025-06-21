package com.company.techportfolio.authorization.adapter.out.persistence.entity

import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * JPA entity representing a user in the authorization system.
 *
 * This entity maps to the "users" database table and represents a user account
 * within the technology portfolio system. It includes basic user information,
 * account status, organizational affiliation, and relationships to roles.
 *
 * The entity follows JPA best practices with proper mapping annotations,
 * lazy loading for relationships, and appropriate constraints. It serves as
 * the foundation for user-based authorization decisions.
 *
 * Database mapping:
 * - Table: users
 * - Primary key: id (auto-generated)
 * - Unique constraints: username, email
 * - Relationships: Many-to-many with roles
 *
 * @property id Unique identifier for the user (auto-generated)
 * @property username Unique username for the user account
 * @property email Unique email address for the user
 * @property firstName User's first name
 * @property lastName User's last name
 * @property isActive Whether the user account is active
 * @property isEnabled Whether the user account is enabled
 * @property createdAt Timestamp when the user was created
 * @property updatedAt Timestamp when the user was last updated
 * @property lastLoginAt Timestamp of the user's last login
 * @property organizationId ID of the organization the user belongs to
 * @property roles Set of roles assigned to the user
 *
 * @author Technology Portfolio Team
 * @since 1.0.0
 */
@Entity
@Table(name = "users")
data class UserEntity(
    /** Unique identifier for the user (auto-generated) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    /** Unique username for the user account */
    @Column(unique = true, nullable = false)
    val username: String,

    /** Unique email address for the user */
    @Column(unique = true, nullable = false)
    val email: String,

    /** User's first name */
    @Column(name = "first_name", nullable = false)
    val firstName: String,

    /** User's last name */
    @Column(name = "last_name", nullable = false)
    val lastName: String,

    /** Whether the user account is active */
    @Column(name = "is_active", nullable = false)
    val isActive: Boolean = true,

    /** Whether the user account is enabled */
    @Column(name = "is_enabled", nullable = false)
    val isEnabled: Boolean = true,

    /** Timestamp when the user was created */
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    /** Timestamp when the user was last updated */
    @Column(name = "updated_at")
    val updatedAt: LocalDateTime? = null,

    /** Timestamp of the user's last login */
    @Column(name = "last_login_at")
    val lastLoginAt: LocalDateTime? = null,

    /** ID of the organization the user belongs to */
    @Column(name = "organization_id")
    val organizationId: Long? = null,

    /** Set of roles assigned to the user */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_roles",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "role_id")]
    )
    val roles: Set<RoleEntity> = emptySet()
) 