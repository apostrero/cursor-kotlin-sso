package com.company.techportfolio.authorization.domain.model

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * User Permissions Test Suite - Domain Model Unit Tests
 *
 * This test class provides comprehensive unit testing for the UserPermissions domain model,
 * focusing on data class behavior, constructor variations, property validation, and
 * edge case handling for the authorization domain.
 *
 * ## Test Strategy:
 * - **Data Class Testing**: Constructor, equality, hashCode, toString, and copy functionality
 * - **Property Validation**: All property types and constraints
 * - **Edge Case Coverage**: Empty values, null handling, special characters, Unicode support
 * - **Boundary Testing**: Large datasets, extreme values, and limits
 * - **Business Logic**: Default values and optional parameters
 *
 * ## Test Coverage:
 * - Constructor variations (full parameters vs defaults)
 * - Property validation (username, permissions, roles, organizationId, isActive)
 * - Special character and Unicode handling
 * - Large dataset handling (many permissions/roles)
 * - Organization ID edge cases (null, zero, negative, max values)
 * - Data class contract verification (equals, hashCode, toString, copy)
 *
 * ## Domain Context:
 * UserPermissions represents the complete authorization context for a user,
 * including their permissions, roles, organizational scope, and activation status.
 * This is a critical domain model for authorization decisions.
 *
 * ## Usage in Authorization:
 * This model is used throughout the authorization service to represent a user's
 * complete permission profile for access control decisions.
 *
 * @author Technology Portfolio Team
 * @version 1.0
 * @since 1.0
 */
class UserPermissionsTest {

    @Test
    fun `should create UserPermissions with all parameters`() {
        // Given
        val username = "testuser"
        val permissions = listOf("portfolio:read", "portfolio:write", "admin:read")
        val roles = listOf("USER", "PORTFOLIO_MANAGER", "ADMIN")
        val organizationId = 1L
        val isActive = true

        // When
        val userPermissions = UserPermissions(
            username = username,
            permissions = permissions,
            roles = roles,
            organizationId = organizationId,
            isActive = isActive
        )

        // Then
        assertEquals(username, userPermissions.username)
        assertEquals(permissions, userPermissions.permissions)
        assertEquals(roles, userPermissions.roles)
        assertEquals(organizationId, userPermissions.organizationId)
        assertEquals(isActive, userPermissions.isActive)
    }

    @Test
    fun `should create UserPermissions with default values`() {
        // Given
        val username = "testuser"

        // When
        val userPermissions = UserPermissions(username = username)

        // Then
        assertEquals(username, userPermissions.username)
        assertTrue(userPermissions.permissions.isEmpty())
        assertTrue(userPermissions.roles.isEmpty())
        assertNull(userPermissions.organizationId)
        assertFalse(userPermissions.isActive)
    }

    @Test
    fun `should handle empty username`() {
        // Given
        val username = ""

        // When
        val userPermissions = UserPermissions(username = username)

        // Then
        assertEquals("", userPermissions.username)
        assertTrue(userPermissions.permissions.isEmpty())
        assertTrue(userPermissions.roles.isEmpty())
        assertNull(userPermissions.organizationId)
        assertFalse(userPermissions.isActive)
    }

    @Test
    fun `should handle special characters in username`() {
        // Given
        val username = "test.user@example.com"
        val permissions = listOf("special:permission-with-dashes", "another:permission_with_underscores")
        val roles = listOf("ROLE-WITH-DASHES", "ROLE_WITH_UNDERSCORES")

        // When
        val userPermissions = UserPermissions(
            username = username,
            permissions = permissions,
            roles = roles,
            isActive = true
        )

        // Then
        assertEquals(username, userPermissions.username)
        assertEquals(permissions, userPermissions.permissions)
        assertEquals(roles, userPermissions.roles)
        assertTrue(userPermissions.isActive)
    }

    @Test
    fun `should handle Unicode characters`() {
        // Given
        val username = "tëstüser"
        val permissions = listOf("spëcial:permissiön", "ánother:açtion")
        val roles = listOf("RÔLE_ÜSER", "ÁDMIN_RÔLE")

        // When
        val userPermissions = UserPermissions(
            username = username,
            permissions = permissions,
            roles = roles,
            isActive = true
        )

        // Then
        assertEquals(username, userPermissions.username)
        assertEquals(permissions, userPermissions.permissions)
        assertEquals(roles, userPermissions.roles)
        assertTrue(userPermissions.isActive)
    }

    @Test
    fun `should handle large lists of permissions and roles`() {
        // Given
        val username = "testuser"
        val permissions = (1..100).map { "permission:$it" }
        val roles = (1..50).map { "ROLE_$it" }

        // When
        val userPermissions = UserPermissions(
            username = username,
            permissions = permissions,
            roles = roles,
            isActive = true
        )

        // Then
        assertEquals(permissions, userPermissions.permissions)
        assertEquals(roles, userPermissions.roles)
        assertEquals(100, userPermissions.permissions.size)
        assertEquals(50, userPermissions.roles.size)
    }

    @Test
    fun `should handle null organization id`() {
        // Given
        val username = "testuser"
        val organizationId = null

        // When
        val userPermissions = UserPermissions(
            username = username,
            organizationId = organizationId,
            isActive = true
        )

        // Then
        assertNull(userPermissions.organizationId)
    }

    @Test
    fun `should handle zero organization id`() {
        // Given
        val username = "testuser"
        val organizationId = 0L

        // When
        val userPermissions = UserPermissions(
            username = username,
            organizationId = organizationId,
            isActive = true
        )

        // Then
        assertEquals(0L, userPermissions.organizationId)
    }

    @Test
    fun `should handle negative organization id`() {
        // Given
        val username = "testuser"
        val organizationId = -1L

        // When
        val userPermissions = UserPermissions(
            username = username,
            organizationId = organizationId,
            isActive = true
        )

        // Then
        assertEquals(-1L, userPermissions.organizationId)
    }

    @Test
    fun `should handle large organization id`() {
        // Given
        val username = "testuser"
        val organizationId = Long.MAX_VALUE

        // When
        val userPermissions = UserPermissions(
            username = username,
            organizationId = organizationId,
            isActive = true
        )

        // Then
        assertEquals(Long.MAX_VALUE, userPermissions.organizationId)
    }

    @Test
    fun `should test data class equality`() {
        // Given
        val permissions = listOf("permission:read")
        val roles = listOf("USER")
        val userPermissions1 = UserPermissions("user", permissions, roles, 1L, true)
        val userPermissions2 = UserPermissions("user", permissions, roles, 1L, true)
        val userPermissions3 = UserPermissions("user2", permissions, roles, 1L, true)

        // Then
        assertEquals(userPermissions1, userPermissions2)
        assertNotEquals(userPermissions1, userPermissions3)
        assertEquals(userPermissions1.hashCode(), userPermissions2.hashCode())
        assertNotEquals(userPermissions1.hashCode(), userPermissions3.hashCode())
    }

    @Test
    fun `should test data class toString`() {
        // Given
        val userPermissions = UserPermissions(
            username = "testuser",
            permissions = listOf("portfolio:read"),
            roles = listOf("USER"),
            organizationId = 1L,
            isActive = true
        )

        // When
        val toString = userPermissions.toString()

        // Then
        assertTrue(toString.contains("testuser"))
        assertTrue(toString.contains("portfolio:read"))
        assertTrue(toString.contains("USER"))
        assertTrue(toString.contains("1"))
        assertTrue(toString.contains("true"))
    }

    @Test
    fun `should test data class copy functionality`() {
        // Given
        val original = UserPermissions(
            username = "testuser",
            permissions = listOf("portfolio:read"),
            roles = listOf("USER"),
            organizationId = 1L,
            isActive = true
        )

        // When
        val copied = original.copy(isActive = false, organizationId = 2L)

        // Then
        assertEquals("testuser", copied.username)
        assertEquals(listOf("portfolio:read"), copied.permissions)
        assertEquals(listOf("USER"), copied.roles)
        assertEquals(2L, copied.organizationId)
        assertFalse(copied.isActive)
        assertNotEquals(original, copied)
    }

    @Test
    fun `should test copy with new permissions`() {
        // Given
        val original = UserPermissions(
            username = "testuser",
            permissions = listOf("portfolio:read"),
            roles = listOf("USER"),
            isActive = true
        )

        // When
        val copied = original.copy(permissions = listOf("portfolio:write", "admin:read"))

        // Then
        assertEquals("testuser", copied.username)
        assertEquals(listOf("portfolio:write", "admin:read"), copied.permissions)
        assertEquals(listOf("USER"), copied.roles)
        assertTrue(copied.isActive)
        assertNotEquals(original.permissions, copied.permissions)
    }

    @Test
    fun `should test copy with new roles`() {
        // Given
        val original = UserPermissions(
            username = "testuser",
            permissions = listOf("portfolio:read"),
            roles = listOf("USER"),
            isActive = true
        )

        // When
        val copied = original.copy(roles = listOf("ADMIN", "MANAGER"))

        // Then
        assertEquals("testuser", copied.username)
        assertEquals(listOf("portfolio:read"), copied.permissions)
        assertEquals(listOf("ADMIN", "MANAGER"), copied.roles)
        assertTrue(copied.isActive)
        assertNotEquals(original.roles, copied.roles)
    }

    @Test
    fun `should handle empty permissions list`() {
        // Given
        val username = "testuser"
        val permissions = emptyList<String>()

        // When
        val userPermissions = UserPermissions(
            username = username,
            permissions = permissions,
            isActive = true
        )

        // Then
        assertTrue(userPermissions.permissions.isEmpty())
        assertEquals(0, userPermissions.permissions.size)
    }

    @Test
    fun `should handle empty roles list`() {
        // Given
        val username = "testuser"
        val roles = emptyList<String>()

        // When
        val userPermissions = UserPermissions(
            username = username,
            roles = roles,
            isActive = true
        )

        // Then
        assertTrue(userPermissions.roles.isEmpty())
        assertEquals(0, userPermissions.roles.size)
    }

    @Test
    fun `should handle active user with no permissions or roles`() {
        // Given
        val username = "testuser"

        // When
        val userPermissions = UserPermissions(
            username = username,
            permissions = emptyList(),
            roles = emptyList(),
            isActive = true
        )

        // Then
        assertEquals(username, userPermissions.username)
        assertTrue(userPermissions.permissions.isEmpty())
        assertTrue(userPermissions.roles.isEmpty())
        assertTrue(userPermissions.isActive)
    }

    @Test
    fun `should handle inactive user with permissions and roles`() {
        // Given
        val username = "inactiveuser"
        val permissions = listOf("portfolio:read")
        val roles = listOf("USER")

        // When
        val userPermissions = UserPermissions(
            username = username,
            permissions = permissions,
            roles = roles,
            isActive = false
        )

        // Then
        assertEquals(username, userPermissions.username)
        assertEquals(permissions, userPermissions.permissions)
        assertEquals(roles, userPermissions.roles)
        assertFalse(userPermissions.isActive)
    }

    @Test
    fun `should handle very long username`() {
        // Given
        val longUsername = "a".repeat(1000)

        // When
        val userPermissions = UserPermissions(username = longUsername)

        // Then
        assertEquals(longUsername, userPermissions.username)
        assertEquals(1000, userPermissions.username.length)
    }

    @Test
    fun `should handle permissions with colons and special characters`() {
        // Given
        val username = "testuser"
        val permissions = listOf(
            "portfolio:read",
            "portfolio:write",
            "admin:users:create",
            "admin:users:delete",
            "special-resource:complex-action",
            "resource_with_underscores:action_with_underscores"
        )

        // When
        val userPermissions = UserPermissions(
            username = username,
            permissions = permissions,
            isActive = true
        )

        // Then
        assertEquals(permissions, userPermissions.permissions)
        assertTrue(userPermissions.permissions.contains("portfolio:read"))
        assertTrue(userPermissions.permissions.contains("admin:users:create"))
        assertTrue(userPermissions.permissions.contains("special-resource:complex-action"))
        assertTrue(userPermissions.permissions.contains("resource_with_underscores:action_with_underscores"))
    }
} 