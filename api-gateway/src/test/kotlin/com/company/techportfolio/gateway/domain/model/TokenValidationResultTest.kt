package com.company.techportfolio.gateway.domain.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

/**
 * Unit test class for the TokenValidationResult domain model.
 *
 * This test class verifies the behavior of the TokenValidationResult data class
 * which represents the outcome of JWT token validation operations in the system.
 * It tests all factory methods, constructors, expiration logic, and data class functionality.
 *
 * Test coverage includes:
 * - Valid token validation result creation
 * - Invalid token validation result creation
 * - Expired token validation result creation
 * - Token expiration detection logic
 * - Constructor and default parameter handling
 * - Data class equality, hashCode, and toString
 * - Copy functionality for immutable updates
 * - Edge cases with null values and time boundaries
 *
 * Testing approach:
 * - Tests all companion object factory methods
 * - Validates token expiration logic with time comparisons
 * - Verifies data class properties and behavior
 * - Tests edge cases and boundary conditions
 * - Ensures proper handling of temporal data
 *
 * @author Technology Portfolio Team
 * @since 1.0.0
 */
class TokenValidationResultTest {

    /**
     * Tests valid token validation result creation using factory method.
     *
     * Verifies that the valid factory method creates a TokenValidationResult
     * with all required properties set correctly for successful token validation.
     * Also tests the automatic expiration detection based on current time.
     *
     * Expected behavior:
     * - Sets isValid to true
     * - Populates all token-related properties
     * - Automatically determines expiration status
     * - Sets errorMessage to null
     * - Preserves all input parameters
     */
    @Test
    fun `should create valid token validation result`() {
        // Given
        val username = "testuser"
        val authorities = listOf("ROLE_USER", "READ_PORTFOLIO")
        val sessionIndex = "session-123"
        val issuedAt = LocalDateTime.now().minusMinutes(30)
        val expiresAt = LocalDateTime.now().plusMinutes(30)

        // When
        val result = TokenValidationResult.valid(
            username = username,
            authorities = authorities,
            sessionIndex = sessionIndex,
            issuedAt = issuedAt,
            expiresAt = expiresAt
        )

        // Then
        assertTrue(result.isValid)
        assertEquals(username, result.username)
        assertEquals(authorities, result.authorities)
        assertEquals(sessionIndex, result.sessionIndex)
        assertEquals(issuedAt, result.issuedAt)
        assertEquals(expiresAt, result.expiresAt)
        assertFalse(result.isExpired) // Should not be expired since expiresAt is in future
        assertNull(result.errorMessage)
    }

    /**
     * Tests valid token validation result with expired token detection.
     *
     * Verifies that the valid factory method correctly detects token expiration
     * even when the token validation itself was successful. This tests the
     * automatic expiration logic based on the expiresAt timestamp.
     *
     * Expected behavior:
     * - Sets isValid to true (validation succeeded)
     * - Sets isExpired to true (token is past expiration)
     * - Populates all token properties correctly
     * - Demonstrates separation of validation and expiration concerns
     */
    @Test
    fun `should create valid token validation result with expired token`() {
        // Given
        val username = "testuser"
        val authorities = listOf("ROLE_USER")
        val sessionIndex = "session-123"
        val issuedAt = LocalDateTime.now().minusHours(2)
        val expiresAt = LocalDateTime.now().minusMinutes(30) // Expired 30 minutes ago

        // When
        val result = TokenValidationResult.valid(
            username = username,
            authorities = authorities,
            sessionIndex = sessionIndex,
            issuedAt = issuedAt,
            expiresAt = expiresAt
        )

        // Then
        assertTrue(result.isValid) // Still valid from validation perspective
        assertTrue(result.isExpired) // But marked as expired
        assertEquals(username, result.username)
        assertEquals(authorities, result.authorities)
    }

    /**
     * Tests invalid token validation result creation using factory method.
     *
     * Verifies that the invalid factory method creates a TokenValidationResult
     * with appropriate properties set for failed token validation scenarios.
     *
     * Expected behavior:
     * - Sets isValid to false
     * - Sets all token-related properties to null or empty
     * - Sets isExpired to false (not applicable for invalid tokens)
     * - Populates errorMessage with validation failure reason
     * - Provides clear validation failure indication
     */
    @Test
    fun `should create invalid token validation result`() {
        // Given
        val errorMessage = "Invalid token signature"

        // When
        val result = TokenValidationResult.invalid(errorMessage)

        // Then
        assertFalse(result.isValid)
        assertNull(result.username)
        assertEquals(emptyList<String>(), result.authorities)
        assertNull(result.sessionIndex)
        assertNull(result.issuedAt)
        assertNull(result.expiresAt)
        assertFalse(result.isExpired)
        assertEquals(errorMessage, result.errorMessage)
    }

    /**
     * Tests expired token validation result creation using factory method.
     *
     * Verifies that the expired factory method creates a TokenValidationResult
     * specifically for tokens that are valid but have expired. This is different
     * from invalid tokens as the token structure and signature are valid.
     *
     * Expected behavior:
     * - Sets isValid to false (expired tokens are not valid for use)
     * - Sets isExpired to true
     * - Populates user information from the expired token
     * - Sets standard expiration error message
     * - Preserves token claims for audit purposes
     */
    @Test
    fun `should create expired token validation result`() {
        // Given
        val username = "testuser"
        val authorities = listOf("ROLE_USER")
        val sessionIndex = "session-123"

        // When
        val result = TokenValidationResult.expired(username, authorities, sessionIndex)

        // Then
        assertFalse(result.isValid)
        assertEquals(username, result.username)
        assertEquals(authorities, result.authorities)
        assertEquals(sessionIndex, result.sessionIndex)
        assertNull(result.issuedAt)
        assertNull(result.expiresAt)
        assertTrue(result.isExpired)
        assertEquals("Token has expired", result.errorMessage)
    }

    /**
     * Tests direct constructor usage with all parameters.
     *
     * Verifies that the primary constructor can be used directly to create
     * TokenValidationResult instances with custom parameter combinations.
     *
     * Expected behavior:
     * - Accepts all parameters directly
     * - Preserves all input values
     * - Provides flexibility for custom validation scenarios
     * - Supports manual expiration flag setting
     */
    @Test
    fun `should create token validation result with constructor`() {
        // Given
        val isValid = true
        val username = "testuser"
        val authorities = listOf("ROLE_ADMIN")
        val sessionIndex = "admin-session"
        val issuedAt = LocalDateTime.now().minusHours(1)
        val expiresAt = LocalDateTime.now().plusHours(1)
        val isExpired = false
        val errorMessage = null

        // When
        val result = TokenValidationResult(
            isValid = isValid,
            username = username,
            authorities = authorities,
            sessionIndex = sessionIndex,
            issuedAt = issuedAt,
            expiresAt = expiresAt,
            isExpired = isExpired,
            errorMessage = errorMessage
        )

        // Then
        assertEquals(isValid, result.isValid)
        assertEquals(username, result.username)
        assertEquals(authorities, result.authorities)
        assertEquals(sessionIndex, result.sessionIndex)
        assertEquals(issuedAt, result.issuedAt)
        assertEquals(expiresAt, result.expiresAt)
        assertEquals(isExpired, result.isExpired)
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
     * - Sets appropriate defaults for validation failure
     */
    @Test
    fun `should create token validation result with default values`() {
        // When
        val result = TokenValidationResult(isValid = false)

        // Then
        assertFalse(result.isValid)
        assertNull(result.username)
        assertEquals(emptyList<String>(), result.authorities)
        assertNull(result.sessionIndex)
        assertNull(result.issuedAt)
        assertNull(result.expiresAt)
        assertFalse(result.isExpired)
        assertNull(result.errorMessage)
    }

    /**
     * Tests data class equality and hashCode implementation.
     *
     * Verifies that the data class properly implements equality comparison
     * and hashCode generation based on all properties, including temporal fields.
     *
     * Expected behavior:
     * - Equal objects have same property values
     * - Equal objects have same hashCode
     * - Different objects are not equal
     * - Follows data class equality contract
     * - Handles temporal field comparisons correctly
     */
    @Test
    fun `should support equality comparison`() {
        // Given
        val result1 = TokenValidationResult.valid(
            username = "user",
            authorities = listOf("ROLE_USER"),
            sessionIndex = "session",
            issuedAt = LocalDateTime.of(2024, 1, 1, 11, 0),
            expiresAt = LocalDateTime.of(2024, 1, 1, 12, 0)
        )
        val result2 = TokenValidationResult.valid(
            username = "user",
            authorities = listOf("ROLE_USER"),
            sessionIndex = "session",
            issuedAt = LocalDateTime.of(2024, 1, 1, 11, 0),
            expiresAt = LocalDateTime.of(2024, 1, 1, 12, 0)
        )
        val result3 = TokenValidationResult.invalid("error")

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
     * that includes key properties for debugging token validation issues.
     *
     * Expected behavior:
     * - Includes class name in string representation
     * - Shows key property values
     * - Provides useful debugging information for token validation
     * - Helps with troubleshooting authentication issues
     */
    @Test
    fun `should support toString representation`() {
        // Given
        val result = TokenValidationResult.valid(
            username = "user",
            authorities = listOf("ROLE_USER"),
            sessionIndex = "session",
            issuedAt = LocalDateTime.of(2024, 1, 1, 11, 0),
            expiresAt = LocalDateTime.of(2024, 1, 1, 12, 0)
        )

        // When
        val toString = result.toString()

        // Then
        assertTrue(toString.contains("TokenValidationResult"))
        assertTrue(toString.contains("isValid=true"))
        assertTrue(toString.contains("username=user"))
    }

    /**
     * Tests copy functionality for immutable updates.
     *
     * Verifies that the data class copy method allows creating modified
     * instances while preserving immutability principles. This is essential
     * for functional programming patterns and safe object manipulation.
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
        val original = TokenValidationResult.valid(
            username = "user",
            authorities = listOf("ROLE_USER"),
            sessionIndex = "session",
            issuedAt = LocalDateTime.of(2024, 1, 1, 11, 0),
            expiresAt = LocalDateTime.of(2024, 1, 1, 12, 0)
        )

        // When
        val copied = original.copy(username = "newuser")

        // Then
        assertEquals("newuser", copied.username)
        assertEquals(original.isValid, copied.isValid)
        assertEquals(original.authorities, copied.authorities)
        assertEquals(original.sessionIndex, copied.sessionIndex)
        assertEquals(original.issuedAt, copied.issuedAt)
        assertEquals(original.expiresAt, copied.expiresAt)
    }

    /**
     * Tests handling of null session index in valid token results.
     *
     * Verifies that valid token validation results can handle null session
     * indices without causing errors. This supports authentication flows
     * that don't use session-based tracking.
     *
     * Expected behavior:
     * - Accepts null session index without errors
     * - Creates valid token validation result
     * - Maintains null value in result
     * - Supports stateless authentication scenarios
     */
    @Test
    fun `should handle null session index in valid result`() {
        // When
        val result = TokenValidationResult.valid(
            username = "user",
            authorities = listOf("ROLE_USER"),
            sessionIndex = null,
            issuedAt = LocalDateTime.now().minusMinutes(30),
            expiresAt = LocalDateTime.now().plusMinutes(30)
        )

        // Then
        assertTrue(result.isValid)
        assertNull(result.sessionIndex)
    }

    /**
     * Tests handling of null session index in expired token results.
     *
     * Verifies that expired token validation results can handle null session
     * indices gracefully. This ensures consistency across different token
     * validation scenarios.
     *
     * Expected behavior:
     * - Accepts null session index for expired tokens
     * - Creates proper expired token result
     * - Maintains null value in result
     * - Handles edge case gracefully
     */
    @Test
    fun `should handle null session index in expired result`() {
        // When
        val result = TokenValidationResult.expired(
            username = "user",
            authorities = listOf("ROLE_USER"),
            sessionIndex = null
        )

        // Then
        assertFalse(result.isValid)
        assertTrue(result.isExpired)
        assertNull(result.sessionIndex)
    }

    /**
     * Tests handling of empty authorities list in token validation.
     *
     * Verifies that token validation results can handle users with no
     * authorities or roles. This supports scenarios where users may have
     * minimal permissions or guest access.
     *
     * Expected behavior:
     * - Accepts empty authorities list without errors
     * - Creates valid token validation result
     * - Preserves empty list in result
     * - Supports minimal permission scenarios
     */
    @Test
    fun `should handle empty authorities list`() {
        // When
        val result = TokenValidationResult.valid(
            username = "user",
            authorities = emptyList(),
            sessionIndex = "session",
            issuedAt = LocalDateTime.now().minusMinutes(30),
            expiresAt = LocalDateTime.now().plusMinutes(30)
        )

        // Then
        assertTrue(result.isValid)
        assertEquals(emptyList<String>(), result.authorities)
    }
} 