package com.company.techportfolio.portfolio.adapter.out.persistence

import com.company.techportfolio.portfolio.adapter.out.persistence.repository.TechnologyJpaRepository
import com.company.techportfolio.portfolio.adapter.out.persistence.entity.TechnologyEntity
import com.company.techportfolio.shared.domain.model.*
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

/**
 * Unit tests for TechnologyRepositoryAdapter.
 * 
 * This test class verifies the functionality of the TechnologyRepositoryAdapter, which
 * is the implementation of the TechnologyRepository and TechnologyQueryRepository interfaces
 * in the hexagonal architecture. It tests the adapter's ability to correctly translate
 * between domain models and JPA entities, and to delegate operations to the underlying
 * Spring Data JPA repository.
 * 
 * ## Test Coverage:
 * - Basic CRUD operations (find, save, update, delete)
 * - Query operations by various criteria (portfolio, category, type, vendor, etc.)
 * - Summary data generation for lightweight responses
 * - Error handling and edge cases
 * - Domain model to entity mapping
 * - Entity to domain model mapping
 * - Entity to summary model mapping
 * 
 * ## Testing Approach:
 * - Uses MockK for mocking the JPA repository
 * - Follows the Given-When-Then pattern for test clarity
 * - Verifies both return values and repository interactions
 * 
 * @author Technology Portfolio Team
 * @since 1.0.0
 * @see TechnologyRepositoryAdapter
 * @see TechnologyJpaRepository
 * @see Technology
 */
class TechnologyRepositoryAdapterTest {

    /**
     * Mock of the Spring Data JPA repository for technology entities.
     */
    private val technologyJpaRepository = mockk<TechnologyJpaRepository>()

    /**
     * The adapter under test.
     */
    private lateinit var technologyRepositoryAdapter: TechnologyRepositoryAdapter

    /**
     * Fixed test date/time for consistent testing.
     */
    private val testDateTime = LocalDateTime.of(2024, 1, 15, 10, 30, 0)

    /**
     * Set up the test environment before each test.
     * 
     * Initializes a fresh instance of the TechnologyRepositoryAdapter with mock
     * dependencies for each test to ensure test isolation.
     */
    @BeforeEach
    fun setUp() {
        clearAllMocks()
        technologyRepositoryAdapter = TechnologyRepositoryAdapter(technologyJpaRepository)
    }

    /**
     * Tests that findById returns a correctly mapped domain model when a technology exists.
     * 
     * Verifies that:
     * 1. The adapter correctly delegates to the JPA repository
     * 2. The returned entity is properly mapped to a domain model
     * 3. All properties are correctly transferred during mapping
     */
    @Test
    fun `findById should return technology when found`() {
        // Given
        val technologyId = 1L
        val entity = createTestTechnologyEntity(technologyId, "Spring Boot")
        every { technologyJpaRepository.findById(technologyId) } returns Optional.of(entity)

        // When
        val result = technologyRepositoryAdapter.findById(technologyId)

        // Then
        assertNotNull(result)
        assertEquals(technologyId, result!!.id)
        assertEquals("Spring Boot", result.name)
        assertEquals(TechnologyType.FRAMEWORK, result.type)

        verify { technologyJpaRepository.findById(technologyId) }
    }

    /**
     * Tests that findById returns null when a technology does not exist.
     * 
     * Verifies that:
     * 1. The adapter correctly delegates to the JPA repository
     * 2. The adapter returns null when the repository returns an empty Optional
     */
    @Test
    fun `findById should return null when not found`() {
        // Given
        val technologyId = 999L
        every { technologyJpaRepository.findById(technologyId) } returns Optional.empty()

        // When
        val result = technologyRepositoryAdapter.findById(technologyId)

        // Then
        assertNull(result)
        verify { technologyJpaRepository.findById(technologyId) }
    }

    /**
     * Tests that findByName returns a correctly mapped domain model when a technology exists.
     * 
     * Verifies that:
     * 1. The adapter correctly delegates to the JPA repository
     * 2. The returned entity is properly mapped to a domain model
     * 3. The name property matches the search criteria
     */
    @Test
    fun `findByName should return technology when found`() {
        // Given
        val name = "Spring Boot"
        val entity = createTestTechnologyEntity(1L, name)
        every { technologyJpaRepository.findByName(name) } returns entity

        // When
        val result = technologyRepositoryAdapter.findByName(name)

        // Then
        assertNotNull(result)
        assertEquals(name, result!!.name)
        verify { technologyJpaRepository.findByName(name) }
    }

    /**
     * Tests that findByName returns null when a technology does not exist.
     * 
     * Verifies that:
     * 1. The adapter correctly delegates to the JPA repository
     * 2. The adapter returns null when the repository returns null
     */
    @Test
    fun `findByName should return null when not found`() {
        // Given
        val name = "Non-existent Technology"
        every { technologyJpaRepository.findByName(name) } returns null

        // When
        val result = technologyRepositoryAdapter.findByName(name)

        // Then
        assertNull(result)
        verify { technologyJpaRepository.findByName(name) }
    }

    /**
     * Tests that findByPortfolioId returns a list of correctly mapped domain models.
     * 
     * Verifies that:
     * 1. The adapter correctly delegates to the JPA repository
     * 2. The returned entities are properly mapped to domain models
     * 3. The correct number of technologies is returned
     */
    @Test
    fun `findByPortfolioId should return portfolio technologies`() {
        // Given
        val portfolioId = 1L
        val entities = listOf(
            createTestTechnologyEntity(1L, "Spring Boot"),
            createTestTechnologyEntity(2L, "PostgreSQL")
        )
        every { technologyJpaRepository.findByPortfolioId(portfolioId) } returns entities

        // When
        val result = technologyRepositoryAdapter.findByPortfolioId(portfolioId)

        // Then
        assertEquals(2, result.size)
        assertEquals("Spring Boot", result[0].name)
        assertEquals("PostgreSQL", result[1].name)
        verify { technologyJpaRepository.findByPortfolioId(portfolioId) }
    }

    /**
     * Tests that findByCategory returns a list of correctly mapped domain models.
     * 
     * Verifies that:
     * 1. The adapter correctly delegates to the JPA repository
     * 2. The returned entities are properly mapped to domain models
     * 3. The category property matches the search criteria
     */
    @Test
    fun `findByCategory should return technologies by category`() {
        // Given
        val category = "Framework"
        val entities = listOf(createTestTechnologyEntity(1L, "Spring Boot"))
        every { technologyJpaRepository.findByCategory(category) } returns entities

        // When
        val result = technologyRepositoryAdapter.findByCategory(category)

        // Then
        assertEquals(1, result.size)
        assertEquals(category, result[0].category)
        verify { technologyJpaRepository.findByCategory(category) }
    }

    /**
     * Tests that findByType returns a list of correctly mapped domain models.
     * 
     * Verifies that:
     * 1. The adapter correctly delegates to the JPA repository
     * 2. The returned entities are properly mapped to domain models
     * 3. The type property matches the search criteria
     */
    @Test
    fun `findByType should return technologies by type`() {
        // Given
        val type = TechnologyType.FRAMEWORK
        val entities = listOf(createTestTechnologyEntity(1L, "Spring Boot"))
        every { technologyJpaRepository.findByType(type) } returns entities

        // When
        val result = technologyRepositoryAdapter.findByType(type)

        // Then
        assertEquals(1, result.size)
        assertEquals(type, result[0].type)
        verify { technologyJpaRepository.findByType(type) }
    }

    /**
     * Tests that findByVendor returns a list of correctly mapped domain models.
     * 
     * Verifies that:
     * 1. The adapter correctly delegates to the JPA repository
     * 2. The returned entities are properly mapped to domain models
     * 3. The vendorName property matches the search criteria
     */
    @Test
    fun `findByVendor should return technologies by vendor`() {
        // Given
        val vendorName = "VMware"
        val entities = listOf(createTestTechnologyEntity(1L, "Spring Boot"))
        every { technologyJpaRepository.findByVendorName(vendorName) } returns entities

        // When
        val result = technologyRepositoryAdapter.findByVendor(vendorName)

        // Then
        assertEquals(1, result.size)
        assertEquals(vendorName, result[0].vendorName)
        verify { technologyJpaRepository.findByVendorName(vendorName) }
    }

    /**
     * Tests that findByMaturityLevel returns a list of correctly mapped domain models.
     * 
     * Verifies that:
     * 1. The adapter correctly delegates to the JPA repository
     * 2. The returned entities are properly mapped to domain models
     * 3. The maturityLevel property matches the search criteria
     */
    @Test
    fun `findByMaturityLevel should return technologies by maturity level`() {
        // Given
        val maturityLevel = MaturityLevel.MATURE
        val entities = listOf(createTestTechnologyEntity(1L, "Spring Boot"))
        every { technologyJpaRepository.findByMaturityLevel(maturityLevel) } returns entities

        // When
        val result = technologyRepositoryAdapter.findByMaturityLevel(maturityLevel)

        // Then
        assertEquals(1, result.size)
        assertEquals(maturityLevel, result[0].maturityLevel)
        verify { technologyJpaRepository.findByMaturityLevel(maturityLevel) }
    }

    /**
     * Tests that findByRiskLevel returns a list of correctly mapped domain models.
     * 
     * Verifies that:
     * 1. The adapter correctly delegates to the JPA repository
     * 2. The returned entities are properly mapped to domain models
     * 3. The riskLevel property matches the search criteria
     */
    @Test
    fun `findByRiskLevel should return technologies by risk level`() {
        // Given
        val riskLevel = RiskLevel.LOW
        val entities = listOf(createTestTechnologyEntity(1L, "Spring Boot"))
        every { technologyJpaRepository.findByRiskLevel(riskLevel) } returns entities

        // When
        val result = technologyRepositoryAdapter.findByRiskLevel(riskLevel)

        // Then
        assertEquals(1, result.size)
        assertEquals(riskLevel, result[0].riskLevel)
        verify { technologyJpaRepository.findByRiskLevel(riskLevel) }
    }

    /**
     * Tests that save correctly persists a new technology.
     * 
     * Verifies that:
     * 1. The adapter correctly delegates to the JPA repository
     * 2. The domain model is properly mapped to an entity before saving
     * 3. The saved entity is properly mapped back to a domain model
     * 4. The returned domain model includes the generated ID
     */
    @Test
    fun `save should save new technology and return saved entity`() {
        // Given
        val technology = createTestTechnology(null, "New Technology")
        val savedEntity = createTestTechnologyEntity(1L, "New Technology")
        every { technologyJpaRepository.save(any()) } returns savedEntity

        // When
        val result = technologyRepositoryAdapter.save(technology)

        // Then
        assertNotNull(result)
        assertEquals(1L, result.id)
        assertEquals("New Technology", result.name)
        verify { technologyJpaRepository.save(any()) }
    }

    @Test
    fun `update should update existing technology and return updated entity`() {
        // Given
        val technology = createTestTechnology(1L, "Updated Technology")
        val updatedEntity = createTestTechnologyEntity(1L, "Updated Technology")
        every { technologyJpaRepository.save(any()) } returns updatedEntity

        // When
        val result = technologyRepositoryAdapter.update(technology)

        // Then
        assertNotNull(result)
        assertEquals(1L, result.id)
        assertEquals("Updated Technology", result.name)
        verify { technologyJpaRepository.save(any()) }
    }

    @Test
    fun `delete should delete technology successfully`() {
        // Given
        val technologyId = 1L
        every { technologyJpaRepository.deleteById(technologyId) } just Runs

        // When
        val result = technologyRepositoryAdapter.delete(technologyId)

        // Then
        assertTrue(result)
        verify { technologyJpaRepository.deleteById(technologyId) }
    }

    @Test
    fun `delete should return false when deletion fails`() {
        // Given
        val technologyId = 1L
        every { technologyJpaRepository.deleteById(technologyId) } throws RuntimeException("Delete failed")

        // When
        val result = technologyRepositoryAdapter.delete(technologyId)

        // Then
        assertFalse(result)
        verify { technologyJpaRepository.deleteById(technologyId) }
    }

    @Test
    fun `existsById should return true when technology exists`() {
        // Given
        val technologyId = 1L
        every { technologyJpaRepository.existsById(technologyId) } returns true

        // When
        val result = technologyRepositoryAdapter.existsById(technologyId)

        // Then
        assertTrue(result)
        verify { technologyJpaRepository.existsById(technologyId) }
    }

    @Test
    fun `existsById should return false when technology does not exist`() {
        // Given
        val technologyId = 999L
        every { technologyJpaRepository.existsById(technologyId) } returns false

        // When
        val result = technologyRepositoryAdapter.existsById(technologyId)

        // Then
        assertFalse(result)
        verify { technologyJpaRepository.existsById(technologyId) }
    }

    @Test
    fun `countByPortfolioId should return technology count for portfolio`() {
        // Given
        val portfolioId = 1L
        val count = 5L
        every { technologyJpaRepository.countByPortfolioId(portfolioId) } returns count

        // When
        val result = technologyRepositoryAdapter.countByPortfolioId(portfolioId)

        // Then
        assertEquals(count, result)
        verify { technologyJpaRepository.countByPortfolioId(portfolioId) }
    }

    @Test
    fun `findTechnologySummary should return summary when technology found`() {
        // Given
        val technologyId = 1L
        val entity = createTestTechnologyEntity(technologyId, "Spring Boot")
        every { technologyJpaRepository.findById(technologyId) } returns Optional.of(entity)

        // When
        val result = technologyRepositoryAdapter.findTechnologySummary(technologyId)

        // Then
        assertNotNull(result)
        assertEquals(technologyId, result!!.id)
        assertEquals("Spring Boot", result.name)
        assertEquals("Framework", result.category)
        assertEquals(TechnologyType.FRAMEWORK, result.type)

        verify { technologyJpaRepository.findById(technologyId) }
    }

    @Test
    fun `findTechnologySummary should return null when technology not found`() {
        // Given
        val technologyId = 999L
        every { technologyJpaRepository.findById(technologyId) } returns Optional.empty()

        // When
        val result = technologyRepositoryAdapter.findTechnologySummary(technologyId)

        // Then
        assertNull(result)
        verify { technologyJpaRepository.findById(technologyId) }
    }

    @Test
    fun `findTechnologySummariesByPortfolio should return portfolio technology summaries`() {
        // Given
        val portfolioId = 1L
        val entities = listOf(
            createTestTechnologyEntity(1L, "Spring Boot"),
            createTestTechnologyEntity(2L, "PostgreSQL")
        )
        every { technologyJpaRepository.findByPortfolioId(portfolioId) } returns entities

        // When
        val result = technologyRepositoryAdapter.findTechnologySummariesByPortfolio(portfolioId)

        // Then
        assertEquals(2, result.size)
        assertEquals("Spring Boot", result[0].name)
        assertEquals("PostgreSQL", result[1].name)

        verify { technologyJpaRepository.findByPortfolioId(portfolioId) }
    }

    @Test
    fun `findTechnologySummariesByCategory should return category technology summaries`() {
        // Given
        val category = "Framework"
        val entities = listOf(createTestTechnologyEntity(1L, "Spring Boot"))
        every { technologyJpaRepository.findByCategory(category) } returns entities

        // When
        val result = technologyRepositoryAdapter.findTechnologySummariesByCategory(category)

        // Then
        assertEquals(1, result.size)
        assertEquals(category, result[0].category)

        verify { technologyJpaRepository.findByCategory(category) }
    }

    @Test
    fun `findTechnologySummariesByType should return type technology summaries`() {
        // Given
        val type = TechnologyType.FRAMEWORK
        val entities = listOf(createTestTechnologyEntity(1L, "Spring Boot"))
        every { technologyJpaRepository.findByType(type) } returns entities

        // When
        val result = technologyRepositoryAdapter.findTechnologySummariesByType(type)

        // Then
        assertEquals(1, result.size)
        assertEquals(type, result[0].type)

        verify { technologyJpaRepository.findByType(type) }
    }

    @Test
    fun `findTechnologySummariesByVendor should return vendor technology summaries`() {
        // Given
        val vendorName = "VMware"
        val entities = listOf(createTestTechnologyEntity(1L, "Spring Boot"))
        every { technologyJpaRepository.findByVendorName(vendorName) } returns entities

        // When
        val result = technologyRepositoryAdapter.findTechnologySummariesByVendor(vendorName)

        // Then
        assertEquals(1, result.size)
        assertEquals(vendorName, result[0].vendorName)

        verify { technologyJpaRepository.findByVendorName(vendorName) }
    }

    @Test
    fun `findAllTechnologySummaries should return all technology summaries`() {
        // Given
        val entities = listOf(
            createTestTechnologyEntity(1L, "Spring Boot"),
            createTestTechnologyEntity(2L, "PostgreSQL")
        )
        every { technologyJpaRepository.findByIsActiveTrue() } returns entities

        // When
        val result = technologyRepositoryAdapter.findAllTechnologySummaries()

        // Then
        assertEquals(2, result.size)

        verify { technologyJpaRepository.findByIsActiveTrue() }
    }

    @Test
    fun `searchTechnologies should return search results`() {
        // Given
        val name = "spring"
        val category = "Framework"
        val type = TechnologyType.FRAMEWORK
        val vendorName = "VMware"
        val entities = listOf(createTestTechnologyEntity(1L, "Spring Boot"))
        
        every { technologyJpaRepository.searchTechnologies(name, category, type, vendorName) } returns entities

        // When
        val result = technologyRepositoryAdapter.searchTechnologies(name, category, type, vendorName)

        // Then
        assertEquals(1, result.size)
        assertEquals("Spring Boot", result[0].name)

        verify { technologyJpaRepository.searchTechnologies(name, category, type, vendorName) }
    }

    // Helper Methods
    private fun createTestTechnologyEntity(id: Long, name: String) = TechnologyEntity(
        id = id,
        name = name,
        description = "Test Technology",
        category = "Framework",
        version = "3.2.0",
        type = TechnologyType.FRAMEWORK,
        maturityLevel = MaturityLevel.MATURE,
        riskLevel = RiskLevel.LOW,
        annualCost = BigDecimal("5000.00"),
        licenseCost = null,
        maintenanceCost = null,
        vendorName = "VMware",
        vendorContact = null,
        supportContractExpiry = null,
        isActive = true,
        createdAt = testDateTime,
        updatedAt = testDateTime,
        portfolioId = 1L
    )

    private fun createTestTechnology(id: Long?, name: String) = Technology(
        id = id,
        name = name,
        description = "Test Technology",
        category = "Framework",
        version = "3.2.0",
        type = TechnologyType.FRAMEWORK,
        maturityLevel = MaturityLevel.MATURE,
        riskLevel = RiskLevel.LOW,
        annualCost = BigDecimal("5000.00"),
        licenseCost = null,
        maintenanceCost = null,
        vendorName = "VMware",
        vendorContact = null,
        supportContractExpiry = null,
        isActive = true,
        createdAt = testDateTime,
        updatedAt = testDateTime,
        portfolioId = 1L
    )
} 