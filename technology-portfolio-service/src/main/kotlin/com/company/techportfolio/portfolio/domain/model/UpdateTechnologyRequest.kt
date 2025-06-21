package com.company.techportfolio.portfolio.domain.model

import com.company.techportfolio.shared.domain.model.MaturityLevel
import com.company.techportfolio.shared.domain.model.RiskLevel
import com.company.techportfolio.shared.domain.model.TechnologyType
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Request data class for updating an existing technology in a portfolio.
 *
 * This data class represents the information that can be updated for an existing
 * technology within a portfolio. All fields are optional to support partial updates,
 * allowing clients to update only the specific attributes they need to change.
 *
 * @property name Optional new name for the technology
 * @property description Optional new description
 * @property category Optional new category
 * @property version Optional new version information
 * @property type Optional new type classification
 * @property maturityLevel Optional new maturity level assessment
 * @property riskLevel Optional new risk level assessment
 * @property annualCost Optional new annual cost
 * @property licenseCost Optional new licensing cost
 * @property maintenanceCost Optional new maintenance cost
 * @property vendorName Optional new vendor/supplier name
 * @property vendorContact Optional new vendor contact information
 * @property supportContractExpiry Optional new support contract expiration date
 *
 * @author Technology Portfolio Team
 * @since 1.0.0
 */
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