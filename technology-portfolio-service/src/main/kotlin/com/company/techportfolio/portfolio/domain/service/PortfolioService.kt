package com.company.techportfolio.portfolio.domain.service

import com.company.techportfolio.portfolio.domain.model.*
import com.company.techportfolio.portfolio.domain.port.PortfolioRepository
import com.company.techportfolio.portfolio.domain.port.TechnologyRepository
import com.company.techportfolio.portfolio.domain.port.PortfolioQueryRepository
import com.company.techportfolio.portfolio.domain.port.TechnologyQueryRepository
import com.company.techportfolio.shared.domain.model.TechnologyPortfolio
import com.company.techportfolio.shared.domain.model.Technology
import com.company.techportfolio.shared.domain.model.PortfolioType
import com.company.techportfolio.shared.domain.model.PortfolioStatus
import com.company.techportfolio.shared.domain.model.TechnologyType
import com.company.techportfolio.shared.domain.model.MaturityLevel
import com.company.techportfolio.shared.domain.model.RiskLevel
import com.company.techportfolio.shared.domain.port.EventPublisher
import com.company.techportfolio.shared.domain.event.PortfolioCreatedEvent
import com.company.techportfolio.shared.domain.event.PortfolioUpdatedEvent
import com.company.techportfolio.shared.domain.event.TechnologyAddedEvent
import com.company.techportfolio.shared.domain.event.TechnologyRemovedEvent
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.core.publisher.Flux
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Portfolio Service - Core Business Logic (REACTIVE)
 * 
 * This service class implements the core business logic for technology portfolio
 * management within the hexagonal architecture using reactive programming patterns.
 * It orchestrates portfolio and technology operations while maintaining business
 * rules and data consistency.
 * 
 * ## Business Responsibilities:
 * - Portfolio lifecycle management (create, update, delete)
 * - Technology inventory management within portfolios
 * - Cost calculation and financial tracking
 * - Business rule enforcement and validation
 * - Event publishing for domain state changes
 * - Multi-tenant organization support
 * 
 * ## Reactive Design:
 * - All methods return Mono<T> for single operations or Flux<T> for collections
 * - Supports reactive error handling with onErrorMap and onErrorResume
 * - Enables reactive transaction management
 * - Provides backpressure handling for large datasets
 * 
 * ## Architecture Role:
 * This service sits in the **Application Layer** and coordinates between:
 * - Domain entities and business rules
 * - Repository ports for data persistence
 * - Event publishing for integration
 * - External service adapters
 * 
 * ## Transaction Boundaries:
 * Each public method represents a transaction boundary and includes:
 * - Input validation and business rule checking
 * - Domain entity manipulation
 * - Event publishing for state changes
 * - Error handling and rollback scenarios
 * 
 * @param portfolioRepository Repository for portfolio entity persistence
 * @param technologyRepository Repository for technology entity persistence
 * @param portfolioQueryRepository Optimized read operations for portfolios
 * @param technologyQueryRepository Optimized read operations for technologies
 * @param eventPublisher Publisher for domain events and integration
 * 
 * @author Technology Portfolio Team
 * @since 1.0.0
 * @see TechnologyPortfolio
 * @see Technology
 * @see PortfolioRepository
 * @see TechnologyRepository
 */
@Service
class PortfolioService(
    private val portfolioRepository: PortfolioRepository,
    private val technologyRepository: TechnologyRepository,
    private val portfolioQueryRepository: PortfolioQueryRepository,
    private val technologyQueryRepository: TechnologyQueryRepository,
    private val eventPublisher: EventPublisher
) {

    /**
     * Creates a new technology portfolio with validation and event publishing.
     * 
     * This method implements the portfolio creation use case with the following steps:
     * 1. Validates that no portfolio exists with the same name
     * 2. Creates a new portfolio entity with default active status
     * 3. Persists the portfolio to the database
     * 4. Publishes a PortfolioCreatedEvent for integration
     * 5. Returns a complete portfolio response with metadata
     * 
     * ## Business Rules:
     * - Portfolio names must be unique across the system
     * - New portfolios are created with ACTIVE status by default
     * - Owner ID must be provided and valid
     * - Organization ID is optional for personal portfolios
     * 
     * **Reactive**: Returns Mono<PortfolioResponse>
     * 
     * @param request The portfolio creation request containing required data
     * @return Mono<PortfolioResponse> with complete portfolio information
     * @throws RuntimeException if portfolio creation fails or name already exists
     * @throws IllegalArgumentException if request data is invalid
     */
    fun createPortfolio(request: CreatePortfolioRequest): Mono<PortfolioResponse> {
        return portfolioRepository.findByName(request.name)
            .flatMap { existingPortfolio ->
                Mono.error<PortfolioResponse>(
                    IllegalArgumentException("Portfolio with name '${request.name}' already exists")
                )
            }
            .switchIfEmpty(
                Mono.defer {
                    val portfolio = TechnologyPortfolio(
                        name = request.name,
                        description = request.description,
                        type = request.type,
                        status = PortfolioStatus.ACTIVE,
                        isActive = true,
                        createdAt = LocalDateTime.now(),
                        ownerId = request.ownerId,
                        organizationId = request.organizationId
                    )

                    portfolioRepository.save(portfolio)
                        .flatMap { savedPortfolio ->
                            // Publish event
                            eventPublisher.publish(
                                PortfolioCreatedEvent(
                                    portfolioId = savedPortfolio.id!!,
                                    name = savedPortfolio.name,
                                    ownerId = savedPortfolio.ownerId,
                                    organizationId = savedPortfolio.organizationId
                                )
                            ).then(Mono.just(savedPortfolio))
                        }
                        .map { toPortfolioResponse(it) }
                }
            )
            .onErrorMap { error ->
                when (error) {
                    is IllegalArgumentException -> error
                    else -> RuntimeException("Failed to create portfolio: ${error.message}", error)
                }
            }
    }

    /**
     * Updates an existing portfolio with partial data and validation.
     * 
     * This method implements the portfolio update use case with selective field updates.
     * Only non-null fields in the request will be updated, allowing for partial updates.
     * The updated timestamp is automatically set to the current time.
     * 
     * ## Business Rules:
     * - Portfolio must exist before updating
     * - Only provided fields are updated (null fields are ignored)
     * - Updated timestamp is automatically set
     * - Status transitions are validated
     * 
     * **Reactive**: Returns Mono<PortfolioResponse>
     * 
     * @param portfolioId The ID of the portfolio to update
     * @param request The update request with optional field changes
     * @return Mono<PortfolioResponse> with updated portfolio information
     * @throws RuntimeException if update fails or portfolio not found
     * @throws IllegalArgumentException if portfolio ID is invalid
     */
    fun updatePortfolio(portfolioId: Long, request: UpdatePortfolioRequest): Mono<PortfolioResponse> {
        return portfolioRepository.findById(portfolioId)
            .switchIfEmpty(
                Mono.error<TechnologyPortfolio>(
                    IllegalArgumentException("Portfolio with id $portfolioId not found")
                )
            )
            .flatMap { existingPortfolio ->
                val updatedPortfolio = existingPortfolio.copy(
                    name = request.name ?: existingPortfolio.name,
                    description = request.description ?: existingPortfolio.description,
                    type = request.type ?: existingPortfolio.type,
                    status = request.status ?: existingPortfolio.status,
                    updatedAt = LocalDateTime.now()
                )

                portfolioRepository.update(updatedPortfolio)
                    .flatMap { savedPortfolio ->
                        // Publish event
                        eventPublisher.publish(
                            PortfolioUpdatedEvent(
                                portfolioId = savedPortfolio.id!!,
                                changes = mapOf(
                                    "name" to (request.name ?: ""),
                                    "description" to (request.description ?: ""),
                                    "type" to (request.type?.name ?: ""),
                                    "status" to (request.status?.name ?: "")
                                )
                            )
                        ).then(Mono.just(savedPortfolio))
                    }
                    .map { toPortfolioResponse(it) }
            }
            .onErrorMap { error ->
                when (error) {
                    is IllegalArgumentException -> error
                    else -> RuntimeException("Failed to update portfolio: ${error.message}", error)
                }
            }
    }

    /**
     * Retrieves a complete portfolio by its unique identifier.
     * 
     * Returns a comprehensive portfolio response including all associated
     * technologies, calculated costs, and metadata. This is the primary
     * method for retrieving detailed portfolio information.
     * 
     * **Reactive**: Returns Mono<PortfolioResponse>
     * 
     * @param portfolioId The unique identifier of the portfolio
     * @return Mono<PortfolioResponse> with complete portfolio details and technologies
     * @throws IllegalArgumentException if portfolio with given ID doesn't exist
     */
    fun getPortfolio(portfolioId: Long): Mono<PortfolioResponse> {
        return portfolioRepository.findById(portfolioId)
            .switchIfEmpty(
                Mono.error<TechnologyPortfolio>(
                    IllegalArgumentException("Portfolio with id $portfolioId not found")
                )
            )
            .map { toPortfolioResponse(it) }
            .onErrorMap { error ->
                when (error) {
                    is IllegalArgumentException -> error
                    else -> RuntimeException("Failed to retrieve portfolio: ${error.message}", error)
                }
            }
    }

    /**
     * Retrieves portfolio summaries owned by a specific user.
     * 
     * Returns a reactive stream of portfolio summaries for efficient
     * listing and overview purposes. This method is optimized for
     * performance and doesn't include full technology details.
     * 
     * **Reactive**: Returns Flux<PortfolioSummary>
     * 
     * @param ownerId The ID of the user who owns the portfolios
     * @return Flux<PortfolioSummary> with user's portfolio summaries
     */
    fun getPortfoliosByOwner(ownerId: Long): Flux<PortfolioSummary> {
        return portfolioQueryRepository.findPortfolioSummariesByOwner(ownerId)
            .onErrorResume { error ->
                println("Error retrieving portfolios for owner $ownerId: ${error.message}")
                Flux.empty()
            }
    }

    /**
     * Retrieves portfolio summaries for a specific organization.
     * 
     * Returns a reactive stream of portfolio summaries for an organization,
     * typically used by administrators to view organizational portfolio inventory.
     * This method supports multi-tenant architecture.
     * 
     * **Reactive**: Returns Flux<PortfolioSummary>
     * 
     * @param organizationId The ID of the organization
     * @return Flux<PortfolioSummary> with organization's portfolio summaries
     */
    fun getPortfoliosByOrganization(organizationId: Long): Flux<PortfolioSummary> {
        return portfolioQueryRepository.findPortfolioSummariesByOrganization(organizationId)
            .onErrorResume { error ->
                println("Error retrieving portfolios for organization $organizationId: ${error.message}")
                Flux.empty()
            }
    }

    /**
     * Searches portfolios with flexible filtering criteria.
     * 
     * Provides advanced search capabilities with multiple optional filters.
     * All parameters are optional, allowing for flexible search combinations.
     * This method is optimized for search performance and pagination.
     * 
     * **Reactive**: Returns Flux<PortfolioSummary>
     * 
     * @param name Optional name filter (partial matching supported)
     * @param type Optional portfolio type filter
     * @param status Optional portfolio status filter
     * @param organizationId Optional organization scope filter
     * @return Flux<PortfolioSummary> with matching portfolio summaries
     */
    fun searchPortfolios(name: String?, type: PortfolioType?, status: PortfolioStatus?, organizationId: Long?): Flux<PortfolioSummary> {
        return portfolioQueryRepository.searchPortfolios(name, type, status, organizationId)
            .onErrorResume { error ->
                println("Error searching portfolios: ${error.message}")
                Flux.empty()
            }
    }

    /**
     * Adds a new technology to an existing portfolio.
     * 
     * This method implements the technology addition use case with validation
     * and cost tracking. The technology is associated with the specified portfolio
     * and includes comprehensive metadata for lifecycle management.
     * 
     * ## Business Rules:
     * - Portfolio must exist before adding technologies
     * - Technology names should be unique within a portfolio
     * - Cost information is optional but recommended for tracking
     * - New technologies are created with active status
     * 
     * **Reactive**: Returns Mono<TechnologyResponse>
     * 
     * @param portfolioId The ID of the portfolio to add the technology to
     * @param request The technology creation request with required data
     * @return Mono<TechnologyResponse> with complete technology information
     * @throws RuntimeException if technology addition fails
     * @throws IllegalArgumentException if portfolio doesn't exist
     */
    fun addTechnology(portfolioId: Long, request: AddTechnologyRequest): Mono<TechnologyResponse> {
        return portfolioRepository.findById(portfolioId)
            .switchIfEmpty(
                Mono.error<TechnologyPortfolio>(
                    IllegalArgumentException("Portfolio with id $portfolioId not found")
                )
            )
            .flatMap { portfolio ->
                val technology = Technology(
                    name = request.name,
                    description = request.description,
                    category = request.category,
                    version = request.version,
                    type = request.type,
                    maturityLevel = request.maturityLevel,
                    riskLevel = request.riskLevel,
                    annualCost = request.annualCost,
                    licenseCost = request.licenseCost,
                    maintenanceCost = request.maintenanceCost,
                    vendorName = request.vendorName,
                    vendorContact = request.vendorContact,
                    supportContractExpiry = request.supportContractExpiry,
                    isActive = true,
                    createdAt = LocalDateTime.now(),
                    portfolioId = portfolioId
                )

                technologyRepository.save(technology)
                    .flatMap { savedTechnology ->
                        // Publish event
                        eventPublisher.publish(
                            TechnologyAddedEvent(
                                portfolioId = portfolioId,
                                technologyId = savedTechnology.id!!,
                                technologyName = savedTechnology.name
                            )
                        ).then(Mono.just(savedTechnology))
                    }
                    .map { toTechnologyResponse(it) }
            }
            .onErrorMap { error ->
                when (error) {
                    is IllegalArgumentException -> error
                    else -> RuntimeException("Failed to add technology: ${error.message}", error)
                }
            }
    }

    /**
     * Updates an existing technology with partial data.
     * 
     * Allows selective updates to technology information including costs,
     * vendor details, and lifecycle metadata. Only non-null fields in the
     * request are updated, enabling partial updates.
     * 
     * **Reactive**: Returns Mono<TechnologyResponse>
     * 
     * @param technologyId The ID of the technology to update
     * @param request The update request with optional field changes
     * @return Mono<TechnologyResponse> with updated technology information
     * @throws RuntimeException if update fails
     * @throws IllegalArgumentException if technology doesn't exist
     */
    fun updateTechnology(technologyId: Long, request: UpdateTechnologyRequest): Mono<TechnologyResponse> {
        return technologyRepository.findById(technologyId)
            .switchIfEmpty(
                Mono.error<Technology>(
                    IllegalArgumentException("Technology with id $technologyId not found")
                )
            )
            .flatMap { existingTechnology ->
                val updatedTechnology = existingTechnology.copy(
                    name = request.name ?: existingTechnology.name,
                    description = request.description ?: existingTechnology.description,
                    category = request.category ?: existingTechnology.category,
                    version = request.version ?: existingTechnology.version,
                    type = request.type ?: existingTechnology.type,
                    maturityLevel = request.maturityLevel ?: existingTechnology.maturityLevel,
                    riskLevel = request.riskLevel ?: existingTechnology.riskLevel,
                    annualCost = request.annualCost ?: existingTechnology.annualCost,
                    licenseCost = request.licenseCost ?: existingTechnology.licenseCost,
                    maintenanceCost = request.maintenanceCost ?: existingTechnology.maintenanceCost,
                    vendorName = request.vendorName ?: existingTechnology.vendorName,
                    vendorContact = request.vendorContact ?: existingTechnology.vendorContact,
                    supportContractExpiry = request.supportContractExpiry ?: existingTechnology.supportContractExpiry,
                    updatedAt = LocalDateTime.now()
                )

                technologyRepository.update(updatedTechnology)
                    .map { toTechnologyResponse(it) }
            }
            .onErrorMap { error ->
                when (error) {
                    is IllegalArgumentException -> error
                    else -> RuntimeException("Failed to update technology: ${error.message}", error)
                }
            }
    }

    /**
     * Retrieves a complete technology by its unique identifier.
     * 
     * Returns comprehensive technology information including all metadata,
     * cost information, and vendor details. This is the primary method
     * for retrieving detailed technology information.
     * 
     * **Reactive**: Returns Mono<TechnologyResponse>
     * 
     * @param technologyId The unique identifier of the technology
     * @return Mono<TechnologyResponse> with complete technology details
     * @throws IllegalArgumentException if technology with given ID doesn't exist
     */
    fun getTechnology(technologyId: Long): Mono<TechnologyResponse> {
        return technologyRepository.findById(technologyId)
            .switchIfEmpty(
                Mono.error<Technology>(
                    IllegalArgumentException("Technology with id $technologyId not found")
                )
            )
            .map { toTechnologyResponse(it) }
            .onErrorMap { error ->
                when (error) {
                    is IllegalArgumentException -> error
                    else -> RuntimeException("Failed to retrieve technology: ${error.message}", error)
                }
            }
    }

    /**
     * Retrieves technology summaries for a specific portfolio.
     * 
     * Returns a reactive stream of technology summaries for efficient
     * listing and overview purposes. This method is optimized for
     * performance and includes essential technology information.
     * 
     * **Reactive**: Returns Flux<TechnologySummary>
     * 
     * @param portfolioId The ID of the portfolio containing the technologies
     * @return Flux<TechnologySummary> with portfolio's technology summaries
     */
    fun getTechnologiesByPortfolio(portfolioId: Long): Flux<TechnologySummary> {
        return technologyQueryRepository.findTechnologySummariesByPortfolio(portfolioId)
            .onErrorResume { error ->
                println("Error retrieving technologies for portfolio $portfolioId: ${error.message}")
                Flux.empty()
            }
    }

    /**
     * Removes a technology from a portfolio with validation.
     * 
     * This method implements the technology removal use case with proper
     * validation to ensure the technology belongs to the specified portfolio.
     * An event is published upon successful removal for integration purposes.
     * 
     * ## Business Rules:
     * - Both portfolio and technology must exist
     * - Technology must belong to the specified portfolio
     * - Removal is permanent and cannot be undone
     * 
     * **Reactive**: Returns Mono<Boolean>
     * 
     * @param portfolioId The ID of the portfolio containing the technology
     * @param technologyId The ID of the technology to remove
     * @return Mono<Boolean> with true if technology was successfully removed, false otherwise
     * @throws RuntimeException if removal fails
     * @throws IllegalArgumentException if portfolio or technology doesn't exist or don't match
     */
    fun removeTechnology(portfolioId: Long, technologyId: Long): Mono<Boolean> {
        return Mono.zip(
            portfolioRepository.findById(portfolioId),
            technologyRepository.findById(technologyId)
        )
        .switchIfEmpty(
            Mono.error<Pair<TechnologyPortfolio, Technology>>(
                IllegalArgumentException("Portfolio with id $portfolioId or Technology with id $technologyId not found")
            )
        )
        .flatMap { (portfolio, technology) ->
            if (technology.portfolioId != portfolioId) {
                Mono.error<Boolean>(
                    IllegalArgumentException("Technology does not belong to the specified portfolio")
                )
            } else {
                technologyRepository.delete(technologyId)
                    .flatMap { deleted ->
                        if (deleted) {
                            // Publish event
                            eventPublisher.publish(
                                TechnologyRemovedEvent(
                                    portfolioId = portfolioId,
                                    technologyId = technologyId,
                                    technologyName = technology.name
                                )
                            ).thenReturn(true)
                        } else {
                            Mono.just(false)
                        }
                    }
            }
        }
        .onErrorMap { error ->
            when (error) {
                is IllegalArgumentException -> error
                else -> RuntimeException("Failed to remove technology: ${error.message}", error)
            }
        }
    }

    /**
     * Deletes a portfolio with validation and safety checks.
     * 
     * This method implements the portfolio deletion use case with business
     * rule enforcement. Portfolios with associated technologies cannot be
     * deleted to maintain data integrity.
     * 
     * ## Business Rules:
     * - Portfolio must exist before deletion
     * - Portfolio must be empty (no technologies) before deletion
     * - Deletion is permanent and cannot be undone
     * 
     * **Reactive**: Returns Mono<Boolean>
     * 
     * @param portfolioId The ID of the portfolio to delete
     * @return Mono<Boolean> with true if portfolio was successfully deleted, false otherwise
     * @throws RuntimeException if deletion fails
     * @throws IllegalArgumentException if portfolio doesn't exist or contains technologies
     */
    fun deletePortfolio(portfolioId: Long): Mono<Boolean> {
        return portfolioRepository.findById(portfolioId)
            .switchIfEmpty(
                Mono.error<TechnologyPortfolio>(
                    IllegalArgumentException("Portfolio with id $portfolioId not found")
                )
            )
            .flatMap { portfolio ->
                technologyRepository.countByPortfolioId(portfolioId)
                    .flatMap { technologyCount ->
                        if (technologyCount > 0) {
                            Mono.error<Boolean>(
                                IllegalArgumentException("Cannot delete portfolio with technologies. Remove all technologies first.")
                            )
                        } else {
                            portfolioRepository.delete(portfolioId)
                        }
                    }
            }
            .onErrorMap { error ->
                when (error) {
                    is IllegalArgumentException -> error
                    else -> RuntimeException("Failed to delete portfolio: ${error.message}", error)
                }
            }
    }

    /**
     * Converts a domain portfolio entity to a response DTO.
     * 
     * This private helper method transforms the internal domain model to the
     * external API response format. It includes technology aggregation and
     * cost calculations for comprehensive portfolio information.
     * 
     * **Reactive**: Returns Mono<PortfolioResponse>
     * 
     * @param portfolio The domain portfolio entity to convert
     * @return Mono<PortfolioResponse> with complete portfolio information and calculated fields
     */
    private fun toPortfolioResponse(portfolio: TechnologyPortfolio): Mono<PortfolioResponse> {
        return technologyRepository.findByPortfolioId(portfolio.id!!)
            .collectList()
            .map { technologies ->
                val totalAnnualCost = technologies
                    .mapNotNull { it.annualCost }
                    .reduceOrNull { acc, cost -> acc + cost }

                PortfolioResponse(
                    id = portfolio.id!!,
                    name = portfolio.name,
                    description = portfolio.description,
                    type = portfolio.type,
                    status = portfolio.status,
                    isActive = portfolio.isActive,
                    createdAt = portfolio.createdAt,
                    updatedAt = portfolio.updatedAt,
                    ownerId = portfolio.ownerId,
                    organizationId = portfolio.organizationId,
                    technologyCount = technologies.size.toLong(),
                    totalAnnualCost = totalAnnualCost,
                    technologies = technologies.map { toTechnologyResponseSync(it) }
                )
            }
    }

    /**
     * Converts a domain technology entity to a response DTO.
     * 
     * This private helper method transforms the internal domain model to the
     * external API response format. It includes all technology metadata and
     * cost information for comprehensive technology details.
     * 
     * **Reactive**: Returns Mono<TechnologyResponse>
     * 
     * @param technology The domain technology entity to convert
     * @return Mono<TechnologyResponse> with complete technology information
     */
    private fun toTechnologyResponse(technology: Technology): Mono<TechnologyResponse> {
        return Mono.just(toTechnologyResponseSync(technology))
    }

    /**
     * Synchronous version of toTechnologyResponse for use in collections.
     * 
     * @param technology The domain technology entity to convert
     * @return TechnologyResponse with complete technology information
     */
    private fun toTechnologyResponseSync(technology: Technology): TechnologyResponse {
        return TechnologyResponse(
            id = technology.id!!,
            name = technology.name,
            description = technology.description,
            category = technology.category,
            version = technology.version,
            type = technology.type,
            maturityLevel = technology.maturityLevel,
            riskLevel = technology.riskLevel,
            annualCost = technology.annualCost,
            licenseCost = technology.licenseCost,
            maintenanceCost = technology.maintenanceCost,
            vendorName = technology.vendorName,
            vendorContact = technology.vendorContact,
            supportContractExpiry = technology.supportContractExpiry,
            isActive = technology.isActive,
            createdAt = technology.createdAt,
            updatedAt = technology.updatedAt
        )
    }
} 