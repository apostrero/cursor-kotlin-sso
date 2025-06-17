package com.company.techportfolio.authorization.domain.model

data class UserPermissions(
    val username: String,
    val permissions: List<String> = emptyList(),
    val roles: List<String> = emptyList(),
    val organizationId: Long? = null,
    val isActive: Boolean = false
) 