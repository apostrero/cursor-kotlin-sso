package com.company.techportfolio.portfolio.domain.service

import com.company.techportfolio.portfolio.domain.model.*
import com.company.techportfolio.portfolio.domain.port.*
import com.company.techportfolio.shared.domain.model.*
import com.company.techportfolio.shared.domain.port.EventPublisher
import com.company.techportfolio.shared.domain.event.*
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Comprehensive unit tests for PortfolioService.
 * 
 * This test class verifies the business logic implemented in the PortfolioService class,
 * which is the core domain service for portfolio management. The tests use MockK to mock
 * dependencies and verify interactions with repositories and event publishers.
 * 
 * ## Test Coverage:
 * - Portfolio CRUD operations
 * - Technology management within portfolios
 * - Error handling and validation
 * - Event publishing for domain events
 * - Business rule enforcement
 * - Query operations and filtering
 * 
 * ## Test Structure:
 * Each test focuses on a specific method or use case, following the
 * Given-When-Then pattern for clear test organization and readability.
 * 
 * @author Technology Portfolio Team
 * @since 1.0.0
 * @see PortfolioService
 */
class PortfolioServiceTest {

    /**
     * Mock repository for portfolio persistence operations.
     */
    private val portfolioRepository = mockk<PortfolioRepository>()
    
    /**
     * Mock repository for technology persistence operations.
     */
    private val technologyRepository = mockk<TechnologyRepository>()
    
    /**
     * Mock repository for optimized portfolio query operations.
     */
    private val portfolioQueryRepository = mockk<PortfolioQueryRepository>()
    
    /**
     * Mock repository for optimized technology query operations.
     */
    private val technologyQueryRepository = mockk<TechnologyQueryRepository>()
    
    /**
     * Mock event publisher for domain events.
     */
    private val eventPublisher = mockk<EventPublisher>()

    /**
     * The service under test.
     */
    private lateinit var portfolioService: PortfolioService

    /**
     * Fixed test date/time for consistent testing.
     */
    private val testDateTime = LocalDateTime.of(2024, 1, 15, 10, 30, 0)

    /**
     * Set up the test environment before each test.
     * 
     * Clears all mock interactions and creates a fresh instance of the
     * PortfolioService with mock dependencies for each test.
     */
    @BeforeEach
    fun setUp() {
        clearAllMocks()
        portfolioService = PortfolioService(
            portfolioRepository,
            technologyRepository,
            portfolioQueryRepository,
            technologyQueryRepository,
            eventPublisher
        )
    }

    /**
     * Tests successful portfolio creation.
     * 
     * Verifies that the service correctly:
     * 1. Checks for existing portfolios with the same name
     * 2. Saves the new portfolio to the repository
     * 3. Publishes a PortfolioCreatedEvent
     * 4. Returns a complete PortfolioResponse with correct data
     */
    @Test
    fun `createPortfolio should create portfolio successfully`() {
        // Given
        val request = CreatePortfolioRequest(
            name = "Test Portfolio",
            description = "Test Description",
            type = PortfolioType.ENTERPRISE,
            ownerId = 100L,
            organizationId = 200L
        )
        val savedPortfolio = createTestPortfolio(1L, "Test Portfolio")
        
        every { portfolioRepository.findByName("Test Portfolio") } returns null
        every { portfolioRepository.save(any()) } returns savedPortfolio
        every { technologyRepository.findByPortfolioId(1L) } returns emptyList()
        every { eventPublisher.publish(any<PortfolioCreatedEvent>()) } just Runs

        // When
        val result = portfolioService.createPortfolio(request)

        // Then
        assertEquals(1L, result.id)
        assertEquals("Test Portfolio", result.name)
        assertEquals("Test Description", result.description)
        assertEquals(PortfolioType.ENTERPRISE, result.type)
        assertEquals(PortfolioStatus.ACTIVE, result.status)
        assertEquals(100L, result.ownerId)
        assertEquals(200L, result.organizationId)
        
        verify { portfolioRepository.findByName("Test Portfolio") }
        verify { portfolioRepository.save(any()) }
        verify { eventPublisher.publish(any<PortfolioCreatedEvent>()) }
    }

    /**
     * Tests portfolio creation with duplicate name.
     * 
     * Verifies that the service correctly:
     * 1. Checks for existing portfolios with the same name
     * 2. Throws an appropriate exception when a duplicate is found
     * 3. Does not attempt to save the portfolio or publish events
     */
    @Test
    fun `createPortfolio should throw RuntimeException when name already exists`() {
        // Given
        val request = CreatePortfolioRequest(
            name = "Existing Portfolio",
            type = PortfolioType.ENTERPRISE,
            ownerId = 100L
        )
        val existingPortfolio = createTestPortfolio(1L, "Existing Portfolio")
        
        every { portfolioRepository.findByName("Existing Portfolio") } returns existingPortfolio

        // When & Then
        val exception = assertThrows<RuntimeException> {
            portfolioService.createPortfolio(request)
        }
        
        assertTrue(exception.message!!.contains("Failed to create portfolio"))
        assertTrue(exception.cause?.message!!.contains("Portfolio with name 'Existing Portfolio' already exists"))
        
        verify { portfolioRepository.findByName("Existing Portfolio") }
        verify(exactly = 0) { portfolioRepository.save(any()) }
        verify(exactly = 0) { eventPublisher.publish(any()) }
    }

    /**
     * Tests retrieving a portfolio by ID when it exists.
     * 
     * Verifies that the service correctly:
     * 1. Retrieves the portfolio from the repository
     * 2. Fetches associated technologies
     * 3. Returns a complete PortfolioResponse with correct data
     */
    @Test
    fun `getPortfolio should return portfolio when found`() {
        // Given
        val portfolioId = 1L
        val portfolio = createTestPortfolio(portfolioId, "Test Portfolio")
        val technologies = emptyList<Technology>()
        
        every { portfolioRepository.findById(portfolioId) } returns portfolio
        every { technologyRepository.findByPortfolioId(portfolioId) } returns technologies

        // When
        val result = portfolioService.getPortfolio(portfolioId)

        // Then
        assertEquals(portfolioId, result.id)
        assertEquals("Test Portfolio", result.name)
        assertEquals(0, result.technologyCount)
        assertEquals(0, result.technologies.size)
        
        verify { portfolioRepository.findById(portfolioId) }
        verify { technologyRepository.findByPortfolioId(portfolioId) }
    }

    /**
     * Tests retrieving a portfolio by ID when it doesn't exist.
     * 
     * Verifies that the service correctly:
     * 1. Attempts to retrieve the portfolio from the repository
     * 2. Throws an appropriate exception when the portfolio is not found
     */
    @Test
    fun `getPortfolio should throw IllegalArgumentException when not found`() {
        // Given
        val portfolioId = 999L
        every { portfolioRepository.findById(portfolioId) } returns null

        // When & Then
        val exception = assertThrows<IllegalArgumentException> {
            portfolioService.getPortfolio(portfolioId)
        }
        
        assertEquals("Portfolio with id $portfolioId not found", exception.message)
        verify { portfolioRepository.findById(portfolioId) }
    }

    /**
     * Tests retrieving portfolios by owner ID.
     * 
     * Verifies that the service correctly:
     * 1. Delegates to the query repository for optimized retrieval
     * 2. Returns the list of portfolio summaries for the specified owner
     */
    @Test
    fun `getPortfoliosByOwner should return owner portfolios`() {
        // Given
        val ownerId = 100L
        val summaries = listOf(
            createTestPortfolioSummary(1L, "Portfolio 1"),
            createTestPortfolioSummary(2L, "Portfolio 2")
        )
        
        every { portfolioQueryRepository.findPortfolioSummariesByOwner(ownerId) } returns summaries

        // When
        val result = portfolioService.getPortfoliosByOwner(ownerId)

        // Then
        assertEquals(2, result.size)
        assertEquals("Portfolio 1", result[0].name)
        assertEquals("Portfolio 2", result[1].name)
        
        verify { portfolioQueryRepository.findPortfolioSummariesByOwner(ownerId) }
    }

    /**
     * Tests retrieving portfolios by organization ID.
     * 
     * Verifies that the service correctly:
     * 1. Delegates to the query repository for optimized retrieval
     * 2. Returns the list of portfolio summaries for the specified organization
     */
    @Test
    fun `getPortfoliosByOrganization should return organization portfolios`() {
        // Given
        val organizationId = 200L
        val summaries = listOf(createTestPortfolioSummary(1L, "Org Portfolio"))
        
        every { portfolioQueryRepository.findPortfolioSummariesByOrganization(organizationId) } returns summaries

        // When
        val result = portfolioService.getPortfoliosByOrganization(organizationId)

        // Then
        assertEquals(1, result.size)
        assertEquals("Org Portfolio", result[0].name)
        
        verify { portfolioQueryRepository.findPortfolioSummariesByOrganization(organizationId) }
    }

    /**
     * Tests searching portfolios with multiple criteria.
     * 
     * Verifies that the service correctly:
     * 1. Delegates to the query repository with all search parameters
     * 2. Returns the filtered list of portfolio summaries
     */
    @Test
    fun `searchPortfolios should return filtered portfolios`() {
        // Given
        val name = "test"
        val type = PortfolioType.ENTERPRISE
        val status = PortfolioStatus.ACTIVE
        val organizationId = 200L
        val summaries = listOf(createTestPortfolioSummary(1L, "Test Portfolio"))
        
        every { portfolioQueryRepository.searchPortfolios(name, type, status, organizationId) } returns summaries

        // When
        val result = portfolioService.searchPortfolios(name, type, status, organizationId)

        // Then
        assertEquals(1, result.size)
        assertEquals("Test Portfolio", result[0].name)
        
        verify { portfolioQueryRepository.searchPortfolios(name, type, status, organizationId) }
    }

    /**
     * Tests successful portfolio update.
     * 
     * Verifies that the service correctly:
     * 1. Retrieves the existing portfolio from the repository
     * 2. Updates the portfolio with the new data
     * 3. Publishes a PortfolioUpdatedEvent
     * 4. Returns a complete PortfolioResponse with updated data
     */
    @Test
    fun `updatePortfolio should update portfolio successfully`() {
        // Given
        val portfolioId = 1L
        val request = UpdatePortfolioRequest(
            name = "Updated Portfolio",
            description = "Updated Description",
            type = PortfolioType.DEPARTMENTAL,
            status = PortfolioStatus.ARCHIVED
        )
        val existingPortfolio = createTestPortfolio(portfolioId, "Original Portfolio")
        val updatedPortfolio = existingPortfolio.copy(
            name = "Updated Portfolio",
            description = "Updated Description",
            type = PortfolioType.DEPARTMENTAL,
            status = PortfolioStatus.ARCHIVED,
            updatedAt = LocalDateTime.now()
        )
        
        every { portfolioRepository.findById(portfolioId) } returns existingPortfolio
        every { portfolioRepository.update(any()) } returns updatedPortfolio
        every { technologyRepository.findByPortfolioId(portfolioId) } returns emptyList()
        every { eventPublisher.publish(any<PortfolioUpdatedEvent>()) } just Runs

        // When
        val result = portfolioService.updatePortfolio(portfolioId, request)

        // Then
        assertEquals(portfolioId, result.id)
        assertEquals("Updated Portfolio", result.name)
        assertEquals("Updated Description", result.description)
        assertEquals(PortfolioType.DEPARTMENTAL, result.type)
        assertEquals(PortfolioStatus.ARCHIVED, result.status)
        
        verify { portfolioRepository.findById(portfolioId) }
        verify { portfolioRepository.update(any()) }
        verify { eventPublisher.publish(any<PortfolioUpdatedEvent>()) }
    }

    /**
     * Tests portfolio update when the portfolio doesn't exist.
     * 
     * Verifies that the service correctly:
     * 1. Attempts to retrieve the portfolio from the repository
     * 2. Throws an appropriate exception when the portfolio is not found
     * 3. Does not attempt to update the portfolio or publish events
     */
    @Test
    fun `updatePortfolio should throw RuntimeException when portfolio not found`() {
        // Given
        val portfolioId = 999L
        val request = UpdatePortfolioRequest(name = "Updated Portfolio")
        
        every { portfolioRepository.findById(portfolioId) } returns null

        // When & Then
        val exception = assertThrows<RuntimeException> {
            portfolioService.updatePortfolio(portfolioId, request)
        }
        
        assertTrue(exception.message!!.contains("Failed to update portfolio"))
        assertTrue(exception.cause is IllegalArgumentException)
        
        verify { portfolioRepository.findById(portfolioId) }
        verify(exactly = 0) { portfolioRepository.update(any()) }
    }

    /**
     * Tests successful portfolio deletion when the portfolio is empty.
     * 
     * Verifies that the service correctly:
     * 1. Retrieves the portfolio from the repository
     * 2. Checks that the portfolio has no technologies
     * 3. Deletes the portfolio from the repository
     * 4. Returns true to indicate successful deletion
     */
    @Test
    fun `deletePortfolio should delete portfolio successfully when empty`() {
        // Given
        val portfolioId = 1L
        val portfolio = createTestPortfolio(portfolioId, "Test Portfolio")
        
        every { portfolioRepository.findById(portfolioId) } returns portfolio
        every { technologyRepository.countByPortfolioId(portfolioId) } returns 0L
        every { portfolioRepository.delete(portfolioId) } returns true

        // When
        val result = portfolioService.deletePortfolio(portfolioId)

        // Then
        assertTrue(result)
        
        verify { portfolioRepository.findById(portfolioId) }
        verify { technologyRepository.countByPortfolioId(portfolioId) }
        verify { portfolioRepository.delete(portfolioId) }
    }

    /**
     * Tests portfolio deletion when the portfolio contains technologies.
     * 
     * Verifies that the service correctly:
     * 1. Retrieves the portfolio from the repository
     * 2. Checks that the portfolio has technologies
     * 3. Throws an appropriate exception to prevent deletion
     * 4. Does not attempt to delete the portfolio
     */
    @Test
    fun `deletePortfolio should throw RuntimeException when portfolio has technologies`() {
        // Given
        val portfolioId = 1L
        val portfolio = createTestPortfolio(portfolioId, "Test Portfolio")
        
        every { portfolioRepository.findById(portfolioId) } returns portfolio
        every { technologyRepository.countByPortfolioId(portfolioId) } returns 3L

        // When & Then
        val exception = assertThrows<RuntimeException> {
            portfolioService.deletePortfolio(portfolioId)
        }
        
        assertTrue(exception.message!!.contains("Failed to delete portfolio"))
        assertTrue(exception.cause?.message!!.contains("Cannot delete portfolio with technologies"))
        
        verify { portfolioRepository.findById(portfolioId) }
        verify { technologyRepository.countByPortfolioId(portfolioId) }
        verify(exactly = 0) { portfolioRepository.delete(any()) }
    }

    /**
     * Tests portfolio deletion when the portfolio doesn't exist.
     * 
     * Verifies that the service correctly:
     * 1. Attempts to retrieve the portfolio from the repository
     * 2. Throws an appropriate exception when the portfolio is not found
     * 3. Does not attempt to check technology count or delete the portfolio
     */
    @Test
    fun `deletePortfolio should throw RuntimeException when portfolio not found`() {
        // Given
        val portfolioId = 999L
        
        every { portfolioRepository.findById(portfolioId) } returns null

        // When & Then
        val exception = assertThrows<RuntimeException> {
            portfolioService.deletePortfolio(portfolioId)
        }
        
        assertTrue(exception.message!!.contains("Failed to delete portfolio"))
        assertTrue(exception.cause is IllegalArgumentException)
        
        verify { portfolioRepository.findById(portfolioId) }
        verify(exactly = 0) { technologyRepository.countByPortfolioId(any()) }
    }

    /**
     * Tests successful technology addition to a portfolio.
     * 
     * Verifies that the service correctly:
     * 1. Retrieves the portfolio from the repository
     * 2. Saves the new technology to the repository
     * 3. Publishes a TechnologyAddedEvent
     * 4. Returns a complete TechnologyResponse with correct data
     */
    @Test
    fun `addTechnology should add technology successfully`() {
        // Given
        val portfolioId = 1L
        val request = AddTechnologyRequest(
            name = "Spring Boot",
            description = "Java framework",
            category = "Framework",
            version = "3.2.0",
            type = TechnologyType.FRAMEWORK,
            maturityLevel = MaturityLevel.MATURE,
            riskLevel = RiskLevel.LOW,
            annualCost = BigDecimal("5000.00"),
            licenseCost = BigDecimal("1000.00"),
            maintenanceCost = BigDecimal("500.00"),
            vendorName = "VMware",
            vendorContact = "support@vmware.com",
            supportContractExpiry = testDateTime.plusYears(1)
        )
        val portfolio = createTestPortfolio(portfolioId, "Test Portfolio")
        val savedTechnology = Technology(
            id = 1L,
            name = "Spring Boot",
            description = "Java framework", // Match the request description
            category = "Framework",
            version = "3.2.0",
            type = TechnologyType.FRAMEWORK,
            maturityLevel = MaturityLevel.MATURE,
            riskLevel = RiskLevel.LOW,
            annualCost = BigDecimal("5000.00"),
            licenseCost = BigDecimal("1000.00"),
            maintenanceCost = BigDecimal("500.00"),
            vendorName = "VMware",
            vendorContact = "support@vmware.com",
            supportContractExpiry = testDateTime.plusYears(1),
            isActive = true,
            createdAt = testDateTime,
            updatedAt = testDateTime,
            portfolioId = portfolioId
        )
        
        every { portfolioRepository.findById(portfolioId) } returns portfolio
        every { technologyRepository.save(any()) } returns savedTechnology
        every { eventPublisher.publish(any<TechnologyAddedEvent>()) } just Runs

        // When
        val result = portfolioService.addTechnology(portfolioId, request)

        // Then
        assertEquals(1L, result.id)
        assertEquals("Spring Boot", result.name)
        assertEquals("Java framework", result.description)
        assertEquals("Framework", result.category)
        assertEquals("3.2.0", result.version)
        assertEquals(TechnologyType.FRAMEWORK, result.type)
        assertEquals(MaturityLevel.MATURE, result.maturityLevel)
        assertEquals(RiskLevel.LOW, result.riskLevel)
        assertEquals(BigDecimal("5000.00"), result.annualCost)
        assertEquals("VMware", result.vendorName)
        
        verify { portfolioRepository.findById(portfolioId) }
        verify { technologyRepository.save(any()) }
        verify { eventPublisher.publish(any<TechnologyAddedEvent>()) }
    }

    /**
     * Tests technology addition when the portfolio doesn't exist.
     * 
     * Verifies that the service correctly:
     * 1. Attempts to retrieve the portfolio from the repository
     * 2. Throws an appropriate exception when the portfolio is not found
     * 3. Does not attempt to save the technology or publish events
     */
    @Test
    fun `addTechnology should throw RuntimeException when portfolio not found`() {
        // Given
        val portfolioId = 999L
        val request = AddTechnologyRequest(
            name = "Spring Boot",
            category = "Framework",
            type = TechnologyType.FRAMEWORK,
            maturityLevel = MaturityLevel.MATURE,
            riskLevel = RiskLevel.LOW
        )
        
        every { portfolioRepository.findById(portfolioId) } returns null

        // When & Then
        val exception = assertThrows<RuntimeException> {
            portfolioService.addTechnology(portfolioId, request)
        }
        
        assertTrue(exception.message!!.contains("Failed to add technology"))
        assertTrue(exception.cause is IllegalArgumentException)
        
        verify { portfolioRepository.findById(portfolioId) }
        verify(exactly = 0) { technologyRepository.save(any()) }
    }

    /**
     * Tests retrieving a technology by ID when it exists.
     * 
     * Verifies that the service correctly:
     * 1. Retrieves the technology from the repository
     * 2. Returns a complete TechnologyResponse with correct data
     */
    @Test
    fun `getTechnology should return technology when found`() {
        // Given
        val technologyId = 1L
        val technology = createTestTechnology(technologyId, "Spring Boot", 1L)
        
        every { technologyRepository.findById(technologyId) } returns technology

        // When
        val result = portfolioService.getTechnology(technologyId)

        // Then
        assertEquals(technologyId, result.id)
        assertEquals("Spring Boot", result.name)
        assertEquals("Framework", result.category)
        assertEquals(TechnologyType.FRAMEWORK, result.type)
        
        verify { technologyRepository.findById(technologyId) }
    }

    /**
     * Tests retrieving a technology by ID when it doesn't exist.
     * 
     * Verifies that the service correctly:
     * 1. Attempts to retrieve the technology from the repository
     * 2. Throws an appropriate exception when the technology is not found
     */
    @Test
    fun `getTechnology should throw IllegalArgumentException when not found`() {
        // Given
        val technologyId = 999L
        
        every { technologyRepository.findById(technologyId) } returns null

        // When & Then
        val exception = assertThrows<IllegalArgumentException> {
            portfolioService.getTechnology(technologyId)
        }
        
        assertEquals("Technology with id $technologyId not found", exception.message)
        verify { technologyRepository.findById(technologyId) }
    }

    /**
     * Tests retrieving technologies by portfolio ID.
     * 
     * Verifies that the service correctly:
     * 1. Delegates to the query repository for optimized retrieval
     * 2. Returns the list of technology summaries for the specified portfolio
     */
    @Test
    fun `getTechnologiesByPortfolio should return portfolio technologies`() {
        // Given
        val portfolioId = 1L
        val summaries = listOf(
            createTestTechnologySummary(1L, "Spring Boot"),
            createTestTechnologySummary(2L, "PostgreSQL")
        )
        
        every { technologyQueryRepository.findTechnologySummariesByPortfolio(portfolioId) } returns summaries

        // When
        val result = portfolioService.getTechnologiesByPortfolio(portfolioId)

        // Then
        assertEquals(2, result.size)
        assertEquals("Spring Boot", result[0].name)
        assertEquals("PostgreSQL", result[1].name)
        
        verify { technologyQueryRepository.findTechnologySummariesByPortfolio(portfolioId) }
    }

    /**
     * Tests successful technology update.
     * 
     * Verifies that the service correctly:
     * 1. Retrieves the existing technology from the repository
     * 2. Updates the technology with the new data
     * 3. Returns a complete TechnologyResponse with updated data
     */
    @Test
    fun `updateTechnology should update technology successfully`() {
        // Given
        val technologyId = 1L
        val request = UpdateTechnologyRequest(
            name = "Updated Spring Boot",
            description = "Updated description",
            version = "3.3.0",
            annualCost = BigDecimal("6000.00"),
            riskLevel = RiskLevel.MEDIUM
        )
        val existingTechnology = createTestTechnology(technologyId, "Spring Boot", 1L)
        val updatedTechnology = existingTechnology.copy(
            name = "Updated Spring Boot",
            description = "Updated description",
            version = "3.3.0",
            annualCost = BigDecimal("6000.00"),
            riskLevel = RiskLevel.MEDIUM,
            updatedAt = LocalDateTime.now()
        )
        
        every { technologyRepository.findById(technologyId) } returns existingTechnology
        every { technologyRepository.update(any()) } returns updatedTechnology

        // When
        val result = portfolioService.updateTechnology(technologyId, request)

        // Then
        assertEquals(technologyId, result.id)
        assertEquals("Updated Spring Boot", result.name)
        assertEquals("Updated description", result.description)
        assertEquals("3.3.0", result.version)
        assertEquals(BigDecimal("6000.00"), result.annualCost)
        assertEquals(RiskLevel.MEDIUM, result.riskLevel)
        
        verify { technologyRepository.findById(technologyId) }
        verify { technologyRepository.update(any()) }
    }

    /**
     * Tests technology update when the technology doesn't exist.
     * 
     * Verifies that the service correctly:
     * 1. Attempts to retrieve the technology from the repository
     * 2. Throws an appropriate exception when the technology is not found
     * 3. Does not attempt to update the technology
     */
    @Test
    fun `updateTechnology should throw RuntimeException when technology not found`() {
        // Given
        val technologyId = 999L
        val request = UpdateTechnologyRequest(name = "Updated Technology")
        
        every { technologyRepository.findById(technologyId) } returns null

        // When & Then
        val exception = assertThrows<RuntimeException> {
            portfolioService.updateTechnology(technologyId, request)
        }
        
        assertTrue(exception.message!!.contains("Failed to update technology"))
        assertTrue(exception.cause is IllegalArgumentException)
        
        verify { technologyRepository.findById(technologyId) }
        verify(exactly = 0) { technologyRepository.update(any()) }
    }

    /**
     * Tests successful technology removal from a portfolio.
     * 
     * Verifies that the service correctly:
     * 1. Retrieves both the portfolio and technology from the repository
     * 2. Validates that the technology belongs to the specified portfolio
     * 3. Deletes the technology from the repository
     * 4. Publishes a TechnologyRemovedEvent
     * 5. Returns true to indicate successful removal
     */
    @Test
    fun `removeTechnology should remove technology successfully`() {
        // Given
        val portfolioId = 1L
        val technologyId = 10L
        val portfolio = createTestPortfolio(portfolioId, "Test Portfolio")
        val technology = createTestTechnology(technologyId, "Spring Boot", portfolioId)
        
        every { portfolioRepository.findById(portfolioId) } returns portfolio
        every { technologyRepository.findById(technologyId) } returns technology
        every { technologyRepository.delete(technologyId) } returns true
        every { eventPublisher.publish(any<TechnologyRemovedEvent>()) } just Runs

        // When
        val result = portfolioService.removeTechnology(portfolioId, technologyId)

        // Then
        assertTrue(result)
        
        verify { portfolioRepository.findById(portfolioId) }
        verify { technologyRepository.findById(technologyId) }
        verify { technologyRepository.delete(technologyId) }
        verify { eventPublisher.publish(any<TechnologyRemovedEvent>()) }
    }

    /**
     * Tests technology removal when the portfolio doesn't exist.
     * 
     * Verifies that the service correctly:
     * 1. Attempts to retrieve the portfolio from the repository
     * 2. Throws an appropriate exception when the portfolio is not found
     * 3. Does not attempt to retrieve or delete the technology
     */
    @Test
    fun `removeTechnology should throw RuntimeException when portfolio not found`() {
        // Given
        val portfolioId = 999L
        val technologyId = 10L
        
        every { portfolioRepository.findById(portfolioId) } returns null

        // When & Then
        val exception = assertThrows<RuntimeException> {
            portfolioService.removeTechnology(portfolioId, technologyId)
        }
        
        assertTrue(exception.message!!.contains("Failed to remove technology"))
        assertTrue(exception.cause is IllegalArgumentException)
        
        verify { portfolioRepository.findById(portfolioId) }
        verify(exactly = 0) { technologyRepository.findById(any()) }
    }

    /**
     * Tests technology removal when the technology doesn't exist.
     * 
     * Verifies that the service correctly:
     * 1. Retrieves the portfolio from the repository
     * 2. Attempts to retrieve the technology from the repository
     * 3. Throws an appropriate exception when the technology is not found
     * 4. Does not attempt to delete the technology
     */
    @Test
    fun `removeTechnology should throw RuntimeException when technology not found`() {
        // Given
        val portfolioId = 1L
        val technologyId = 999L
        val portfolio = createTestPortfolio(portfolioId, "Test Portfolio")
        
        every { portfolioRepository.findById(portfolioId) } returns portfolio
        every { technologyRepository.findById(technologyId) } returns null

        // When & Then
        val exception = assertThrows<RuntimeException> {
            portfolioService.removeTechnology(portfolioId, technologyId)
        }
        
        assertTrue(exception.message!!.contains("Failed to remove technology"))
        assertTrue(exception.cause is IllegalArgumentException)
        
        verify { portfolioRepository.findById(portfolioId) }
        verify { technologyRepository.findById(technologyId) }
        verify(exactly = 0) { technologyRepository.delete(any()) }
    }

    /**
     * Tests technology removal when the technology belongs to a different portfolio.
     * 
     * Verifies that the service correctly:
     * 1. Retrieves both the portfolio and technology from the repository
     * 2. Validates that the technology belongs to the specified portfolio
     * 3. Throws an appropriate exception when the technology belongs to a different portfolio
     * 4. Does not attempt to delete the technology
     */
    @Test
    fun `removeTechnology should throw RuntimeException when technology belongs to different portfolio`() {
        // Given
        val portfolioId = 1L
        val technologyId = 10L
        val portfolio = createTestPortfolio(portfolioId, "Test Portfolio")
        val technology = createTestTechnology(technologyId, "Spring Boot", 2L) // Different portfolio
        
        every { portfolioRepository.findById(portfolioId) } returns portfolio
        every { technologyRepository.findById(technologyId) } returns technology

        // When & Then
        val exception = assertThrows<RuntimeException> {
            portfolioService.removeTechnology(portfolioId, technologyId)
        }
        
        assertTrue(exception.message!!.contains("Failed to remove technology"))
        assertTrue(exception.cause?.message!!.contains("Technology does not belong to the specified portfolio"))
        
        verify { portfolioRepository.findById(portfolioId) }
        verify { technologyRepository.findById(technologyId) }
        verify(exactly = 0) { technologyRepository.delete(any()) }
    }

    // Helper Methods
    /**
     * Creates a test TechnologyPortfolio instance with the specified ID and name.
     * 
     * @param id The portfolio ID
     * @param name The portfolio name
     * @return A test TechnologyPortfolio instance
     */
    private fun createTestPortfolio(id: Long, name: String) = TechnologyPortfolio(
        id = id,
        name = name,
        description = "Test Description",
        type = PortfolioType.ENTERPRISE,
        status = PortfolioStatus.ACTIVE,
        isActive = true,
        createdAt = testDateTime,
        updatedAt = testDateTime,
        ownerId = 100L,
        organizationId = 200L
    )

    /**
     * Creates a test PortfolioSummary instance with the specified ID and name.
     * 
     * @param id The portfolio ID
     * @param name The portfolio name
     * @return A test PortfolioSummary instance
     */
    private fun createTestPortfolioSummary(id: Long, name: String) = PortfolioSummary(
        id = id,
        name = name,
        type = PortfolioType.ENTERPRISE,
        status = PortfolioStatus.ACTIVE,
        ownerId = 100L,
        organizationId = 200L,
        technologyCount = 5,
        totalAnnualCost = BigDecimal("10000.00"),
        lastUpdated = testDateTime
    )

    /**
     * Creates a test Technology instance with the specified ID, name, and portfolio ID.
     * 
     * @param id The technology ID
     * @param name The technology name
     * @param portfolioId The portfolio ID that owns this technology
     * @return A test Technology instance
     */
    private fun createTestTechnology(id: Long, name: String, portfolioId: Long) = Technology(
        id = id,
        name = name,
        description = "Test Technology",
        category = "Framework",
        version = "3.2.0",
        type = TechnologyType.FRAMEWORK,
        maturityLevel = MaturityLevel.MATURE,
        riskLevel = RiskLevel.LOW,
        annualCost = BigDecimal("5000.00"),
        licenseCost = BigDecimal("1000.00"),
        maintenanceCost = BigDecimal("500.00"),
        vendorName = "VMware",
        vendorContact = "support@vmware.com",
        supportContractExpiry = testDateTime.plusYears(1),
        isActive = true,
        createdAt = testDateTime,
        updatedAt = testDateTime,
        portfolioId = portfolioId
    )

    /**
     * Creates a test TechnologySummary instance with the specified ID and name.
     * 
     * @param id The technology ID
     * @param name The technology name
     * @return A test TechnologySummary instance
     */
    private fun createTestTechnologySummary(id: Long, name: String) = TechnologySummary(
        id = id,
        name = name,
        category = "Framework",
        type = TechnologyType.FRAMEWORK,
        maturityLevel = MaturityLevel.MATURE,
        riskLevel = RiskLevel.LOW,
        annualCost = BigDecimal("5000.00"),
        vendorName = "VMware"
    )
} 