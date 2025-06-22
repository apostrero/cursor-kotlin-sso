package com.company.techportfolio.portfolio.adapter.out.persistence.entity

import com.company.techportfolio.shared.domain.model.PortfolioStatus
import com.company.techportfolio.shared.domain.model.PortfolioType
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

/**
 * Unit tests for PortfolioEntity.
 *
 * This test class verifies the functionality of the PortfolioEntity class, which
 * is the JPA entity representation of a technology portfolio in the persistence layer.
 * It tests the entity's construction, property initialization, and default values.
 *
 * ## Test Coverage:
 * - Entity construction with all parameters
 * - Entity construction with minimal required parameters
 * - Default value initialization
 * - Support for all portfolio types
 * - Support for all portfolio statuses
 *
 * ## Testing Approach:
 * - Direct instantiation of entities with various parameter combinations
 * - Verification of property values after construction
 * - Verification of default values for optional parameters
 *
 * @author Technology Portfolio Team
 * @since 1.0.0
 * @see PortfolioEntity
 * @see PortfolioType
 * @see PortfolioStatus
 */
class PortfolioEntityTest {

    /**
     * Fixed test date/time for consistent testing.
     */
    private val testDateTime = LocalDateTime.of(2024, 1, 15, 10, 30, 0)

    /**
     * Tests entity construction with all parameters provided.
     *
     * Verifies that:
     * 1. All properties are correctly initialized with the provided values
     * 2. No default values are used when explicit values are provided
     * 3. Both required and optional parameters are properly set
     */
    @Test
    fun `should create entity with all parameters`() {
        // When
        val entity = PortfolioEntity(
            id = 1L,
            name = "Test Portfolio",
            description = "Test Description",
            type = PortfolioType.ENTERPRISE,
            status = PortfolioStatus.ACTIVE,
            isActive = true,
            createdAt = testDateTime,
            updatedAt = testDateTime.plusHours(1),
            ownerId = 100L,
            organizationId = 200L
        )

        // Then
        assertEquals(1L, entity.id)
        assertEquals("Test Portfolio", entity.name)
        assertEquals("Test Description", entity.description)
        assertEquals(PortfolioType.ENTERPRISE, entity.type)
        assertEquals(PortfolioStatus.ACTIVE, entity.status)
        assertTrue(entity.isActive)
        assertEquals(testDateTime, entity.createdAt)
        assertEquals(testDateTime.plusHours(1), entity.updatedAt)
        assertEquals(100L, entity.ownerId)
        assertEquals(200L, entity.organizationId)
    }

    /**
     * Tests entity construction with only required parameters.
     *
     * Verifies that:
     * 1. Required properties are correctly initialized with the provided values
     * 2. Optional properties are initialized with null or default values
     * 3. Default value for isActive is true
     */
    @Test
    fun `should create entity with minimal parameters`() {
        // When
        val entity = PortfolioEntity(
            name = "Minimal Portfolio",
            type = PortfolioType.PERSONAL,
            status = PortfolioStatus.UNDER_REVIEW,
            createdAt = testDateTime,
            ownerId = 100L
        )

        // Then
        assertNull(entity.id)
        assertEquals("Minimal Portfolio", entity.name)
        assertNull(entity.description)
        assertEquals(PortfolioType.PERSONAL, entity.type)
        assertEquals(PortfolioStatus.UNDER_REVIEW, entity.status)
        assertTrue(entity.isActive) // Default value
        assertEquals(testDateTime, entity.createdAt)
        assertNull(entity.updatedAt)
        assertEquals(100L, entity.ownerId)
        assertNull(entity.organizationId)
    }

    /**
     * Tests entity construction with all possible portfolio types.
     *
     * Verifies that:
     * 1. The entity can be constructed with each enum value from PortfolioType
     * 2. The type property is correctly set to the provided enum value
     * 3. All enum values from the domain model are supported
     */
    @Test
    fun `should handle all portfolio types`() {
        // Given
        val types = listOf(
            PortfolioType.PERSONAL,
            PortfolioType.PROJECT,
            PortfolioType.DEPARTMENT,
            PortfolioType.ENTERPRISE
        )

        // When & Then
        types.forEach { type ->
            val entity = PortfolioEntity(
                name = "Portfolio $type",
                type = type,
                status = PortfolioStatus.ACTIVE,
                createdAt = testDateTime,
                ownerId = 100L
            )
            assertEquals(type, entity.type)
        }
    }

    /**
     * Tests entity construction with all possible portfolio statuses.
     *
     * Verifies that:
     * 1. The entity can be constructed with each enum value from PortfolioStatus
     * 2. The status property is correctly set to the provided enum value
     * 3. All enum values from the domain model are supported
     */
    @Test
    fun `should handle all portfolio statuses`() {
        // Given
        val statuses = listOf(
            PortfolioStatus.ACTIVE,
            PortfolioStatus.INACTIVE,
            PortfolioStatus.ARCHIVED,
            PortfolioStatus.UNDER_REVIEW
        )

        // When & Then
        statuses.forEach { status ->
            val entity = PortfolioEntity(
                name = "Portfolio $status",
                type = PortfolioType.ENTERPRISE,
                status = status,
                createdAt = testDateTime,
                ownerId = 100L
            )
            assertEquals(status, entity.status)
        }
    }
} 