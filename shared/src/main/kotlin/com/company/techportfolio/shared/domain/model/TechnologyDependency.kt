package com.company.techportfolio.shared.domain.model

import java.time.LocalDateTime

data class TechnologyDependency(
    val id: Long? = null,
    val type: DependencyType,
    val description: String? = null,
    val strength: DependencyStrength,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val technologyId: Long,
    val dependentTechnologyId: Long
) 