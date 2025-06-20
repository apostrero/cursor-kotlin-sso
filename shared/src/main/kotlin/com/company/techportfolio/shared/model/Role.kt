package com.company.techportfolio.shared.model

import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

/**
 * JPA entity representing a role in the authorization system.
 * 
 * This entity maps to the "roles" database table and represents roles that
 * can be assigned to users. Roles are collections of permissions that provide
 * a hierarchical approach to authorization management.
 * 
 * @property id Unique identifier for the role (auto-generated)
 * @property name Unique name of the role (required)
 * @property description Optional description of the role's purpose
 * @property isActive Whether the role is currently active
 * @property createdAt Timestamp when the role was created
 * @property permissions Set of permissions granted by this role
 * 
 * @author Technology Portfolio Team
 * @since 1.0.0
 */
@Entity
@Table(name = "roles")
data class Role(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @field:NotBlank(message = "Role name is required")
    @field:Size(max = 100, message = "Role name must not exceed 100 characters")
    @Column(unique = true, nullable = false)
    val name: String,

    @field:Size(max = 500, message = "Description must not exceed 500 characters")
    @Column
    val description: String? = null,

    @Column(nullable = false)
    val isActive: Boolean = true,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "role_permissions",
        joinColumns = [JoinColumn(name = "role_id")],
        inverseJoinColumns = [JoinColumn(name = "permission_id")]
    )
    val permissions: Set<Permission> = emptySet()
) 