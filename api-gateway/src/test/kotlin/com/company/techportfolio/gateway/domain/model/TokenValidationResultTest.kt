package com.company.techportfolio.gateway.domain.model

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.time.LocalDateTime

class TokenValidationResultTest {

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