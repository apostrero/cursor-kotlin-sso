package com.company.techportfolio.gateway.domain.model

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.time.LocalDateTime

/**
 * Unit test class for the AuthenticationResult domain model.
 * 
 * This test class verifies the behavior of the AuthenticationResult data class
 * which represents the outcome of authentication operations in the system.
 * It tests all factory methods, constructors, and data class functionality.
 * 
 * Test coverage includes:
 * - Success authentication result creation
 * - Failure authentication result creation
 * - Not authenticated result creation
 * - Constructor and default parameter handling
 * - Data class equality, hashCode, and toString
 * - Copy functionality for immutable updates
 * - Edge cases with null values
 * 
 * Testing approach:
 * - Tests all companion object factory methods
 * - Validates data class properties and behavior
 * - Verifies immutability and functional programming patterns
 * - Tests edge cases and boundary conditions
 * 
 * @author Technology Portfolio Team
 * @since 1.0.0
 */
class AuthenticationResultTest {

    /**
     * Tests successful authentication result creation using factory method.
     * 
     * Verifies that the success factory method creates an AuthenticationResult
     * with all required properties set correctly for successful authentication.
     * 
     * Expected behavior:
     * - Sets isAuthenticated to true
     * - Populates all user-related properties
     * - Sets errorMessage to null
     * - Preserves all input parameters
     */
    @Test
    fun `should create successful authentication result`() {
        // Given
        val username = "testuser"
        val authorities = listOf("ROLE_USER", "READ_PORTFOLIO")
        val token = "jwt-token-123"
        val sessionIndex = "session-123"
        val expiresAt = LocalDateTime.now().plusHours(1)

        // When
        val result = AuthenticationResult.success(
            username = username,
            authorities = authorities,
            token = token,
            sessionIndex = sessionIndex,
            expiresAt = expiresAt
        )

        // Then
        assertTrue(result.isAuthenticated)
        assertEquals(username, result.username)
        assertEquals(authorities, result.authorities)
        assertEquals(token, result.token)
        assertEquals(sessionIndex, result.sessionIndex)
        assertEquals(expiresAt, result.expiresAt)
        assertNull(result.errorMessage)
    }

    /**
     * Tests failure authentication result creation using factory method.
     * 
     * Verifies that the failure factory method creates an AuthenticationResult
     * with appropriate properties set for failed authentication scenarios.
     * 
     * Expected behavior:
     * - Sets isAuthenticated to false
     * - Sets all user-related properties to null or empty
     * - Populates errorMessage with failure reason
     * - Provides clear failure indication
     */
    @Test
    fun `should create failure authentication result`() {
        // Given
        val errorMessage = "Authentication failed"

        // When
        val result = AuthenticationResult.failure(errorMessage)

        // Then
        assertFalse(result.isAuthenticated)
        assertNull(result.username)
        assertEquals(emptyList<String>(), result.authorities)
        assertNull(result.token)
        assertNull(result.sessionIndex)
        assertNull(result.expiresAt)
        assertEquals(errorMessage, result.errorMessage)
    }

    /**
     * Tests not authenticated result creation using factory method.
     * 
     * Verifies that the notAuthenticated factory method creates an AuthenticationResult
     * with neutral state indicating no authentication attempt was made.
     * 
     * Expected behavior:
     * - Sets isAuthenticated to false
     * - Sets all properties to null or empty
     * - Does not include error message (different from failure)
     * - Represents neutral/initial state
     */
    @Test
    fun `should create not authenticated result`() {
        // When
        val result = AuthenticationResult.notAuthenticated()

        // Then
        assertFalse(result.isAuthenticated)
        assertNull(result.username)
        assertEquals(emptyList<String>(), result.authorities)
        assertNull(result.token)
        assertNull(result.sessionIndex)
        assertNull(result.expiresAt)
        assertNull(result.errorMessage)
    }

    /**
     * Tests direct constructor usage with all parameters.
     * 
     * Verifies that the primary constructor can be used directly to create
     * AuthenticationResult instances with custom parameter combinations.
     * 
     * Expected behavior:
     * - Accepts all parameters directly
     * - Preserves all input values
     * - Provides flexibility for custom scenarios
     */
    @Test
    fun `should create authentication result with constructor`() {
        // Given
        val isAuthenticated = true
        val username = "testuser"
        val authorities = listOf("ROLE_ADMIN")
        val token = "admin-token"
        val sessionIndex = "admin-session"
        val expiresAt = LocalDateTime.now().plusHours(2)
        val errorMessage = null

        // When
        val result = AuthenticationResult(
            isAuthenticated = isAuthenticated,
            username = username,
            authorities = authorities,
            token = token,
            sessionIndex = sessionIndex,
            expiresAt = expiresAt,
            errorMessage = errorMessage
        )

        // Then
        assertEquals(isAuthenticated, result.isAuthenticated)
        assertEquals(username, result.username)
        assertEquals(authorities, result.authorities)
        assertEquals(token, result.token)
        assertEquals(sessionIndex, result.sessionIndex)
        assertEquals(expiresAt, result.expiresAt)
        assertEquals(errorMessage, result.errorMessage)
    }

    /**
     * Tests constructor with default parameter values.
     * 
     * Verifies that the constructor properly handles default values
     * when only required parameters are provided.
     * 
     * Expected behavior:
     * - Uses default values for optional parameters
     * - Creates valid instance with minimal input
     * - Demonstrates proper default parameter handling
     */
    @Test
    fun `should create authentication result with default values`() {
        // When
        val result = AuthenticationResult(isAuthenticated = false)

        // Then
        assertFalse(result.isAuthenticated)
        assertNull(result.username)
        assertEquals(emptyList<String>(), result.authorities)
        assertNull(result.token)
        assertNull(result.sessionIndex)
        assertNull(result.expiresAt)
        assertNull(result.errorMessage)
    }

    /**
     * Tests data class equality and hashCode implementation.
     * 
     * Verifies that the data class properly implements equality comparison
     * and hashCode generation based on all properties.
     * 
     * Expected behavior:
     * - Equal objects have same property values
     * - Equal objects have same hashCode
     * - Different objects are not equal
     * - Follows data class equality contract
     */
    @Test
    fun `should support equality comparison`() {
        // Given
        val result1 = AuthenticationResult.success(
            username = "user",
            authorities = listOf("ROLE_USER"),
            token = "token",
            sessionIndex = "session",
            expiresAt = LocalDateTime.of(2024, 1, 1, 12, 0)
        )
        val result2 = AuthenticationResult.success(
            username = "user",
            authorities = listOf("ROLE_USER"),
            token = "token",
            sessionIndex = "session",
            expiresAt = LocalDateTime.of(2024, 1, 1, 12, 0)
        )
        val result3 = AuthenticationResult.failure("error")

        // Then
        assertEquals(result1, result2)
        assertNotEquals(result1, result3)
        assertEquals(result1.hashCode(), result2.hashCode())
        assertNotEquals(result1.hashCode(), result3.hashCode())
    }

    /**
     * Tests toString implementation for debugging and logging.
     * 
     * Verifies that the data class provides meaningful string representation
     * that includes key properties for debugging purposes.
     * 
     * Expected behavior:
     * - Includes class name in string representation
     * - Shows key property values
     * - Provides useful debugging information
     */
    @Test
    fun `should support toString representation`() {
        // Given
        val result = AuthenticationResult.success(
            username = "user",
            authorities = listOf("ROLE_USER"),
            token = "token",
            sessionIndex = "session",
            expiresAt = LocalDateTime.of(2024, 1, 1, 12, 0)
        )

        // When
        val toString = result.toString()

        // Then
        assertTrue(toString.contains("AuthenticationResult"))
        assertTrue(toString.contains("isAuthenticated=true"))
        assertTrue(toString.contains("username=user"))
    }

    /**
     * Tests copy functionality for immutable updates.
     * 
     * Verifies that the data class copy method allows creating modified
     * instances while preserving immutability principles.
     * 
     * Expected behavior:
     * - Creates new instance with modified properties
     * - Preserves unchanged properties from original
     * - Maintains immutability of original instance
     * - Supports functional programming patterns
     */
    @Test
    fun `should support copy functionality`() {
        // Given
        val original = AuthenticationResult.success(
            username = "user",
            authorities = listOf("ROLE_USER"),
            token = "token",
            sessionIndex = "session",
            expiresAt = LocalDateTime.of(2024, 1, 1, 12, 0)
        )

        // When
        val copied = original.copy(username = "newuser")

        // Then
        assertEquals("newuser", copied.username)
        assertEquals(original.isAuthenticated, copied.isAuthenticated)
        assertEquals(original.authorities, copied.authorities)
        assertEquals(original.token, copied.token)
        assertEquals(original.sessionIndex, copied.sessionIndex)
        assertEquals(original.expiresAt, copied.expiresAt)
    }

    /**
     * Tests handling of null session index in success scenarios.
     * 
     * Verifies that the success factory method properly handles null
     * session index values, which may occur in certain authentication flows.
     * 
     * Expected behavior:
     * - Accepts null session index without errors
     * - Creates valid successful authentication result
     * - Maintains null value in result
     */
    @Test
    fun `should handle null session index in success`() {
        // When
        val result = AuthenticationResult.success(
            username = "user",
            authorities = listOf("ROLE_USER"),
            token = "token",
            sessionIndex = null,
            expiresAt = LocalDateTime.now().plusHours(1)
        )

        // Then
        assertTrue(result.isAuthenticated)
        assertNull(result.sessionIndex)
    }

    @Test
    fun `should handle empty authorities list`() {
        // When
        val result = AuthenticationResult.success(
            username = "user",
            authorities = emptyList(),
            token = "token",
            sessionIndex = "session",
            expiresAt = LocalDateTime.now().plusHours(1)
        )

        // Then
        assertTrue(result.isAuthenticated)
        assertEquals(emptyList<String>(), result.authorities)
    }
} 