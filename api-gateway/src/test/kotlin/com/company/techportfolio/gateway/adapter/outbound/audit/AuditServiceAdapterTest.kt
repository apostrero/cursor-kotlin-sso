package com.company.techportfolio.gateway.adapter.outbound.audit

import com.company.techportfolio.gateway.adapter.out.audit.AuditServiceAdapter
import com.company.techportfolio.gateway.domain.port.*
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import java.time.LocalDateTime

/**
 * Unit test class for the AuditServiceAdapter.
 * 
 * This test class verifies the behavior of the AuditServiceAdapter which handles
 * communication with the external audit microservice via REST API calls.
 * It tests audit event logging functionality and error handling.
 * 
 * Test coverage includes:
 * - Authentication event logging to audit service
 * - Authorization event logging for compliance
 * - Token lifecycle event logging
 * - Error handling when audit service is unavailable
 * - REST API communication patterns
 * - Event data serialization and transmission
 * 
 * Testing approach:
 * - Uses MockK for mocking RestTemplate dependencies
 * - Tests successful audit logging scenarios
 * - Verifies error handling and graceful degradation
 * - Validates REST API call parameters and URLs
 * - Ensures audit failures don't impact main operations
 * 
 * @author Technology Portfolio Team
 * @since 1.0.0
 */

class AuditServiceAdapterTest {

    private val restTemplate = mockk<RestTemplate>()
    private lateinit var auditServiceAdapter: AuditServiceAdapter
    private val auditServiceUrl = "http://localhost:8084"

    @BeforeEach
    fun setUp() {
        clearAllMocks()
        auditServiceAdapter = AuditServiceAdapter(restTemplate)
        ReflectionTestUtils.setField(auditServiceAdapter, "auditServiceUrl", auditServiceUrl)
    }

    @Test
    fun `should log authentication event successfully`() {
        // Given
        val event = AuthenticationEvent(
            username = "testuser",
            eventType = AuthenticationEventType.LOGIN_SUCCESS,
            timestamp = LocalDateTime.now(),
            sessionIndex = "session-123",
            ipAddress = "192.168.1.1",
            userAgent = "Mozilla/5.0",
            success = true,
            errorMessage = null
        )
        val expectedUrl = "$auditServiceUrl/api/audit/authentication"

        every { restTemplate.postForObject(expectedUrl, event, Unit::class.java) } returns Unit

        // When
        auditServiceAdapter.logAuthenticationEvent(event)

        // Then
        verify(exactly = 1) { restTemplate.postForObject(expectedUrl, event, Unit::class.java) }
    }

    @Test
    fun `should handle authentication event logging failure gracefully`() {
        // Given
        val event = AuthenticationEvent(
            username = "testuser",
            eventType = AuthenticationEventType.LOGIN_FAILURE,
            timestamp = LocalDateTime.now(),
            success = false,
            errorMessage = "Invalid credentials"
        )
        val expectedUrl = "$auditServiceUrl/api/audit/authentication"

        every { restTemplate.postForObject(expectedUrl, event, Unit::class.java) } throws RestClientException("Service unavailable")

        // When
        auditServiceAdapter.logAuthenticationEvent(event)

        // Then
        verify(exactly = 1) { restTemplate.postForObject(expectedUrl, event, Unit::class.java) }
    }

    @Test
    fun `should log authorization event successfully`() {
        // Given
        val event = AuthorizationEvent(
            username = "testuser",
            resource = "portfolio",
            action = "read",
            timestamp = LocalDateTime.now(),
            authorized = true,
            permissions = listOf("READ_PORTFOLIO"),
            errorMessage = null
        )
        val expectedUrl = "$auditServiceUrl/api/audit/authorization"

        every { restTemplate.postForObject(expectedUrl, event, Unit::class.java) } returns Unit

        // When
        auditServiceAdapter.logAuthorizationEvent(event)

        // Then
        verify(exactly = 1) { restTemplate.postForObject(expectedUrl, event, Unit::class.java) }
    }

    @Test
    fun `should log token event successfully`() {
        // Given
        val event = TokenEvent(
            username = "testuser",
            eventType = TokenEventType.TOKEN_GENERATED,
            timestamp = LocalDateTime.now(),
            tokenId = "token-123",
            sessionIndex = "session-123"
        )
        val expectedUrl = "$auditServiceUrl/api/audit/token"

        every { restTemplate.postForObject(expectedUrl, event, Unit::class.java) } returns Unit

        // When
        auditServiceAdapter.logTokenEvent(event)

        // Then
        verify(exactly = 1) { restTemplate.postForObject(expectedUrl, event, Unit::class.java) }
    }

    @Test
    fun `should handle different authentication event types`() {
        // Given
        val events = listOf(
            AuthenticationEvent("user1", AuthenticationEventType.LOGIN_SUCCESS, success = true),
            AuthenticationEvent("user2", AuthenticationEventType.LOGIN_FAILURE, success = false),
            AuthenticationEvent("user3", AuthenticationEventType.LOGOUT, success = true),
            AuthenticationEvent("user4", AuthenticationEventType.TOKEN_REFRESH, success = true),
            AuthenticationEvent("user5", AuthenticationEventType.TOKEN_EXPIRED, success = false)
        )
        val expectedUrl = "$auditServiceUrl/api/audit/authentication"

        every { restTemplate.postForObject(expectedUrl, any<AuthenticationEvent>(), Unit::class.java) } returns Unit

        // When
        events.forEach { auditServiceAdapter.logAuthenticationEvent(it) }

        // Then
        verify(exactly = 5) { restTemplate.postForObject(expectedUrl, any<AuthenticationEvent>(), Unit::class.java) }
    }

    @Test
    fun `should handle different token event types`() {
        // Given
        val events = listOf(
            TokenEvent("user1", TokenEventType.TOKEN_GENERATED),
            TokenEvent("user2", TokenEventType.TOKEN_VALIDATED),
            TokenEvent("user3", TokenEventType.TOKEN_REFRESHED),
            TokenEvent("user4", TokenEventType.TOKEN_EXPIRED),
            TokenEvent("user5", TokenEventType.TOKEN_INVALID)
        )
        val expectedUrl = "$auditServiceUrl/api/audit/token"

        every { restTemplate.postForObject(expectedUrl, any<TokenEvent>(), Unit::class.java) } returns Unit

        // When
        events.forEach { auditServiceAdapter.logTokenEvent(it) }

        // Then
        verify(exactly = 5) { restTemplate.postForObject(expectedUrl, any<TokenEvent>(), Unit::class.java) }
    }
} 