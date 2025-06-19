package com.company.techportfolio.gateway.domain.model

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.time.LocalDateTime

class AuthenticationResultTest {

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