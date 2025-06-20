package com.company.techportfolio.portfolio.domain.port

import com.company.techportfolio.shared.domain.model.TechnologyPortfolio
import com.company.techportfolio.shared.domain.model.PortfolioType
import com.company.techportfolio.shared.domain.model.PortfolioStatus
import com.company.techportfolio.portfolio.domain.model.PortfolioSummary

/**
 * Portfolio Repository Port - Domain Interface
 * 
 * This interface defines the contract for portfolio data access operations
 * within the hexagonal architecture. It represents the domain's requirements
 * for portfolio persistence without coupling to specific implementation details.
 * 
 * ## Responsibilities:
 * - Portfolio entity CRUD operations
 * - Portfolio querying and filtering
 * - Ownership and organization-based access
 * - Business rule enforcement at the data layer
 * 
 * ## Implementation Notes:
 * - Implementations should handle data validation
 * - Database constraints should enforce business rules
 * - Null returns indicate entity not found
 * - Exceptions should be thrown for constraint violations
 * 
 * @author Technology Portfolio Team
 * @since 1.0.0
 * @see TechnologyPortfolio
 * @see PortfolioType
 * @see PortfolioStatus
 */
interface PortfolioRepository {
    
    /**
     * Finds a portfolio by its unique identifier.
     * 
     * @param id The unique identifier of the portfolio
     * @return The portfolio if found, null otherwise
     */
    fun findById(id: Long): TechnologyPortfolio?
    
    /**
     * Finds a portfolio by its unique name.
     * 
     * Portfolio names are unique across the system to prevent confusion
     * and ensure clear identification.
     * 
     * @param name The unique name of the portfolio
     * @return The portfolio if found, null otherwise
     */
    fun findByName(name: String): TechnologyPortfolio?
    
    /**
     * Finds all portfolios owned by a specific user.
     * 
     * Returns portfolios where the user is the designated owner,
     * supporting personal portfolio management.
     * 
     * @param ownerId The unique identifier of the owner
     * @return List of portfolios owned by the user (empty if none found)
     */
    fun findByOwnerId(ownerId: Long): List<TechnologyPortfolio>
    
    /**
     * Finds all portfolios belonging to a specific organization.
     * 
     * Returns portfolios associated with an organization,
     * supporting multi-tenant organizational structure.
     * 
     * @param organizationId The unique identifier of the organization
     * @return List of portfolios in the organization (empty if none found)
     */
    fun findByOrganizationId(organizationId: Long): List<TechnologyPortfolio>
    
    /**
     * Finds all portfolios of a specific type.
     * 
     * Allows filtering portfolios by their classification type
     * (e.g., PERSONAL, TEAM, ENTERPRISE).
     * 
     * @param type The portfolio type to filter by
     * @return List of portfolios of the specified type (empty if none found)
     */
    fun findByType(type: PortfolioType): List<TechnologyPortfolio>
    
    /**
     * Finds all portfolios with a specific status.
     * 
     * Allows filtering portfolios by their current status
     * (e.g., ACTIVE, ARCHIVED, DEPRECATED).
     * 
     * @param status The portfolio status to filter by
     * @return List of portfolios with the specified status (empty if none found)
     */
    fun findByStatus(status: PortfolioStatus): List<TechnologyPortfolio>
    
    /**
     * Retrieves all portfolios in the system.
     * 
     * This method should be used with caution in production environments
     * as it may return large datasets. Consider pagination for large systems.
     * 
     * @return List of all portfolios (empty if none exist)
     */
    fun findAll(): List<TechnologyPortfolio>
    
    /**
     * Saves a new portfolio to the repository.
     * 
     * Creates a new portfolio entity with generated ID and timestamps.
     * The portfolio name must be unique across the system.
     * 
     * @param portfolio The portfolio entity to save (ID should be null for new entities)
     * @return The saved portfolio with generated ID and timestamps
     * @throws IllegalArgumentException if portfolio name already exists
     * @throws RuntimeException if save operation fails
     */
    fun save(portfolio: TechnologyPortfolio): TechnologyPortfolio
    
    /**
     * Updates an existing portfolio in the repository.
     * 
     * Updates the portfolio entity with new values and updated timestamp.
     * The portfolio must exist before updating.
     * 
     * @param portfolio The portfolio entity to update (ID must not be null)
     * @return The updated portfolio with new timestamp
     * @throws IllegalArgumentException if portfolio doesn't exist
     * @throws RuntimeException if update operation fails
     */
    fun update(portfolio: TechnologyPortfolio): TechnologyPortfolio
    
    /**
     * Deletes a portfolio by its unique identifier.
     * 
     * Permanently removes the portfolio from the repository.
     * The portfolio should be empty (no associated technologies) before deletion.
     * 
     * @param id The unique identifier of the portfolio to delete
     * @return true if the portfolio was deleted, false if not found
     * @throws RuntimeException if deletion fails due to constraints
     */
    fun delete(id: Long): Boolean
    
    /**
     * Checks if a portfolio exists by its unique identifier.
     * 
     * Provides a lightweight way to verify portfolio existence
     * without retrieving the full entity.
     * 
     * @param id The unique identifier of the portfolio
     * @return true if the portfolio exists, false otherwise
     */
    fun existsById(id: Long): Boolean
    
    /**
     * Counts the number of portfolios owned by a specific user.
     * 
     * Provides portfolio count statistics for user dashboard
     * and quota management purposes.
     * 
     * @param ownerId The unique identifier of the owner
     * @return The number of portfolios owned by the user
     */
    fun countByOwnerId(ownerId: Long): Long
    
    /**
     * Counts the number of portfolios in a specific organization.
     * 
     * Provides portfolio count statistics for organizational
     * reporting and management purposes.
     * 
     * @param organizationId The unique identifier of the organization
     * @return The number of portfolios in the organization
     */
    fun countByOrganizationId(organizationId: Long): Long
}