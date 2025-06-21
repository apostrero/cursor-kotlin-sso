package com.company.techportfolio.portfolio.domain.port

import com.company.techportfolio.portfolio.domain.model.PortfolioSummary
import com.company.techportfolio.shared.domain.model.PortfolioStatus
import com.company.techportfolio.shared.domain.model.PortfolioType
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Portfolio Query Repository Port - Optimized Read Operations (REACTIVE)
 *
 * This interface defines the contract for optimized portfolio query operations
 * within the hexagonal architecture using reactive programming patterns.
 * It focuses on read-only operations that return lightweight summary objects
 * for efficient listing, searching, and reporting purposes.
 *
 * ## Responsibilities:
 * - Optimized portfolio summary queries
 * - Advanced search and filtering capabilities
 * - Performance-focused read operations
 * - Aggregated data retrieval for reporting
 *
 * ## Reactive Design:
 * - Single items return Mono<T> for non-blocking operations
 * - Collections return Flux<T> for streaming and backpressure handling
 * - Supports reactive error handling with onErrorMap and onErrorResume
 * - Enables reactive caching and performance optimization
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
 * - Empty Mono/Flux indicates entity not found
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
     * @return Mono<PortfolioSummary> containing the portfolio summary if found, empty if not found
     */
    fun findPortfolioSummary(id: Long): Mono<PortfolioSummary>

    /**
     * Finds portfolio summaries owned by a specific user.
     *
     * Returns lightweight portfolio summaries for efficient user dashboard
     * display and personal portfolio management. Results are optimized
     * for listing and overview purposes.
     *
     * @param ownerId The unique identifier of the owner
     * @return Flux<PortfolioSummary> containing portfolio summaries owned by the user
     */
    fun findPortfolioSummariesByOwner(ownerId: Long): Flux<PortfolioSummary>

    /**
     * Finds portfolio summaries for a specific organization.
     *
     * Returns all portfolio summaries belonging to an organization,
     * supporting multi-tenant organizational reporting and management.
     * Results include aggregated statistics for organizational oversight.
     *
     * @param organizationId The unique identifier of the organization
     * @return Flux<PortfolioSummary> containing portfolio summaries in the organization
     */
    fun findPortfolioSummariesByOrganization(organizationId: Long): Flux<PortfolioSummary>

    /**
     * Finds portfolio summaries of a specific type.
     *
     * Allows filtering portfolio summaries by their classification type
     * (e.g., PERSONAL, TEAM, ENTERPRISE). Useful for categorized reporting
     * and type-specific management views.
     *
     * @param type The portfolio type to filter by
     * @return Flux<PortfolioSummary> containing portfolio summaries of the specified type
     */
    fun findPortfolioSummariesByType(type: PortfolioType): Flux<PortfolioSummary>

    /**
     * Finds portfolio summaries with a specific status.
     *
     * Allows filtering portfolio summaries by their current status
     * (e.g., ACTIVE, ARCHIVED, DEPRECATED). Essential for lifecycle
     * management and status-based reporting.
     *
     * @param status The portfolio status to filter by
     * @return Flux<PortfolioSummary> containing portfolio summaries with the specified status
     */
    fun findPortfolioSummariesByStatus(status: PortfolioStatus): Flux<PortfolioSummary>

    /**
     * Retrieves all portfolio summaries in the system.
     *
     * Returns lightweight summaries of all portfolios for system-wide
     * reporting and administration. This method should be used with
     * caution in production environments as it may return large datasets.
     * Consider pagination for large systems.
     *
     * @return Flux<PortfolioSummary> containing all portfolio summaries
     */
    fun findAllPortfolioSummaries(): Flux<PortfolioSummary>

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
     * - Reactive streaming enables backpressure handling
     *
     * @param name Optional name filter for partial matching (case-insensitive)
     * @param type Optional portfolio type filter
     * @param status Optional portfolio status filter
     * @param organizationId Optional organization scope filter
     * @return Flux<PortfolioSummary> containing portfolio summaries matching the search criteria
     */
    fun searchPortfolios(
        name: String?,
        type: PortfolioType?,
        status: PortfolioStatus?,
        organizationId: Long?
    ): Flux<PortfolioSummary>
}