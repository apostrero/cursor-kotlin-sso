package com.company.techportfolio.portfolio.domain.model

import com.company.techportfolio.shared.domain.model.PortfolioType
import com.company.techportfolio.shared.domain.model.PortfolioStatus
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Summary data class representing key information about a technology portfolio.
 * 
 * This data class provides a condensed view of a portfolio, containing only
 * the most essential information for listing and overview purposes. It's used
 * when full portfolio details are not needed, improving performance and
 * reducing data transfer.
 * 
 * @property id Unique identifier of the portfolio
 * @property name Name of the portfolio
 * @property type Type/category of the portfolio
 * @property status Current status of the portfolio
 * @property ownerId ID of the user who owns this portfolio
 * @property organizationId Optional ID of the organization this portfolio belongs to
 * @property technologyCount Number of technologies in this portfolio
 * @property totalAnnualCost Optional total annual cost of all technologies
 * @property lastUpdated Timestamp when the portfolio was last updated
 * 
 * @author Technology Portfolio Team
 * @since 1.0.0
 */
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