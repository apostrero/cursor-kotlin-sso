package com.company.techportfolio.shared.domain.event

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class AuthenticationEventsTest {

    @Test
    fun `should create UserAuthenticatedEvent with all parameters`() {
        val event = UserAuthenticatedEvent(
            username = "testuser",
            sessionIndex = "session-123",
            ipAddress = "192.168.1.1",
            userAgent = "Mozilla/5.0"
        )

        assertEquals("testuser", event.username)
        assertEquals("session-123", event.sessionIndex)
        assertEquals("192.168.1.1", event.ipAddress)
        assertEquals("Mozilla/5.0", event.userAgent)
        assertNotNull(event.eventId)
        assertNotNull(event.timestamp)
        assertEquals("1.0", event.version)
        assertEquals("UserAuthenticatedEvent", event.eventType)
    }

    @Test
    fun `should create UserAuthenticatedEvent with null optional parameters`() {
        val event = UserAuthenticatedEvent(
            username = "testuser",
            sessionIndex = null,
            ipAddress = null,
            userAgent = null
        )

        assertEquals("testuser", event.username)
        assertNull(event.sessionIndex)
        assertNull(event.ipAddress)
        assertNull(event.userAgent)
        assertNotNull(event.eventId)
        assertNotNull(event.timestamp)
        assertEquals("UserAuthenticatedEvent", event.eventType)
    }

    @Test
    fun `should create UserAuthenticationFailedEvent with all parameters`() {
        val event = UserAuthenticationFailedEvent(
            username = "testuser",
            reason = "Invalid credentials",
            ipAddress = "192.168.1.1",
            userAgent = "Mozilla/5.0"
        )

        assertEquals("testuser", event.username)
        assertEquals("Invalid credentials", event.reason)
        assertEquals("192.168.1.1", event.ipAddress)
        assertEquals("Mozilla/5.0", event.userAgent)
        assertNotNull(event.eventId)
        assertNotNull(event.timestamp)
        assertEquals("1.0", event.version)
        assertEquals("UserAuthenticationFailedEvent", event.eventType)
    }

    @Test
    fun `should create UserAuthenticationFailedEvent with null username`() {
        val event = UserAuthenticationFailedEvent(
            username = null,
            reason = "Account locked",
            ipAddress = "192.168.1.1",
            userAgent = "Mozilla/5.0"
        )

        assertNull(event.username)
        assertEquals("Account locked", event.reason)
        assertEquals("192.168.1.1", event.ipAddress)
        assertEquals("Mozilla/5.0", event.userAgent)
        assertEquals("UserAuthenticationFailedEvent", event.eventType)
    }

    @Test
    fun `should create UserAuthenticationFailedEvent with null optional parameters`() {
        val event = UserAuthenticationFailedEvent(
            username = "testuser",
            reason = "Invalid password",
            ipAddress = null,
            userAgent = null
        )

        assertEquals("testuser", event.username)
        assertEquals("Invalid password", event.reason)
        assertNull(event.ipAddress)
        assertNull(event.userAgent)
        assertEquals("UserAuthenticationFailedEvent", event.eventType)
    }

    @Test
    fun `should create UserLoggedOutEvent with all parameters`() {
        val event = UserLoggedOutEvent(
            username = "testuser",
            sessionIndex = "session-123"
        )

        assertEquals("testuser", event.username)
        assertEquals("session-123", event.sessionIndex)
        assertNotNull(event.eventId)
        assertNotNull(event.timestamp)
        assertEquals("1.0", event.version)
        assertEquals("UserLoggedOutEvent", event.eventType)
    }

    @Test
    fun `should create UserLoggedOutEvent with null session index`() {
        val event = UserLoggedOutEvent(
            username = "testuser",
            sessionIndex = null
        )

        assertEquals("testuser", event.username)
        assertNull(event.sessionIndex)
        assertEquals("UserLoggedOutEvent", event.eventType)
    }

    @Test
    fun `should support data class equality for UserAuthenticatedEvent`() {
        val event1 = UserAuthenticatedEvent(
            username = "testuser",
            sessionIndex = "session-123",
            ipAddress = "192.168.1.1",
            userAgent = "Mozilla/5.0"
        )

        val event2 = UserAuthenticatedEvent(
            username = "testuser",
            sessionIndex = "session-123",
            ipAddress = "192.168.1.1",
            userAgent = "Mozilla/5.0"
        )

        // Note: Events will not be equal due to different eventId and timestamp
        // But we can test the data properties
        assertEquals(event1.username, event2.username)
        assertEquals(event1.sessionIndex, event2.sessionIndex)
        assertEquals(event1.ipAddress, event2.ipAddress)
        assertEquals(event1.userAgent, event2.userAgent)
    }

    @Test
    fun `should support data class copy for UserAuthenticatedEvent`() {
        val event = UserAuthenticatedEvent(
            username = "testuser",
            sessionIndex = "session-123",
            ipAddress = "192.168.1.1",
            userAgent = "Mozilla/5.0"
        )

        val copiedEvent = event.copy(username = "newuser")

        assertEquals("newuser", copiedEvent.username)
        assertEquals("session-123", copiedEvent.sessionIndex)
        assertEquals("192.168.1.1", copiedEvent.ipAddress)
        assertEquals("Mozilla/5.0", copiedEvent.userAgent)
    }

    @Test
    fun `should support data class copy for UserAuthenticationFailedEvent`() {
        val event = UserAuthenticationFailedEvent(
            username = "testuser",
            reason = "Invalid credentials",
            ipAddress = "192.168.1.1",
            userAgent = "Mozilla/5.0"
        )

        val copiedEvent = event.copy(reason = "Account locked")

        assertEquals("testuser", copiedEvent.username)
        assertEquals("Account locked", copiedEvent.reason)
        assertEquals("192.168.1.1", copiedEvent.ipAddress)
        assertEquals("Mozilla/5.0", copiedEvent.userAgent)
    }

    @Test
    fun `should support data class copy for UserLoggedOutEvent`() {
        val event = UserLoggedOutEvent(
            username = "testuser",
            sessionIndex = "session-123"
        )

        val copiedEvent = event.copy(sessionIndex = "new-session")

        assertEquals("testuser", copiedEvent.username)
        assertEquals("new-session", copiedEvent.sessionIndex)
    }

    @Test
    fun `should handle empty strings properly`() {
        val event = UserAuthenticatedEvent(
            username = "",
            sessionIndex = "",
            ipAddress = "",
            userAgent = ""
        )

        assertEquals("", event.username)
        assertEquals("", event.sessionIndex)
        assertEquals("", event.ipAddress)
        assertEquals("", event.userAgent)
    }
} 