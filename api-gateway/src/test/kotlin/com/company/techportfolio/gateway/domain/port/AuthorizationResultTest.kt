package com.company.techportfolio.gateway.domain.port

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Unit test class for the AuthorizationResult domain model.
 *
 * This test class verifies the behavior of the AuthorizationResult data class
 * which represents the outcome of authorization operations in the system.
 * It tests all factory methods, constructors, and data class functionality
 * for role-based access control (RBAC) decisions.
 *
 * Test coverage includes:
 * - Authorized result creation with permissions
 * - Unauthorized result creation with and without error messages
 * - Constructor and default parameter handling
 * - Data class equality, hashCode, and toString
 * - Copy functionality for immutable updates
 * - Edge cases with empty values and special characters
 * - Permission list handling
 *
 * Testing approach:
 * - Tests all companion object factory methods
 * - Validates authorization decision logic
 * - Verifies data class properties and behavior
 * - Tests edge cases and boundary conditions
 * - Ensures proper permission management
 *
 * @author Technology Portfolio Team
 * @since 1.0.0
 */
class AuthorizationResultTest {

    /**
     * Tests authorized result creation using factory method.
     *
     * Verifies that the authorized factory method creates an AuthorizationResult
     * with all required properties set correctly for successful authorization.
     *
     * Expected behavior:
     * - Sets isAuthorized to true
     * - Populates all authorization context properties
     * - Includes granted permissions list
     * - Sets errorMessage to null
     * - Preserves all input parameters
     */
    @Test
    fun `should create authorized result`() {
        // Given
        val username = "testuser"
        val resource = "portfolio"
        val action = "read"
        val permissions = listOf("READ_PORTFOLIO", "VIEW_ANALYTICS")

        // When
        val result = AuthorizationResult.authorized(username, resource, action, permissions)

        // Then
        assertTrue(result.isAuthorized)
        assertEquals(username, result.username)
        assertEquals(resource, result.resource)
        assertEquals(action, result.action)
        assertEquals(permissions, result.permissions)
        assertNull(result.errorMessage)
    }

    /**
     * Tests unauthorized result creation without error message.
     *
     * Verifies that the unauthorized factory method creates an AuthorizationResult
     * for failed authorization without providing a specific error message.
     *
     * Expected behavior:
     * - Sets isAuthorized to false
     * - Populates authorization context properties
     * - Sets permissions to empty list
     * - Sets errorMessage to null (no specific error)
     * - Indicates authorization denial without details
     */
    @Test
    fun `should create unauthorized result without error message`() {
        // Given
        val username = "testuser"
        val resource = "portfolio"
        val action = "delete"

        // When
        val result = AuthorizationResult.unauthorized(username, resource, action)

        // Then
        assertFalse(result.isAuthorized)
        assertEquals(username, result.username)
        assertEquals(resource, result.resource)
        assertEquals(action, result.action)
        assertEquals(emptyList<String>(), result.permissions)
        assertNull(result.errorMessage)
    }

    /**
     * Tests unauthorized result creation with specific error message.
     *
     * Verifies that the unauthorized factory method can include a specific
     * error message explaining why authorization was denied.
     *
     * Expected behavior:
     * - Sets isAuthorized to false
     * - Populates authorization context properties
     * - Sets permissions to empty list
     * - Includes specific error message
     * - Provides detailed authorization failure reason
     */
    @Test
    fun `should create unauthorized result with error message`() {
        // Given
        val username = "testuser"
        val resource = "portfolio"
        val action = "delete"
        val errorMessage = "Insufficient permissions"

        // When
        val result = AuthorizationResult.unauthorized(username, resource, action, errorMessage)

        // Then
        assertFalse(result.isAuthorized)
        assertEquals(username, result.username)
        assertEquals(resource, result.resource)
        assertEquals(action, result.action)
        assertEquals(emptyList<String>(), result.permissions)
        assertEquals(errorMessage, result.errorMessage)
    }

    /**
     * Tests direct constructor usage with all parameters.
     *
     * Verifies that the primary constructor can be used directly to create
     * AuthorizationResult instances with custom parameter combinations.
     *
     * Expected behavior:
     * - Accepts all parameters directly
     * - Preserves all input values
     * - Provides flexibility for custom authorization scenarios
     * - Supports manual permission list management
     */
    @Test
    fun `should create authorization result with constructor`() {
        // Given
        val isAuthorized = true
        val username = "testuser"
        val resource = "portfolio"
        val action = "read"
        val permissions = listOf("READ_PORTFOLIO")
        val errorMessage = null

        // When
        val result = AuthorizationResult(
            isAuthorized = isAuthorized,
            username = username,
            resource = resource,
            action = action,
            permissions = permissions,
            errorMessage = errorMessage
        )

        // Then
        assertEquals(isAuthorized, result.isAuthorized)
        assertEquals(username, result.username)
        assertEquals(resource, result.resource)
        assertEquals(action, result.action)
        assertEquals(permissions, result.permissions)
        assertEquals(errorMessage, result.errorMessage)
    }

    /**
     * Tests constructor with default empty permissions parameter.
     *
     * Verifies that the constructor properly handles default values
     * when permissions parameter is not provided.
     *
     * Expected behavior:
     * - Uses empty list as default for permissions
     * - Creates valid instance with minimal input
     * - Demonstrates proper default parameter handling
     * - Sets appropriate defaults for authorization context
     */
    @Test
    fun `should create authorization result with default empty permissions`() {
        // Given
        val isAuthorized = false
        val username = "testuser"
        val resource = "portfolio"
        val action = "read"

        // When
        val result = AuthorizationResult(
            isAuthorized = isAuthorized,
            username = username,
            resource = resource,
            action = action
        )

        // Then
        assertEquals(isAuthorized, result.isAuthorized)
        assertEquals(username, result.username)
        assertEquals(resource, result.resource)
        assertEquals(action, result.action)
        assertEquals(emptyList<String>(), result.permissions)
        assertNull(result.errorMessage)
    }

    /**
     * Tests data class equality and hashCode implementation.
     *
     * Verifies that the data class properly implements equality comparison
     * and hashCode generation based on all properties including permission lists.
     *
     * Expected behavior:
     * - Equal objects have same property values
     * - Equal objects have same hashCode
     * - Different objects are not equal
     * - Follows data class equality contract
     * - Handles permission list comparisons correctly
     */
    @Test
    fun `should support equality comparison`() {
        // Given
        val result1 = AuthorizationResult.authorized(
            username = "user",
            resource = "portfolio",
            action = "read",
            permissions = listOf("READ_PORTFOLIO")
        )
        val result2 = AuthorizationResult.authorized(
            username = "user",
            resource = "portfolio",
            action = "read",
            permissions = listOf("READ_PORTFOLIO")
        )
        val result3 = AuthorizationResult.unauthorized("user", "portfolio", "delete")

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
     * that includes key properties for debugging authorization decisions.
     *
     * Expected behavior:
     * - Includes class name in string representation
     * - Shows key property values
     * - Provides useful debugging information for authorization
     * - Helps with troubleshooting access control issues
     */
    @Test
    fun `should support toString representation`() {
        // Given
        val result = AuthorizationResult.authorized(
            username = "user",
            resource = "portfolio",
            action = "read",
            permissions = listOf("READ_PORTFOLIO")
        )

        // When
        val toString = result.toString()

        // Then
        assertTrue(toString.contains("AuthorizationResult"))
        assertTrue(toString.contains("isAuthorized=true"))
        assertTrue(toString.contains("username=user"))
        assertTrue(toString.contains("resource=portfolio"))
        assertTrue(toString.contains("action=read"))
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
        val original = AuthorizationResult.authorized(
            username = "user",
            resource = "portfolio",
            action = "read",
            permissions = listOf("READ_PORTFOLIO")
        )

        // When
        val copied = original.copy(action = "write")

        // Then
        assertEquals("write", copied.action)
        assertEquals(original.isAuthorized, copied.isAuthorized)
        assertEquals(original.username, copied.username)
        assertEquals(original.resource, copied.resource)
        assertEquals(original.permissions, copied.permissions)
        assertEquals(original.errorMessage, copied.errorMessage)
    }

    /**
     * Tests handling of empty username in authorization context.
     *
     * Verifies that the authorization result can handle edge cases
     * such as empty usernames without causing errors.
     *
     * Expected behavior:
     * - Accepts empty username without errors
     * - Creates valid authorization result
     * - Maintains empty value in result
     * - Handles edge case gracefully
     */
    @Test
    fun `should handle empty username`() {
        // When
        val result = AuthorizationResult.authorized(
            username = "",
            resource = "portfolio",
            action = "read",
            permissions = listOf("READ_PORTFOLIO")
        )

        // Then
        assertTrue(result.isAuthorized)
        assertEquals("", result.username)
    }

    /**
     * Tests handling of empty resource in authorization context.
     *
     * Verifies that the authorization result can handle edge cases
     * such as empty resource names without causing errors. This supports
     * scenarios where resource identification may be minimal.
     *
     * Expected behavior:
     * - Accepts empty resource without errors
     * - Creates valid authorization result
     * - Maintains empty value in result
     * - Handles edge case gracefully
     */
    @Test
    fun `should handle empty resource`() {
        // When
        val result = AuthorizationResult.authorized(
            username = "user",
            resource = "",
            action = "read",
            permissions = listOf("READ_PORTFOLIO")
        )

        // Then
        assertTrue(result.isAuthorized)
        assertEquals("", result.resource)
    }

    /**
     * Tests handling of empty action in authorization context.
     *
     * Verifies that the authorization result can handle edge cases
     * such as empty action names without causing errors. This supports
     * scenarios where action specification may be minimal.
     *
     * Expected behavior:
     * - Accepts empty action without errors
     * - Creates valid authorization result
     * - Maintains empty value in result
     * - Handles edge case gracefully
     */
    @Test
    fun `should handle empty action`() {
        // When
        val result = AuthorizationResult.authorized(
            username = "user",
            resource = "portfolio",
            action = "",
            permissions = listOf("READ_PORTFOLIO")
        )

        // Then
        assertTrue(result.isAuthorized)
        assertEquals("", result.action)
    }

    /**
     * Tests handling of empty permissions list in authorization result.
     *
     * Verifies that authorized results can have empty permissions lists,
     * which might occur in scenarios where authorization is granted
     * through other mechanisms or for public resources.
     *
     * Expected behavior:
     * - Accepts empty permissions list without errors
     * - Creates valid authorization result
     * - Maintains empty list in result
     * - Supports authorization without explicit permissions
     */
    @Test
    fun `should handle empty permissions list`() {
        // When
        val result = AuthorizationResult.authorized(
            username = "user",
            resource = "portfolio",
            action = "read",
            permissions = emptyList()
        )

        // Then
        assertTrue(result.isAuthorized)
        assertEquals(emptyList<String>(), result.permissions)
    }

    /**
     * Tests handling of special characters in authorization parameters.
     *
     * Verifies that the authorization result can handle parameters containing
     * special characters commonly found in usernames, resource paths, and
     * complex action specifications.
     *
     * Expected behavior:
     * - Accepts special characters without errors
     * - Preserves special characters in result
     * - Supports complex parameter formats
     * - Handles real-world parameter scenarios
     */
    @Test
    fun `should handle special characters in parameters`() {
        // Given
        val username = "test@user.com"
        val resource = "portfolio/sub-resource"
        val action = "read:write"
        val permissions = listOf("READ_PORTFOLIO", "WRITE_PORTFOLIO")

        // When
        val result = AuthorizationResult.authorized(username, resource, action, permissions)

        // Then
        assertTrue(result.isAuthorized)
        assertEquals(username, result.username)
        assertEquals(resource, result.resource)
        assertEquals(action, result.action)
        assertEquals(permissions, result.permissions)
    }

    /**
     * Tests handling of long error messages in authorization failures.
     *
     * Verifies that the authorization result can handle detailed error
     * messages that provide comprehensive information about authorization
     * failures, including required and current permissions.
     *
     * Expected behavior:
     * - Accepts long error messages without truncation
     * - Preserves complete error message content
     * - Supports detailed failure explanations
     * - Handles verbose authorization feedback
     */
    @Test
    fun `should handle long error message`() {
        // Given
        val username = "user"
        val resource = "portfolio"
        val action = "delete"
        val errorMessage = "User does not have sufficient permissions to perform this action. " +
                "Required permissions: DELETE_PORTFOLIO, MANAGE_USERS. " +
                "Current permissions: READ_PORTFOLIO, WRITE_PORTFOLIO."

        // When
        val result = AuthorizationResult.unauthorized(username, resource, action, errorMessage)

        // Then
        assertFalse(result.isAuthorized)
        assertEquals(errorMessage, result.errorMessage)
    }

    /**
     * Tests handling of Unicode characters in authorization parameters.
     *
     * Verifies that the authorization result can handle international
     * characters and Unicode text in usernames, resources, actions, and
     * permissions. This supports global applications with multilingual users.
     *
     * Expected behavior:
     * - Accepts Unicode characters without errors
     * - Preserves Unicode characters correctly
     * - Supports international character sets
     * - Handles multilingual authorization contexts
     */
    @Test
    fun `should handle unicode characters in parameters`() {
        // Given
        val username = "用户测试"
        val resource = "组合"
        val action = "读取"
        val permissions = listOf("读取权限")

        // When
        val result = AuthorizationResult.authorized(username, resource, action, permissions)

        // Then
        assertTrue(result.isAuthorized)
        assertEquals(username, result.username)
        assertEquals(resource, result.resource)
        assertEquals(action, result.action)
        assertEquals(permissions, result.permissions)
    }
} 