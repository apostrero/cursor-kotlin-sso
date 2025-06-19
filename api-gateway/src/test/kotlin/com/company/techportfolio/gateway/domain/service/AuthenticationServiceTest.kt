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