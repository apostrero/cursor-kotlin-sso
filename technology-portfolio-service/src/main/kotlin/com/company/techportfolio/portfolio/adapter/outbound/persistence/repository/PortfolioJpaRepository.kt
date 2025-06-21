package com.company.techportfolio.portfolio.adapter.out.persistence.repository

import com.company.techportfolio.portfolio.adapter.out.persistence.entity.PortfolioEntity
import com.company.techportfolio.shared.domain.model.PortfolioStatus
import com.company.techportfolio.shared.domain.model.PortfolioType
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Portfolio Reactive Repository Interface
 *
 * Spring Data R2DBC reactive repository interface for PortfolioEntity persistence operations.
 * Extends ReactiveCrudRepository to provide reactive CRUD operations and defines custom
 * query methods for portfolio-specific data access patterns using reactive streams.
 *
 * ## Repository Features:
 * - Reactive CRUD operations via ReactiveCrudRepository inheritance
 * - Custom finder methods using Spring Data naming conventions
 * - Advanced search with custom SQL query
 * - Optimized queries for common access patterns
 * - Count operations for statistics and reporting
 * - Non-blocking database operations
 *
 * ## Query Methods:
 * - **Derived Queries**: Spring Data generates reactive implementations from method names
 * - **Custom SQL**: Complex search query with multiple optional parameters
 * - **Active Filtering**: Automatic filtering of inactive records
 * - **Parameter Binding**: Safe parameter binding to prevent SQL injection
 * - **Reactive Streams**: All methods return Mono<T> or Flux<T>
 *
 * ## Performance Considerations:
 * - Indexes recommended on: name, owner_id, organization_id, type, status
 * - Search query uses LIKE for name matching (consider full-text search for large datasets)
 * - Active flag filtering reduces result set size
 * - Count operations are optimized for dashboard statistics
 * - Non-blocking I/O improves concurrency and resource utilization
 *
 * @author Technology Portfolio Team
 * @since 1.0.0
 * @see PortfolioEntity
 * @see ReactiveCrudRepository
 * @see PortfolioType
 * @see PortfolioStatus
 */
@Repository
interface PortfolioJpaRepository : ReactiveCrudRepository<PortfolioEntity, Long> {

    /**
     * Finds a portfolio by its unique name.
     *
     * Portfolio names are unique across the system. This method is used
     * for name validation during portfolio creation and name-based lookups.
     *
     * @param name The unique name of the portfolio
     * @return Mono<PortfolioEntity> containing the portfolio entity if found, empty if not found
     */
    fun findByName(name: String): Mono<PortfolioEntity>

    /**
     * Finds all portfolios owned by a specific user.
     *
     * Returns portfolios where the user is the designated owner.
     * Used for user dashboard and personal portfolio management.
     *
     * @param ownerId The unique identifier of the owner
     * @return Flux<PortfolioEntity> containing portfolio entities owned by the user
     */
    fun findByOwnerId(ownerId: Long): Flux<PortfolioEntity>

    /**
     * Finds all portfolios belonging to a specific organization.
     *
     * Returns portfolios associated with an organization for
     * multi-tenant organizational portfolio management.
     *
     * @param organizationId The unique identifier of the organization
     * @return Flux<PortfolioEntity> containing portfolio entities in the organization
     */
    fun findByOrganizationId(organizationId: Long): Flux<PortfolioEntity>

    /**
     * Finds all portfolios of a specific type.
     *
     * Filters portfolios by their classification type for
     * categorized reporting and type-specific management.
     *
     * @param type The portfolio type to filter by
     * @return Flux<PortfolioEntity> containing portfolio entities of the specified type
     */
    fun findByType(type: PortfolioType): Flux<PortfolioEntity>

    /**
     * Finds all portfolios with a specific status.
     *
     * Filters portfolios by their current status for lifecycle
     * management and status-based reporting.
     *
     * @param status The portfolio status to filter by
     * @return Flux<PortfolioEntity> containing portfolio entities with the specified status
     */
    fun findByStatus(status: PortfolioStatus): Flux<PortfolioEntity>

    /**
     * Finds all active portfolios.
     *
     * Returns only portfolios with isActive = true, effectively
     * filtering out soft-deleted or inactive portfolios.
     *
     * @return Flux<PortfolioEntity> containing active portfolio entities
     */
    fun findByIsActiveTrue(): Flux<PortfolioEntity>

    /**
     * Counts the number of portfolios owned by a specific user.
     *
     * Provides portfolio count statistics for user dashboard
     * and quota management. Optimized count operation.
     *
     * @param ownerId The unique identifier of the owner
     * @return Mono<Long> containing the number of portfolios owned by the user
     */
    fun countByOwnerId(ownerId: Long): Mono<Long>

    /**
     * Counts the number of portfolios in a specific organization.
     *
     * Provides portfolio count statistics for organizational
     * reporting and management. Optimized count operation.
     *
     * @param organizationId The unique identifier of the organization
     * @return Mono<Long> containing the number of portfolios in the organization
     */
    fun countByOrganizationId(organizationId: Long): Mono<Long>

    /**
     * Searches portfolios with flexible filtering criteria.
     *
     * Advanced search method using custom SQL query with multiple
     * optional parameters. All parameters are optional, allowing for
     * flexible search combinations. Uses LIKE operator for name
     * matching to support partial name searches.
     *
     * ## Search Features:
     * - **Name Search**: Case-insensitive partial matching using LIKE
     * - **Type Filter**: Exact match on portfolio type
     * - **Status Filter**: Exact match on portfolio status
     * - **Organization Scope**: Filter by organization membership
     * - **Active Only**: Automatically filters to active portfolios
     * - **Parameter Safety**: Uses @Param annotations for safe binding
     * - **Reactive Streams**: Returns Flux for streaming results
     *
     * ## Query Logic:
     * - All conditions are combined with AND
     * - NULL parameters are ignored (no filtering applied)
     * - Name search uses %name% pattern for substring matching
     * - Results include only active portfolios (isActive = true)
     *
     * ## Performance Notes:
     * - Consider database indexes on searchable fields
     * - LIKE operations may be slow on large datasets
     * - Consider full-text search for advanced name matching
     * - Reactive streaming enables backpressure handling
     *
     * @param name Optional name filter for partial matching (case-insensitive)
     * @param type Optional portfolio type filter (exact match)
     * @param status Optional portfolio status filter (exact match)
     * @param organizationId Optional organization scope filter (exact match)
     * @return Flux<PortfolioEntity> containing portfolio entities matching the search criteria
     */
    @Query(
        """
        SELECT * FROM portfolios 
        WHERE (:name IS NULL OR name ILIKE '%' || :name || '%') 
        AND (:type IS NULL OR type = :type) 
        AND (:status IS NULL OR status = :status) 
        AND (:organizationId IS NULL OR organization_id = :organizationId)
        AND is_active = true
    """
    )
    fun searchPortfolios(
        @Param("name") name: String?,
        @Param("type") type: PortfolioType?,
        @Param("status") status: PortfolioStatus?,
        @Param("organizationId") organizationId: Long?
    ): Flux<PortfolioEntity>
} 