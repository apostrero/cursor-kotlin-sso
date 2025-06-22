package com.company.techportfolio.authorization.domain.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Authorization Response Test Suite - Domain Model Unit Tests
 *
 * This test class provides comprehensive unit testing for the AuthorizationResponse domain model,
 * which represents the result of authorization decisions in the authorization service.
 * The tests validate data class behavior, factory methods, and response variations.
 *
 * ## Test Strategy:
 * - **Data Class Testing**: Constructor, equality, hashCode, toString functionality
 * - **Factory Method Testing**: Companion object authorized() and unauthorized() methods
 * - **Response Validation**: Success and failure scenarios with proper data
 * - **Parameter Handling**: Optional parameters and default values
 * - **Edge Case Coverage**: Empty values, special characters, Unicode support
 *
 * ## Test Coverage:
 * - Factory method variations (authorized/unauthorized with different parameters)
 * - Direct constructor usage
 * - Parameter validation (isAuthorized, username, resource, action, permissions, roles)
 * - Error message handling for unauthorized responses
 * - Organization context handling
 * - Special character and Unicode support
 * - Data class contract verification
 *
 * ## Domain Context:
 * AuthorizationResponse represents the output of authorization decisions, containing
 * the authorization result, user context, granted permissions, assigned roles, and
 * any error information for failed authorization attempts.
 *
 * ## Authorization Flow:
 * This model is returned by the authorization service and typically converted to
 * HTTP responses. It provides complete information about the authorization decision
 * including the reasoning (permissions/roles) and any error details.
 *
 * ## Factory Methods:
 * The companion object provides convenient factory methods:
 * - `authorized()`: Creates successful authorization responses
 * - `unauthorized()`: Creates failed authorization responses with error details
 *
 * @author Technology Portfolio Team
 * @version 1.0
 * @since 1.0
 */
class AuthorizationResponseTest {

    @Test
    fun `should create authorized response using companion object`() {
        // Given
        val username = "testuser"
        val resource = "portfolio"
        val action = "read"
        val permissions = listOf("portfolio:read", "portfolio:write")
        val roles = listOf("USER", "PORTFOLIO_MANAGER")
        val organizationId = 1L

        // When
        val response = AuthorizationResponse.authorized(
            username = username,
            resource = resource,
            action = action,
            permissions = permissions,
            roles = roles,
            organizationId = organizationId
        )

        // Then
        assertTrue(response.isAuthorized)
        assertEquals(username, response.username)
        assertEquals(resource, response.resource)
        assertEquals(action, response.action)
        assertEquals(permissions, response.permissions)
        assertEquals(roles, response.roles)
        assertEquals(organizationId, response.organizationId)
        assertNull(response.errorMessage)
    }

    @Test
    fun `should create authorized response with minimal parameters`() {
        // Given
        val username = "testuser"

        // When
        val response = AuthorizationResponse.authorized(username = username)

        // Then
        assertTrue(response.isAuthorized)
        assertEquals(username, response.username)
        assertNull(response.resource)
        assertNull(response.action)
        assertTrue(response.permissions.isEmpty())
        assertTrue(response.roles.isEmpty())
        assertNull(response.organizationId)
        assertNull(response.errorMessage)
    }

    @Test
    fun `should create unauthorized response using companion object`() {
        // Given
        val username = "testuser"
        val resource = "admin"
        val action = "delete"
        val errorMessage = "Access denied"

        // When
        val response = AuthorizationResponse.unauthorized(
            username = username,
            resource = resource,
            action = action,
            errorMessage = errorMessage
        )

        // Then
        assertFalse(response.isAuthorized)
        assertEquals(username, response.username)
        assertEquals(resource, response.resource)
        assertEquals(action, response.action)
        assertTrue(response.permissions.isEmpty())
        assertTrue(response.roles.isEmpty())
        assertNull(response.organizationId)
        assertEquals(errorMessage, response.errorMessage)
    }

    @Test
    fun `should create unauthorized response with minimal parameters`() {
        // Given
        val username = "testuser"

        // When
        val response = AuthorizationResponse.unauthorized(username = username)

        // Then
        assertFalse(response.isAuthorized)
        assertEquals(username, response.username)
        assertNull(response.resource)
        assertNull(response.action)
        assertTrue(response.permissions.isEmpty())
        assertTrue(response.roles.isEmpty())
        assertNull(response.organizationId)
        assertNull(response.errorMessage)
    }

    @Test
    fun `should create response with constructor directly`() {
        // Given
        val isAuthorized = true
        val username = "testuser"
        val resource = "portfolio"
        val action = "read"
        val permissions = listOf("portfolio:read")
        val roles = listOf("USER")
        val organizationId = 5L
        val errorMessage = null

        // When
        val response = AuthorizationResponse(
            isAuthorized = isAuthorized,
            username = username,
            resource = resource,
            action = action,
            permissions = permissions,
            roles = roles,
            organizationId = organizationId,
            errorMessage = errorMessage
        )

        // Then
        assertEquals(isAuthorized, response.isAuthorized)
        assertEquals(username, response.username)
        assertEquals(resource, response.resource)
        assertEquals(action, response.action)
        assertEquals(permissions, response.permissions)
        assertEquals(roles, response.roles)
        assertEquals(organizationId, response.organizationId)
        assertEquals(errorMessage, response.errorMessage)
    }

    @Test
    fun `should handle empty username`() {
        // Given
        val username = ""

        // When
        val response = AuthorizationResponse.authorized(username = username)

        // Then
        assertTrue(response.isAuthorized)
        assertEquals("", response.username)
    }

    @Test
    fun `should handle special characters in parameters`() {
        // Given
        val username = "test.user@example.com"
        val resource = "portfolio/sub-resource"
        val action = "read-write"
        val errorMessage = "Access denied: insufficient permissions & roles!"

        // When
        val response = AuthorizationResponse.unauthorized(
            username = username,
            resource = resource,
            action = action,
            errorMessage = errorMessage
        )

        // Then
        assertEquals(username, response.username)
        assertEquals(resource, response.resource)
        assertEquals(action, response.action)
        assertEquals(errorMessage, response.errorMessage)
    }

    @Test
    fun `should handle Unicode characters`() {
        // Given
        val username = "tëstüser"
        val resource = "pörtfölio"
        val action = "réad"
        val permissions = listOf("spëcial:permissiön")
        val roles = listOf("RÔLE_ÜSER")

        // When
        val response = AuthorizationResponse.authorized(
            username = username,
            resource = resource,
            action = action,
            permissions = permissions,
            roles = roles
        )

        // Then
        assertEquals(username, response.username)
        assertEquals(resource, response.resource)
        assertEquals(action, response.action)
        assertEquals(permissions, response.permissions)
        assertEquals(roles, response.roles)
    }

    @Test
    fun `should handle large lists of permissions and roles`() {
        // Given
        val username = "testuser"
        val permissions = (1..100).map { "permission:$it" }
        val roles = (1..50).map { "ROLE_$it" }

        // When
        val response = AuthorizationResponse.authorized(
            username = username,
            permissions = permissions,
            roles = roles
        )

        // Then
        assertEquals(permissions, response.permissions)
        assertEquals(roles, response.roles)
        assertEquals(100, response.permissions.size)
        assertEquals(50, response.roles.size)
    }

    @Test
    fun `should test data class equality`() {
        // Given
        val response1 = AuthorizationResponse.authorized("user", "resource", "action")
        val response2 = AuthorizationResponse.authorized("user", "resource", "action")
        val response3 = AuthorizationResponse.authorized("user2", "resource", "action")

        // Then
        assertEquals(response1, response2)
        assertNotEquals(response1, response3)
        assertEquals(response1.hashCode(), response2.hashCode())
        assertNotEquals(response1.hashCode(), response3.hashCode())
    }

    @Test
    fun `should test data class toString`() {
        // Given
        val response = AuthorizationResponse.authorized(
            username = "testuser",
            resource = "portfolio",
            action = "read",
            permissions = listOf("portfolio:read"),
            roles = listOf("USER")
        )

        // When
        val toString = response.toString()

        // Then
        assertTrue(toString.contains("testuser"))
        assertTrue(toString.contains("portfolio"))
        assertTrue(toString.contains("read"))
        assertTrue(toString.contains("portfolio:read"))
        assertTrue(toString.contains("USER"))
        assertTrue(toString.contains("true"))
    }

    @Test
    fun `should test data class copy functionality`() {
        // Given
        val original = AuthorizationResponse.authorized(
            username = "testuser",
            resource = "portfolio",
            action = "read"
        )

        // When
        val copied = original.copy(isAuthorized = false, errorMessage = "Access denied")

        // Then
        assertFalse(copied.isAuthorized)
        assertEquals("testuser", copied.username)
        assertEquals("portfolio", copied.resource)
        assertEquals("read", copied.action)
        assertEquals("Access denied", copied.errorMessage)
        assertNotEquals(original, copied)
    }

    @Test
    fun `should handle null organization id`() {
        // Given
        val username = "testuser"

        // When
        val response = AuthorizationResponse.authorized(
            username = username,
            organizationId = null
        )

        // Then
        assertNull(response.organizationId)
    }

    @Test
    fun `should handle zero organization id`() {
        // Given
        val username = "testuser"
        val organizationId = 0L

        // When
        val response = AuthorizationResponse.authorized(
            username = username,
            organizationId = organizationId
        )

        // Then
        assertEquals(0L, response.organizationId)
    }

    @Test
    fun `should handle negative organization id`() {
        // Given
        val username = "testuser"
        val organizationId = -1L

        // When
        val response = AuthorizationResponse.authorized(
            username = username,
            organizationId = organizationId
        )

        // Then
        assertEquals(-1L, response.organizationId)
    }

    @Test
    fun `should handle very long error message`() {
        // Given
        val username = "testuser"
        val longErrorMessage = "Error: " + "a".repeat(1000)

        // When
        val response = AuthorizationResponse.unauthorized(
            username = username,
            errorMessage = longErrorMessage
        )

        // Then
        assertEquals(longErrorMessage, response.errorMessage)
        assertEquals(1007, response.errorMessage?.length)
    }

    @Test
    fun `should handle empty permissions and roles lists`() {
        // Given
        val username = "testuser"
        val permissions = emptyList<String>()
        val roles = emptyList<String>()

        // When
        val response = AuthorizationResponse.authorized(
            username = username,
            permissions = permissions,
            roles = roles
        )

        // Then
        assertTrue(response.permissions.isEmpty())
        assertTrue(response.roles.isEmpty())
    }

    @Test
    fun `should handle null resource and action in authorized response`() {
        // Given
        val username = "testuser"

        // When
        val response = AuthorizationResponse.authorized(
            username = username,
            resource = null,
            action = null
        )

        // Then
        assertTrue(response.isAuthorized)
        assertEquals(username, response.username)
        assertNull(response.resource)
        assertNull(response.action)
    }

    @Test
    fun `should handle null resource and action in unauthorized response`() {
        // Given
        val username = "testuser"
        val errorMessage = "User not found"

        // When
        val response = AuthorizationResponse.unauthorized(
            username = username,
            resource = null,
            action = null,
            errorMessage = errorMessage
        )

        // Then
        assertFalse(response.isAuthorized)
        assertEquals(username, response.username)
        assertNull(response.resource)
        assertNull(response.action)
        assertEquals(errorMessage, response.errorMessage)
    }

    @Test
    fun `should compare authorized and unauthorized responses`() {
        // Given
        val username = "testuser"
        val resource = "portfolio"
        val action = "read"

        val authorizedResponse = AuthorizationResponse.authorized(username, resource, action)
        val unauthorizedResponse = AuthorizationResponse.unauthorized(username, resource, action)

        // Then
        assertTrue(authorizedResponse.isAuthorized)
        assertFalse(unauthorizedResponse.isAuthorized)
        assertEquals(authorizedResponse.username, unauthorizedResponse.username)
        assertEquals(authorizedResponse.resource, unauthorizedResponse.resource)
        assertEquals(authorizedResponse.action, unauthorizedResponse.action)
        assertNotEquals(authorizedResponse, unauthorizedResponse)
    }
} 