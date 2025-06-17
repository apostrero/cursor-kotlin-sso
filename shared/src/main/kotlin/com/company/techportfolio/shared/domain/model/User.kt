package com.company.techportfolio.shared.domain.model

import java.time.LocalDateTime

data class User(
    val id: Long? = null,
    val username: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val isActive: Boolean = true,
    val isEnabled: Boolean = true,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime? = null,
    val lastLoginAt: LocalDateTime? = null,
    val organizationId: Long? = null,
    val roles: Set<Role> = emptySet()
) {
    fun getFullName(): String = "$firstName $lastName"
    
    fun hasRole(roleName: String): Boolean = roles.any { it.name == roleName }
    
    fun hasAnyRole(roleNames: List<String>): Boolean = roles.any { it.name in roleNames }
    
    fun isActiveAndEnabled(): Boolean = isActive && isEnabled
} 