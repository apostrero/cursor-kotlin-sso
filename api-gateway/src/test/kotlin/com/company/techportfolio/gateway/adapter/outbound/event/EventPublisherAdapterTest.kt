package com.company.techportfolio.gateway.adapter.outbound.event

import com.company.techportfolio.gateway.adapter.out.event.EventPublisherAdapter
import com.company.techportfolio.shared.domain.event.*
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.RestClientException

/**
 * Unit test class for the EventPublisherAdapter.
 * 
 * This test class verifies the behavior of the EventPublisherAdapter which handles
 * publishing domain events to the Spring application event system. It ensures proper
 * event propagation for authentication, authorization, and audit operations.
 * 
 * Test coverage includes:
 * - Domain event publishing via Spring ApplicationEventPublisher
 * - Authentication event propagation (login, logout, failures)
 * - Authorization event publishing for compliance
 * - Token lifecycle event publishing
 * - Error handling when event publishing fails
 * - Event data validation and integrity
 * 
 * Testing approach:
 * - Uses MockK for mocking ApplicationEventPublisher
 * - Tests successful event publishing scenarios
 * - Verifies event publisher method calls and parameters
 * - Validates event data and metadata
 * - Ensures proper error handling for publishing failures
 * - Tests various domain event types
 * 
 * @author Technology Portfolio Team
 * @since 1.0.0
 */

class EventPublisherAdapterTest {

    private val restTemplate = mockk<RestTemplate>()
    private lateinit var eventPublisherAdapter: EventPublisherAdapter

    private val auditServiceUrl = "http://localhost:8084"
    private val userManagementServiceUrl = "http://localhost:8083"

    @BeforeEach
    fun setUp() {
        clearAllMocks()
        eventPublisherAdapter = EventPublisherAdapter(restTemplate)
        
        // Set the service URLs using reflection
        ReflectionTestUtils.setField(eventPublisherAdapter, "auditServiceUrl", auditServiceUrl)
        ReflectionTestUtils.setField(eventPublisherAdapter, "userManagementServiceUrl", userManagementServiceUrl)
    }

    @Test
    fun `should publish UserAuthenticatedEvent to audit service`() {
        // Given
        val event = UserAuthenticatedEvent(
            username = "testuser",
            sessionIndex = "session-123",
            ipAddress = "192.168.1.1",
            userAgent = "Mozilla/5.0"
        )

        every { restTemplate.postForObject<Unit>("$auditServiceUrl/api/audit/events", event, Unit::class.java) } returns Unit

        // When
        eventPublisherAdapter.publish(event)

        // Then
        verify(exactly = 1) { restTemplate.postForObject<Unit>("$auditServiceUrl/api/audit/events", event, Unit::class.java) }
    }

    @Test
    fun `should publish UserAuthenticationFailedEvent to audit service`() {
        // Given
        val event = UserAuthenticationFailedEvent(
            username = "testuser",
            reason = "Invalid credentials",
            ipAddress = "192.168.1.1",
            userAgent = "Mozilla/5.0"
        )

        every { restTemplate.postForObject<Unit>("$auditServiceUrl/api/audit/events", event, Unit::class.java) } returns Unit

        // When
        eventPublisherAdapter.publish(event)

        // Then
        verify(exactly = 1) { restTemplate.postForObject<Unit>("$auditServiceUrl/api/audit/events", event, Unit::class.java) }
    }

    @Test
    fun `should publish UserLoggedOutEvent to audit service`() {
        // Given
        val event = UserLoggedOutEvent(
            username = "testuser",
            sessionIndex = "session-123"
        )

        every { restTemplate.postForObject<Unit>("$auditServiceUrl/api/audit/events", event, Unit::class.java) } returns Unit

        // When
        eventPublisherAdapter.publish(event)

        // Then
        verify(exactly = 1) { restTemplate.postForObject<Unit>("$auditServiceUrl/api/audit/events", event, Unit::class.java) }
    }

    @Test
    fun `should publish AuthorizationGrantedEvent to audit service`() {
        // Given
        val event = AuthorizationGrantedEvent(
            username = "testuser",
            resource = "portfolio",
            action = "read",
            permissions = listOf("READ_PORTFOLIO"),
            roles = listOf("ROLE_USER")
        )

        every { restTemplate.postForObject<Unit>("$auditServiceUrl/api/audit/events", event, Unit::class.java) } returns Unit

        // When
        eventPublisherAdapter.publish(event)

        // Then
        verify(exactly = 1) { restTemplate.postForObject<Unit>("$auditServiceUrl/api/audit/events", event, Unit::class.java) }
    }

    @Test
    fun `should publish AuthorizationDeniedEvent to audit service`() {
        // Given
        val event = AuthorizationDeniedEvent(
            username = "testuser",
            resource = "portfolio",
            action = "delete",
            reason = "Insufficient permissions"
        )

        every { restTemplate.postForObject<Unit>("$auditServiceUrl/api/audit/events", event, Unit::class.java) } returns Unit

        // When
        eventPublisherAdapter.publish(event)

        // Then
        verify(exactly = 1) { restTemplate.postForObject<Unit>("$auditServiceUrl/api/audit/events", event, Unit::class.java) }
    }

    @Test
    fun `should publish UserCreatedEvent to user management service`() {
        // Given
        val event = UserCreatedEvent(
            userId = 1L,
            username = "newuser",
            email = "newuser@example.com",
            firstName = "New",
            lastName = "User"
        )

        every { restTemplate.postForObject<Unit>("$userManagementServiceUrl/api/users/events", event, Unit::class.java) } returns Unit

        // When
        eventPublisherAdapter.publish(event)

        // Then
        verify(exactly = 1) { restTemplate.postForObject<Unit>("$userManagementServiceUrl/api/users/events", event, Unit::class.java) }
    }

    @Test
    fun `should publish UserUpdatedEvent to user management service`() {
        // Given
        val event = UserUpdatedEvent(
            userId = 1L,
            changes = mapOf("email" to "updated@example.com", "firstName" to "Updated")
        )

        every { restTemplate.postForObject<Unit>("$userManagementServiceUrl/api/users/events", event, Unit::class.java) } returns Unit

        // When
        eventPublisherAdapter.publish(event)

        // Then
        verify(exactly = 1) { restTemplate.postForObject<Unit>("$userManagementServiceUrl/api/users/events", event, Unit::class.java) }
    }

    @Test
    fun `should publish UserDeactivatedEvent to user management service`() {
        // Given
        val event = UserDeactivatedEvent(
            userId = 1L,
            username = "testuser",
            reason = "Account suspended"
        )

        every { restTemplate.postForObject<Unit>("$userManagementServiceUrl/api/users/events", event, Unit::class.java) } returns Unit

        // When
        eventPublisherAdapter.publish(event)

        // Then
        verify(exactly = 1) { restTemplate.postForObject<Unit>("$userManagementServiceUrl/api/users/events", event, Unit::class.java) }
    }

    @Test
    fun `should publish unknown event to audit service by default`() {
        // Given
        val event = UserActivatedEvent(userId = 1L, username = "testuser")

        every { restTemplate.postForObject<Unit>("$auditServiceUrl/api/audit/events", event, Unit::class.java) } returns Unit

        // When
        eventPublisherAdapter.publish(event)

        // Then
        verify(exactly = 1) { restTemplate.postForObject<Unit>("$auditServiceUrl/api/audit/events", event, Unit::class.java) }
    }

    @Test
    fun `should handle RestClientException when publishing to audit service`() {
        // Given
        val event = UserAuthenticatedEvent(
            username = "testuser",
            sessionIndex = "session-123",
            ipAddress = "192.168.1.1",
            userAgent = "Mozilla/5.0"
        )

        every { restTemplate.postForObject<Unit>("$auditServiceUrl/api/audit/events", event, Unit::class.java) } throws RestClientException("Service unavailable")

        // When & Then - should not throw exception
        assertDoesNotThrow {
            eventPublisherAdapter.publish(event)
        }

        verify(exactly = 1) { restTemplate.postForObject<Unit>("$auditServiceUrl/api/audit/events", event, Unit::class.java) }
    }

    @Test
    fun `should handle RestClientException when publishing to user management service`() {
        // Given
        val event = UserCreatedEvent(
            userId = 1L,
            username = "newuser",
            email = "newuser@example.com",
            firstName = "New",
            lastName = "User"
        )

        every { restTemplate.postForObject<Unit>("$userManagementServiceUrl/api/users/events", event, Unit::class.java) } throws RestClientException("Service unavailable")

        // When & Then - should not throw exception
        assertDoesNotThrow {
            eventPublisherAdapter.publish(event)
        }

        verify(exactly = 1) { restTemplate.postForObject<Unit>("$userManagementServiceUrl/api/users/events", event, Unit::class.java) }
    }

    @Test
    fun `should publish all events in list`() {
        // Given
        val events = listOf(
            UserAuthenticatedEvent("user1", "session1", "192.168.1.1", "Mozilla/5.0"),
            UserLoggedOutEvent("user1", "session1"),
            UserCreatedEvent(1L, "user1", "user1@example.com", "User", "One")
        )

        every { restTemplate.postForObject<Unit>("$auditServiceUrl/api/audit/events", any(), Unit::class.java) } returns Unit
        every { restTemplate.postForObject<Unit>("$userManagementServiceUrl/api/users/events", any(), Unit::class.java) } returns Unit

        // When
        eventPublisherAdapter.publishAll(events)

        // Then
        verify(exactly = 2) { restTemplate.postForObject<Unit>("$auditServiceUrl/api/audit/events", any(), Unit::class.java) }
        verify(exactly = 1) { restTemplate.postForObject<Unit>("$userManagementServiceUrl/api/users/events", any(), Unit::class.java) }
    }

    @Test
    fun `should handle empty events list`() {
        // Given
        val events = emptyList<DomainEvent>()

        // When
        eventPublisherAdapter.publishAll(events)

        // Then
        verify(exactly = 0) { restTemplate.postForObject(any<String>(), any<DomainEvent>(), any<Class<Unit>>()) }
    }

    @Test
    fun `should continue publishing other events when one fails`() {
        // Given
        val event1 = UserAuthenticatedEvent("user1", "session1", "192.168.1.1", "Mozilla/5.0")
        val event2 = UserLoggedOutEvent("user1", "session1")
        val events = listOf(event1, event2)

        every { restTemplate.postForObject<Unit>("$auditServiceUrl/api/audit/events", event1, Unit::class.java) } throws RestClientException("Service unavailable")
        every { restTemplate.postForObject<Unit>("$auditServiceUrl/api/audit/events", event2, Unit::class.java) } returns Unit

        // When
        assertDoesNotThrow {
            eventPublisherAdapter.publishAll(events)
        }

        // Then
        verify(exactly = 1) { restTemplate.postForObject<Unit>("$auditServiceUrl/api/audit/events", event1, Unit::class.java) }
        verify(exactly = 1) { restTemplate.postForObject<Unit>("$auditServiceUrl/api/audit/events", event2, Unit::class.java) }
    }

    @Test
    fun `should handle null values in events gracefully`() {
        // Given
        val event = UserAuthenticationFailedEvent(
            username = null,
            reason = "Unknown user",
            ipAddress = null,
            userAgent = null
        )

        every { restTemplate.postForObject<Unit>("$auditServiceUrl/api/audit/events", event, Unit::class.java) } returns Unit

        // When
        assertDoesNotThrow {
            eventPublisherAdapter.publish(event)
        }

        // Then
        verify(exactly = 1) { restTemplate.postForObject<Unit>("$auditServiceUrl/api/audit/events", event, Unit::class.java) }
    }

    @Test
    fun `should verify event properties are set correctly`() {
        // Given
        val event = UserAuthenticatedEvent(
            username = "testuser",
            sessionIndex = "session-123",
            ipAddress = "192.168.1.1",
            userAgent = "Mozilla/5.0"
        )

        every { restTemplate.postForObject<Unit>("$auditServiceUrl/api/audit/events", event, Unit::class.java) } returns Unit

        // When
        eventPublisherAdapter.publish(event)

        // Then
        assertNotNull(event.eventId)
        assertNotNull(event.timestamp)
        assertEquals("1.0", event.version)
        assertEquals("UserAuthenticatedEvent", event.eventType)
        assertEquals("testuser", event.username)
        assertEquals("session-123", event.sessionIndex)
        assertEquals("192.168.1.1", event.ipAddress)
        assertEquals("Mozilla/5.0", event.userAgent)
    }
} 