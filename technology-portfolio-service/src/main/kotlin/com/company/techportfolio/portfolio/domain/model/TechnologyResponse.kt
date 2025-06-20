package com.company.techportfolio.portfolio.domain.model

import com.company.techportfolio.shared.domain.model.TechnologyType
import com.company.techportfolio.shared.domain.model.MaturityLevel
import com.company.techportfolio.shared.domain.model.RiskLevel
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Response data class representing a complete technology within a portfolio.
 * 
 * This data class represents the full information about a technology that is
 * returned to clients. It includes all technical details, cost information,
 * vendor details, risk assessments, and metadata.
 * 
 * @property id Unique identifier of the technology
 * @property name Name of the technology
 * @property description Optional description of the technology
 * @property category Category this technology belongs to
 * @property version Optional version information
 * @property type Type classification of the technology
 * @property maturityLevel Maturity level assessment
 * @property riskLevel Risk level assessment
 * @property annualCost Optional annual cost of the technology
 * @property licenseCost Optional licensing cost
 * @property maintenanceCost Optional maintenance cost
 * @property vendorName Optional vendor/supplier name
 * @property vendorContact Optional vendor contact information
 * @property supportContractExpiry Optional support contract expiration date
 * @property isActive Whether the technology is currently active
 * @property createdAt Timestamp when the technology was created
 * @property updatedAt Timestamp when the technology was last updated
 * 
 * @author Technology Portfolio Team
 * @since 1.0.0
 */
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