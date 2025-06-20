package com.company.techportfolio.portfolio.domain.port

import com.company.techportfolio.shared.domain.model.PortfolioType
import com.company.techportfolio.shared.domain.model.PortfolioStatus
import com.company.techportfolio.portfolio.domain.model.PortfolioSummary

/**
 * Portfolio Query Repository Port - Optimized Read Operations
 * 
 * This interface defines the contract for optimized portfolio query operations
 * within the hexagonal architecture. It focuses on read-only operations that
 * return lightweight summary objects for efficient listing, searching, and
 * reporting purposes.
 * 
 * ## Responsibilities:
 * - Optimized portfolio summary queries
 * - Advanced search and filtering capabilities
 * - Performance-focused read operations
 * - Aggregated data retrieval for reporting
 * 
 * ## Design Principles:
 * - Separation of read and write concerns (CQRS pattern)
 * - Optimized for query performance over consistency
 * - Returns lightweight DTOs instead of full entities
 * - Supports complex filtering and search scenarios
 * 
 * ## Implementation Notes:
 * - Implementations may use read replicas or materialized views
 * - Query optimization is prioritized over write performance
 * - Results should be cacheable where appropriate
 * - Null returns indicate entity not found
 * 
 * @author Technology Portfolio Team
 * @since 1.0.0
 * @see PortfolioSummary
 * @see PortfolioType
 * @see PortfolioStatus
 */
interface PortfolioQueryRepository {
    /**
     * Finds a portfolio summary by its unique identifier.
     * 
     * Returns a lightweight portfolio summary with essential information
     * and calculated fields like technology count and total costs.
     * 
     * @param id The unique identifier of the portfolio
     * @return The portfolio summary if found, null otherwise
     */
    fun findPortfolioSummary(id: Long): PortfolioSummary?
    
    /**
     * Finds portfolio summaries owned by a specific user.
     * 
     * Returns lightweight portfolio summaries for efficient user dashboard
     * display and personal portfolio management. Results are optimized
     * for listing and overview purposes.
     * 
     * @param ownerId The unique identifier of the owner
     * @return List of portfolio summaries owned by the user (empty if none found)
     */
    fun findPortfolioSummariesByOwner(ownerId: Long): List<PortfolioSummary>
    
    /**
     * Finds portfolio summaries for a specific organization.
     * 
     * Returns all portfolio summaries belonging to an organization,
     * supporting multi-tenant organizational reporting and management.
     * Results include aggregated statistics for organizational oversight.
     * 
     * @param organizationId The unique identifier of the organization
     * @return List of portfolio summaries in the organization (empty if none found)
     */
    fun findPortfolioSummariesByOrganization(organizationId: Long): List<PortfolioSummary>
    
    /**
     * Finds portfolio summaries of a specific type.
     * 
     * Allows filtering portfolio summaries by their classification type
     * (e.g., PERSONAL, TEAM, ENTERPRISE). Useful for categorized reporting
     * and type-specific management views.
     * 
     * @param type The portfolio type to filter by
     * @return List of portfolio summaries of the specified type (empty if none found)
     */
    fun findPortfolioSummariesByType(type: PortfolioType): List<PortfolioSummary>
    
    /**
     * Finds portfolio summaries with a specific status.
     * 
     * Allows filtering portfolio summaries by their current status
     * (e.g., ACTIVE, ARCHIVED, DEPRECATED). Essential for lifecycle
     * management and status-based reporting.
     * 
     * @param status The portfolio status to filter by
     * @return List of portfolio summaries with the specified status (empty if none found)
     */
    fun findPortfolioSummariesByStatus(status: PortfolioStatus): List<PortfolioSummary>
    
    /**
     * Retrieves all portfolio summaries in the system.
     * 
     * Returns lightweight summaries of all portfolios for system-wide
     * reporting and administration. This method should be used with
     * caution in production environments as it may return large datasets.
     * Consider pagination for large systems.
     * 
     * @return List of all portfolio summaries (empty if none exist)
     */
    fun findAllPortfolioSummaries(): List<PortfolioSummary>
    
    /**
     * Searches portfolio summaries with flexible filtering criteria.
     * 
     * Provides advanced search capabilities with multiple optional filters.
     * All parameters are optional, allowing for flexible search combinations.
     * This method is optimized for search performance and supports complex
     * query scenarios.
     * 
     * ## Search Capabilities:
     * - Name-based partial matching (case-insensitive)
     * - Type-based filtering
     * - Status-based filtering
     * - Organization scope filtering
     * - Combined filter support
     * 
     * ## Performance Notes:
     * - Implementations should use appropriate indexes
     * - Results may be cached for frequently used searches
     * - Consider pagination for large result sets
     * 
     * @param name Optional name filter for partial matching (case-insensitive)
     * @param type Optional portfolio type filter
     * @param status Optional portfolio status filter
     * @param organizationId Optional organization scope filter
     * @return List of portfolio summaries matching the search criteria (empty if none found)
     */
    fun searchPortfolios(name: String?, type: PortfolioType?, status: PortfolioStatus?, organizationId: Long?): List<PortfolioSummary>
}