package com.company.techportfolio.gateway.domain.service

import com.company.techportfolio.gateway.domain.model.AuthenticationResult
import com.company.techportfolio.gateway.domain.model.TokenValidationResult
import com.company.techportfolio.gateway.domain.port.*
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.saml2.provider.service.authentication.Saml2Authentication
import java.time.LocalDateTime

/**
 * Unit test class for the AuthenticationService domain service.
 * 
 * This test class verifies the behavior of the AuthenticationService which orchestrates
 * authentication and authorization operations in the system. It tests the coordination
 * between various ports and the implementation of business logic for authentication flows.
 * 
 * Test coverage includes:
 * - User authentication with SAML integration
 * - JWT token validation and refresh operations
 * - User authorization checks with permission validation
 * - Integration with authentication, authorization, and audit ports
 * - Error handling and edge cases
 * - Business logic validation and flow control
 * 
 * Testing approach:
 * - Uses MockK for mocking all port dependencies
 * - Tests service orchestration and business logic
 * - Verifies port interactions and method calls
 * - Validates error handling and edge cases
 * - Follows Given-When-Then test structure
 * 
 * @author Technology Portfolio Team
 * @since 1.0.0
 */

class AuthenticationServiceTest {

    private val authenticationPort = mockk<AuthenticationPort>()
    private val authorizationPort = mockk<AuthorizationPort>()
    private val auditPort = mockk<AuditPort>()
    
    private lateinit var authenticationService: AuthenticationService

    @BeforeEach
    fun setUp() {
        clearAllMocks()
        authenticationService = AuthenticationService(authenticationPort, authorizationPort, auditPort)
    }

    @Test
    fun `should authenticate user successfully with SAML authentication`() {
        // Given
        val username = "testuser"
        val authorities = listOf("ROLE_USER", "READ_PORTFOLIO")
        val sessionIndex = "session-123"
        val token = "jwt-token-123"
        val permissions = listOf("READ_PORTFOLIO", "WRITE_PORTFOLIO")

        val samlAuth = mockk<Saml2Authentication> {
            every { principal } returns username
            every { getAuthorities() } returns authorities.map { SimpleGrantedAuthority(it) }
            every { credentials } returns sessionIndex
        }

        every { authenticationPort.generateToken(username, authorities, sessionIndex) } returns token
        every { authorizationPort.getUserPermissions(username) } returns permissions
        every { auditPort.logAuthenticationEvent(any()) } just Runs
        every { auditPort.logTokenEvent(any()) } just Runs

        // When
        val result = authenticationService.authenticateUser(samlAuth)

        // Then
        assertTrue(result.isAuthenticated)
        assertEquals(username, result.username)
        assertEquals(authorities, result.authorities)
        assertEquals(token, result.token)
        assertEquals(sessionIndex, result.sessionIndex)
        assertNotNull(result.expiresAt)
        assertNull(result.errorMessage)

        verify(exactly = 1) { authenticationPort.generateToken(username, authorities, sessionIndex) }
        verify(exactly = 1) { authorizationPort.getUserPermissions(username) }
        verify(exactly = 1) { 
            auditPort.logAuthenticationEvent(
                match { event ->
                    event.username == username &&
                    event.eventType == AuthenticationEventType.LOGIN_SUCCESS &&
                    event.sessionIndex == sessionIndex &&
                    event.success
                }
            )
        }
        verify(exactly = 1) {
            auditPort.logTokenEvent(
                match { event ->
                    event.username == username &&
                    event.eventType == TokenEventType.TOKEN_GENERATED &&
                    event.sessionIndex == sessionIndex
                }
            )
        }
    }

    @Test
    fun `should handle authentication failure when exception occurs`() {
        // Given
        val samlAuth = mockk<Saml2Authentication> {
            every { principal } throws RuntimeException("SAML authentication failed")
        }

        every { auditPort.logAuthenticationEvent(any()) } just Runs

        // When
        val result = authenticationService.authenticateUser(samlAuth)

        // Then
        assertFalse(result.isAuthenticated)
        assertNull(result.username)
        assertEquals(emptyList<String>(), result.authorities)
        assertNull(result.token)
        assertNull(result.sessionIndex)
        assertNull(result.expiresAt)
        assertTrue(result.errorMessage!!.contains("Authentication failed"))

        verify(exactly = 1) {
            auditPort.logAuthenticationEvent(
                match { event ->
                    event.username == "unknown" &&
                    event.eventType == AuthenticationEventType.LOGIN_FAILURE &&
                    !event.success &&
                    event.errorMessage != null
                }
            )
        }
        verify(exactly = 0) { authenticationPort.generateToken(any(), any(), any()) }
        verify(exactly = 0) { authorizationPort.getUserPermissions(any()) }
    }

    @Test
    fun `should validate token successfully`() {
        // Given
        val token = "valid-jwt-token"
        val username = "testuser"
        val authorities = listOf("ROLE_USER")
        val sessionIndex = "session-123"
        val validationResult = TokenValidationResult.valid(
            username = username,
            authorities = authorities,
            sessionIndex = sessionIndex,
            issuedAt = LocalDateTime.now().minusMinutes(30),
            expiresAt = LocalDateTime.now().plusMinutes(30)
        )

        every { authenticationPort.validateToken(token) } returns validationResult
        every { auditPort.logTokenEvent(any()) } just Runs

        // When
        val result = authenticationService.validateToken(token)

        // Then
        assertTrue(result.isValid)
        assertEquals(username, result.username)
        assertEquals(authorities, result.authorities)
        assertEquals(sessionIndex, result.sessionIndex)

        verify(exactly = 1) { authenticationPort.validateToken(token) }
        verify(exactly = 1) {
            auditPort.logTokenEvent(
                match { event ->
                    event.username == username &&
                    event.eventType == TokenEventType.TOKEN_VALIDATED &&
                    event.sessionIndex == sessionIndex
                }
            )
        }
    }

    @Test
    fun `should handle invalid token validation`() {
        // Given
        val token = "invalid-jwt-token"
        val validationResult = TokenValidationResult.invalid("Invalid token signature")

        every { authenticationPort.validateToken(token) } returns validationResult
        every { auditPort.logTokenEvent(any()) } just Runs

        // When
        val result = authenticationService.validateToken(token)

        // Then
        assertFalse(result.isValid)
        assertEquals("Invalid token signature", result.errorMessage)

        verify(exactly = 1) { authenticationPort.validateToken(token) }
        verify(exactly = 1) {
            auditPort.logTokenEvent(
                match { event ->
                    event.username == "unknown" &&
                    event.eventType == TokenEventType.TOKEN_INVALID
                }
            )
        }
    }

    /**
     * Tests token validation exception handling.
     * 
     * Verifies that the service gracefully handles exceptions during token
     * validation by returning an invalid result and logging appropriate events.
     * 
     * Expected behavior:
     * - Returns invalid TokenValidationResult on exception
     * - Logs token validation failure event
     * - Provides appropriate error message
     * - Handles authentication port failures gracefully
     */
    @Test
    fun `should handle token validation exception`() {
        // Given
        val token = "problematic-token"

        every { authenticationPort.validateToken(token) } throws RuntimeException("Token parsing failed")
        every { auditPort.logTokenEvent(any()) } just Runs

        // When
        val result = authenticationService.validateToken(token)

        // Then
        assertFalse(result.isValid)
        assertTrue(result.errorMessage!!.contains("Token validation failed"))

        verify(exactly = 1) { authenticationPort.validateToken(token) }
        verify(exactly = 1) {
            auditPort.logTokenEvent(
                match { event ->
                    event.username == "unknown" &&
                    event.eventType == TokenEventType.TOKEN_INVALID
                }
            )
        }
    }

    /**
     * Tests successful token refresh operation.
     * 
     * Verifies that the service can refresh tokens by coordinating with the
     * authentication port and logging appropriate audit events.
     * 
     * Expected behavior:
     * - Extracts user information from original token
     * - Calls authentication port to refresh token
     * - Returns new token on success
     * - Logs token refresh event with user context
     */
    @Test
    fun `should refresh token successfully`() {
        // Given
        val token = "old-jwt-token"
        val refreshedToken = "new-jwt-token"
        val username = "testuser"
        val sessionIndex = "ROLE_USER"

        every { authenticationPort.extractUsernameFromToken(token) } returns username
        every { authenticationPort.extractAuthoritiesFromToken(token) } returns listOf(sessionIndex)
        every { authenticationPort.refreshToken(token) } returns refreshedToken
        every { auditPort.logTokenEvent(any()) } just Runs

        // When
        val result = authenticationService.refreshToken(token)

        // Then
        assertEquals(refreshedToken, result)

        verify(exactly = 1) { authenticationPort.extractUsernameFromToken(token) }
        verify(exactly = 1) { authenticationPort.extractAuthoritiesFromToken(token) }
        verify(exactly = 1) { authenticationPort.refreshToken(token) }
        verify(exactly = 1) {
            auditPort.logTokenEvent(
                match { event ->
                    event.username == username &&
                    event.eventType == TokenEventType.TOKEN_REFRESHED &&
                    event.sessionIndex == sessionIndex
                }
            )
        }
    }

    /**
     * Tests token refresh failure when username extraction fails.
     * 
     * Verifies that the service handles cases where username cannot be
     * extracted from the token during refresh operation.
     * 
     * Expected behavior:
     * - Returns null when username extraction fails
     * - Does not log successful refresh event
     * - Handles extraction failure gracefully
     * - Prevents refresh without user context
     */
    @Test
    fun `should handle token refresh failure when username extraction fails`() {
        // Given
        val token = "problematic-token"

        every { authenticationPort.extractUsernameFromToken(token) } returns null
        every { authenticationPort.extractAuthoritiesFromToken(token) } returns listOf("ROLE_USER")
        every { authenticationPort.refreshToken(token) } returns "new-token"
        every { auditPort.logTokenEvent(any()) } just Runs

        // When
        val result = authenticationService.refreshToken(token)

        // Then
        assertNull(result)

        verify(exactly = 1) { authenticationPort.extractUsernameFromToken(token) }
        verify(exactly = 1) { authenticationPort.extractAuthoritiesFromToken(token) }
        verify(exactly = 1) { authenticationPort.refreshToken(token) }
        verify(exactly = 0) {
            auditPort.logTokenEvent(
                match { event -> event.eventType == TokenEventType.TOKEN_REFRESHED }
            )
        }
    }

    /**
     * Tests token refresh failure when refresh operation returns null.
     * 
     * Verifies that the service handles cases where the authentication port
     * cannot refresh the token (e.g., token is too old or invalid).
     * 
     * Expected behavior:
     * - Returns null when refresh operation fails
     * - Does not log successful refresh event
     * - Handles authentication port failure gracefully
     * - Supports token lifecycle management
     */
    @Test
    fun `should handle token refresh failure when refresh returns null`() {
        // Given
        val token = "expired-token"
        val username = "testuser"

        every { authenticationPort.extractUsernameFromToken(token) } returns username
        every { authenticationPort.extractAuthoritiesFromToken(token) } returns listOf("ROLE_USER")
        every { authenticationPort.refreshToken(token) } returns null
        every { auditPort.logTokenEvent(any()) } just Runs

        // When
        val result = authenticationService.refreshToken(token)

        // Then
        assertNull(result)

        verify(exactly = 0) {
            auditPort.logTokenEvent(
                match { event -> event.eventType == TokenEventType.TOKEN_REFRESHED }
            )
        }
    }

    /**
     * Tests token refresh exception handling.
     * 
     * Verifies that the service gracefully handles exceptions during token
     * refresh operations by returning null and logging appropriate events.
     * 
     * Expected behavior:
     * - Returns null on exception during refresh
     * - Logs token invalid event
     * - Handles authentication port exceptions gracefully
     * - Provides safe fallback behavior
     */
    @Test
    fun `should handle token refresh exception`() {
        // Given
        val token = "problematic-token"

        every { authenticationPort.extractUsernameFromToken(token) } throws RuntimeException("Token extraction failed")
        every { auditPort.logTokenEvent(any()) } just Runs

        // When
        val result = authenticationService.refreshToken(token)

        // Then
        assertNull(result)

        verify(exactly = 1) {
            auditPort.logTokenEvent(
                match { event ->
                    event.username == "unknown" &&
                    event.eventType == TokenEventType.TOKEN_INVALID
                }
            )
        }
    }

    /**
     * Tests successful user authorization operation.
     * 
     * Verifies that the service can authorize users by coordinating with the
     * authorization port and logging appropriate audit events.
     * 
     * Expected behavior:
     * - Calls authorization port for user authorization
     * - Returns authorization result with permissions
     * - Logs authorization event with full context
     * - Supports role-based access control
     */
    @Test
    fun `should authorize user successfully`() {
        // Given
        val username = "testuser"
        val resource = "portfolio"
        val action = "read"
        val permissions = listOf("READ_PORTFOLIO")
        val authorizationResult = AuthorizationResult.authorized(username, resource, action, permissions)

        every { authorizationPort.authorizeUser(username, resource, action) } returns authorizationResult
        every { auditPort.logAuthorizationEvent(any()) } just Runs

        // When
        val result = authenticationService.authorizeUser(username, resource, action)

        // Then
        assertTrue(result.isAuthorized)
        assertEquals(username, result.username)
        assertEquals(resource, result.resource)
        assertEquals(action, result.action)
        assertEquals(permissions, result.permissions)

        verify(exactly = 1) { authorizationPort.authorizeUser(username, resource, action) }
        verify(exactly = 1) {
            auditPort.logAuthorizationEvent(
                match { event ->
                    event.username == username &&
                    event.resource == resource &&
                    event.action == action &&
                    event.authorized &&
                    event.permissions == permissions
                }
            )
        }
    }

    /**
     * Tests user authorization failure handling.
     * 
     * Verifies that the service properly handles authorization failures
     * by returning unauthorized results and logging appropriate events.
     * 
     * Expected behavior:
     * - Returns unauthorized result with error message
     * - Logs authorization failure event
     * - Preserves authorization context information
     * - Supports access control auditing
     */
    @Test
    fun `should handle authorization failure`() {
        // Given
        val username = "testuser"
        val resource = "portfolio"
        val action = "delete"
        val errorMessage = "Insufficient permissions"
        val authorizationResult = AuthorizationResult.unauthorized(username, resource, action, errorMessage)

        every { authorizationPort.authorizeUser(username, resource, action) } returns authorizationResult
        every { auditPort.logAuthorizationEvent(any()) } just Runs

        // When
        val result = authenticationService.authorizeUser(username, resource, action)

        // Then
        assertFalse(result.isAuthorized)
        assertEquals(username, result.username)
        assertEquals(resource, result.resource)
        assertEquals(action, result.action)
        assertEquals(errorMessage, result.errorMessage)

        verify(exactly = 1) { authorizationPort.authorizeUser(username, resource, action) }
        verify(exactly = 1) {
            auditPort.logAuthorizationEvent(
                match { event ->
                    event.username == username &&
                    event.resource == resource &&
                    event.action == action &&
                    !event.authorized &&
                    event.errorMessage == errorMessage
                }
            )
        }
    }

    /**
     * Tests authorization exception handling.
     * 
     * Verifies that the service gracefully handles exceptions during
     * authorization operations by returning unauthorized results and
     * logging appropriate events.
     * 
     * Expected behavior:
     * - Returns unauthorized result on exception
     * - Logs authorization failure event
     * - Provides appropriate error message
     * - Handles authorization port failures gracefully
     */
    @Test
    fun `should handle authorization exception`() {
        // Given
        val username = "testuser"
        val resource = "portfolio"
        val action = "read"

        every { authorizationPort.authorizeUser(username, resource, action) } throws RuntimeException("Authorization service unavailable")
        every { auditPort.logAuthorizationEvent(any()) } just Runs

        // When
        val result = authenticationService.authorizeUser(username, resource, action)

        // Then
        assertFalse(result.isAuthorized)
        assertEquals(username, result.username)
        assertEquals(resource, result.resource)
        assertEquals(action, result.action)
        assertTrue(result.errorMessage!!.contains("Authorization failed"))

        verify(exactly = 1) { authorizationPort.authorizeUser(username, resource, action) }
        verify(exactly = 1) {
            auditPort.logAuthorizationEvent(
                match { event ->
                    event.username == username &&
                    event.resource == resource &&
                    event.action == action &&
                    !event.authorized &&
                    event.errorMessage != null
                }
            )
        }
    }

    /**
     * Tests handling of non-SAML authentication gracefully.
     * 
     * Verifies that the service properly handles authentication objects
     * that are not SAML-based by returning authentication failures.
     * 
     * Expected behavior:
     * - Returns authentication failure for non-SAML auth
     * - Logs authentication failure event
     * - Provides appropriate error message
     * - Supports authentication type validation
     */
    @Test
    fun `should handle non-SAML authentication gracefully`() {
        // Given
        val nonSamlAuth = mockk<Authentication> {
            every { principal } returns "testuser"
        }

        every { auditPort.logAuthenticationEvent(any()) } just Runs

        // When
        val result = authenticationService.authenticateUser(nonSamlAuth)

        // Then
        assertFalse(result.isAuthenticated)
        assertTrue(result.errorMessage!!.contains("Authentication failed"))

        verify(exactly = 1) {
            auditPort.logAuthenticationEvent(
                match { event ->
                    event.username == "unknown" &&
                    event.eventType == AuthenticationEventType.LOGIN_FAILURE &&
                    !event.success
                }
            )
        }
    }
} 