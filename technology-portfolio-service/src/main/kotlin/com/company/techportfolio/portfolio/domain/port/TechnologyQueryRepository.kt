package com.company.techportfolio.portfolio.domain.port

import com.company.techportfolio.portfolio.domain.model.TechnologySummary
import com.company.techportfolio.shared.domain.model.TechnologyType
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Technology Query Repository Port - Optimized Read Operations (REACTIVE)
 *
 * This interface defines the contract for optimized technology query operations
 * within the hexagonal architecture using reactive programming patterns.
 * It focuses on read-only operations that return lightweight summary objects
 * for efficient listing, searching, and reporting purposes.
 *
 * ## Responsibilities:
 * - Optimized technology summary queries
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
 * @see TechnologySummary
 * @see TechnologyType
 */
interface TechnologyQueryRepository {

    /**
     * Finds a technology summary by its unique identifier.
     *
     * Returns a lightweight technology summary with essential information
     * and calculated fields like cost information and risk assessments.
     *
     * @param id The unique identifier of the technology
     * @return Mono<TechnologySummary> containing the technology summary if found, empty if not found
     */
    fun findTechnologySummary(id: Long): Mono<TechnologySummary>

    /**
     * Finds technology summaries for a specific portfolio.
     *
     * Returns lightweight technology summaries for efficient portfolio
     * inventory display and management. Results are optimized for
     * listing and overview purposes within portfolio context.
     *
     * @param portfolioId The unique identifier of the portfolio
     * @return Flux<TechnologySummary> containing technology summaries in the portfolio
     */
    fun findTechnologySummariesByPortfolio(portfolioId: Long): Flux<TechnologySummary>

    /**
     * Finds technology summaries in a specific category.
     *
     * Allows filtering technology summaries by their category classification
     * (e.g., "Framework", "Database", "Cloud Service"). Useful for
     * categorized reporting and category-specific management views.
     *
     * @param category The technology category to filter by
     * @return Flux<TechnologySummary> containing technology summaries in the category
     */
    fun findTechnologySummariesByCategory(category: String): Flux<TechnologySummary>

    /**
     * Finds technology summaries of a specific type.
     *
     * Allows filtering technology summaries by their type classification
     * (e.g., FRAMEWORK, DATABASE, CLOUD_SERVICE, TOOL). Essential for
     * type-based reporting and technology taxonomy management.
     *
     * @param type The technology type to filter by
     * @return Flux<TechnologySummary> containing technology summaries of the specified type
     */
    fun findTechnologySummariesByType(type: TechnologyType): Flux<TechnologySummary>

    /**
     * Finds technology summaries from a specific vendor.
     *
     * Allows filtering technology summaries by their vendor or supplier,
     * supporting vendor relationship management and vendor-specific
     * reporting and analysis.
     *
     * @param vendorName The vendor name to filter by
     * @return Flux<TechnologySummary> containing technology summaries from the vendor
     */
    fun findTechnologySummariesByVendor(vendorName: String): Flux<TechnologySummary>

    /**
     * Retrieves all technology summaries in the system.
     *
     * Returns lightweight summaries of all technologies for system-wide
     * reporting and administration. This method should be used with
     * caution in production environments as it may return large datasets.
     * Consider pagination for large systems.
     *
     * @return Flux<TechnologySummary> containing all technology summaries
     */
    fun findAllTechnologySummaries(): Flux<TechnologySummary>

    /**
     * Searches technology summaries with flexible filtering criteria.
     *
     * Provides advanced search capabilities with multiple optional filters.
     * All parameters are optional, allowing for flexible search combinations.
     * This method is optimized for search performance and supports complex
     * query scenarios.
     *
     * ## Search Capabilities:
     * - Name-based partial matching (case-insensitive)
     * - Category-based filtering
     * - Type-based filtering
     * - Vendor-based filtering
     * - Combined filter support
     *
     * ## Performance Notes:
     * - Implementations should use appropriate indexes
     * - Results may be cached for frequently used searches
     * - Consider pagination for large result sets
     * - Reactive streaming enables backpressure handling
     *
     * @param name Optional name filter for partial matching (case-insensitive)
     * @param category Optional technology category filter
     * @param type Optional technology type filter
     * @param vendorName Optional vendor name filter
     * @return Flux<TechnologySummary> containing technology summaries matching the search criteria
     */
    fun searchTechnologies(
        name: String?,
        category: String?,
        type: TechnologyType?,
        vendorName: String?
    ): Flux<TechnologySummary>
} 