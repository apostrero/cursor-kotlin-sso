package com.company.techportfolio.portfolio.domain.model

import com.company.techportfolio.shared.domain.model.MaturityLevel
import com.company.techportfolio.shared.domain.model.RiskLevel
import com.company.techportfolio.shared.domain.model.TechnologyType
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Request data class for adding a new technology to a portfolio.
 *
 * This data class represents the comprehensive information required to add
 * a new technology to an existing portfolio. It includes technical details,
 * cost information, vendor details, and risk assessment data.
 *
 * @property name The name of the technology (required)
 * @property description Optional description of the technology
 * @property category The category this technology belongs to (required)
 * @property version Optional version information
 * @property type The type classification of the technology
 * @property maturityLevel The maturity level assessment
 * @property riskLevel The risk level assessment
 * @property annualCost Optional annual cost of the technology
 * @property licenseCost Optional licensing cost
 * @property maintenanceCost Optional maintenance cost
 * @property vendorName Optional vendor/supplier name
 * @property vendorContact Optional vendor contact information
 * @property supportContractExpiry Optional support contract expiration date
 *
 * @author Technology Portfolio Team
 * @since 1.0.0
 */
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