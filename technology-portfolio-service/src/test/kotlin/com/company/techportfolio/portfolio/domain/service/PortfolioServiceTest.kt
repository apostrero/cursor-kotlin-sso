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
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.time.LocalDateTime
import org.junit.jupiter.api.Assertions.*

class PortfolioServiceTest {

    private lateinit var portfolioService: PortfolioService
    private lateinit var portfolioRepository: PortfolioRepository
    private lateinit var technologyRepository: TechnologyRepository
    private lateinit var portfolioQueryRepository: PortfolioQueryRepository
    private lateinit var technologyQueryRepository: TechnologyQueryRepository
    private lateinit var eventPublisher: EventPublisher

    @BeforeEach
    fun setUp() {
        portfolioRepository = mockk()
        technologyRepository = mockk()
        portfolioQueryRepository = mockk()
        technologyQueryRepository = mockk()
        eventPublisher = mockk(relaxed = true)

        portfolioService = PortfolioService(
            portfolioRepository,
            technologyRepository,
            portfolioQueryRepository,
            technologyQueryRepository,
            eventPublisher
        )
    }

    @Test
    fun `createPortfolio should create portfolio successfully`() {
        // Given
        val request = CreatePortfolioRequest(
            name = "Test Portfolio",
            description = "Test Description",
            type = PortfolioType.ENTERPRISE,
            ownerId = 1L,
            organizationId = 100L
        )

        val portfolio = TechnologyPortfolio(
            id = 1L,
            name = request.name,
            description = request.description,
            type = request.type,
            status = PortfolioStatus.ACTIVE,
            isActive = true,
            createdAt = LocalDateTime.now(),
            ownerId = request.ownerId,
            organizationId = request.organizationId
        )

        every { portfolioRepository.findByName(request.name) } returns null
        every { portfolioRepository.save(any()) } returns portfolio
        every { technologyRepository.findByPortfolioId(1L) } returns emptyList()

        // When
        val result = portfolioService.createPortfolio(request)

        // Then
        assertNotNull(result)
        assertEquals(1L, result.id)
        assertEquals(request.name, result.name)
        assertEquals(request.description, result.description)
        assertEquals(request.type, result.type)
        assertEquals(PortfolioStatus.ACTIVE, result.status)
        assertEquals(0, result.technologyCount)

        verify {
            portfolioRepository.findByName(request.name)
            portfolioRepository.save(any())
            eventPublisher.publish(any<PortfolioCreatedEvent>())
        }
    }

    @Test
    fun `createPortfolio should throw exception when portfolio with same name exists`() {
        // Given
        val request = CreatePortfolioRequest(
            name = "Test Portfolio",
            description = "Test Description",
            type = PortfolioType.ENTERPRISE,
            ownerId = 1L
        )

        val existingPortfolio = TechnologyPortfolio(
            id = 1L,
            name = request.name,
            description = "Existing Description",
            type = PortfolioType.ENTERPRISE,
            status = PortfolioStatus.ACTIVE,
            isActive = true,
            createdAt = LocalDateTime.now(),
            ownerId = 2L
        )

        every { portfolioRepository.findByName(request.name) } returns existingPortfolio

        // When & Then
        val exception = assertThrows<RuntimeException> {
            portfolioService.createPortfolio(request)
        }
        assertTrue(exception.message!!.contains("already exists"))

        verify {
            portfolioRepository.findByName(request.name)
        }
        verify(exactly = 0) {
            portfolioRepository.save(any())
            eventPublisher.publish(any<PortfolioCreatedEvent>())
        }
    }

    @Test
    fun `updatePortfolio should update portfolio successfully`() {
        // Given
        val portfolioId = 1L
        val request = UpdatePortfolioRequest(
            name = "Updated Portfolio",
            description = "Updated Description",
            status = PortfolioStatus.ARCHIVED
        )

        val existingPortfolio = TechnologyPortfolio(
            id = portfolioId,
            name = "Original Name",
            description = "Original Description",
            type = PortfolioType.ENTERPRISE,
            status = PortfolioStatus.ACTIVE,
            isActive = true,
            createdAt = LocalDateTime.now(),
            ownerId = 1L
        )

        val updatedPortfolio = existingPortfolio.copy(
            name = request.name!!,
            description = request.description!!,
            status = request.status!!,
            updatedAt = LocalDateTime.now()
        )

        every { portfolioRepository.findById(portfolioId) } returns existingPortfolio
        every { portfolioRepository.update(any()) } returns updatedPortfolio
        every { technologyRepository.findByPortfolioId(portfolioId) } returns emptyList()

        // When
        val result = portfolioService.updatePortfolio(portfolioId, request)

        // Then
        assertNotNull(result)
        assertEquals(portfolioId, result.id)
        assertEquals(request.name, result.name)
        assertEquals(request.description, result.description)
        assertEquals(request.status, result.status)

        verify {
            portfolioRepository.findById(portfolioId)
            portfolioRepository.update(any())
            eventPublisher.publish(any<PortfolioUpdatedEvent>())
        }
    }

    @Test
    fun `updatePortfolio should throw exception when portfolio not found`() {
        // Given
        val portfolioId = 999L
        val request = UpdatePortfolioRequest(name = "Updated Portfolio")

        every { portfolioRepository.findById(portfolioId) } returns null

        // When & Then
        val exception = assertThrows<RuntimeException> {
            portfolioService.updatePortfolio(portfolioId, request)
        }
        assertTrue(exception.message!!.contains("not found"))

        verify {
            portfolioRepository.findById(portfolioId)
        }
        verify(exactly = 0) {
            portfolioRepository.update(any())
            eventPublisher.publish(any<PortfolioUpdatedEvent>())
        }
    }

    @Test
    fun `addTechnology should add technology to portfolio successfully`() {
        // Given
        val portfolioId = 1L
        val request = AddTechnologyRequest(
            name = "Test Technology",
            description = "Test Technology Description",
            type = TechnologyType.DATABASE,
            maturityLevel = MaturityLevel.PRODUCTION,
            riskLevel = RiskLevel.LOW,
            annualCost = BigDecimal("1000.00"),
            vendorName = "Test Vendor",
            version = "1.0.0",
            category = "Database"
        )

        val portfolio = TechnologyPortfolio(
            id = portfolioId,
            name = "Test Portfolio",
            description = "Test Description",
            type = PortfolioType.ENTERPRISE,
            status = PortfolioStatus.ACTIVE,
            isActive = true,
            createdAt = LocalDateTime.now(),
            ownerId = 1L
        )

        val technology = Technology(
            id = 1L,
            name = request.name,
            description = request.description,
            category = request.category,
            type = request.type,
            maturityLevel = request.maturityLevel,
            riskLevel = request.riskLevel,
            annualCost = request.annualCost,
            vendorName = request.vendorName,
            version = request.version,
            portfolioId = portfolioId,
            isActive = true,
            createdAt = LocalDateTime.now()
        )

        every { portfolioRepository.findById(portfolioId) } returns portfolio
        every { technologyRepository.save(any()) } returns technology

        // When
        val result = portfolioService.addTechnology(portfolioId, request)

        // Then
        assertNotNull(result)
        assertEquals(1L, result.id)
        assertEquals(request.name, result.name)
        assertEquals(request.description, result.description)
        assertEquals(request.type, result.type)
        assertEquals(request.maturityLevel, result.maturityLevel)
        assertEquals(request.riskLevel, result.riskLevel)
        assertEquals(request.annualCost, result.annualCost)
        assertEquals(request.vendorName, result.vendorName)
        assertEquals(request.version, result.version)

        verify {
            portfolioRepository.findById(portfolioId)
            technologyRepository.save(any())
            eventPublisher.publish(any<TechnologyAddedEvent>())
        }
    }

    @Test
    fun `addTechnology should throw exception when portfolio not found`() {
        // Given
        val portfolioId = 999L
        val request = AddTechnologyRequest(
            name = "Test Technology",
            description = "Test Technology Description",
            type = TechnologyType.DATABASE,
            maturityLevel = MaturityLevel.PRODUCTION,
            riskLevel = RiskLevel.LOW,
            annualCost = BigDecimal("1000.00"),
            vendorName = "Test Vendor",
            version = "1.0.0",
            category = "Database"
        )

        every { portfolioRepository.findById(portfolioId) } returns null

        // When & Then
        val exception = assertThrows<RuntimeException> {
            portfolioService.addTechnology(portfolioId, request)
        }
        assertTrue(exception.message!!.contains("not found"))

        verify {
            portfolioRepository.findById(portfolioId)
        }
        verify(exactly = 0) {
            technologyRepository.save(any())
            eventPublisher.publish(any<TechnologyAddedEvent>())
        }
    }

    @Test
    fun `removeTechnology should remove technology from portfolio successfully`() {
        // Given
        val portfolioId = 1L
        val technologyId = 1L

        val portfolio = TechnologyPortfolio(
            id = portfolioId,
            name = "Test Portfolio",
            description = "Test Description",
            type = PortfolioType.ENTERPRISE,
            status = PortfolioStatus.ACTIVE,
            isActive = true,
            createdAt = LocalDateTime.now(),
            ownerId = 1L
        )

        val technology = Technology(
            id = technologyId,
            name = "Test Technology",
            description = "Test Technology Description",
            category = "Database",
            type = TechnologyType.DATABASE,
            maturityLevel = MaturityLevel.PRODUCTION,
            riskLevel = RiskLevel.LOW,
            annualCost = BigDecimal("1000.00"),
            vendorName = "Test Vendor",
            version = "1.0.0",
            portfolioId = portfolioId,
            isActive = true,
            createdAt = LocalDateTime.now()
        )

        every { portfolioRepository.findById(portfolioId) } returns portfolio
        every { technologyRepository.findById(technologyId) } returns technology
        every { technologyRepository.delete(technologyId) } returns true

        // When
        val result = portfolioService.removeTechnology(portfolioId, technologyId)

        // Then
        assertTrue(result)

        verify {
            portfolioRepository.findById(portfolioId)
            technologyRepository.findById(technologyId)
            technologyRepository.delete(technologyId)
            eventPublisher.publish(any<TechnologyRemovedEvent>())
        }
    }

    @Test
    fun `removeTechnology should throw exception when portfolio not found`() {
        // Given
        val portfolioId = 999L
        val technologyId = 1L

        every { portfolioRepository.findById(portfolioId) } returns null

        // When & Then
        val exception = assertThrows<RuntimeException> {
            portfolioService.removeTechnology(portfolioId, technologyId)
        }
        assertTrue(exception.message!!.contains("not found"))

        verify {
            portfolioRepository.findById(portfolioId)
        }
        verify(exactly = 0) {
            technologyRepository.findById(any())
            technologyRepository.delete(any())
            eventPublisher.publish(any<TechnologyRemovedEvent>())
        }
    }

    @Test
    fun `removeTechnology should throw exception when technology not found`() {
        // Given
        val portfolioId = 1L
        val technologyId = 999L

        val portfolio = TechnologyPortfolio(
            id = portfolioId,
            name = "Test Portfolio",
            description = "Test Description",
            type = PortfolioType.ENTERPRISE,
            status = PortfolioStatus.ACTIVE,
            isActive = true,
            createdAt = LocalDateTime.now(),
            ownerId = 1L
        )

        every { portfolioRepository.findById(portfolioId) } returns portfolio
        every { technologyRepository.findById(technologyId) } returns null

        // When & Then
        val exception = assertThrows<RuntimeException> {
            portfolioService.removeTechnology(portfolioId, technologyId)
        }
        assertTrue(exception.message!!.contains("not found"))

        verify {
            portfolioRepository.findById(portfolioId)
            technologyRepository.findById(technologyId)
        }
        verify(exactly = 0) {
            technologyRepository.delete(any())
            eventPublisher.publish(any<TechnologyRemovedEvent>())
        }
    }

    @Test
    fun `removeTechnology should throw exception when technology belongs to different portfolio`() {
        // Given
        val portfolioId = 1L
        val technologyId = 1L

        val portfolio = TechnologyPortfolio(
            id = portfolioId,
            name = "Test Portfolio",
            description = "Test Description",
            type = PortfolioType.ENTERPRISE,
            status = PortfolioStatus.ACTIVE,
            isActive = true,
            createdAt = LocalDateTime.now(),
            ownerId = 1L
        )

        val technology = Technology(
            id = technologyId,
            name = "Test Technology",
            description = "Test Technology Description",
            category = "Database",
            type = TechnologyType.DATABASE,
            maturityLevel = MaturityLevel.PRODUCTION,
            riskLevel = RiskLevel.LOW,
            annualCost = BigDecimal("1000.00"),
            vendorName = "Test Vendor",
            version = "1.0.0",
            portfolioId = 999L, // Different portfolio
            isActive = true,
            createdAt = LocalDateTime.now()
        )

        every { portfolioRepository.findById(portfolioId) } returns portfolio
        every { technologyRepository.findById(technologyId) } returns technology

        // When & Then
        val exception = assertThrows<RuntimeException> {
            portfolioService.removeTechnology(portfolioId, technologyId)
        }
        assertTrue(exception.message!!.contains("does not belong"))

        verify {
            portfolioRepository.findById(portfolioId)
            technologyRepository.findById(technologyId)
        }
        verify(exactly = 0) {
            technologyRepository.delete(any())
            eventPublisher.publish(any<TechnologyRemovedEvent>())
        }
    }

    @Test
    fun `getPortfolio should return portfolio with technologies`() {
        // Given
        val portfolioId = 1L

        val portfolio = TechnologyPortfolio(
            id = portfolioId,
            name = "Test Portfolio",
            description = "Test Description",
            type = PortfolioType.ENTERPRISE,
            status = PortfolioStatus.ACTIVE,
            isActive = true,
            createdAt = LocalDateTime.now(),
            ownerId = 1L
        )

        val technologies = listOf(
            Technology(
                id = 1L,
                name = "Technology 1",
                description = "Description 1",
                category = "Database",
                type = TechnologyType.DATABASE,
                maturityLevel = MaturityLevel.PRODUCTION,
                riskLevel = RiskLevel.LOW,
                annualCost = BigDecimal("1000.00"),
                vendorName = "Vendor 1",
                version = "1.0.0",
                portfolioId = portfolioId,
                isActive = true,
                createdAt = LocalDateTime.now()
            ),
            Technology(
                id = 2L,
                name = "Technology 2",
                description = "Description 2",
                category = "Application",
                type = TechnologyType.APPLICATION,
                maturityLevel = MaturityLevel.DEVELOPMENT,
                riskLevel = RiskLevel.MEDIUM,
                annualCost = BigDecimal("2000.00"),
                vendorName = "Vendor 2",
                version = "2.0.0",
                portfolioId = portfolioId,
                isActive = true,
                createdAt = LocalDateTime.now()
            )
        )

        every { portfolioRepository.findById(portfolioId) } returns portfolio
        every { technologyRepository.findByPortfolioId(portfolioId) } returns technologies

        // When
        val result = portfolioService.getPortfolio(portfolioId)

        // Then
        assertNotNull(result)
        assertEquals(portfolioId, result.id)
        assertEquals(portfolio.name, result.name)
        assertEquals(portfolio.description, result.description)
        assertEquals(portfolio.type, result.type)
        assertEquals(portfolio.status, result.status)
        assertEquals(2, result.technologies.size)
        assertEquals("Technology 1", result.technologies[0].name)
        assertEquals("Technology 2", result.technologies[1].name)

        verify {
            portfolioRepository.findById(portfolioId)
            technologyRepository.findByPortfolioId(portfolioId)
        }
    }

    @Test
    fun `getPortfolio should throw exception when portfolio not found`() {
        // Given
        val portfolioId = 999L

        every { portfolioRepository.findById(portfolioId) } returns null

        // When & Then
        val exception = assertThrows<RuntimeException> {
            portfolioService.getPortfolio(portfolioId)
        }
        assertTrue(exception.message!!.contains("not found"))

        verify {
            portfolioRepository.findById(portfolioId)
        }
        verify(exactly = 0) {
            technologyRepository.findByPortfolioId(any())
        }
    }
} 