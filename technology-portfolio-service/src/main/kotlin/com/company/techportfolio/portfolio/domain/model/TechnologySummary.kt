package com.company.techportfolio.portfolio.domain.model

import com.company.techportfolio.shared.domain.model.MaturityLevel
import com.company.techportfolio.shared.domain.model.RiskLevel
import com.company.techportfolio.shared.domain.model.TechnologyType
import java.math.BigDecimal

/**
 * Summary data class representing key information about a technology.
 *
 * This data class provides a condensed view of a technology, containing only
 * the most essential information for listing and overview purposes. It's used
 * when full technology details are not needed, improving performance and
 * reducing data transfer.
 *
 * @property id Unique identifier of the technology
 * @property name Name of the technology
 * @property category Category this technology belongs to
 * @property type Type classification of the technology
 * @property maturityLevel Maturity level assessment
 * @property riskLevel Risk level assessment
 * @property annualCost Optional annual cost of the technology
 * @property vendorName Optional vendor/supplier name
 *
 * @author Technology Portfolio Team
 * @since 1.0.0
 */
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