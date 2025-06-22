package com.company.techportfolio.portfolio.adapter.out.persistence

import com.company.techportfolio.portfolio.adapter.out.persistence.entity.PortfolioEntity
import com.company.techportfolio.portfolio.adapter.out.persistence.entity.TechnologyEntity
import com.company.techportfolio.portfolio.adapter.out.persistence.repository.PortfolioJpaRepository
import com.company.techportfolio.portfolio.adapter.out.persistence.repository.TechnologyJpaRepository
import com.company.techportfolio.shared.domain.model.*
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Unit tests for PortfolioRepositoryAdapter.
 *
 * This test class verifies the functionality of the PortfolioRepositoryAdapter, which
 * is the implementation of the PortfolioRepository and PortfolioQueryRepository interfaces
 * in the hexagonal architecture. It tests the adapter's ability to correctly translate
 * between domain models and R2DBC entities, and to delegate operations to the underlying
 * Spring Data R2DBC repositories.
 *
 * ## Test Coverage:
 * - Basic CRUD operations (find, save, update, delete)
 * - Query operations by various criteria
 * - Summary data generation and aggregation
 * - Error handling and edge cases
 * - Domain model to entity mapping
 * - Entity to domain model mapping
 *
 * ## Testing Approach:
 * - Uses MockK for mocking R2DBC repositories
 * - Uses StepVerifier for testing reactive streams
 * - Follows the Given-When-Then pattern for test clarity
 * - Verifies both return values and repository interactions
 *
 * @author Technology Portfolio Team
 * @since 1.0.0
 * @see PortfolioRepositoryAdapter
 * @see PortfolioJpaRepository
 * @see TechnologyJpaRepository
 */
class PortfolioRepositoryAdapterTest {

    /**
     * Mock of the Spring Data R2DBC repository for portfolio entities.
     */
    private val portfolioJpaRepository = mockk<PortfolioJpaRepository>()

    /**
     * Mock of the Spring Data R2DBC repository for technology entities.
     */
    private val technologyJpaRepository = mockk<TechnologyJpaRepository>()

    /**
     * The adapter under test.
     */
    private lateinit var portfolioRepositoryAdapter: PortfolioRepositoryAdapter

    /**
     * Fixed test date/time for consistent testing.
     */
    private val testDateTime = LocalDateTime.of(2024, 1, 15, 10, 30, 0)

    /**
     * Set up the test environment before each test.
     *
     * Initializes a fresh instance of the PortfolioRepositoryAdapter with mock
     * dependencies for each test to ensure test isolation.
     */
    @BeforeEach
    fun setUp() {
        clearAllMocks()
        portfolioRepositoryAdapter = PortfolioRepositoryAdapter(
            portfolioJpaRepository,
            technologyJpaRepository
        )
    }

    /**
     * Tests that findById returns a correctly mapped domain model when a portfolio exists.
     *
     * Verifies that:
     * 1. The adapter correctly delegates to the R2DBC repository
     * 2. The returned entity is properly mapped to a domain model
     * 3. All properties are correctly transferred during mapping
     */
    @Test
    fun `findById should return portfolio when found`() {
        // Given
        val portfolioId = 1L
        val entity = createTestPortfolioEntity(portfolioId, "Test Portfolio")
        every { portfolioJpaRepository.findById(portfolioId) } returns Mono.just(entity)

        // When
        val result = portfolioRepositoryAdapter.findById(portfolioId)

        // Then
        StepVerifier.create(result)
            .expectNextMatches { portfolio ->
                portfolio.id == portfolioId &&
                        portfolio.name == "Test Portfolio" &&
                        portfolio.type == PortfolioType.ENTERPRISE &&
                        portfolio.status == PortfolioStatus.ACTIVE
            }
            .verifyComplete()

        verify { portfolioJpaRepository.findById(portfolioId) }
    }

    /**
     * Tests that findById returns empty Mono when a portfolio does not exist.
     *
     * Verifies that:
     * 1. The adapter correctly delegates to the R2DBC repository
     * 2. The adapter returns empty Mono when the repository returns empty Mono
     */
    @Test
    fun `findById should return empty when not found`() {
        // Given
        val portfolioId = 999L
        every { portfolioJpaRepository.findById(portfolioId) } returns Mono.empty()

        // When
        val result = portfolioRepositoryAdapter.findById(portfolioId)

        // Then
        StepVerifier.create(result)
            .verifyComplete()

        verify { portfolioJpaRepository.findById(portfolioId) }
    }

    /**
     * Tests that findByName returns a correctly mapped domain model when a portfolio exists.
     *
     * Verifies that:
     * 1. The adapter correctly delegates to the R2DBC repository
     * 2. The returned entity is properly mapped to a domain model
     * 3. The name property matches the search criteria
     */
    @Test
    fun `findByName should return portfolio when found`() {
        // Given
        val name = "Test Portfolio"
        val entity = createTestPortfolioEntity(1L, name)
        every { portfolioJpaRepository.findByName(name) } returns Mono.just(entity)

        // When
        val result = portfolioRepositoryAdapter.findByName(name)

        // Then
        StepVerifier.create(result)
            .expectNextMatches { portfolio -> portfolio.name == name }
            .verifyComplete()

        verify { portfolioJpaRepository.findByName(name) }
    }

    /**
     * Tests that findByName returns empty Mono when a portfolio does not exist.
     *
     * Verifies that:
     * 1. The adapter correctly delegates to the R2DBC repository
     * 2. The adapter returns empty Mono when the repository returns empty Mono
     */
    @Test
    fun `findByName should return empty when not found`() {
        // Given
        val name = "Non-existent Portfolio"
        every { portfolioJpaRepository.findByName(name) } returns Mono.empty()

        // When
        val result = portfolioRepositoryAdapter.findByName(name)

        // Then
        StepVerifier.create(result)
            .verifyComplete()

        verify { portfolioJpaRepository.findByName(name) }
    }

    /**
     * Tests that findByOwnerId returns a Flux of correctly mapped domain models.
     *
     * Verifies that:
     * 1. The adapter correctly delegates to the R2DBC repository
     * 2. The returned entities are properly mapped to domain models
     * 3. The correct number of portfolios is returned
     */
    @Test
    fun `findByOwnerId should return owner portfolios`() {
        // Given
        val ownerId = 123L
        val entities = listOf(
            createTestPortfolioEntity(1L, "Portfolio 1"),
            createTestPortfolioEntity(2L, "Portfolio 2")
        )
        every { portfolioJpaRepository.findByOwnerId(ownerId) } returns Flux.fromIterable(entities)

        // When
        val result = portfolioRepositoryAdapter.findByOwnerId(ownerId)

        // Then
        StepVerifier.create(result)
            .expectNextMatches { it.name == "Portfolio 1" }
            .expectNextMatches { it.name == "Portfolio 2" }
            .verifyComplete()

        verify { portfolioJpaRepository.findByOwnerId(ownerId) }
    }

    /**
     * Tests that findByOrganizationId returns a Flux of correctly mapped domain models.
     *
     * Verifies that:
     * 1. The adapter correctly delegates to the R2DBC repository
     * 2. The returned entities are properly mapped to domain models
     * 3. The organizationId property matches the search criteria
     */
    @Test
    fun `findByOrganizationId should return organization portfolios`() {
        // Given
        val organizationId = 200L
        val entities = listOf(createTestPortfolioEntity(1L, "Org Portfolio"))
        every { portfolioJpaRepository.findByOrganizationId(organizationId) } returns Flux.fromIterable(entities)

        // When
        val result = portfolioRepositoryAdapter.findByOrganizationId(organizationId)

        // Then
        StepVerifier.create(result)
            .expectNextMatches { it.name == "Org Portfolio" }
            .verifyComplete()

        verify { portfolioJpaRepository.findByOrganizationId(organizationId) }
    }

    /**
     * Tests that findByType returns portfolios of specified type.
     *
     * Verifies that:
     * 1. The adapter correctly delegates to the R2DBC repository
     * 2. The returned entities are properly mapped to domain models
     * 3. The type property matches the search criteria
     */
    @Test
    fun `findByType should return portfolios of specified type`() {
        // Given
        val type = PortfolioType.ENTERPRISE
        val entities = listOf(createTestPortfolioEntity(1L, "Enterprise Portfolio"))
        every { portfolioJpaRepository.findByType(type) } returns Flux.fromIterable(entities)

        // When
        val result = portfolioRepositoryAdapter.findByType(type)

        // Then
        StepVerifier.create(result)
            .expectNextMatches { it.type == type }
            .verifyComplete()

        verify { portfolioJpaRepository.findByType(type) }
    }

    /**
     * Tests that findByStatus returns portfolios with specified status.
     *
     * Verifies that:
     * 1. The adapter correctly delegates to the R2DBC repository
     * 2. The returned entities are properly mapped to domain models
     * 3. The status property matches the search criteria
     */
    @Test
    fun `findByStatus should return portfolios with specified status`() {
        // Given
        val status = PortfolioStatus.ACTIVE
        val entities = listOf(createTestPortfolioEntity(1L, "Active Portfolio"))
        every { portfolioJpaRepository.findByStatus(status) } returns Flux.fromIterable(entities)

        // When
        val result = portfolioRepositoryAdapter.findByStatus(status)

        // Then
        StepVerifier.create(result)
            .expectNextMatches { it.status == status }
            .verifyComplete()

        verify { portfolioJpaRepository.findByStatus(status) }
    }

    /**
     * Tests that findAll returns all active portfolios.
     *
     * Verifies that:
     * 1. The adapter correctly delegates to the R2DBC repository's findByIsActiveTrue method
     * 2. The returned entities are properly mapped to domain models
     * 3. The correct number of portfolios is returned
     */
    @Test
    fun `findAll should return all active portfolios`() {
        // Given
        val entities = listOf(
            createTestPortfolioEntity(1L, "Portfolio 1"),
            createTestPortfolioEntity(2L, "Portfolio 2")
        )
        every { portfolioJpaRepository.findByIsActiveTrue() } returns Flux.fromIterable(entities)

        // When
        val result = portfolioRepositoryAdapter.findAll()

        // Then
        StepVerifier.create(result)
            .expectNextCount(2)
            .verifyComplete()

        verify { portfolioJpaRepository.findByIsActiveTrue() }
    }

    /**
     * Tests that save correctly persists a new portfolio.
     *
     * Verifies that:
     * 1. The adapter correctly delegates to the R2DBC repository
     * 2. The domain model is properly mapped to an entity before saving
     * 3. The saved entity is properly mapped back to a domain model
     * 4. The returned domain model includes the generated ID
     */
    @Test
    fun `save should save new portfolio and return saved entity`() {
        // Given
        val portfolio = createTestTechnologyPortfolio(null, "New Portfolio")
        val savedEntity = createTestPortfolioEntity(1L, "New Portfolio")
        every { portfolioJpaRepository.save(any()) } returns Mono.just(savedEntity)

        // When
        val result = portfolioRepositoryAdapter.save(portfolio)

        // Then
        StepVerifier.create(result)
            .expectNextMatches { saved ->
                saved.id == 1L && saved.name == "New Portfolio"
            }
            .verifyComplete()

        verify { portfolioJpaRepository.save(any()) }
    }

    @Test
    fun `update should update existing portfolio and return updated entity`() {
        // Given
        val portfolio = createTestTechnologyPortfolio(1L, "Updated Portfolio")
        val updatedEntity = createTestPortfolioEntity(1L, "Updated Portfolio")
        every { portfolioJpaRepository.save(any()) } returns Mono.just(updatedEntity)

        // When
        val result = portfolioRepositoryAdapter.update(portfolio)

        // Then
        StepVerifier.create(result)
            .expectNextMatches { updated ->
                updated.id == 1L && updated.name == "Updated Portfolio"
            }
            .verifyComplete()

        verify { portfolioJpaRepository.save(any()) }
    }

    @Test
    fun `delete should delete portfolio successfully`() {
        // Given
        val portfolioId = 1L
        every { portfolioJpaRepository.existsById(portfolioId) } returns Mono.just(true)
        every { portfolioJpaRepository.deleteById(eq(portfolioId)) } returns Mono.empty()

        // When
        val result = portfolioRepositoryAdapter.delete(portfolioId)

        // Then
        StepVerifier.create(result)
            .expectNext(true)
            .verifyComplete()

        verify { portfolioJpaRepository.existsById(portfolioId) }
        verify { portfolioJpaRepository.deleteById(eq(portfolioId)) }
    }

    @Test
    fun `delete should return false when deletion fails`() {
        // Given
        val portfolioId = 1L
        every { portfolioJpaRepository.existsById(portfolioId) } returns Mono.just(true)
        every { portfolioJpaRepository.deleteById(eq(portfolioId)) } returns Mono.error(RuntimeException("Delete failed"))

        // When
        val result = portfolioRepositoryAdapter.delete(portfolioId)

        // Then
        StepVerifier.create(result)
            .expectNext(false)
            .verifyComplete()

        verify { portfolioJpaRepository.existsById(portfolioId) }
        verify { portfolioJpaRepository.deleteById(eq(portfolioId)) }
    }

    @Test
    fun `delete should return false when portfolio does not exist`() {
        // Given
        val portfolioId = 999L
        every { portfolioJpaRepository.existsById(portfolioId) } returns Mono.just(false)

        // When
        val result = portfolioRepositoryAdapter.delete(portfolioId)

        // Then
        StepVerifier.create(result)
            .expectNext(false)
            .verifyComplete()

        verify { portfolioJpaRepository.existsById(portfolioId) }
        // deleteById should not be called when portfolio doesn't exist
    }

    @Test
    fun `existsById should return true when portfolio exists`() {
        // Given
        val portfolioId = 1L
        every { portfolioJpaRepository.existsById(portfolioId) } returns Mono.just(true)

        // When
        val result = portfolioRepositoryAdapter.existsById(portfolioId)

        // Then
        StepVerifier.create(result)
            .expectNext(true)
            .verifyComplete()

        verify { portfolioJpaRepository.existsById(portfolioId) }
    }

    @Test
    fun `existsById should return false when portfolio does not exist`() {
        // Given
        val portfolioId = 999L
        every { portfolioJpaRepository.existsById(portfolioId) } returns Mono.just(false)

        // When
        val result = portfolioRepositoryAdapter.existsById(portfolioId)

        // Then
        StepVerifier.create(result)
            .expectNext(false)
            .verifyComplete()

        verify { portfolioJpaRepository.existsById(portfolioId) }
    }

    @Test
    fun `countByOwnerId should return portfolio count for owner`() {
        // Given
        val ownerId = 123L
        val count = 5L
        every { portfolioJpaRepository.countByOwnerId(ownerId) } returns Mono.just(count)

        // When
        val result = portfolioRepositoryAdapter.countByOwnerId(ownerId)

        // Then
        StepVerifier.create(result)
            .expectNext(count)
            .verifyComplete()

        verify { portfolioJpaRepository.countByOwnerId(ownerId) }
    }

    @Test
    fun `countByOrganizationId should return portfolio count for organization`() {
        // Given
        val organizationId = 200L
        val count = 3L
        every { portfolioJpaRepository.countByOrganizationId(organizationId) } returns Mono.just(count)

        // When
        val result = portfolioRepositoryAdapter.countByOrganizationId(organizationId)

        // Then
        StepVerifier.create(result)
            .expectNext(count)
            .verifyComplete()

        verify { portfolioJpaRepository.countByOrganizationId(organizationId) }
    }

    @Test
    fun `findPortfolioSummary should return summary when portfolio found`() {
        // Given
        val portfolioId = 1L
        val entity = createTestPortfolioEntity(portfolioId, "Test Portfolio")
        val technologyCount = 3L
        val technologies = listOf(
            createTestTechnologyEntity(1L, "Tech 1", BigDecimal("1000.00")),
            createTestTechnologyEntity(2L, "Tech 2", BigDecimal("2000.00")),
            createTestTechnologyEntity(3L, "Tech 3", null)
        )

        every { portfolioJpaRepository.findById(portfolioId) } returns Mono.just(entity)
        every { technologyJpaRepository.countByPortfolioId(portfolioId) } returns Mono.just(technologyCount)
        every { technologyJpaRepository.findByPortfolioId(portfolioId) } returns Flux.fromIterable(technologies)

        // When
        val result = portfolioRepositoryAdapter.findPortfolioSummary(portfolioId)

        // Then
        StepVerifier.create(result)
            .expectNextMatches { summary ->
                summary.id == portfolioId &&
                        summary.name == "Test Portfolio" &&
                        summary.technologyCount == technologyCount.toInt() &&
                        summary.totalAnnualCost == BigDecimal("3000.00")
            }
            .verifyComplete()

        verify { portfolioJpaRepository.findById(portfolioId) }
        verify { technologyJpaRepository.countByPortfolioId(portfolioId) }
        verify { technologyJpaRepository.findByPortfolioId(portfolioId) }
    }

    @Test
    fun `findPortfolioSummary should return null when portfolio not found`() {
        // Given
        val portfolioId = 999L
        every { portfolioJpaRepository.findById(portfolioId) } returns Mono.empty()

        // When
        val result = portfolioRepositoryAdapter.findPortfolioSummary(portfolioId)

        // Then
        StepVerifier.create(result)
            .verifyComplete()

        verify { portfolioJpaRepository.findById(portfolioId) }
    }

    @Test
    fun `findPortfolioSummariesByOwner should return owner portfolio summaries`() {
        // Given
        val ownerId = 123L
        val entities = listOf(createTestPortfolioEntity(1L, "Portfolio 1"))
        every { portfolioJpaRepository.findByOwnerId(ownerId) } returns Flux.fromIterable(entities)
        every { technologyJpaRepository.countByPortfolioId(1L) } returns Mono.just(2L)
        every { technologyJpaRepository.findByPortfolioId(1L) } returns Flux.empty()

        // When
        val result = portfolioRepositoryAdapter.findPortfolioSummariesByOwner(ownerId)

        // Then
        StepVerifier.create(result)
            .expectNextMatches { summary ->
                summary.id == 1L &&
                        summary.name == "Portfolio 1" &&
                        summary.technologyCount == 2
            }
            .verifyComplete()

        verify { portfolioJpaRepository.findByOwnerId(ownerId) }
        verify { technologyJpaRepository.countByPortfolioId(1L) }
        verify { technologyJpaRepository.findByPortfolioId(1L) }
    }

    @Test
    fun `findPortfolioSummariesByOrganization should return organization portfolio summaries`() {
        // Given
        val organizationId = 200L
        val entities = listOf(createTestPortfolioEntity(1L, "Org Portfolio"))
        every { portfolioJpaRepository.findByOrganizationId(organizationId) } returns Flux.fromIterable(entities)
        every { technologyJpaRepository.countByPortfolioId(1L) } returns Mono.just(1L)
        every { technologyJpaRepository.findByPortfolioId(1L) } returns Flux.empty()

        // When
        val result = portfolioRepositoryAdapter.findPortfolioSummariesByOrganization(organizationId)

        // Then
        StepVerifier.create(result)
            .expectNextMatches { summary ->
                summary.id == 1L &&
                        summary.name == "Org Portfolio"
            }
            .verifyComplete()

        verify { portfolioJpaRepository.findByOrganizationId(organizationId) }
        verify { technologyJpaRepository.countByPortfolioId(1L) }
        verify { technologyJpaRepository.findByPortfolioId(1L) }
    }

    @Test
    fun `findPortfolioSummariesByType should return type-filtered portfolio summaries`() {
        // Given
        val type = PortfolioType.ENTERPRISE
        val entities = listOf(createTestPortfolioEntity(1L, "Enterprise Portfolio"))
        every { portfolioJpaRepository.findByType(type) } returns Flux.fromIterable(entities)
        every { technologyJpaRepository.countByPortfolioId(1L) } returns Mono.just(0L)
        every { technologyJpaRepository.findByPortfolioId(1L) } returns Flux.empty()

        // When
        val result = portfolioRepositoryAdapter.findPortfolioSummariesByType(type)

        // Then
        StepVerifier.create(result)
            .expectNextMatches { summary ->
                summary.id == 1L &&
                        summary.type == type
            }
            .verifyComplete()

        verify { portfolioJpaRepository.findByType(type) }
        verify { technologyJpaRepository.countByPortfolioId(1L) }
        verify { technologyJpaRepository.findByPortfolioId(1L) }
    }

    @Test
    fun `findPortfolioSummariesByStatus should return status-filtered portfolio summaries`() {
        // Given
        val status = PortfolioStatus.ACTIVE
        val entities = listOf(createTestPortfolioEntity(1L, "Active Portfolio"))
        every { portfolioJpaRepository.findByStatus(status) } returns Flux.fromIterable(entities)
        every { technologyJpaRepository.countByPortfolioId(1L) } returns Mono.just(0L)
        every { technologyJpaRepository.findByPortfolioId(1L) } returns Flux.empty()

        // When
        val result = portfolioRepositoryAdapter.findPortfolioSummariesByStatus(status)

        // Then
        StepVerifier.create(result)
            .expectNextMatches { summary ->
                summary.id == 1L &&
                        summary.status == status
            }
            .verifyComplete()

        verify { portfolioJpaRepository.findByStatus(status) }
        verify { technologyJpaRepository.countByPortfolioId(1L) }
        verify { technologyJpaRepository.findByPortfolioId(1L) }
    }

    @Test
    fun `findAllPortfolioSummaries should return all portfolio summaries`() {
        // Given
        val entities = listOf(
            createTestPortfolioEntity(1L, "Portfolio 1"),
            createTestPortfolioEntity(2L, "Portfolio 2")
        )
        every { portfolioJpaRepository.findByIsActiveTrue() } returns Flux.fromIterable(entities)
        every { technologyJpaRepository.countByPortfolioId(1L) } returns Mono.just(1L)
        every { technologyJpaRepository.countByPortfolioId(2L) } returns Mono.just(2L)
        every { technologyJpaRepository.findByPortfolioId(1L) } returns Flux.empty()
        every { technologyJpaRepository.findByPortfolioId(2L) } returns Flux.empty()

        // When
        val result = portfolioRepositoryAdapter.findAllPortfolioSummaries()

        // Then
        StepVerifier.create(result)
            .expectNextCount(2)
            .verifyComplete()

        verify { portfolioJpaRepository.findByIsActiveTrue() }
        verify { technologyJpaRepository.countByPortfolioId(1L) }
        verify { technologyJpaRepository.countByPortfolioId(2L) }
        verify { technologyJpaRepository.findByPortfolioId(1L) }
        verify { technologyJpaRepository.findByPortfolioId(2L) }
    }

    @Test
    fun `searchPortfolios should return search results`() {
        // Given
        val name = "enterprise"
        val type = PortfolioType.ENTERPRISE
        val status = PortfolioStatus.ACTIVE
        val organizationId = 200L
        val entities = listOf(createTestPortfolioEntity(1L, "Enterprise Portfolio"))

        every { portfolioJpaRepository.searchPortfolios(name, type, status, organizationId) } returns Flux.fromIterable(
            entities
        )
        every { technologyJpaRepository.countByPortfolioId(1L) } returns Mono.just(0L)
        every { technologyJpaRepository.findByPortfolioId(1L) } returns Flux.empty()

        // When
        val result = portfolioRepositoryAdapter.searchPortfolios(name, type, status, organizationId)

        // Then
        StepVerifier.create(result)
            .expectNextMatches { portfolio ->
                portfolio.name == "Enterprise Portfolio"
            }
            .verifyComplete()

        verify { portfolioJpaRepository.searchPortfolios(name, type, status, organizationId) }
        verify { technologyJpaRepository.countByPortfolioId(1L) }
        verify { technologyJpaRepository.findByPortfolioId(1L) }
    }

    // Helper Methods
    private fun createTestPortfolioEntity(id: Long, name: String) = PortfolioEntity(
        id = id,
        name = name,
        description = "Test Description",
        type = PortfolioType.ENTERPRISE,
        status = PortfolioStatus.ACTIVE,
        isActive = true,
        createdAt = testDateTime,
        updatedAt = testDateTime,
        ownerId = 123L,
        organizationId = 200L
    )

    private fun createTestTechnologyPortfolio(id: Long?, name: String) = TechnologyPortfolio(
        id = id,
        name = name,
        description = "Test Description",
        type = PortfolioType.ENTERPRISE,
        status = PortfolioStatus.ACTIVE,
        isActive = true,
        createdAt = testDateTime,
        updatedAt = testDateTime,
        ownerId = 123L,
        organizationId = 200L
    )

    private fun createTestTechnologyEntity(id: Long, name: String, annualCost: BigDecimal?) = TechnologyEntity(
        id = id,
        name = name,
        description = "Test Technology",
        category = "Framework",
        version = "1.0.0",
        type = TechnologyType.FRAMEWORK,
        maturityLevel = MaturityLevel.MATURE,
        riskLevel = RiskLevel.LOW,
        annualCost = annualCost,
        licenseCost = null,
        maintenanceCost = null,
        vendorName = "Test Vendor",
        vendorContact = null,
        supportContractExpiry = null,
        isActive = true,
        createdAt = testDateTime,
        updatedAt = testDateTime,
        portfolioId = 1L
    )
} 