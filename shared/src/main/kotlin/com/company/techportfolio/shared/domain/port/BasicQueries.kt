package com.company.techportfolio.shared.domain.port

/**
 * Query to retrieve a user by username.
 */
data class GetUserQuery(
    val username: String
) : Query()

/**
 * Query to retrieve all users in an organization.
 */
data class GetUsersByOrganizationQuery(
    val organizationId: Long
) : Query()

/**
 * Query to retrieve a portfolio by ID.
 */
data class GetPortfolioQuery(
    val portfolioId: Long
) : Query()

/**
 * Query to retrieve all portfolios owned by a specific user.
 */
data class GetPortfoliosByOwnerQuery(
    val ownerId: Long
) : Query()

/**
 * Query to retrieve all portfolios in an organization.
 */
data class GetPortfoliosByOrganizationQuery(
    val organizationId: Long
) : Query()

/**
 * Query to retrieve all technologies in a portfolio.
 */
data class GetTechnologiesByPortfolioQuery(
    val portfolioId: Long
) : Query()

/**
 * Query to search portfolios with various criteria.
 */
data class SearchPortfoliosQuery(
    val name: String? = null,
    val type: String? = null,
    val status: String? = null,
    val organizationId: Long? = null
) : Query() 