package com.company.techportfolio.portfolio.domain.model

import com.company.techportfolio.shared.domain.model.PortfolioStatus
import com.company.techportfolio.shared.domain.model.PortfolioType

/**
 * Request data class for updating an existing technology portfolio.
 *
 * This data class represents the information that can be updated for an existing
 * portfolio. All fields are optional to support partial updates, allowing clients
 * to update only the fields they need to change.
 *
 * @property name Optional new name for the portfolio
 * @property description Optional new description for the portfolio
 * @property type Optional new type/category for the portfolio
 * @property status Optional new status for the portfolio
 *
 * @author Technology Portfolio Team
 * @since 1.0.0
 */
data class UpdatePortfolioRequest(
    val name: String? = null,
    val description: String? = null,
    val type: PortfolioType? = null,
    val status: PortfolioStatus? = null
) 