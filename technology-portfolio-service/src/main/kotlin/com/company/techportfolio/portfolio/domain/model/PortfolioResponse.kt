package com.company.techportfolio.portfolio.domain.model

import com.company.techportfolio.shared.domain.model.PortfolioStatus
import com.company.techportfolio.shared.domain.model.PortfolioType
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Response data class representing a complete technology portfolio.
 *
 * This data class represents the full information about a technology portfolio
 * that is returned to clients. It includes all portfolio metadata, ownership
 * information, cost summaries, and associated technologies.
 *
 * @property id Unique identifier of the portfolio
 * @property name Name of the portfolio
 * @property description Optional description of the portfolio
 * @property type Type/category of the portfolio
 * @property status Current status of the portfolio
 * @property isActive Whether the portfolio is currently active
 * @property createdAt Timestamp when the portfolio was created
 * @property updatedAt Timestamp when the portfolio was last updated
 * @property ownerId ID of the user who owns this portfolio
 * @property organizationId Optional ID of the organization this portfolio belongs to
 * @property technologyCount Number of technologies in this portfolio
 * @property totalAnnualCost Optional total annual cost of all technologies
 * @property technologies List of technologies in this portfolio
 *
 * @author Technology Portfolio Team
 * @since 1.0.0
 */
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