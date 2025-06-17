package com.company.techportfolio.shared.domain.model

import java.math.BigDecimal
import java.time.LocalDateTime

data class Technology(
    val id: Long? = null,
    val name: String,
    val description: String? = null,
    val category: String,
    val version: String? = null,
    val type: TechnologyType,
    val maturityLevel: MaturityLevel,
    val riskLevel: RiskLevel,
    val annualCost: BigDecimal? = null,
    val licenseCost: BigDecimal? = null,
    val maintenanceCost: BigDecimal? = null,
    val vendorName: String? = null,
    val vendorContact: String? = null,
    val supportContractExpiry: LocalDateTime? = null,
    val isActive: Boolean = true,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime? = null,
    val portfolioId: Long,
    val assessments: Set<TechnologyAssessment> = emptySet(),
    val dependencies: Set<TechnologyDependency> = emptySet()
) 