package com.company.techportfolio.shared.domain.model

import java.time.LocalDateTime

data class Organization(
    val id: Long? = null,
    val name: String,
    val description: String? = null,
    val isActive: Boolean = true,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime? = null,
    val parentOrganizationId: Long? = null
) 