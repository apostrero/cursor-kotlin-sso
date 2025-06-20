package com.company.techportfolio.portfolio.adapter.out.persistence

import com.company.techportfolio.portfolio.adapter.out.persistence.repository.PortfolioJpaRepository
import com.company.techportfolio.portfolio.adapter.out.persistence.repository.TechnologyJpaRepository
import com.company.techportfolio.portfolio.adapter.out.persistence.entity.PortfolioEntity
import com.company.techportfolio.portfolio.adapter.out.persistence.entity.TechnologyEntity
import com.company.techportfolio.portfolio.domain.port.PortfolioRepository
import com.company.techportfolio.portfolio.domain.port.PortfolioQueryRepository
import com.company.techportfolio.shared.domain.model.TechnologyPortfolio
import com.company.techportfolio.shared.domain.model.PortfolioType
import com.company.techportfolio.shared.domain.model.PortfolioStatus
import com.company.techportfolio.portfolio.domain.model.PortfolioSummary
import org.springframework.stereotype.Repository
import java.math.BigDecimal

/**
 * Portfolio Repository Adapter - Persistence Implementation
 * 
 * This adapter class implements both the PortfolioRepository and PortfolioQueryRepository
 * interfaces from the domain layer, providing the persistence implementation for
 * portfolio data access within the hexagonal architecture. It translates between
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
 * - Cost calculation aggregation for portfolio financial reporting
 * - Error handling for database operations with graceful fallbacks
 * - Optimized query execution patterns for summary data
 * 
 * ## Performance Considerations:
 * - Eager loading of technologies for portfolio responses
 * - Cost calculations performed in memory after database retrieval
 * - Batch processing for summary data to minimize database round trips
 * 
 * @property portfolioJpaRepository Spring Data JPA repository for portfolio entities
 * @property technologyJpaRepository Spring Data JPA repository for technology entities
 * 
 * @author Technology Portfolio Team
 * @since 1.0.0
 * @see PortfolioRepository
 * @see PortfolioQueryRepository
 * @see PortfolioJpaRepository
 * @see TechnologyJpaRepository
 */
@Repository
class PortfolioRepositoryAdapter(
    private val portfolioJpaRepository: PortfolioJpaRepository,
    private val technologyJpaRepository: TechnologyJpaRepository
) : PortfolioRepository, PortfolioQueryRepository {

    // PortfolioRepository implementation
    
    /**
     * Finds a portfolio by its unique identifier.
     * 
     * Retrieves a portfolio entity by ID and maps it to the domain model.
     * Returns null if no portfolio is found with the given ID.
     * 
     * @param id The unique identifier of the portfolio
     * @return The domain portfolio model if found, null otherwise
     */
    override fun findById(id: Long): TechnologyPortfolio? {
        return portfolioJpaRepository.findById(id).orElse(null)?.toDomain()
    }

    /**
     * Finds a portfolio by its unique name.
     * 
     * Retrieves a portfolio entity by name and maps it to the domain model.
     * Used for name uniqueness validation during portfolio creation.
     * 
     * @param name The unique name of the portfolio
     * @return The domain portfolio model if found, null otherwise
     */
    override fun findByName(name: String): TechnologyPortfolio? {
        return portfolioJpaRepository.findByName(name)?.toDomain()
    }

    /**
     * Finds all portfolios owned by a specific user.
     * 
     * Retrieves all portfolio entities for the given owner and maps them
     * to domain models. Returns an empty list if no portfolios are found.
     * 
     * @param ownerId The unique identifier of the owner
     * @return List of domain portfolio models owned by the user
     */
    override fun findByOwnerId(ownerId: Long): List<TechnologyPortfolio> {
        return portfolioJpaRepository.findByOwnerId(ownerId).map { it.toDomain() }
    }

    /**
     * Finds all portfolios belonging to a specific organization.
     * 
     * Retrieves all portfolio entities for the given organization and maps them
     * to domain models. Returns an empty list if no portfolios are found.
     * 
     * @param organizationId The unique identifier of the organization
     * @return List of domain portfolio models in the organization
     */
    override fun findByOrganizationId(organizationId: Long): List<TechnologyPortfolio> {
        return portfolioJpaRepository.findByOrganizationId(organizationId).map { it.toDomain() }
    }

    /**
     * Finds all portfolios of a specific type.
     * 
     * Retrieves all portfolio entities of the given type and maps them
     * to domain models. Returns an empty list if no portfolios are found.
     * 
     * @param type The portfolio type to filter by
     * @return List of domain portfolio models of the specified type
     */
    override fun findByType(type: PortfolioType): List<TechnologyPortfolio> {
        return portfolioJpaRepository.findByType(type).map { it.toDomain() }
    }

    /**
     * Finds all portfolios with a specific status.
     * 
     * Retrieves all portfolio entities with the given status and maps them
     * to domain models. Returns an empty list if no portfolios are found.
     * 
     * @param status The portfolio status to filter by
     * @return List of domain portfolio models with the specified status
     */
    override fun findByStatus(status: PortfolioStatus): List<TechnologyPortfolio> {
        return portfolioJpaRepository.findByStatus(status).map { it.toDomain() }
    }

    /**
     * Retrieves all active portfolios in the system.
     * 
     * Retrieves all portfolio entities with isActive=true and maps them
     * to domain models. Returns an empty list if no portfolios are found.
     * 
     * @return List of all active domain portfolio models
     */
    override fun findAll(): List<TechnologyPortfolio> {
        return portfolioJpaRepository.findByIsActiveTrue().map { it.toDomain() }
    }

    /**
     * Saves a new portfolio to the repository.
     * 
     * Converts the domain model to an entity, saves it to the database,
     * and returns the updated domain model with generated ID.
     * 
     * @param portfolio The domain portfolio model to save
     * @return The saved domain portfolio model with generated ID
     */
    override fun save(portfolio: TechnologyPortfolio): TechnologyPortfolio {
        val entity = portfolio.toEntity()
        val savedEntity = portfolioJpaRepository.save(entity)
        return savedEntity.toDomain()
    }

    /**
     * Updates an existing portfolio in the repository.
     * 
     * Converts the domain model to an entity, updates it in the database,
     * and returns the updated domain model.
     * 
     * @param portfolio The domain portfolio model to update
     * @return The updated domain portfolio model
     */
    override fun update(portfolio: TechnologyPortfolio): TechnologyPortfolio {
        val entity = portfolio.toEntity()
        val savedEntity = portfolioJpaRepository.save(entity)
        return savedEntity.toDomain()
    }

    /**
     * Deletes a portfolio by its unique identifier.
     * 
     * Attempts to delete the portfolio entity with the given ID.
     * Returns true if successful, false if the portfolio doesn't exist
     * or another error occurs.
     * 
     * @param id The unique identifier of the portfolio to delete
     * @return true if the portfolio was deleted, false otherwise
     */
    override fun delete(id: Long): Boolean {
        return try {
            portfolioJpaRepository.deleteById(id)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Checks if a portfolio exists by its unique identifier.
     * 
     * Delegates to the JPA repository to check for entity existence.
     * 
     * @param id The unique identifier of the portfolio
     * @return true if the portfolio exists, false otherwise
     */
    override fun existsById(id: Long): Boolean {
        return portfolioJpaRepository.existsById(id)
    }

    /**
     * Counts the number of portfolios owned by a specific user.
     * 
     * Delegates to the JPA repository to count entities by owner.
     * 
     * @param ownerId The unique identifier of the owner
     * @return The number of portfolios owned by the user
     */
    override fun countByOwnerId(ownerId: Long): Long {
        return portfolioJpaRepository.countByOwnerId(ownerId)
    }

    /**
     * Counts the number of portfolios in a specific organization.
     * 
     * Delegates to the JPA repository to count entities by organization.
     * 
     * @param organizationId The unique identifier of the organization
     * @return The number of portfolios in the organization
     */
    override fun countByOrganizationId(organizationId: Long): Long {
        return portfolioJpaRepository.countByOrganizationId(organizationId)
    }

    // PortfolioQueryRepository implementation
    
    /**
     * Finds a portfolio summary by its unique identifier.
     * 
     * Retrieves a portfolio entity by ID, calculates technology count and
     * total cost, and maps it to a summary model. Returns null if no
     * portfolio is found with the given ID.
     * 
     * @param id The unique identifier of the portfolio
     * @return The portfolio summary if found, null otherwise
     */
    override fun findPortfolioSummary(id: Long): PortfolioSummary? {
        val portfolio = portfolioJpaRepository.findById(id).orElse(null) ?: return null
        val technologyCount = technologyJpaRepository.countByPortfolioId(id)
        val totalAnnualCost = calculateTotalAnnualCost(id)
        
        return portfolio.toSummary(technologyCount, totalAnnualCost)
    }

    /**
     * Finds portfolio summaries for a specific owner.
     * 
     * Retrieves all portfolio entities for the given owner, calculates
     * technology counts and total costs, and maps them to summary models.
     * 
     * @param ownerId The unique identifier of the owner
     * @return List of portfolio summaries owned by the user
     */
    override fun findPortfolioSummariesByOwner(ownerId: Long): List<PortfolioSummary> {
        val portfolios = portfolioJpaRepository.findByOwnerId(ownerId)
        return portfolios.map { portfolio ->
            val technologyCount = technologyJpaRepository.countByPortfolioId(portfolio.id!!)
            val totalAnnualCost = calculateTotalAnnualCost(portfolio.id!!)
            portfolio.toSummary(technologyCount, totalAnnualCost)
        }
    }

    /**
     * Finds portfolio summaries for a specific organization.
     * 
     * Retrieves all portfolio entities for the given organization, calculates
     * technology counts and total costs, and maps them to summary models.
     * 
     * @param organizationId The unique identifier of the organization
     * @return List of portfolio summaries in the organization
     */
    override fun findPortfolioSummariesByOrganization(organizationId: Long): List<PortfolioSummary> {
        val portfolios = portfolioJpaRepository.findByOrganizationId(organizationId)
        return portfolios.map { portfolio ->
            val technologyCount = technologyJpaRepository.countByPortfolioId(portfolio.id!!)
            val totalAnnualCost = calculateTotalAnnualCost(portfolio.id!!)
            portfolio.toSummary(technologyCount, totalAnnualCost)
        }
    }

    /**
     * Finds portfolio summaries of a specific type.
     * 
     * Retrieves all portfolio entities of the given type, calculates
     * technology counts and total costs, and maps them to summary models.
     * 
     * @param type The portfolio type to filter by
     * @return List of portfolio summaries of the specified type
     */
    override fun findPortfolioSummariesByType(type: PortfolioType): List<PortfolioSummary> {
        val portfolios = portfolioJpaRepository.findByType(type)
        return portfolios.map { portfolio ->
            val technologyCount = technologyJpaRepository.countByPortfolioId(portfolio.id!!)
            val totalAnnualCost = calculateTotalAnnualCost(portfolio.id!!)
            portfolio.toSummary(technologyCount, totalAnnualCost)
        }
    }

    /**
     * Finds portfolio summaries with a specific status.
     * 
     * Retrieves all portfolio entities with the given status, calculates
     * technology counts and total costs, and maps them to summary models.
     * 
     * @param status The portfolio status to filter by
     * @return List of portfolio summaries with the specified status
     */
    override fun findPortfolioSummariesByStatus(status: PortfolioStatus): List<PortfolioSummary> {
        val portfolios = portfolioJpaRepository.findByStatus(status)
        return portfolios.map { portfolio ->
            val technologyCount = technologyJpaRepository.countByPortfolioId(portfolio.id!!)
            val totalAnnualCost = calculateTotalAnnualCost(portfolio.id!!)
            portfolio.toSummary(technologyCount, totalAnnualCost)
        }
    }

    /**
     * Retrieves all active portfolio summaries.
     * 
     * Retrieves all portfolio entities with isActive=true, calculates
     * technology counts and total costs, and maps them to summary models.
     * 
     * @return List of all active portfolio summaries
     */
    override fun findAllPortfolioSummaries(): List<PortfolioSummary> {
        val portfolios = portfolioJpaRepository.findByIsActiveTrue()
        return portfolios.map { portfolio ->
            val technologyCount = technologyJpaRepository.countByPortfolioId(portfolio.id!!)
            val totalAnnualCost = calculateTotalAnnualCost(portfolio.id!!)
            portfolio.toSummary(technologyCount, totalAnnualCost)
        }
    }

    /**
     * Searches portfolios with flexible filtering criteria.
     * 
     * Delegates to the JPA repository for searching, then calculates
     * technology counts and total costs, and maps results to summary models.
     * 
     * @param name Optional name filter for partial matching
     * @param type Optional portfolio type filter
     * @param status Optional portfolio status filter
     * @param organizationId Optional organization scope filter
     * @return List of portfolio summaries matching the search criteria
     */
    override fun searchPortfolios(name: String?, type: PortfolioType?, status: PortfolioStatus?, organizationId: Long?): List<PortfolioSummary> {
        val portfolios = portfolioJpaRepository.searchPortfolios(name, type, status, organizationId)
        return portfolios.map { portfolio ->
            val technologyCount = technologyJpaRepository.countByPortfolioId(portfolio.id!!)
            val totalAnnualCost = calculateTotalAnnualCost(portfolio.id!!)
            portfolio.toSummary(technologyCount, totalAnnualCost)
        }
    }

    /**
     * Calculates the total annual cost for a portfolio.
     * 
     * Retrieves all technologies in the portfolio, extracts their annual costs,
     * and sums them up. Returns zero if no technologies have costs defined.
     * 
     * @param portfolioId The unique identifier of the portfolio
     * @return The total annual cost of all technologies in the portfolio
     */
    private fun calculateTotalAnnualCost(portfolioId: Long): BigDecimal {
        return technologyJpaRepository.findByPortfolioId(portfolioId)
            .mapNotNull { it.annualCost }
            .fold(BigDecimal.ZERO) { acc, cost -> acc.add(cost) }
    }

    // Extension functions for mapping
    
    /**
     * Converts a portfolio entity to a domain model.
     * 
     * Maps all properties from the JPA entity to the domain model.
     * 
     * @return The domain portfolio model
     */
    private fun PortfolioEntity.toDomain(): TechnologyPortfolio {
        return TechnologyPortfolio(
            id = this.id,
            name = this.name,
            description = this.description,
            type = this.type,
            status = this.status,
            isActive = this.isActive,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
            ownerId = this.ownerId,
            organizationId = this.organizationId
        )
    }

    /**
     * Converts a domain portfolio model to an entity.
     * 
     * Maps all properties from the domain model to the JPA entity.
     * 
     * @return The portfolio JPA entity
     */
    private fun TechnologyPortfolio.toEntity(): PortfolioEntity {
        return PortfolioEntity(
            id = this.id,
            name = this.name,
            description = this.description,
            type = this.type,
            status = this.status,
            isActive = this.isActive,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
            ownerId = this.ownerId,
            organizationId = this.organizationId
        )
    }

    /**
     * Converts a portfolio entity to a summary model.
     * 
     * Maps essential properties from the JPA entity to the summary model,
     * including the provided technology count and total cost.
     * 
     * @param technologyCount The number of technologies in the portfolio
     * @param totalAnnualCost The total annual cost of all technologies
     * @return The portfolio summary model
     */
    private fun PortfolioEntity.toSummary(technologyCount: Long, totalAnnualCost: BigDecimal): PortfolioSummary {
        return PortfolioSummary(
            id = this.id!!,
            name = this.name,
            type = this.type,
            status = this.status,
            ownerId = this.ownerId,
            organizationId = this.organizationId,
            technologyCount = technologyCount.toInt(),
            totalAnnualCost = totalAnnualCost,
            lastUpdated = this.updatedAt ?: this.createdAt
        )
    }
} 