package com.company.techportfolio.portfolio.adapter.out.persistence

import com.company.techportfolio.portfolio.adapter.out.persistence.entity.PortfolioEntity
import com.company.techportfolio.portfolio.adapter.out.persistence.repository.PortfolioJpaRepository
import com.company.techportfolio.portfolio.adapter.out.persistence.repository.TechnologyJpaRepository
import com.company.techportfolio.portfolio.domain.model.PortfolioSummary
import com.company.techportfolio.portfolio.domain.port.PortfolioQueryRepository
import com.company.techportfolio.portfolio.domain.port.PortfolioRepository
import com.company.techportfolio.shared.domain.model.PortfolioStatus
import com.company.techportfolio.shared.domain.model.PortfolioType
import com.company.techportfolio.shared.domain.model.TechnologyPortfolio
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal

/**
 * Portfolio Repository Adapter - Reactive Persistence Implementation
 *
 * This adapter class implements both the PortfolioRepository and PortfolioQueryRepository
 * interfaces from the domain layer, providing the reactive persistence implementation for
 * portfolio data access within the hexagonal architecture. It translates between
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
 * - Cost calculation aggregation for portfolio financial reporting
 * - Reactive error handling for database operations with graceful fallbacks
 * - Optimized query execution patterns for summary data
 * - Non-blocking database operations throughout
 *
 * ## Performance Considerations:
 * - Reactive streaming of technologies for portfolio responses
 * - Cost calculations performed reactively after database retrieval
 * - Batch processing for summary data to minimize database round trips
 * - Non-blocking I/O improves concurrency and resource utilization
 *
 * @property portfolioJpaRepository Spring Data R2DBC repository for portfolio entities
 * @property technologyJpaRepository Spring Data R2DBC repository for technology entities
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

    private val logger: Logger = LoggerFactory.getLogger(PortfolioRepositoryAdapter::class.java)

    // PortfolioRepository implementation

    /**
     * Finds a portfolio by its unique identifier.
     *
     * Retrieves a portfolio entity by ID and maps it to the domain model.
     * Returns empty Mono if no portfolio is found with the given ID.
     *
     * @param id The unique identifier of the portfolio
     * @return Mono containing the domain portfolio model if found, empty otherwise
     */
    override fun findById(id: Long): Mono<TechnologyPortfolio> {
        return portfolioJpaRepository.findById(id)
            .map { it.toDomain() }
            .onErrorMap { e -> RuntimeException("Error finding portfolio with id $id", e) }
    }

    /**
     * Finds a portfolio by its unique name.
     *
     * Retrieves a portfolio entity by name and maps it to the domain model.
     * Used for name uniqueness validation during portfolio creation.
     *
     * @param name The unique name of the portfolio
     * @return Mono containing the domain portfolio model if found, empty otherwise
     */
    override fun findByName(name: String): Mono<TechnologyPortfolio> {
        return portfolioJpaRepository.findByName(name)
            .map { it.toDomain() }
            .onErrorMap { e -> RuntimeException("Error finding portfolio with name $name", e) }
    }

    /**
     * Finds all portfolios owned by a specific user.
     *
     * Retrieves all portfolio entities for the given owner and maps them
     * to domain models. Returns empty Flux if no portfolios are found.
     *
     * @param ownerId The unique identifier of the owner
     * @return Flux containing domain portfolio models owned by the user
     */
    override fun findByOwnerId(ownerId: Long): Flux<TechnologyPortfolio> {
        return portfolioJpaRepository.findByOwnerId(ownerId)
            .map { it.toDomain() }
            .onErrorMap { e -> RuntimeException("Error finding portfolios for owner $ownerId", e) }
    }

    /**
     * Finds all portfolios belonging to a specific organization.
     *
     * Retrieves all portfolio entities for the given organization and maps them
     * to domain models. Returns empty Flux if no portfolios are found.
     *
     * @param organizationId The unique identifier of the organization
     * @return Flux containing domain portfolio models in the organization
     */
    override fun findByOrganizationId(organizationId: Long): Flux<TechnologyPortfolio> {
        return portfolioJpaRepository.findByOrganizationId(organizationId)
            .map { it.toDomain() }
            .onErrorMap { e -> RuntimeException("Error finding portfolios for organization $organizationId", e) }
    }

    /**
     * Finds all portfolios of a specific type.
     *
     * Retrieves all portfolio entities of the given type and maps them
     * to domain models. Returns empty Flux if no portfolios are found.
     *
     * @param type The portfolio type to filter by
     * @return Flux containing domain portfolio models of the specified type
     */
    override fun findByType(type: PortfolioType): Flux<TechnologyPortfolio> {
        return portfolioJpaRepository.findByType(type)
            .map { it.toDomain() }
            .onErrorMap { e -> RuntimeException("Error finding portfolios for type $type", e) }
    }

    /**
     * Finds all portfolios with a specific status.
     *
     * Retrieves all portfolio entities with the given status and maps them
     * to domain models. Returns empty Flux if no portfolios are found.
     *
     * @param status The portfolio status to filter by
     * @return Flux containing domain portfolio models with the specified status
     */
    override fun findByStatus(status: PortfolioStatus): Flux<TechnologyPortfolio> {
        return portfolioJpaRepository.findByStatus(status)
            .map { it.toDomain() }
            .onErrorMap { e -> RuntimeException("Error finding portfolios for status $status", e) }
    }

    /**
     * Retrieves all active portfolios in the system.
     *
     * Retrieves all portfolio entities with isActive=true and maps them
     * to domain models. Returns empty Flux if no portfolios are found.
     *
     * @return Flux containing all active domain portfolio models
     */
    override fun findAll(): Flux<TechnologyPortfolio> {
        return portfolioJpaRepository.findByIsActiveTrue()
            .map { it.toDomain() }
            .onErrorMap { e -> RuntimeException("Error finding all portfolios", e) }
    }

    /**
     * Saves a new portfolio to the repository.
     *
     * Converts the domain model to an entity, saves it to the database,
     * and returns the updated domain model with generated ID.
     *
     * @param portfolio The domain portfolio model to save
     * @return Mono containing the saved domain portfolio model with generated ID
     */
    override fun save(portfolio: TechnologyPortfolio): Mono<TechnologyPortfolio> {
        return Mono.just(portfolio.toEntity())
            .flatMap { entity -> portfolioJpaRepository.save(entity) }
            .map { it.toDomain() }
            .onErrorMap { e -> RuntimeException("Error saving portfolio ${portfolio.name}", e) }
    }

    /**
     * Updates an existing portfolio in the repository.
     *
     * Converts the domain model to an entity, updates it in the database,
     * and returns the updated domain model.
     *
     * @param portfolio The domain portfolio model to update
     * @return Mono containing the updated domain portfolio model
     */
    override fun update(portfolio: TechnologyPortfolio): Mono<TechnologyPortfolio> {
        return Mono.just(portfolio.toEntity())
            .flatMap { entity -> portfolioJpaRepository.save(entity) }
            .map { it.toDomain() }
            .onErrorMap { e -> RuntimeException("Error updating portfolio ${portfolio.name}", e) }
    }

    /**
     * Deletes a portfolio by its unique identifier.
     *
     * Attempts to delete the portfolio entity with the given ID.
     * Returns true if successful, false if the portfolio doesn't exist
     * or another error occurs.
     *
     * @param id The unique identifier of the portfolio to delete
     * @return Mono<Boolean> containing true if the portfolio was deleted, false otherwise
     */
    override fun delete(id: Long): Mono<Boolean> {
        return portfolioJpaRepository.existsById(id)
            .flatMap { exists ->
                if (exists) {
                    portfolioJpaRepository.deleteById(id)
                        .then(Mono.just(true))
                } else {
                    Mono.just(false)
                }
            }
            .onErrorReturn(false)
    }

    /**
     * Checks if a portfolio exists by its unique identifier.
     *
     * Delegates to the R2DBC repository to check for entity existence.
     *
     * @param id The unique identifier of the portfolio
     * @return Mono<Boolean> containing true if the portfolio exists, false otherwise
     */
    override fun existsById(id: Long): Mono<Boolean> {
        return portfolioJpaRepository.existsById(id)
            .onErrorReturn(false)
    }

    /**
     * Counts the number of portfolios owned by a specific user.
     *
     * Delegates to the R2DBC repository to count entities by owner.
     *
     * @param ownerId The unique identifier of the owner
     * @return Mono<Long> containing the number of portfolios owned by the user
     */
    override fun countByOwnerId(ownerId: Long): Mono<Long> {
        return portfolioJpaRepository.countByOwnerId(ownerId)
            .onErrorReturn(0L)
    }

    /**
     * Counts the number of portfolios in a specific organization.
     *
     * Delegates to the R2DBC repository to count entities by organization.
     *
     * @param organizationId The unique identifier of the organization
     * @return Mono<Long> containing the number of portfolios in the organization
     */
    override fun countByOrganizationId(organizationId: Long): Mono<Long> {
        return portfolioJpaRepository.countByOrganizationId(organizationId)
            .onErrorReturn(0L)
    }

    // PortfolioQueryRepository implementation

    /**
     * Finds a portfolio summary by its unique identifier.
     *
     * Retrieves a portfolio entity by ID, calculates technology count and
     * total cost, and maps it to a summary model. Returns empty Mono if no
     * portfolio is found with the given ID.
     *
     * @param id The unique identifier of the portfolio
     * @return Mono containing the portfolio summary if found, empty otherwise
     */
    override fun findPortfolioSummary(id: Long): Mono<PortfolioSummary> {
        return portfolioJpaRepository.findById(id)
            .flatMap { portfolio ->
                Mono.zip(
                    technologyJpaRepository.countByPortfolioId(id),
                    calculateTotalAnnualCost(id)
                ).map { tuple ->
                    portfolio.toSummary(tuple.t1, tuple.t2)
                }
            }
            .onErrorMap { e -> RuntimeException("Error finding portfolio summary for id $id", e) }
    }

    /**
     * Finds portfolio summaries for a specific owner.
     *
     * Retrieves all portfolio entities for the given owner, calculates
     * technology counts and total costs, and maps them to summary models.
     *
     * @param ownerId The unique identifier of the portfolio owner
     * @return Flux containing portfolio summaries owned by the specified user
     */
    override fun findPortfolioSummariesByOwner(ownerId: Long): Flux<PortfolioSummary> {
        return portfolioJpaRepository.findByOwnerId(ownerId)
            .flatMap { portfolio ->
                portfolio.id?.let { portfolioId ->
                    Mono.zip(
                        technologyJpaRepository.countByPortfolioId(portfolioId),
                        calculateTotalAnnualCost(portfolioId)
                    ).map { tuple ->
                        portfolio.toSummary(tuple.t1, tuple.t2)
                    }
                } ?: Mono.empty<PortfolioSummary>()
            }
            .onErrorMap { e -> RuntimeException("Error finding portfolio summaries for owner $ownerId", e) }
    }

    /**
     * Finds portfolio summaries for a specific organization.
     *
     * Retrieves all portfolio entities for the given organization, calculates
     * technology counts and total costs, and maps them to summary models.
     *
     * @param organizationId The unique identifier of the organization
     * @return Flux containing portfolio summaries in the organization
     */
    override fun findPortfolioSummariesByOrganization(organizationId: Long): Flux<PortfolioSummary> {
        return portfolioJpaRepository.findByOrganizationId(organizationId)
            .flatMap { portfolio ->
                portfolio.id?.let { portfolioId ->
                    Mono.zip(
                        technologyJpaRepository.countByPortfolioId(portfolioId),
                        calculateTotalAnnualCost(portfolioId)
                    ).map { tuple ->
                        portfolio.toSummary(tuple.t1, tuple.t2)
                    }
                } ?: Mono.empty<PortfolioSummary>()
            }
            .onErrorMap { e ->
                RuntimeException(
                    "Error finding portfolio summaries for organization $organizationId",
                    e
                )
            }
    }

    /**
     * Finds portfolio summaries of a specific type.
     *
     * Retrieves all portfolio entities of the given type, calculates
     * technology counts and total costs, and maps them to summary models.
     *
     * @param type The portfolio type to filter by
     * @return Flux containing portfolio summaries of the specified type
     */
    override fun findPortfolioSummariesByType(type: PortfolioType): Flux<PortfolioSummary> {
        return portfolioJpaRepository.findByType(type)
            .flatMap { portfolio ->
                portfolio.id?.let { portfolioId ->
                    Mono.zip(
                        technologyJpaRepository.countByPortfolioId(portfolioId),
                        calculateTotalAnnualCost(portfolioId)
                    ).map { tuple ->
                        portfolio.toSummary(tuple.t1, tuple.t2)
                    }
                } ?: Mono.empty<PortfolioSummary>()
            }
            .onErrorMap { e -> RuntimeException("Error finding portfolio summaries for type $type", e) }
    }

    /**
     * Finds portfolio summaries with a specific status.
     *
     * Retrieves all portfolio entities with the given status, calculates
     * technology counts and total costs, and maps them to summary models.
     *
     * @param status The portfolio status to filter by
     * @return Flux containing portfolio summaries with the specified status
     */
    override fun findPortfolioSummariesByStatus(status: PortfolioStatus): Flux<PortfolioSummary> {
        return portfolioJpaRepository.findByStatus(status)
            .flatMap { portfolio ->
                portfolio.id?.let { portfolioId ->
                    Mono.zip(
                        technologyJpaRepository.countByPortfolioId(portfolioId),
                        calculateTotalAnnualCost(portfolioId)
                    ).map { tuple ->
                        portfolio.toSummary(tuple.t1, tuple.t2)
                    }
                } ?: Mono.empty<PortfolioSummary>()
            }
            .onErrorMap { e -> RuntimeException("Error finding portfolio summaries for status $status", e) }
    }

    /**
     * Retrieves all active portfolio summaries.
     *
     * Retrieves all portfolio entities with isActive=true, calculates
     * technology counts and total costs, and maps them to summary models.
     *
     * @return Flux containing all active portfolio summaries
     */
    override fun findAllPortfolioSummaries(): Flux<PortfolioSummary> {
        return portfolioJpaRepository.findByIsActiveTrue()
            .flatMap { portfolio ->
                portfolio.id?.let { portfolioId ->
                    Mono.zip(
                        technologyJpaRepository.countByPortfolioId(portfolioId),
                        calculateTotalAnnualCost(portfolioId)
                    ).map { tuple ->
                        portfolio.toSummary(tuple.t1, tuple.t2)
                    }
                } ?: Mono.empty<PortfolioSummary>()
            }
            .onErrorMap { e -> RuntimeException("Error finding all portfolio summaries", e) }
    }

    /**
     * Searches portfolios with flexible filtering criteria.
     *
     * Delegates to the R2DBC repository for searching, then calculates
     * technology counts and total costs, and maps results to summary models.
     *
     * @param name Optional name filter for partial matching
     * @param type Optional portfolio type filter
     * @param status Optional portfolio status filter
     * @param organizationId Optional organization scope filter
     * @return Flux containing portfolio summaries matching the search criteria
     */
    override fun searchPortfolios(
        name: String?,
        type: PortfolioType?,
        status: PortfolioStatus?,
        organizationId: Long?
    ): Flux<PortfolioSummary> {
        return portfolioJpaRepository.searchPortfolios(name, type, status, organizationId)
            .flatMap { portfolio ->
                portfolio.id?.let { portfolioId ->
                    Mono.zip(
                        technologyJpaRepository.countByPortfolioId(portfolioId),
                        calculateTotalAnnualCost(portfolioId)
                    ).map { tuple ->
                        portfolio.toSummary(tuple.t1, tuple.t2)
                    }
                } ?: Mono.empty<PortfolioSummary>()
            }
            .onErrorMap { e -> RuntimeException("Error searching portfolios", e) }
    }

    /**
     * Calculates the total annual cost for a portfolio.
     *
     * Retrieves all technologies in the portfolio, extracts their annual costs,
     * and sums them up. Returns zero if no technologies have costs defined.
     *
     * @param portfolioId The unique identifier of the portfolio
     * @return Mono<BigDecimal> containing the total annual cost of all technologies in the portfolio
     */
    private fun calculateTotalAnnualCost(portfolioId: Long): Mono<BigDecimal> {
        return technologyJpaRepository.findByPortfolioId(portfolioId)
            .mapNotNull { it.annualCost }
            .reduce(BigDecimal.ZERO) { acc, cost -> acc.add(cost) }
            .defaultIfEmpty(BigDecimal.ZERO)
            .onErrorReturn(BigDecimal.ZERO)
    }

    // Extension functions for mapping

    /**
     * Converts a portfolio entity to a domain model.
     *
     * Maps all properties from the R2DBC entity to the domain model.
     *
     * @return The domain portfolio model
     */
    private fun PortfolioEntity.toDomain(): TechnologyPortfolio {
        logger.debug("Converting PortfolioEntity to domain - ID: ${this.id}, Name: '${this.name}', Type: ${this.type}")
        val result = TechnologyPortfolio(
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
        logger.debug("Created TechnologyPortfolio domain object - ID: ${result.id}, Name: '${result.name}', Type: ${result.type}")
        return result
    }

    /**
     * Converts a domain portfolio model to an entity.
     *
     * Maps all properties from the domain model to the R2DBC entity.
     *
     * @return The portfolio R2DBC entity
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
     * Maps essential properties from the R2DBC entity to the summary model,
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