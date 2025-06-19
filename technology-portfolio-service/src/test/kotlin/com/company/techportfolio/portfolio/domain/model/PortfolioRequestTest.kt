package com.company.techportfolio.portfolio.domain.model

import com.company.techportfolio.shared.domain.model.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.math.BigDecimal
import java.time.LocalDateTime

class PortfolioRequestTest {

    @Test
    fun `should create CreatePortfolioRequest with all parameters`() {
        // Given
        val name = "Test Portfolio"
        val description = "Test Description"
        val type = PortfolioType.ENTERPRISE
        val ownerId = 1L
        val organizationId = 100L

        // When
        val request = CreatePortfolioRequest(
            name = name,
            description = description,
            type = type,
            ownerId = ownerId,
            organizationId = organizationId
        )

        // Then
        assertEquals(name, request.name)
        assertEquals(description, request.description)
        assertEquals(type, request.type)
        assertEquals(ownerId, request.ownerId)
        assertEquals(organizationId, request.organizationId)
    }

    @Test
    fun `should create CreatePortfolioRequest with minimal parameters`() {
        // Given
        val name = "Minimal Portfolio"
        val type = PortfolioType.DEPARTMENT
        val ownerId = 2L

        // When
        val request = CreatePortfolioRequest(
            name = name,
            type = type,
            ownerId = ownerId
        )

        // Then
        assertEquals(name, request.name)
        assertNull(request.description)
        assertEquals(type, request.type)
        assertEquals(ownerId, request.ownerId)
        assertNull(request.organizationId)
    }

    @Test
    fun `should create UpdatePortfolioRequest with all parameters`() {
        // Given
        val name = "Updated Portfolio"
        val description = "Updated Description"
        val type = PortfolioType.PROJECT
        val status = PortfolioStatus.INACTIVE

        // When
        val request = UpdatePortfolioRequest(
            name = name,
            description = description,
            type = type,
            status = status
        )

        // Then
        assertEquals(name, request.name)
        assertEquals(description, request.description)
        assertEquals(type, request.type)
        assertEquals(status, request.status)
    }

    @Test
    fun `should create UpdatePortfolioRequest with null parameters`() {
        // When
        val request = UpdatePortfolioRequest()

        // Then
        assertNull(request.name)
        assertNull(request.description)
        assertNull(request.type)
        assertNull(request.status)
    }

    @Test
    fun `should create AddTechnologyRequest with all parameters`() {
        // Given
        val name = "Spring Boot"
        val description = "Java Framework"
        val category = "Framework"
        val version = "3.0.0"
        val type = TechnologyType.FRAMEWORK
        val maturityLevel = MaturityLevel.MATURE
        val riskLevel = RiskLevel.LOW
        val annualCost = BigDecimal("1000.00")
        val licenseCost = BigDecimal("500.00")
        val maintenanceCost = BigDecimal("200.00")
        val vendorName = "VMware"
        val vendorContact = "support@vmware.com"
        val supportContractExpiry = LocalDateTime.of(2024, 12, 31, 23, 59)

        // When
        val request = AddTechnologyRequest(
            name = name,
            description = description,
            category = category,
            version = version,
            type = type,
            maturityLevel = maturityLevel,
            riskLevel = riskLevel,
            annualCost = annualCost,
            licenseCost = licenseCost,
            maintenanceCost = maintenanceCost,
            vendorName = vendorName,
            vendorContact = vendorContact,
            supportContractExpiry = supportContractExpiry
        )

        // Then
        assertEquals(name, request.name)
        assertEquals(description, request.description)
        assertEquals(category, request.category)
        assertEquals(version, request.version)
        assertEquals(type, request.type)
        assertEquals(maturityLevel, request.maturityLevel)
        assertEquals(riskLevel, request.riskLevel)
        assertEquals(annualCost, request.annualCost)
        assertEquals(licenseCost, request.licenseCost)
        assertEquals(maintenanceCost, request.maintenanceCost)
        assertEquals(vendorName, request.vendorName)
        assertEquals(vendorContact, request.vendorContact)
        assertEquals(supportContractExpiry, request.supportContractExpiry)
    }

    @Test
    fun `should create AddTechnologyRequest with minimal parameters`() {
        // Given
        val name = "Docker"
        val category = "Container"
        val type = TechnologyType.PLATFORM
        val maturityLevel = MaturityLevel.MATURE
        val riskLevel = RiskLevel.MEDIUM

        // When
        val request = AddTechnologyRequest(
            name = name,
            category = category,
            type = type,
            maturityLevel = maturityLevel,
            riskLevel = riskLevel
        )

        // Then
        assertEquals(name, request.name)
        assertNull(request.description)
        assertEquals(category, request.category)
        assertNull(request.version)
        assertEquals(type, request.type)
        assertEquals(maturityLevel, request.maturityLevel)
        assertEquals(riskLevel, request.riskLevel)
        assertNull(request.annualCost)
        assertNull(request.licenseCost)
        assertNull(request.maintenanceCost)
        assertNull(request.vendorName)
        assertNull(request.vendorContact)
        assertNull(request.supportContractExpiry)
    }

    @Test
    fun `should create UpdateTechnologyRequest with all parameters`() {
        // Given
        val name = "Updated Spring Boot"
        val description = "Updated Java Framework"
        val category = "Updated Framework"
        val version = "3.1.0"
        val type = TechnologyType.FRAMEWORK
        val maturityLevel = MaturityLevel.MATURE
        val riskLevel = RiskLevel.LOW
        val annualCost = BigDecimal("1200.00")
        val licenseCost = BigDecimal("600.00")
        val maintenanceCost = BigDecimal("250.00")
        val vendorName = "Updated VMware"
        val vendorContact = "updated-support@vmware.com"
        val supportContractExpiry = LocalDateTime.of(2025, 12, 31, 23, 59)

        // When
        val request = UpdateTechnologyRequest(
            name = name,
            description = description,
            category = category,
            version = version,
            type = type,
            maturityLevel = maturityLevel,
            riskLevel = riskLevel,
            annualCost = annualCost,
            licenseCost = licenseCost,
            maintenanceCost = maintenanceCost,
            vendorName = vendorName,
            vendorContact = vendorContact,
            supportContractExpiry = supportContractExpiry
        )

        // Then
        assertEquals(name, request.name)
        assertEquals(description, request.description)
        assertEquals(category, request.category)
        assertEquals(version, request.version)
        assertEquals(type, request.type)
        assertEquals(maturityLevel, request.maturityLevel)
        assertEquals(riskLevel, request.riskLevel)
        assertEquals(annualCost, request.annualCost)
        assertEquals(licenseCost, request.licenseCost)
        assertEquals(maintenanceCost, request.maintenanceCost)
        assertEquals(vendorName, request.vendorName)
        assertEquals(vendorContact, request.vendorContact)
        assertEquals(supportContractExpiry, request.supportContractExpiry)
    }

    @Test
    fun `should create UpdateTechnologyRequest with null parameters`() {
        // When
        val request = UpdateTechnologyRequest()

        // Then
        assertNull(request.name)
        assertNull(request.description)
        assertNull(request.category)
        assertNull(request.version)
        assertNull(request.type)
        assertNull(request.maturityLevel)
        assertNull(request.riskLevel)
        assertNull(request.annualCost)
        assertNull(request.licenseCost)
        assertNull(request.maintenanceCost)
        assertNull(request.vendorName)
        assertNull(request.vendorContact)
        assertNull(request.supportContractExpiry)
    }

    @Test
    fun `should create PortfolioResponse with all parameters`() {
        // Given
        val id = 1L
        val name = "Test Portfolio"
        val description = "Test Description"
        val type = PortfolioType.PERSONAL
        val status = PortfolioStatus.ACTIVE
        val isActive = true
        val createdAt = LocalDateTime.now()
        val updatedAt = LocalDateTime.now()
        val ownerId = 1L
        val organizationId = 100L
        val technologyCount = 5
        val totalAnnualCost = BigDecimal("5000.00")
        val technologies = listOf(
            TechnologyResponse(
                id = 1L,
                name = "Spring Boot",
                description = "Java Framework",
                category = "Framework",
                version = "3.0.0",
                type = TechnologyType.FRAMEWORK,
                maturityLevel = MaturityLevel.MATURE,
                riskLevel = RiskLevel.LOW,
                annualCost = BigDecimal("1000.00"),
                licenseCost = BigDecimal("500.00"),
                maintenanceCost = BigDecimal("200.00"),
                vendorName = "VMware",
                vendorContact = "support@vmware.com",
                supportContractExpiry = LocalDateTime.of(2024, 12, 31, 23, 59),
                isActive = true,
                createdAt = createdAt,
                updatedAt = updatedAt
            )
        )

        // When
        val response = PortfolioResponse(
            id = id,
            name = name,
            description = description,
            type = type,
            status = status,
            isActive = isActive,
            createdAt = createdAt,
            updatedAt = updatedAt,
            ownerId = ownerId,
            organizationId = organizationId,
            technologyCount = technologyCount,
            totalAnnualCost = totalAnnualCost,
            technologies = technologies
        )

        // Then
        assertEquals(id, response.id)
        assertEquals(name, response.name)
        assertEquals(description, response.description)
        assertEquals(type, response.type)
        assertEquals(status, response.status)
        assertEquals(isActive, response.isActive)
        assertEquals(createdAt, response.createdAt)
        assertEquals(updatedAt, response.updatedAt)
        assertEquals(ownerId, response.ownerId)
        assertEquals(organizationId, response.organizationId)
        assertEquals(technologyCount, response.technologyCount)
        assertEquals(totalAnnualCost, response.totalAnnualCost)
        assertEquals(technologies, response.technologies)
    }

    @Test
    fun `should create TechnologyResponse with all parameters`() {
        // Given
        val id = 1L
        val name = "Spring Boot"
        val description = "Java Framework"
        val category = "Framework"
        val version = "3.0.0"
        val type = TechnologyType.FRAMEWORK
        val maturityLevel = MaturityLevel.MATURE
        val riskLevel = RiskLevel.LOW
        val annualCost = BigDecimal("1000.00")
        val licenseCost = BigDecimal("500.00")
        val maintenanceCost = BigDecimal("200.00")
        val vendorName = "VMware"
        val vendorContact = "support@vmware.com"
        val supportContractExpiry = LocalDateTime.of(2024, 12, 31, 23, 59)
        val isActive = true
        val createdAt = LocalDateTime.now()
        val updatedAt = LocalDateTime.now()

        // When
        val response = TechnologyResponse(
            id = id,
            name = name,
            description = description,
            category = category,
            version = version,
            type = type,
            maturityLevel = maturityLevel,
            riskLevel = riskLevel,
            annualCost = annualCost,
            licenseCost = licenseCost,
            maintenanceCost = maintenanceCost,
            vendorName = vendorName,
            vendorContact = vendorContact,
            supportContractExpiry = supportContractExpiry,
            isActive = isActive,
            createdAt = createdAt,
            updatedAt = updatedAt
        )

        // Then
        assertEquals(id, response.id)
        assertEquals(name, response.name)
        assertEquals(description, response.description)
        assertEquals(category, response.category)
        assertEquals(version, response.version)
        assertEquals(type, response.type)
        assertEquals(maturityLevel, response.maturityLevel)
        assertEquals(riskLevel, response.riskLevel)
        assertEquals(annualCost, response.annualCost)
        assertEquals(licenseCost, response.licenseCost)
        assertEquals(maintenanceCost, response.maintenanceCost)
        assertEquals(vendorName, response.vendorName)
        assertEquals(vendorContact, response.vendorContact)
        assertEquals(supportContractExpiry, response.supportContractExpiry)
        assertEquals(isActive, response.isActive)
        assertEquals(createdAt, response.createdAt)
        assertEquals(updatedAt, response.updatedAt)
    }

    @Test
    fun `should test data class equality for CreatePortfolioRequest`() {
        // Given
        val request1 = CreatePortfolioRequest("Test", "Desc", PortfolioType.ENTERPRISE, 1L, 100L)
        val request2 = CreatePortfolioRequest("Test", "Desc", PortfolioType.ENTERPRISE, 1L, 100L)
        val request3 = CreatePortfolioRequest("Different", "Desc", PortfolioType.ENTERPRISE, 1L, 100L)

        // Then
        assertEquals(request1, request2)
        assertNotEquals(request1, request3)
        assertEquals(request1.hashCode(), request2.hashCode())
        assertNotEquals(request1.hashCode(), request3.hashCode())
    }

    @Test
    fun `should test data class toString for CreatePortfolioRequest`() {
        // Given
        val request = CreatePortfolioRequest("Test Portfolio", "Description", PortfolioType.ENTERPRISE, 1L, 100L)

        // When
        val toString = request.toString()

        // Then
        assertTrue(toString.contains("Test Portfolio"))
        assertTrue(toString.contains("Description"))
        assertTrue(toString.contains("ENTERPRISE"))
        assertTrue(toString.contains("1"))
        assertTrue(toString.contains("100"))
    }

    @Test
    fun `should handle BigDecimal precision in cost fields`() {
        // Given
        val annualCost = BigDecimal("1000.999")
        val licenseCost = BigDecimal("500.001")
        val maintenanceCost = BigDecimal("200.50")

        // When
        val request = AddTechnologyRequest(
            name = "Test Tech",
            category = "Test Category",
            type = TechnologyType.TOOL,
            maturityLevel = MaturityLevel.EMERGING,
            riskLevel = RiskLevel.HIGH,
            annualCost = annualCost,
            licenseCost = licenseCost,
            maintenanceCost = maintenanceCost
        )

        // Then
        assertEquals(annualCost, request.annualCost)
        assertEquals(licenseCost, request.licenseCost)
        assertEquals(maintenanceCost, request.maintenanceCost)
    }

    @Test
    fun `should handle special characters in string fields`() {
        // Given
        val name = "Test & Special Characters: @#$%"
        val description = "Description with émojis 🚀 and unicode ñáéíóú"
        val vendorContact = "test+email@domain.co.uk"

        // When
        val request = AddTechnologyRequest(
            name = name,
            description = description,
            category = "Category",
            type = TechnologyType.LIBRARY,
            maturityLevel = MaturityLevel.MATURE,
            riskLevel = RiskLevel.LOW,
            vendorContact = vendorContact
        )

        // Then
        assertEquals(name, request.name)
        assertEquals(description, request.description)
        assertEquals(vendorContact, request.vendorContact)
    }

    @Test
    fun `should create PortfolioSummary with all parameters`() {
        // Given
        val id = 1L
        val name = "Summary Portfolio"
        val type = PortfolioType.DEPARTMENTAL
        val status = PortfolioStatus.ACTIVE
        val ownerId = 1L
        val organizationId = 100L
        val technologyCount = 10
        val totalAnnualCost = BigDecimal("15000.00")
        val lastUpdated = LocalDateTime.now()

        // When
        val summary = PortfolioSummary(
            id = id,
            name = name,
            type = type,
            status = status,
            ownerId = ownerId,
            organizationId = organizationId,
            technologyCount = technologyCount,
            totalAnnualCost = totalAnnualCost,
            lastUpdated = lastUpdated
        )

        // Then
        assertEquals(id, summary.id)
        assertEquals(name, summary.name)
        assertEquals(type, summary.type)
        assertEquals(status, summary.status)
        assertEquals(ownerId, summary.ownerId)
        assertEquals(organizationId, summary.organizationId)
        assertEquals(technologyCount, summary.technologyCount)
        assertEquals(totalAnnualCost, summary.totalAnnualCost)
        assertEquals(lastUpdated, summary.lastUpdated)
    }

    @Test
    fun `should create TechnologySummary with all parameters`() {
        // Given
        val id = 1L
        val name = "Summary Technology"
        val category = "Summary Category"
        val type = TechnologyType.DATABASE
        val maturityLevel = MaturityLevel.MATURE
        val riskLevel = RiskLevel.MEDIUM
        val annualCost = BigDecimal("2500.00")
        val vendorName = "Summary Vendor"

        // When
        val summary = TechnologySummary(
            id = id,
            name = name,
            category = category,
            type = type,
            maturityLevel = maturityLevel,
            riskLevel = riskLevel,
            annualCost = annualCost,
            vendorName = vendorName
        )

        // Then
        assertEquals(id, summary.id)
        assertEquals(name, summary.name)
        assertEquals(category, summary.category)
        assertEquals(type, summary.type)
        assertEquals(maturityLevel, summary.maturityLevel)
        assertEquals(riskLevel, summary.riskLevel)
        assertEquals(annualCost, summary.annualCost)
        assertEquals(vendorName, summary.vendorName)
    }
} 