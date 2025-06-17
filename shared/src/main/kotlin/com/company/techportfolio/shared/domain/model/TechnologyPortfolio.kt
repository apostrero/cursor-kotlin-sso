package com.company.techportfolio.shared.domain.model

import java.time.LocalDateTime

data class TechnologyPortfolio(
    val id: Long? = null,
    val name: String,
    val description: String? = null,
    val type: PortfolioType,
    val status: PortfolioStatus = PortfolioStatus.ACTIVE,
    val isActive: Boolean = true,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime? = null,
    val ownerId: Long,
    val organizationId: Long? = null,
    val technologies: Set<Technology> = emptySet(),
    val assessments: Set<PortfolioAssessment> = emptySet()
) 