package com.company.techportfolio.portfolio.adapter.out.persistence

import com.company.techportfolio.portfolio.adapter.out.persistence.entity.TechnologyEntity
import com.company.techportfolio.portfolio.adapter.out.persistence.repository.TechnologyJpaRepository
import com.company.techportfolio.portfolio.domain.model.TechnologySummary
import com.company.techportfolio.portfolio.domain.port.TechnologyQueryRepository
import com.company.techportfolio.portfolio.domain.port.TechnologyRepository
import com.company.techportfolio.shared.domain.model.Technology
import com.company.techportfolio.shared.domain.model.TechnologyType
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Technology Repository Adapter - Reactive Persistence Implementation
 *
 * This adapter class implements both the TechnologyRepository and TechnologyQueryRepository
 * interfaces from the domain layer, providing the reactive persistence implementation for
 * technology data access within the hexagonal architecture. It translates between
 * domain models and R2DBC entities, delegating actual database operations to Spring Data
 * R2DBC repositories.
 *
 * ## Architecture Role:
 * - Acts as an **outbound adapter** in the hexagonal architecture
 * - Implements domain port interfaces with infrastructure-specific code
 * - Isolates the domain layer from persistence implementation details
 * - Handles the translation between domain models and persistence entities
 * - Provides reactive streams for non-blocking database operations
 *
 * ## Implementation Features:
 * - Dual implementation of both standard and query-optimized repository interfaces
 * - Mapping between domain models and R2DBC entities via extension functions
 * - Efficient summary data retrieval for list operations
 * - Reactive error handling for database operations with graceful fallbacks
 * - Support for various technology filtering criteria
 * - Non-blocking database operations throughout
 *
 * ## Performance Considerations:
 * - Optimized for portfolio-based technology access patterns
 * - Lightweight summary objects for list operations
 * - Efficient search with multiple optional criteria
 * - Reactive streaming improves concurrency and resource utilization
 *
 * @property technologyJpaRepository Spring Data R2DBC repository for technology entities
 *
 * @author Technology Portfolio Team
 * @since 1.0.0
 * @see TechnologyRepository
 * @see TechnologyQueryRepository
 * @see TechnologyJpaRepository
 * @see Technology
 * @see TechnologySummary
 */
@Repository
class TechnologyRepositoryAdapter(
    private val technologyJpaRepository: TechnologyJpaRepository
) : TechnologyRepository, TechnologyQueryRepository {

    // TechnologyRepository implementation

    /**
     * Finds a technology by its unique identifier.
     *
     * Retrieves a technology entity by ID and maps it to the domain model.
     * Returns empty Mono if no technology is found with the given ID.
     *
     * @param id The unique identifier of the technology
     * @return Mono containing the domain technology model if found, empty otherwise
     */
    override fun findById(id: Long): Mono<Technology> {
        return technologyJpaRepository.findById(id)
            .map { it.toDomain() }
            .onErrorMap { e -> RuntimeException("Error finding technology with id $id", e) }
    }

    /**
     * Finds a technology by its name.
     *
     * Retrieves a technology entity by name and maps it to the domain model.
     * Used for name-based lookups across the system.
     *
     * @param name The name of the technology
     * @return Mono containing the domain technology model if found, empty otherwise
     */
    override fun findByName(name: String): Mono<Technology> {
        return technologyJpaRepository.findByName(name)
            .map { it.toDomain() }
            .onErrorMap { e -> RuntimeException("Error finding technology with name $name", e) }
    }

    /**
     * Finds all technologies associated with a specific portfolio.
     *
     * Retrieves all technology entities for the given portfolio and maps them
     * to domain models. Returns empty Flux if no technologies are found.
     *
     * @param portfolioId The unique identifier of the portfolio
     * @return Flux containing domain technology models in the portfolio
     */
    override fun findByPortfolioId(portfolioId: Long): Flux<Technology> {
        return technologyJpaRepository.findByPortfolioId(portfolioId)
            .map { it.toDomain() }
            .onErrorMap { e -> RuntimeException("Error finding technologies for portfolio $portfolioId", e) }
    }

    /**
     * Finds all technologies in a specific category.
     *
     * Retrieves all technology entities in the given category and maps them
     * to domain models. Returns empty Flux if no technologies are found.
     *
     * @param category The technology category to filter by
     * @return Flux containing domain technology models in the category
     */
    override fun findByCategory(category: String): Flux<Technology> {
        return technologyJpaRepository.findByCategory(category)
            .map { it.toDomain() }
            .onErrorMap { e -> RuntimeException("Error finding technologies for category $category", e) }
    }

    /**
     * Finds all technologies of a specific type.
     *
     * Retrieves all technology entities of the given type and maps them
     * to domain models. Returns empty Flux if no technologies are found.
     *
     * @param type The technology type to filter by
     * @return Flux containing domain technology models of the specified type
     */
    override fun findByType(type: TechnologyType): Flux<Technology> {
        return technologyJpaRepository.findByType(type)
            .map { it.toDomain() }
            .onErrorMap { e -> RuntimeException("Error finding technologies for type $type", e) }
    }

    /**
     * Finds all technologies from a specific vendor.
     *
     * Retrieves all technology entities from the given vendor and maps them
     * to domain models. Returns empty Flux if no technologies are found.
     *
     * @param vendorName The vendor name to filter by
     * @return Flux containing domain technology models from the vendor
     */
    override fun findByVendor(vendorName: String): Flux<Technology> {
        return technologyJpaRepository.findByVendorName(vendorName)
            .map { it.toDomain() }
            .onErrorMap { e -> RuntimeException("Error finding technologies for vendor $vendorName", e) }
    }

    /**
     * Finds all technologies with a specific maturity level.
     *
     * Retrieves all technology entities with the given maturity level and maps them
     * to domain models. Returns empty Flux if no technologies are found.
     *
     * @param maturityLevel The maturity level to filter by
     * @return Flux containing domain technology models with the specified maturity level
     */
    override fun findByMaturityLevel(maturityLevel: com.company.techportfolio.shared.domain.model.MaturityLevel): Flux<Technology> {
        return technologyJpaRepository.findByMaturityLevel(maturityLevel)
            .map { it.toDomain() }
            .onErrorMap { e -> RuntimeException("Error finding technologies for maturity level $maturityLevel", e) }
    }

    /**
     * Finds all technologies with a specific risk level.
     *
     * Retrieves all technology entities with the given risk level and maps them
     * to domain models. Returns empty Flux if no technologies are found.
     *
     * @param riskLevel The risk level to filter by
     * @return Flux containing domain technology models with the specified risk level
     */
    override fun findByRiskLevel(riskLevel: com.company.techportfolio.shared.domain.model.RiskLevel): Flux<Technology> {
        return technologyJpaRepository.findByRiskLevel(riskLevel)
            .map { it.toDomain() }
            .onErrorMap { e -> RuntimeException("Error finding technologies for risk level $riskLevel", e) }
    }

    /**
     * Saves a new technology to the repository.
     *
     * Converts the domain model to an entity, saves it to the database,
     * and returns the updated domain model with generated ID.
     *
     * @param technology The domain technology model to save
     * @return Mono containing the saved domain technology model with generated ID
     */
    override fun save(technology: Technology): Mono<Technology> {
        return Mono.just(technology.toEntity())
            .flatMap { entity -> technologyJpaRepository.save(entity) }
            .map { it.toDomain() }
            .onErrorMap { e -> RuntimeException("Error saving technology ${technology.name}", e) }
    }

    /**
     * Updates an existing technology in the repository.
     *
     * Converts the domain model to an entity, updates it in the database,
     * and returns the updated domain model.
     *
     * @param technology The domain technology model to update
     * @return Mono containing the updated domain technology model
     */
    override fun update(technology: Technology): Mono<Technology> {
        return Mono.just(technology.toEntity())
            .flatMap { entity -> technologyJpaRepository.save(entity) }
            .map { it.toDomain() }
            .onErrorMap { e -> RuntimeException("Error updating technology ${technology.name}", e) }
    }

    /**
     * Deletes a technology by its unique identifier.
     *
     * Attempts to delete the technology entity with the given ID.
     * Returns true if successful, false if the technology doesn't exist
     * or another error occurs.
     *
     * @param id The unique identifier of the technology to delete
     * @return Mono<Boolean> containing true if the technology was deleted, false otherwise
     */
    override fun delete(id: Long): Mono<Boolean> {
        return technologyJpaRepository.existsById(id)
            .flatMap { exists ->
                if (exists) {
                    technologyJpaRepository.deleteById(id)
                        .then(Mono.just(true))
                } else {
                    Mono.just(false)
                }
            }
            .onErrorReturn(false)
    }

    /**
     * Checks if a technology exists by its unique identifier.
     *
     * Delegates to the R2DBC repository to check for entity existence.
     *
     * @param id The unique identifier of the technology
     * @return Mono<Boolean> containing true if the technology exists, false otherwise
     */
    override fun existsById(id: Long): Mono<Boolean> {
        return technologyJpaRepository.existsById(id)
            .onErrorReturn(false)
    }

    /**
     * Counts the number of technologies in a specific portfolio.
     *
     * Delegates to the R2DBC repository to count entities by portfolio.
     *
     * @param portfolioId The unique identifier of the portfolio
     * @return Mono<Long> containing the number of technologies in the portfolio
     */
    override fun countByPortfolioId(portfolioId: Long): Mono<Long> {
        return technologyJpaRepository.countByPortfolioId(portfolioId)
            .onErrorReturn(0L)
    }

    // TechnologyQueryRepository implementation

    /**
     * Finds a technology summary by its unique identifier.
     *
     * Retrieves a technology entity by ID and maps it to a summary model.
     * Returns empty Mono if no technology is found with the given ID.
     *
     * @param id The unique identifier of the technology
     * @return Mono containing the technology summary if found, empty otherwise
     */
    override fun findTechnologySummary(id: Long): Mono<TechnologySummary> {
        return technologyJpaRepository.findById(id)
            .map { it.toSummary() }
            .onErrorMap { e -> RuntimeException("Error finding technology summary for id $id", e) }
    }

    /**
     * Finds technology summaries for a specific portfolio.
     *
     * Retrieves all technology entities for the given portfolio and maps them
     * to summary models.
     *
     * @param portfolioId The unique identifier of the portfolio
     * @return Flux containing technology summaries in the portfolio
     */
    override fun findTechnologySummariesByPortfolio(portfolioId: Long): Flux<TechnologySummary> {
        return technologyJpaRepository.findByPortfolioId(portfolioId)
            .map { it.toSummary() }
            .onErrorMap { e -> RuntimeException("Error finding technology summaries for portfolio $portfolioId", e) }
    }

    /**
     * Finds technology summaries in a specific category.
     *
     * Retrieves all technology entities in the given category and maps them
     * to summary models.
     *
     * @param category The technology category to filter by
     * @return Flux containing technology summaries in the category
     */
    override fun findTechnologySummariesByCategory(category: String): Flux<TechnologySummary> {
        return technologyJpaRepository.findByCategory(category)
            .map { it.toSummary() }
            .onErrorMap { e -> RuntimeException("Error finding technology summaries for category $category", e) }
    }

    /**
     * Finds technology summaries of a specific type.
     *
     * Retrieves all technology entities of the given type and maps them
     * to summary models.
     *
     * @param type The technology type to filter by
     * @return Flux containing technology summaries of the specified type
     */
    override fun findTechnologySummariesByType(type: TechnologyType): Flux<TechnologySummary> {
        return technologyJpaRepository.findByType(type)
            .map { it.toSummary() }
            .onErrorMap { e -> RuntimeException("Error finding technology summaries for type $type", e) }
    }

    /**
     * Finds technology summaries from a specific vendor.
     *
     * Retrieves all technology entities from the given vendor and maps them
     * to summary models.
     *
     * @param vendorName The vendor name to filter by
     * @return Flux containing technology summaries from the vendor
     */
    override fun findTechnologySummariesByVendor(vendorName: String): Flux<TechnologySummary> {
        return technologyJpaRepository.findByVendorName(vendorName)
            .map { it.toSummary() }
            .onErrorMap { e -> RuntimeException("Error finding technology summaries for vendor $vendorName", e) }
    }

    /**
     * Retrieves all active technology summaries.
     *
     * Retrieves all technology entities with isActive=true and maps them
     * to summary models.
     *
     * @return Flux containing all active technology summaries
     */
    override fun findAllTechnologySummaries(): Flux<TechnologySummary> {
        return technologyJpaRepository.findByIsActiveTrue()
            .map { it.toSummary() }
            .onErrorMap { e -> RuntimeException("Error finding all technology summaries", e) }
    }

    /**
     * Searches technologies with flexible filtering criteria.
     *
     * Delegates to the R2DBC repository for searching, then maps results
     * to summary models.
     *
     * @param name Optional name filter for partial matching
     * @param category Optional category filter
     * @param type Optional technology type filter
     * @param vendorName Optional vendor name filter for partial matching
     * @return Flux containing technology summaries matching the search criteria
     */
    override fun searchTechnologies(
        name: String?,
        category: String?,
        type: TechnologyType?,
        vendorName: String?
    ): Flux<TechnologySummary> {
        return technologyJpaRepository.searchTechnologies(name, category, type, vendorName)
            .map { it.toSummary() }
            .onErrorMap { e -> RuntimeException("Error searching technologies", e) }
    }

    // Extension functions for mapping

    /**
     * Converts a technology entity to a domain model.
     *
     * Maps all properties from the R2DBC entity to the domain model.
     *
     * @return The domain technology model
     */
    private fun TechnologyEntity.toDomain(): Technology {
        return Technology(
            id = this.id,
            name = this.name,
            description = this.description,
            category = this.category,
            version = this.version,
            type = this.type,
            maturityLevel = this.maturityLevel,
            riskLevel = this.riskLevel,
            annualCost = this.annualCost,
            licenseCost = this.licenseCost,
            maintenanceCost = this.maintenanceCost,
            vendorName = this.vendorName,
            vendorContact = this.vendorContact,
            supportContractExpiry = this.supportContractExpiry,
            isActive = this.isActive,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
            portfolioId = this.portfolioId
        )
    }

    /**
     * Converts a domain technology model to an entity.
     *
     * Maps all properties from the domain model to the R2DBC entity.
     *
     * @return The technology R2DBC entity
     */
    private fun Technology.toEntity(): TechnologyEntity {
        return TechnologyEntity(
            id = this.id,
            name = this.name,
            description = this.description,
            category = this.category,
            version = this.version,
            type = this.type,
            maturityLevel = this.maturityLevel,
            riskLevel = this.riskLevel,
            annualCost = this.annualCost,
            licenseCost = this.licenseCost,
            maintenanceCost = this.maintenanceCost,
            vendorName = this.vendorName,
            vendorContact = this.vendorContact,
            supportContractExpiry = this.supportContractExpiry,
            isActive = this.isActive,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
            portfolioId = this.portfolioId
        )
    }

    /**
     * Converts a technology entity to a summary model.
     *
     * Maps essential properties from the R2DBC entity to the summary model.
     *
     * @return The technology summary model
     */
    private fun TechnologyEntity.toSummary(): TechnologySummary {
        return TechnologySummary(
            id = this.id!!,
            name = this.name,
            category = this.category,
            type = this.type,
            maturityLevel = this.maturityLevel,
            riskLevel = this.riskLevel,
            annualCost = this.annualCost,
            vendorName = this.vendorName
        )
    }
} 