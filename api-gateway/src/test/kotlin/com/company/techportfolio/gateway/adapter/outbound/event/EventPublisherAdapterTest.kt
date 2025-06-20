package com.company.techportfolio.gateway.adapter.outbound.event

import com.company.techportfolio.gateway.adapter.out.event.EventPublisherAdapter
import com.company.techportfolio.shared.domain.event.*
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.RequestBodyUriSpec
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.WebClientResponseException

/**
 * Unit test class for the Reactive EventPublisherAdapter.
 * 
 * This test class verifies the behavior of the reactive EventPublisherAdapter which handles
 * publishing domain events to external microservices using WebClient. It ensures proper
 * reactive event propagation for authentication, authorization, and audit operations.
 * 
 * Test coverage includes:
 * - Reactive domain event publishing via WebClient
 * - Authentication event propagation (login, logout, failures)
 * - Authorization event publishing for compliance
 * - Token lifecycle event publishing
 * - Reactive error handling when event publishing fails
 * - Event data validation and integrity
 * - Reactive composition and backpressure handling
 * 
 * Testing approach:
 * - Uses MockK for mocking WebClient and its components
 * - Tests successful reactive event publishing scenarios
 * - Verifies WebClient method calls and parameters
 * - Validates event data and metadata
 * - Ensures proper reactive error handling for publishing failures
 * - Tests various domain event types with reactive patterns
 * 
 * @author Technology Portfolio Team
 * @since 1.0.0
 */
class EventPublisherAdapterTest {

    private val webClient = mockk<WebClient>()
    private val requestBodyUriSpec = mockk<RequestBodyUriSpec>()
    private val requestBodySpec = mockk<RequestBodySpec>()
    private val responseSpec = mockk<ResponseSpec>()
    
    private lateinit var eventPublisherAdapter: EventPublisherAdapter

    private val auditServiceUrl = "http://localhost:8084"
    private val userManagementServiceUrl = "http://localhost:8083"

    @BeforeEach
    fun setUp() {
        clearAllMocks()
        eventPublisherAdapter = EventPublisherAdapter(webClient)
        
        // Set the service URLs using reflection
        ReflectionTestUtils.setField(eventPublisherAdapter, "auditServiceUrl", auditServiceUrl)
        ReflectionTestUtils.setField(eventPublisherAdapter, "userManagementServiceUrl", userManagementServiceUrl)
        ReflectionTestUtils.setField(eventPublisherAdapter, "timeout", "5s")
        
        // Setup WebClient mock chain
        every { webClient.post() } returns requestBodyUriSpec
        every { requestBodyUriSpec.uri(any<String>()) } returns requestBodySpec
        every { requestBodySpec.bodyValue(any<DomainEvent>()) } returns requestBodySpec
        every { requestBodySpec.retrieve() } returns responseSpec
        every { responseSpec.bodyToMono(Void::class.java) } returns Mono.empty()
    }

    @Test
    fun `should publish UserAuthenticatedEvent to audit service reactively`() {
        // Given
        val event = UserAuthenticatedEvent(
            username = "testuser",
            sessionIndex = "session-123",
            ipAddress = "192.168.1.1",
            userAgent = "Mozilla/5.0"
        )

        // When & Then
        StepVerifier.create(eventPublisherAdapter.publish(event))
            .verifyComplete()

        verify(exactly = 1) { 
            webClient.post()
            requestBodyUriSpec.uri("$auditServiceUrl/api/audit/events")
            requestBodySpec.bodyValue(event)
            responseSpec.bodyToMono(Void::class.java)
        }
    }

    @Test
    fun `should publish UserAuthenticationFailedEvent to audit service reactively`() {
        // Given
        val event = UserAuthenticationFailedEvent(
            username = "testuser",
            reason = "Invalid credentials",
            ipAddress = "192.168.1.1",
            userAgent = "Mozilla/5.0"
        )

        // When & Then
        StepVerifier.create(eventPublisherAdapter.publish(event))
            .verifyComplete()

        verify(exactly = 1) { 
            requestBodyUriSpec.uri("$auditServiceUrl/api/audit/events")
            requestBodySpec.bodyValue(event)
        }
    }

    @Test
    fun `should publish UserLoggedOutEvent to audit service reactively`() {
        // Given
        val event = UserLoggedOutEvent(
            username = "testuser",
            sessionIndex = "session-123"
        )

        // When & Then
        StepVerifier.create(eventPublisherAdapter.publish(event))
            .verifyComplete()

        verify(exactly = 1) { 
            requestBodyUriSpec.uri("$auditServiceUrl/api/audit/events")
            requestBodySpec.bodyValue(event)
        }
    }

    @Test
    fun `should publish AuthorizationGrantedEvent to audit service reactively`() {
        // Given
        val event = AuthorizationGrantedEvent(
            username = "testuser",
            resource = "portfolio",
            action = "read",
            permissions = listOf("READ_PORTFOLIO"),
            roles = listOf("ROLE_USER")
        )

        // When & Then
        StepVerifier.create(eventPublisherAdapter.publish(event))
            .verifyComplete()

        verify(exactly = 1) { 
            requestBodyUriSpec.uri("$auditServiceUrl/api/audit/events")
            requestBodySpec.bodyValue(event)
        }
    }

    @Test
    fun `should publish AuthorizationDeniedEvent to audit service reactively`() {
        // Given
        val event = AuthorizationDeniedEvent(
            username = "testuser",
            resource = "portfolio",
            action = "delete",
            reason = "Insufficient permissions"
        )

        // When & Then
        StepVerifier.create(eventPublisherAdapter.publish(event))
            .verifyComplete()

        verify(exactly = 1) { 
            requestBodyUriSpec.uri("$auditServiceUrl/api/audit/events")
            requestBodySpec.bodyValue(event)
        }
    }

    @Test
    fun `should publish UserCreatedEvent to user management service reactively`() {
        // Given
        val event = UserCreatedEvent(
            userId = 1L,
            username = "newuser",
            email = "newuser@example.com",
            firstName = "New",
            lastName = "User"
        )

        // When & Then
        StepVerifier.create(eventPublisherAdapter.publish(event))
            .verifyComplete()

        verify(exactly = 1) { 
            requestBodyUriSpec.uri("$userManagementServiceUrl/api/users/events")
            requestBodySpec.bodyValue(event)
        }
    }

    @Test
    fun `should publish UserUpdatedEvent to user management service reactively`() {
        // Given
        val event = UserUpdatedEvent(
            userId = 1L,
            changes = mapOf("email" to "updated@example.com", "firstName" to "Updated")
        )

        // When & Then
        StepVerifier.create(eventPublisherAdapter.publish(event))
            .verifyComplete()

        verify(exactly = 1) { 
            requestBodyUriSpec.uri("$userManagementServiceUrl/api/users/events")
            requestBodySpec.bodyValue(event)
        }
    }

    @Test
    fun `should publish UserDeactivatedEvent to user management service reactively`() {
        // Given
        val event = UserDeactivatedEvent(
            userId = 1L,
            username = "testuser",
            reason = "Account suspended"
        )

        // When & Then
        StepVerifier.create(eventPublisherAdapter.publish(event))
            .verifyComplete()

        verify(exactly = 1) { 
            requestBodyUriSpec.uri("$userManagementServiceUrl/api/users/events")
            requestBodySpec.bodyValue(event)
        }
    }

    @Test
    fun `should publish unknown event to audit service by default reactively`() {
        // Given
        val event = UserActivatedEvent(userId = 1L, username = "testuser")

        // When & Then
        StepVerifier.create(eventPublisherAdapter.publish(event))
            .verifyComplete()

        verify(exactly = 1) { 
            requestBodyUriSpec.uri("$auditServiceUrl/api/audit/events")
            requestBodySpec.bodyValue(event)
        }
    }

    @Test
    fun `should handle WebClientResponseException when publishing to audit service reactively`() {
        // Given
        val event = UserAuthenticatedEvent(
            username = "testuser",
            sessionIndex = "session-123",
            ipAddress = "192.168.1.1",
            userAgent = "Mozilla/5.0"
        )

        every { responseSpec.bodyToMono(Void::class.java) } returns 
            Mono.error(WebClientResponseException.create(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "Service unavailable",
                null, null, null
            ))

        // When & Then - should complete successfully despite error
        StepVerifier.create(eventPublisherAdapter.publish(event))
            .verifyComplete()

        verify(exactly = 1) { 
            requestBodyUriSpec.uri("$auditServiceUrl/api/audit/events")
            requestBodySpec.bodyValue(event)
        }
    }

    @Test
    fun `should handle WebClientResponseException when publishing to user management service reactively`() {
        // Given
        val event = UserCreatedEvent(
            userId = 1L,
            username = "newuser",
            email = "newuser@example.com",
            firstName = "New",
            lastName = "User"
        )

        every { responseSpec.bodyToMono(Void::class.java) } returns 
            Mono.error(WebClientResponseException.create(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "Service unavailable",
                null, null, null
            ))

        // When & Then - should complete successfully despite error
        StepVerifier.create(eventPublisherAdapter.publish(event))
            .verifyComplete()

        verify(exactly = 1) { 
            requestBodyUriSpec.uri("$userManagementServiceUrl/api/users/events")
            requestBodySpec.bodyValue(event)
        }
    }

    @Test
    fun `should publish all events in list reactively`() {
        // Given
        val events = listOf(
            UserAuthenticatedEvent("user1", "session1", "192.168.1.1", "Mozilla/5.0"),
            UserLoggedOutEvent("user1", "session1"),
            UserCreatedEvent(1L, "user1", "user1@example.com", "User", "One")
        )

        // When & Then
        StepVerifier.create(eventPublisherAdapter.publishAll(events))
            .verifyComplete()

        verify(exactly = 2) { 
            requestBodyUriSpec.uri("$auditServiceUrl/api/audit/events")
        }
        verify(exactly = 1) { 
            requestBodyUriSpec.uri("$userManagementServiceUrl/api/users/events")
        }
    }

    @Test
    fun `should handle empty events list reactively`() {
        // Given
        val events = emptyList<DomainEvent>()

        // When & Then
        StepVerifier.create(eventPublisherAdapter.publishAll(events))
            .verifyComplete()

        verify(exactly = 0) { 
            webClient.post()
        }
    }

    @Test
    fun `should continue publishing other events when one fails reactively`() {
        // Given
        val event1 = UserAuthenticatedEvent("user1", "session1", "192.168.1.1", "Mozilla/5.0")
        val event2 = UserLoggedOutEvent("user1", "session1")
        val events = listOf(event1, event2)

        every { responseSpec.bodyToMono(Void::class.java) } returnsMany listOf(
            Mono.error(WebClientResponseException.create(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "Service unavailable",
                null, null, null
            )),
            Mono.empty<Void>()
        )

        // When & Then
        StepVerifier.create(eventPublisherAdapter.publishAll(events))
            .verifyComplete()

        verify(exactly = 2) { 
            requestBodyUriSpec.uri("$auditServiceUrl/api/audit/events")
        }
    }

    @Test
    fun `should verify event properties are set correctly reactively`() {
        // Given
        val event = UserAuthenticatedEvent(
            username = "testuser",
            sessionIndex = "session-123",
            ipAddress = "192.168.1.1",
            userAgent = "Mozilla/5.0"
        )

        // When & Then
        StepVerifier.create(eventPublisherAdapter.publish(event))
            .verifyComplete()

        assertNotNull(event.eventId)
        assertNotNull(event.timestamp)
        assertEquals("1.0", event.version)
        assertEquals("UserAuthenticatedEvent", event.eventType)
        assertEquals("testuser", event.username)
        assertEquals("session-123", event.sessionIndex)
        assertEquals("192.168.1.1", event.ipAddress)
        assertEquals("Mozilla/5.0", event.userAgent)
    }

    @Test
    fun `should handle reactive composition of multiple event publications`() {
        // Given
        val event1 = UserAuthenticatedEvent("user1", "session1", "192.168.1.1", "Mozilla/5.0")
        val event2 = UserLoggedOutEvent("user1", "session1")

        // When & Then
        StepVerifier.create(
            eventPublisherAdapter.publish(event1)
                .then(eventPublisherAdapter.publish(event2))
        )
            .verifyComplete()

        verify(exactly = 2) { 
            requestBodyUriSpec.uri("$auditServiceUrl/api/audit/events")
        }
    }
} 