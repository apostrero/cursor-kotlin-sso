package com.company.techportfolio.portfolio.domain.model

import com.company.techportfolio.shared.domain.model.PortfolioType
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Unit tests for CreatePortfolioRequest data class.
 *
 * This test class verifies the functionality of the CreatePortfolioRequest data class,
 * which is used to represent the request payload for creating new portfolios in the system.
 * The tests ensure that the data class properly handles required and optional parameters,
 * and that it correctly implements data class functionality like equality and copy operations.
 *
 * ## Test Coverage:
 * - Creation with all parameters
 * - Creation with minimal required parameters
 * - Data class operations (equals, hashCode, toString, copy)
 *
 * @author Technology Portfolio Team
 * @since 1.0.0
 * @see CreatePortfolioRequest
 */
class CreatePortfolioRequestTest {

    /**
     * Tests creating a request with all available parameters.
     *
     * Verifies that a CreatePortfolioRequest can be instantiated with all
     * parameters (required and optional) and that all values are correctly stored.
     */
    @Test
    fun `should create request with all parameters`() {
        // Given & When
        val request = CreatePortfolioRequest(
            name = "Test Portfolio",
            description = "Test Description",
            type = PortfolioType.ENTERPRISE,
            ownerId = 100L,
            organizationId = 200L
        )

        // Then
        assertEquals("Test Portfolio", request.name)
        assertEquals("Test Description", request.description)
        assertEquals(PortfolioType.ENTERPRISE, request.type)
        assertEquals(100L, request.ownerId)
        assertEquals(200L, request.organizationId)
    }

    /**
     * Tests creating a request with only the required parameters.
     *
     * Verifies that a CreatePortfolioRequest can be instantiated with only
     * the required parameters, and that optional parameters default to null.
     * This test ensures the data class correctly handles minimal initialization.
     */
    @Test
    fun `should create request with minimal parameters`() {
        // Given & When
        val request = CreatePortfolioRequest(
            name = "Minimal Portfolio",
            type = PortfolioType.PERSONAL,
            ownerId = 100L
        )

        // Then
        assertEquals("Minimal Portfolio", request.name)
        assertNull(request.description)
        assertEquals(PortfolioType.PERSONAL, request.type)
        assertEquals(100L, request.ownerId)
        assertNull(request.organizationId)
    }

    /**
     * Tests data class operations for CreatePortfolioRequest.
     *
     * Verifies that the data class correctly implements equals, hashCode,
     * toString, and copy operations as expected from a Kotlin data class.
     * This ensures the class behaves properly in collections and when used
     * with standard data class operations.
     */
    @Test
    fun `should support data class operations`() {
        // Given
        val request1 = CreatePortfolioRequest(
            name = "Portfolio",
            type = PortfolioType.ENTERPRISE,
            ownerId = 100L
        )
        val request2 = CreatePortfolioRequest(
            name = "Portfolio",
            type = PortfolioType.ENTERPRISE,
            ownerId = 100L
        )
        val request3 = request1.copy(name = "Different Portfolio")

        // Then
        assertEquals(request1, request2)
        assertNotEquals(request1, request3)
        assertEquals(request1.hashCode(), request2.hashCode())
        assertTrue(request1.toString().contains("Portfolio"))
    }
} 