package com.company.techportfolio.gateway.domain.port

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class AuthorizationResultTest {

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