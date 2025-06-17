package com.company.techportfolio.shared.domain.model

import java.time.LocalDateTime

data class Permission(
    val id: Long? = null,
    val name: String,
    val description: String? = null,
    val resource: String,
    val action: String,
    val isActive: Boolean = true,
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    fun getPermissionString(): String = "$resource:$action"
} 