package com.company.techportfolio.portfolio.domain.model

import com.company.techportfolio.shared.domain.model.PortfolioType

/**
 * Request data class for creating a new technology portfolio.
 * 
 * This data class represents the information required to create a new portfolio
 * within the technology portfolio management system. It includes essential
 * portfolio metadata and ownership information.
 * 
 * @property name The name of the portfolio (required)
 * @property description Optional description of the portfolio's purpose
 * @property type The type/category of the portfolio
 * @property ownerId ID of the user who owns this portfolio
 * @property organizationId Optional ID of the organization this portfolio belongs to
 * 
 * @author Technology Portfolio Team
 * @since 1.0.0
 */
data class CreatePortfolioRequest(
    val name: String,
    val description: String? = null,
    val type: PortfolioType,
    val ownerId: Long,
    val organizationId: Long? = null
) 