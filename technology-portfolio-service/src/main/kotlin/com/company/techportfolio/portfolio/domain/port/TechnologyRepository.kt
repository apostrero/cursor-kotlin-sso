package com.company.techportfolio.portfolio.domain.port

import com.company.techportfolio.portfolio.domain.model.TechnologySummary
import com.company.techportfolio.shared.domain.model.Technology
import com.company.techportfolio.shared.domain.model.TechnologyType
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Technology Repository Port - Domain Interface (REACTIVE)
 *
 * This interface defines the contract for technology data access operations
 * within the hexagonal architecture using reactive programming patterns.
 *
 * ## Responsibilities:
 * - Technology entity CRUD operations
 * - Technology querying and filtering by various criteria
 * - Portfolio association management
 * - Cost and vendor information tracking
 * - Risk and maturity assessment data access
 *
 * ## Reactive Design:
 * - Single items return Mono<T> for non-blocking operations
 * - Collections return Flux<T> for streaming and backpressure handling
 * - Supports reactive error handling with onErrorMap and onErrorResume
 * - Enables reactive transaction management
 *
 * ## Implementation Notes:
 * - Implementations should handle data validation
 * - Database constraints should enforce referential integrity
 * - Empty Mono/Flux indicates entity not found
 * - Exceptions should be thrown for constraint violations
 *
 * @author Technology Portfolio Team
 * @since 1.0.0
 * @see Technology
 * @see TechnologyType
 * @see TechnologySummary
 */
interface TechnologyRepository {

    /**
     * Finds a technology by its unique identifier.
     *
     * @param id The unique identifier of the technology
     * @return Mono<Technology> containing the technology if found, empty if not found
     */
    fun findById(id: Long): Mono<Technology>

    /**
     * Finds a technology by its name.
     *
     * Technology names should be unique within a portfolio context
     * to prevent confusion and ensure clear identification.
     *
     * @param name The name of the technology
     * @return Mono<Technology> containing the technology if found, empty if not found
     */
    fun findByName(name: String): Mono<Technology>

    /**
     * Finds all technologies associated with a specific portfolio.
     *
     * Returns all technologies that belong to the specified portfolio,
     * supporting portfolio inventory management and reporting.
     *
     * @param portfolioId The unique identifier of the portfolio
     * @return Flux<Technology> containing technologies in the portfolio
     */
    fun findByPortfolioId(portfolioId: Long): Flux<Technology>

    /**
     * Finds all technologies in a specific category.
     *
     * Allows filtering technologies by their category classification
     * (e.g., "Framework", "Database", "Cloud Service").
     *
     * @param category The technology category to filter by
     * @return Flux<Technology> containing technologies in the category
     */
    fun findByCategory(category: String): Flux<Technology>

    /**
     * Finds all technologies of a specific type.
     *
     * Allows filtering technologies by their type classification
     * (e.g., FRAMEWORK, DATABASE, CLOUD_SERVICE, TOOL).
     *
     * @param type The technology type to filter by
     * @return Flux<Technology> containing technologies of the specified type
     */
    fun findByType(type: TechnologyType): Flux<Technology>

    /**
     * Finds all technologies from a specific vendor.
     *
     * Allows filtering technologies by their vendor or supplier,
     * supporting vendor management and relationship tracking.
     *
     * @param vendorName The vendor name to filter by
     * @return Flux<Technology> containing technologies from the vendor
     */
    fun findByVendor(vendorName: String): Flux<Technology>

    /**
     * Finds all technologies with a specific maturity level.
     *
     * Allows filtering technologies by their maturity assessment
     * (e.g., EXPERIMENTAL, EMERGING, MATURE, LEGACY).
     *
     * @param maturityLevel The maturity level to filter by
     * @return Flux<Technology> containing technologies with the specified maturity level
     */
    fun findByMaturityLevel(maturityLevel: com.company.techportfolio.shared.domain.model.MaturityLevel): Flux<Technology>

    /**
     * Finds all technologies with a specific risk level.
     *
     * Allows filtering technologies by their risk assessment
     * (e.g., LOW, MEDIUM, HIGH, CRITICAL).
     *
     * @param riskLevel The risk level to filter by
     * @return Flux<Technology> containing technologies with the specified risk level
     */
    fun findByRiskLevel(riskLevel: com.company.techportfolio.shared.domain.model.RiskLevel): Flux<Technology>

    /**
     * Saves a new technology to the repository.
     *
     * Creates a new technology entity with generated ID and timestamps.
     * The technology must be associated with a valid portfolio.
     *
     * @param technology The technology entity to save (ID should be null for new entities)
     * @return Mono<Technology> containing the saved technology with generated ID and timestamps
     * @throws IllegalArgumentException if associated portfolio doesn't exist
     * @throws RuntimeException if save operation fails
     */
    fun save(technology: Technology): Mono<Technology>

    /**
     * Updates an existing technology in the repository.
     *
     * Updates the technology entity with new values and updated timestamp.
     * The technology must exist before updating.
     *
     * @param technology The technology entity to update (ID must not be null)
     * @return Mono<Technology> containing the updated technology with new timestamp
     * @throws IllegalArgumentException if technology doesn't exist
     * @throws RuntimeException if update operation fails
     */
    fun update(technology: Technology): Mono<Technology>

    /**
     * Deletes a technology by its unique identifier.
     *
     * Permanently removes the technology from the repository.
     * This operation should be used carefully as it cannot be undone.
     *
     * @param id The unique identifier of the technology to delete
     * @return Mono<Boolean> containing true if the technology was deleted, false if not found
     * @throws RuntimeException if deletion fails due to constraints
     */
    fun delete(id: Long): Mono<Boolean>

    /**
     * Checks if a technology exists by its unique identifier.
     *
     * Provides a lightweight way to verify technology existence
     * without retrieving the full entity.
     *
     * @param id The unique identifier of the technology
     * @return Mono<Boolean> containing true if the technology exists, false otherwise
     */
    fun existsById(id: Long): Mono<Boolean>

    /**
     * Counts the number of technologies in a specific portfolio.
     *
     * Provides technology count statistics for portfolio reporting
     * and management purposes.
     *
     * @param portfolioId The unique identifier of the portfolio
     * @return Mono<Long> containing the number of technologies in the portfolio
     */
    fun countByPortfolioId(portfolioId: Long): Mono<Long>
} 