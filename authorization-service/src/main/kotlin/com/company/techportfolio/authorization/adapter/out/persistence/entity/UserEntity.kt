package com.company.techportfolio.authorization.adapter.out.persistence.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "users")
data class UserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(unique = true, nullable = false)
    val username: String,
    
    @Column(unique = true, nullable = false)
    val email: String,
    
    @Column(name = "first_name", nullable = false)
    val firstName: String,
    
    @Column(name = "last_name", nullable = false)
    val lastName: String,
    
    @Column(name = "is_active", nullable = false)
    val isActive: Boolean = true,
    
    @Column(name = "is_enabled", nullable = false)
    val isEnabled: Boolean = true,
    
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "updated_at")
    val updatedAt: LocalDateTime? = null,
    
    @Column(name = "last_login_at")
    val lastLoginAt: LocalDateTime? = null,
    
    @Column(name = "organization_id")
    val organizationId: Long? = null,
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_roles",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "role_id")]
    )
    val roles: Set<RoleEntity> = emptySet()
)

@Entity
@Table(name = "roles")
data class RoleEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(unique = true, nullable = false)
    val name: String,
    
    @Column
    val description: String? = null,
    
    @Column(name = "is_active", nullable = false)
    val isActive: Boolean = true,
    
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "role_permissions",
        joinColumns = [JoinColumn(name = "role_id")],
        inverseJoinColumns = [JoinColumn(name = "permission_id")]
    )
    val permissions: Set<PermissionEntity> = emptySet(),
    
    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    val users: Set<UserEntity> = emptySet()
)

@Entity
@Table(name = "permissions")
data class PermissionEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(unique = true, nullable = false)
    val name: String,
    
    @Column
    val description: String? = null,
    
    @Column(nullable = false)
    val resource: String,
    
    @Column(nullable = false)
    val action: String,
    
    @Column(name = "is_active", nullable = false)
    val isActive: Boolean = true,
    
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @ManyToMany(mappedBy = "permissions", fetch = FetchType.LAZY)
    val roles: Set<RoleEntity> = emptySet()
) {
    fun getPermissionString(): String = "$resource:$action"
} 