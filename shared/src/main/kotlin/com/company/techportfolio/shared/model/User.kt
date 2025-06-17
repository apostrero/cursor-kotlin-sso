package com.company.techportfolio.shared.model

import jakarta.persistence.*
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @field:NotBlank(message = "Username is required")
    @field:Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Column(unique = true, nullable = false)
    val username: String,

    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Email should be valid")
    @Column(unique = true, nullable = false)
    val email: String,

    @field:NotBlank(message = "First name is required")
    @field:Size(max = 100, message = "First name must not exceed 100 characters")
    @Column(nullable = false)
    val firstName: String,

    @field:NotBlank(message = "Last name is required")
    @field:Size(max = 100, message = "Last name must not exceed 100 characters")
    @Column(nullable = false)
    val lastName: String,

    @Column(nullable = false)
    val isActive: Boolean = true,

    @Column(nullable = false)
    val isEnabled: Boolean = true,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime? = null,

    @Column(name = "last_login_at")
    val lastLoginAt: LocalDateTime? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    val organization: Organization? = null,

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_roles",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "role_id")]
    )
    val roles: Set<Role> = emptySet()
) {
    fun getFullName(): String = "$firstName $lastName"

    fun hasRole(roleName: String): Boolean = roles.any { it.name == roleName }

    fun hasAnyRole(roleNames: List<String>): Boolean = roles.any { it.name in roleNames }
}

@Entity
@Table(name = "organizations")
data class Organization(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @field:NotBlank(message = "Organization name is required")
    @field:Size(max = 200, message = "Organization name must not exceed 200 characters")
    @Column(nullable = false)
    val name: String,

    @field:Size(max = 500, message = "Description must not exceed 500 characters")
    @Column
    val description: String? = null,

    @Column(nullable = false)
    val isActive: Boolean = true,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_organization_id")
    val parentOrganization: Organization? = null
)

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

@Entity
@Table(name = "permissions")
data class Permission(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @field:NotBlank(message = "Permission name is required")
    @field:Size(max = 100, message = "Permission name must not exceed 100 characters")
    @Column(unique = true, nullable = false)
    val name: String,

    @field:Size(max = 500, message = "Description must not exceed 500 characters")
    @Column
    val description: String? = null,

    @field:NotBlank(message = "Resource is required")
    @field:Size(max = 100, message = "Resource must not exceed 100 characters")
    @Column(nullable = false)
    val resource: String,

    @field:NotBlank(message = "Action is required")
    @field:Size(max = 50, message = "Action must not exceed 50 characters")
    @Column(nullable = false)
    val action: String,

    @Column(nullable = false)
    val isActive: Boolean = true,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    fun getPermissionString(): String = "$resource:$action"
} 