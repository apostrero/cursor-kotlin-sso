package com.company.techportfolio.portfolio.domain.model

import com.company.techportfolio.shared.domain.model.PortfolioType
import com.company.techportfolio.shared.domain.model.PortfolioStatus
import com.company.techportfolio.shared.domain.model.TechnologyType
import com.company.techportfolio.shared.domain.model.MaturityLevel
import com.company.techportfolio.shared.domain.model.RiskLevel
import java.math.BigDecimal
import java.time.LocalDateTime

data class CreatePortfolioRequest(
    val name: String,
    val description: String? = null,
    val type: PortfolioType,
    val ownerId: Long,
    val organizationId: Long? = null
)

data class UpdatePortfolioRequest(
    val name: String? = null,
    val description: String? = null,
    val type: PortfolioType? = null,
    val status: PortfolioStatus? = null
)

data class AddTechnologyRequest(
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
    val supportContractExpiry: LocalDateTime? = null
)

data class UpdateTechnologyRequest(
    val name: String? = null,
    val description: String? = null,
    val category: String? = null,
    val version: String? = null,
    val type: TechnologyType? = null,
    val maturityLevel: MaturityLevel? = null,
    val riskLevel: RiskLevel? = null,
    val annualCost: BigDecimal? = null,
    val licenseCost: BigDecimal? = null,
    val maintenanceCost: BigDecimal? = null,
    val vendorName: String? = null,
    val vendorContact: String? = null,
    val supportContractExpiry: LocalDateTime? = null
)

data class PortfolioResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val type: PortfolioType,
    val status: PortfolioStatus,
    val isActive: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime?,
    val ownerId: Long,
    val organizationId: Long?,
    val technologyCount: Int,
    val totalAnnualCost: BigDecimal?,
    val technologies: List<TechnologyResponse> = emptyList()
)

data class TechnologyResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val category: String,
    val version: String?,
    val type: TechnologyType,
    val maturityLevel: MaturityLevel,
    val riskLevel: RiskLevel,
    val annualCost: BigDecimal?,
    val licenseCost: BigDecimal?,
    val maintenanceCost: BigDecimal?,
    val vendorName: String?,
    val vendorContact: String?,
    val supportContractExpiry: LocalDateTime?,
    val isActive: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime?
)

data class PortfolioSummary(
    val id: Long,
    val name: String,
    val type: PortfolioType,
    val status: PortfolioStatus,
    val ownerId: Long,
    val organizationId: Long?,
    val technologyCount: Int,
    val totalAnnualCost: BigDecimal?,
    val lastUpdated: LocalDateTime
)

data class TechnologySummary(
    val id: Long,
    val name: String,
    val category: String,
    val type: TechnologyType,
    val maturityLevel: MaturityLevel,
    val riskLevel: RiskLevel,
    val annualCost: BigDecimal?,
    val vendorName: String?
) 