package com.company.techportfolio.portfolio.adapter.out.persistence

import com.company.techportfolio.portfolio.adapter.out.persistence.repository.TechnologyJpaRepository
import com.company.techportfolio.portfolio.adapter.out.persistence.entity.TechnologyEntity
import com.company.techportfolio.portfolio.domain.port.TechnologyRepository
import com.company.techportfolio.portfolio.domain.port.TechnologyQueryRepository
import com.company.techportfolio.shared.domain.model.Technology
import com.company.techportfolio.shared.domain.model.TechnologyType
import com.company.techportfolio.portfolio.domain.model.TechnologySummary
import org.springframework.stereotype.Repository

/**
 * Technology Repository Adapter - Persistence Implementation
 * 
 * This adapter class implements both the TechnologyRepository and TechnologyQueryRepository
 * interfaces from the domain layer, providing the persistence implementation for
 * technology data access within the hexagonal architecture. It translates between
 * domain models and JPA entities, delegating actual database operations to Spring Data
 * JPA repositories.
 * 
 * ## Architecture Role:
 * - Acts as an **outbound adapter** in the hexagonal architecture
 * - Implements domain port interfaces with infrastructure-specific code
 * - Isolates the domain layer from persistence implementation details
 * - Handles the translation between domain models and persistence entities
 * 
 * ## Implementation Features:
 * - Dual implementation of both standard and query-optimized repository interfaces
 * - Mapping between domain models and JPA entities via extension functions
 * - Efficient summary data retrieval for list operations
 * - Error handling for database operations with graceful fallbacks
 * - Support for various technology filtering criteria
 * 
 * ## Performance Considerations:
 * - Optimized for portfolio-based technology access patterns
 * - Lightweight summary objects for list operations
 * - Efficient search with multiple optional criteria
 * 
 * @property technologyJpaRepository Spring Data JPA repository for technology entities
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
     * Returns null if no technology is found with the given ID.
     * 
     * @param id The unique identifier of the technology
     * @return The domain technology model if found, null otherwise
     */
    override fun findById(id: Long): Technology? {
        return technologyJpaRepository.findById(id).orElse(null)?.toDomain()
    }

    /**
     * Finds a technology by its name.
     * 
     * Retrieves a technology entity by name and maps it to the domain model.
     * Used for name-based lookups across the system.
     * 
     * @param name The name of the technology
     * @return The domain technology model if found, null otherwise
     */
    override fun findByName(name: String): Technology? {
        return technologyJpaRepository.findByName(name)?.toDomain()
    }

    /**
     * Finds all technologies associated with a specific portfolio.
     * 
     * Retrieves all technology entities for the given portfolio and maps them
     * to domain models. Returns an empty list if no technologies are found.
     * 
     * @param portfolioId The unique identifier of the portfolio
     * @return List of domain technology models in the portfolio
     */
    override fun findByPortfolioId(portfolioId: Long): List<Technology> {
        return technologyJpaRepository.findByPortfolioId(portfolioId).map { it.toDomain() }
    }

    /**
     * Finds all technologies in a specific category.
     * 
     * Retrieves all technology entities in the given category and maps them
     * to domain models. Returns an empty list if no technologies are found.
     * 
     * @param category The technology category to filter by
     * @return List of domain technology models in the category
     */
    override fun findByCategory(category: String): List<Technology> {
        return technologyJpaRepository.findByCategory(category).map { it.toDomain() }
    }

    /**
     * Finds all technologies of a specific type.
     * 
     * Retrieves all technology entities of the given type and maps them
     * to domain models. Returns an empty list if no technologies are found.
     * 
     * @param type The technology type to filter by
     * @return List of domain technology models of the specified type
     */
    override fun findByType(type: TechnologyType): List<Technology> {
        return technologyJpaRepository.findByType(type).map { it.toDomain() }
    }

    /**
     * Finds all technologies from a specific vendor.
     * 
     * Retrieves all technology entities from the given vendor and maps them
     * to domain models. Returns an empty list if no technologies are found.
     * 
     * @param vendorName The vendor name to filter by
     * @return List of domain technology models from the vendor
     */
    override fun findByVendor(vendorName: String): List<Technology> {
        return technologyJpaRepository.findByVendorName(vendorName).map { it.toDomain() }
    }

    /**
     * Finds all technologies with a specific maturity level.
     * 
     * Retrieves all technology entities with the given maturity level and maps them
     * to domain models. Returns an empty list if no technologies are found.
     * 
     * @param maturityLevel The maturity level to filter by
     * @return List of domain technology models with the specified maturity level
     */
    override fun findByMaturityLevel(maturityLevel: com.company.techportfolio.shared.domain.model.MaturityLevel): List<Technology> {
        return technologyJpaRepository.findByMaturityLevel(maturityLevel).map { it.toDomain() }
    }

    /**
     * Finds all technologies with a specific risk level.
     * 
     * Retrieves all technology entities with the given risk level and maps them
     * to domain models. Returns an empty list if no technologies are found.
     * 
     * @param riskLevel The risk level to filter by
     * @return List of domain technology models with the specified risk level
     */
    override fun findByRiskLevel(riskLevel: com.company.techportfolio.shared.domain.model.RiskLevel): List<Technology> {
        return technologyJpaRepository.findByRiskLevel(riskLevel).map { it.toDomain() }
    }

    /**
     * Saves a new technology to the repository.
     * 
     * Converts the domain model to an entity, saves it to the database,
     * and returns the updated domain model with generated ID.
     * 
     * @param technology The domain technology model to save
     * @return The saved domain technology model with generated ID
     */
    override fun save(technology: Technology): Technology {
        val entity = technology.toEntity()
        val savedEntity = technologyJpaRepository.save(entity)
        return savedEntity.toDomain()
    }

    /**
     * Updates an existing technology in the repository.
     * 
     * Converts the domain model to an entity, updates it in the database,
     * and returns the updated domain model.
     * 
     * @param technology The domain technology model to update
     * @return The updated domain technology model
     */
    override fun update(technology: Technology): Technology {
        val entity = technology.toEntity()
        val savedEntity = technologyJpaRepository.save(entity)
        return savedEntity.toDomain()
    }

    /**
     * Deletes a technology by its unique identifier.
     * 
     * Attempts to delete the technology entity with the given ID.
     * Returns true if successful, false if the technology doesn't exist
     * or another error occurs.
     * 
     * @param id The unique identifier of the technology to delete
     * @return true if the technology was deleted, false otherwise
     */
    override fun delete(id: Long): Boolean {
        return try {
            technologyJpaRepository.deleteById(id)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Checks if a technology exists by its unique identifier.
     * 
     * Delegates to the JPA repository to check for entity existence.
     * 
     * @param id The unique identifier of the technology
     * @return true if the technology exists, false otherwise
     */
    override fun existsById(id: Long): Boolean {
        return technologyJpaRepository.existsById(id)
    }

    /**
     * Counts the number of technologies in a specific portfolio.
     * 
     * Delegates to the JPA repository to count entities by portfolio.
     * 
     * @param portfolioId The unique identifier of the portfolio
     * @return The number of technologies in the portfolio
     */
    override fun countByPortfolioId(portfolioId: Long): Long {
        return technologyJpaRepository.countByPortfolioId(portfolioId)
    }

    // TechnologyQueryRepository implementation
    
    /**
     * Finds a technology summary by its unique identifier.
     * 
     * Retrieves a technology entity by ID and maps it to a summary model.
     * Returns null if no technology is found with the given ID.
     * 
     * @param id The unique identifier of the technology
     * @return The technology summary if found, null otherwise
     */
    override fun findTechnologySummary(id: Long): TechnologySummary? {
        return technologyJpaRepository.findById(id).orElse(null)?.toSummary()
    }

    /**
     * Finds technology summaries for a specific portfolio.
     * 
     * Retrieves all technology entities for the given portfolio and maps them
     * to summary models. Returns an empty list if no technologies are found.
     * 
     * @param portfolioId The unique identifier of the portfolio
     * @return List of technology summaries in the portfolio
     */
    override fun findTechnologySummariesByPortfolio(portfolioId: Long): List<TechnologySummary> {
        return technologyJpaRepository.findByPortfolioId(portfolioId).map { it.toSummary() }
    }

    /**
     * Finds technology summaries in a specific category.
     * 
     * Retrieves all technology entities in the given category and maps them
     * to summary models. Returns an empty list if no technologies are found.
     * 
     * @param category The technology category to filter by
     * @return List of technology summaries in the category
     */
    override fun findTechnologySummariesByCategory(category: String): List<TechnologySummary> {
        return technologyJpaRepository.findByCategory(category).map { it.toSummary() }
    }

    /**
     * Finds technology summaries of a specific type.
     * 
     * Retrieves all technology entities of the given type and maps them
     * to summary models. Returns an empty list if no technologies are found.
     * 
     * @param type The technology type to filter by
     * @return List of technology summaries of the specified type
     */
    override fun findTechnologySummariesByType(type: TechnologyType): List<TechnologySummary> {
        return technologyJpaRepository.findByType(type).map { it.toSummary() }
    }

    /**
     * Finds technology summaries from a specific vendor.
     * 
     * Retrieves all technology entities from the given vendor and maps them
     * to summary models. Returns an empty list if no technologies are found.
     * 
     * @param vendorName The vendor name to filter by
     * @return List of technology summaries from the vendor
     */
    override fun findTechnologySummariesByVendor(vendorName: String): List<TechnologySummary> {
        return technologyJpaRepository.findByVendorName(vendorName).map { it.toSummary() }
    }

    /**
     * Retrieves all active technology summaries.
     * 
     * Retrieves all technology entities with isActive=true and maps them
     * to summary models. Returns an empty list if no technologies are found.
     * 
     * @return List of all active technology summaries
     */
    override fun findAllTechnologySummaries(): List<TechnologySummary> {
        return technologyJpaRepository.findByIsActiveTrue().map { it.toSummary() }
    }

    /**
     * Searches technologies with flexible filtering criteria.
     * 
     * Delegates to the JPA repository for searching and maps results to summary models.
     * All parameters are optional, allowing for flexible search combinations.
     * 
     * @param name Optional name filter for partial matching
     * @param category Optional category filter
     * @param type Optional technology type filter
     * @param vendorName Optional vendor name filter
     * @return List of technology summaries matching the search criteria
     */
    override fun searchTechnologies(name: String?, category: String?, type: TechnologyType?, vendorName: String?): List<TechnologySummary> {
        return technologyJpaRepository.searchTechnologies(name, category, type, vendorName).map { it.toSummary() }
    }

    /**
     * Converts a technology entity to a domain model.
     * 
     * Maps all properties from the JPA entity to the domain model.
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
     * Maps all properties from the domain model to the JPA entity.
     * 
     * @return The technology JPA entity
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
     * Maps essential properties from the JPA entity to the summary model.
     * The summary contains only the most important information needed for listings.
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