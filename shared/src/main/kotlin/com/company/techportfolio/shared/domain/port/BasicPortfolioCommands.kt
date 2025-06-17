package com.company.techportfolio.shared.domain.port

/**
 * Command to create a new portfolio.
 */
data class CreatePortfolioCommand(
    val name: String,
    val description: String?,
    val type: String,
    val ownerId: Long,
    val organizationId: Long?
) : Command()

/**
 * Command to update an existing portfolio.
 */
data class UpdatePortfolioCommand(
    val portfolioId: Long,
    val name: String? = null,
    val description: String? = null,
    val type: String? = null,
    val status: String? = null
) : Command()

/**
 * Command to add a technology to a portfolio.
 */
data class AddTechnologyCommand(
    val portfolioId: Long,
    val name: String,
    val description: String?,
    val category: String,
    val type: String,
    val maturityLevel: String,
    val riskLevel: String,
    val vendorName: String?,
    val annualCost: java.math.BigDecimal?
) : Command()

/**
 * Command to remove a technology from a portfolio.
 */
data class RemoveTechnologyCommand(
    val portfolioId: Long,
    val technologyId: Long
) : Command() 