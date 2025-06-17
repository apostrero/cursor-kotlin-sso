package com.company.techportfolio.shared.domain.model

import java.time.LocalDateTime

data class Role(
    val id: Long? = null,
    val name: String,
    val description: String? = null,
    val isActive: Boolean = true,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val permissions: Set<Permission> = emptySet()
) 