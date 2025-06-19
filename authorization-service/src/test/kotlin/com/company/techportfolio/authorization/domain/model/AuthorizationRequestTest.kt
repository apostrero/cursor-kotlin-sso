package com.company.techportfolio.authorization.domain.model

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class AuthorizationRequestTest {

    @Test
    fun `should create AuthorizationRequest with all parameters`() {
        // Given
        val username = "testuser"
        val resource = "portfolio"
        val action = "read"
        val context = mapOf("organizationId" to 1L, "department" to "IT")

        // When
        val request = AuthorizationRequest(
            username = username,
            resource = resource,
            action = action,
            context = context
        )

        // Then
        assertEquals(username, request.username)
        assertEquals(resource, request.resource)
        assertEquals(action, request.action)
        assertEquals(context, request.context)
    }

    @Test
    fun `should create AuthorizationRequest with default empty context`() {
        // Given
        val username = "testuser"
        val resource = "portfolio"
        val action = "read"

        // When
        val request = AuthorizationRequest(
            username = username,
            resource = resource,
            action = action
        )

        // Then
        assertEquals(username, request.username)
        assertEquals(resource, request.resource)
        assertEquals(action, request.action)
        assertTrue(request.context.isEmpty())
    }

    @Test
    fun `should handle empty username`() {
        // Given
        val username = ""
        val resource = "portfolio"
        val action = "read"

        // When
        val request = AuthorizationRequest(
            username = username,
            resource = resource,
            action = action
        )

        // Then
        assertEquals("", request.username)
        assertEquals(resource, request.resource)
        assertEquals(action, request.action)
    }

    @Test
    fun `should handle special characters in parameters`() {
        // Given
        val username = "test.user@example.com"
        val resource = "portfolio/sub-resource"
        val action = "read-write"
        val context = mapOf("special" to "value with spaces & symbols!")

        // When
        val request = AuthorizationRequest(
            username = username,
            resource = resource,
            action = action,
            context = context
        )

        // Then
        assertEquals(username, request.username)
        assertEquals(resource, request.resource)
        assertEquals(action, request.action)
        assertEquals(context, request.context)
    }

    @Test
    fun `should handle Unicode characters`() {
        // Given
        val username = "tëstüser"
        val resource = "pörtfölio"
        val action = "réad"
        val context = mapOf("unicode" to "vålue with ümlauts")

        // When
        val request = AuthorizationRequest(
            username = username,
            resource = resource,
            action = action,
            context = context
        )

        // Then
        assertEquals(username, request.username)
        assertEquals(resource, request.resource)
        assertEquals(action, request.action)
        assertEquals(context, request.context)
    }

    @Test
    fun `should handle complex context map`() {
        // Given
        val username = "testuser"
        val resource = "portfolio"
        val action = "read"
        val context = mapOf(
            "organizationId" to 1L,
            "department" to "IT",
            "level" to 5,
            "isManager" to true,
            "tags" to listOf("urgent", "review"),
            "metadata" to mapOf("created" to "2023-01-01", "version" to 1.0)
        )

        // When
        val request = AuthorizationRequest(
            username = username,
            resource = resource,
            action = action,
            context = context
        )

        // Then
        assertEquals(username, request.username)
        assertEquals(resource, request.resource)
        assertEquals(action, request.action)
        assertEquals(context, request.context)
        assertEquals(1L, request.context["organizationId"])
        assertEquals("IT", request.context["department"])
        assertEquals(5, request.context["level"])
        assertEquals(true, request.context["isManager"])
        assertEquals(listOf("urgent", "review"), request.context["tags"])
        assertEquals(mapOf("created" to "2023-01-01", "version" to 1.0), request.context["metadata"])
    }

    @Test
    fun `should test data class equality`() {
        // Given
        val context = mapOf("key" to "value")
        val request1 = AuthorizationRequest("user", "resource", "action", context)
        val request2 = AuthorizationRequest("user", "resource", "action", context)
        val request3 = AuthorizationRequest("user2", "resource", "action", context)

        // Then
        assertEquals(request1, request2)
        assertNotEquals(request1, request3)
        assertEquals(request1.hashCode(), request2.hashCode())
        assertNotEquals(request1.hashCode(), request3.hashCode())
    }

    @Test
    fun `should test data class toString`() {
        // Given
        val request = AuthorizationRequest(
            username = "testuser",
            resource = "portfolio",
            action = "read",
            context = mapOf("key" to "value")
        )

        // When
        val toString = request.toString()

        // Then
        assertTrue(toString.contains("testuser"))
        assertTrue(toString.contains("portfolio"))
        assertTrue(toString.contains("read"))
        assertTrue(toString.contains("key"))
        assertTrue(toString.contains("value"))
    }

    @Test
    fun `should test data class copy functionality`() {
        // Given
        val original = AuthorizationRequest(
            username = "testuser",
            resource = "portfolio",
            action = "read",
            context = mapOf("key" to "value")
        )

        // When
        val copied = original.copy(action = "write")

        // Then
        assertEquals("testuser", copied.username)
        assertEquals("portfolio", copied.resource)
        assertEquals("write", copied.action)
        assertEquals(mapOf("key" to "value"), copied.context)
        assertNotEquals(original, copied)
    }

    @Test
    fun `should test copy with new context`() {
        // Given
        val original = AuthorizationRequest(
            username = "testuser",
            resource = "portfolio",
            action = "read",
            context = mapOf("key1" to "value1")
        )

        // When
        val copied = original.copy(context = mapOf("key2" to "value2"))

        // Then
        assertEquals("testuser", copied.username)
        assertEquals("portfolio", copied.resource)
        assertEquals("read", copied.action)
        assertEquals(mapOf("key2" to "value2"), copied.context)
        assertNotEquals(original.context, copied.context)
    }

    @Test
    fun `should handle empty values in context map`() {
        // Given
        val username = "testuser"
        val resource = "portfolio"
        val action = "read"
        val context = mapOf<String, Any>(
            "validKey" to "validValue",
            "emptyKey" to ""
        )

        // When
        val request = AuthorizationRequest(
            username = username,
            resource = resource,
            action = action,
            context = context
        )

        // Then
        assertEquals(context, request.context)
        assertEquals("validValue", request.context["validKey"])
        assertEquals("", request.context["emptyKey"])
    }

    @Test
    fun `should handle very long strings`() {
        // Given
        val longString = "a".repeat(1000)
        val username = longString
        val resource = "portfolio"
        val action = "read"

        // When
        val request = AuthorizationRequest(
            username = username,
            resource = resource,
            action = action
        )

        // Then
        assertEquals(longString, request.username)
        assertEquals(1000, request.username.length)
    }
} 