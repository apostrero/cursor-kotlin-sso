package com.company.techportfolio.gateway.adapter.inbound.web

import com.company.techportfolio.gateway.domain.model.AuthenticationResult
import com.company.techportfolio.gateway.domain.model.TokenValidationResult
import com.company.techportfolio.gateway.domain.port.AuthorizationResult
import com.company.techportfolio.gateway.domain.service.AuthenticationService
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import java.time.LocalDateTime

class AuthenticationControllerTest {

    private val authenticationService = mockk<AuthenticationService>()
    private lateinit var authenticationController: AuthenticationController

    @BeforeEach
    fun setUp() {
        clearAllMocks()
        authenticationController = AuthenticationController(authenticationService)
    }

    @Test
    fun `should authenticate user successfully`() {
        // Given
        val mockAuthentication = mockk<Authentication>()
        val authResult = AuthenticationResult.success(
            username = "testuser",
            authorities = listOf("ROLE_USER"),
            token = "jwt-token-123",
            sessionIndex = "session-123",
            expiresAt = LocalDateTime.now().plusHours(1)
        )

        every { authenticationService.authenticateUser(mockAuthentication) } returns authResult

        // When
        val response = authenticationController.authenticateUser(mockAuthentication)

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(authResult, response.body)
        assertTrue(response.body!!.isAuthenticated)
        assertEquals("testuser", response.body!!.username)
        assertEquals("jwt-token-123", response.body!!.token)

        verify(exactly = 1) { authenticationService.authenticateUser(mockAuthentication) }
    }

    @Test
    fun `should return bad request when authentication fails`() {
        // Given
        val mockAuthentication = mockk<Authentication>()
        val authResult = AuthenticationResult.failure("Authentication failed")

        every { authenticationService.authenticateUser(mockAuthentication) } returns authResult

        // When
        val response = authenticationController.authenticateUser(mockAuthentication)

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals(authResult, response.body)
        assertFalse(response.body!!.isAuthenticated)
        assertEquals("Authentication failed", response.body!!.errorMessage)

        verify(exactly = 1) { authenticationService.authenticateUser(mockAuthentication) }
    }

    @Test
    fun `should validate token successfully`() {
        // Given
        val authorization = "Bearer jwt-token-123"
        val token = "jwt-token-123"
        val validationResult = TokenValidationResult.valid(
            username = "testuser",
            authorities = listOf("ROLE_USER"),
            sessionIndex = "session-123",
            issuedAt = LocalDateTime.now().minusMinutes(30),
            expiresAt = LocalDateTime.now().plusMinutes(30)
        )

        every { authenticationService.validateToken(token) } returns validationResult

        // When
        val response = authenticationController.validateToken(authorization)

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(validationResult, response.body)
        assertTrue(response.body!!.isValid)
        assertEquals("testuser", response.body!!.username)

        verify(exactly = 1) { authenticationService.validateToken(token) }
    }

    @Test
    fun `should return unauthorized when token validation fails`() {
        // Given
        val authorization = "Bearer invalid-token"
        val token = "invalid-token"
        val validationResult = TokenValidationResult.invalid("Invalid token signature")

        every { authenticationService.validateToken(token) } returns validationResult

        // When
        val response = authenticationController.validateToken(authorization)

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)
        assertEquals(validationResult, response.body)
        assertFalse(response.body!!.isValid)
        assertEquals("Invalid token signature", response.body!!.errorMessage)

        verify(exactly = 1) { authenticationService.validateToken(token) }
    }

    @Test
    fun `should handle authorization header without Bearer prefix`() {
        // Given
        val authorization = "jwt-token-123" // No "Bearer " prefix
        val token = "jwt-token-123"
        val validationResult = TokenValidationResult.valid(
            username = "testuser",
            authorities = listOf("ROLE_USER"),
            sessionIndex = "session-123",
            issuedAt = LocalDateTime.now().minusMinutes(30),
            expiresAt = LocalDateTime.now().plusMinutes(30)
        )

        every { authenticationService.validateToken(token) } returns validationResult

        // When
        val response = authenticationController.validateToken(authorization)

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertTrue(response.body!!.isValid)

        verify(exactly = 1) { authenticationService.validateToken(token) }
    }

    @Test
    fun `should refresh token successfully`() {
        // Given
        val authorization = "Bearer old-jwt-token"
        val oldToken = "old-jwt-token"
        val newToken = "new-jwt-token"

        every { authenticationService.refreshToken(oldToken) } returns newToken

        // When
        val response = authenticationController.refreshToken(authorization)

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(mapOf("token" to newToken), response.body)

        verify(exactly = 1) { authenticationService.refreshToken(oldToken) }
    }

    @Test
    fun `should return unauthorized when token refresh fails`() {
        // Given
        val authorization = "Bearer expired-token"
        val expiredToken = "expired-token"

        every { authenticationService.refreshToken(expiredToken) } returns null

        // When
        val response = authenticationController.refreshToken(authorization)

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)
        assertEquals(mapOf("error" to "Token refresh failed"), response.body)

        verify(exactly = 1) { authenticationService.refreshToken(expiredToken) }
    }

    @Test
    fun `should authorize user successfully`() {
        // Given
        val username = "testuser"
        val resource = "portfolio"
        val action = "read"
        val authorizationResult = AuthorizationResult.authorized(
            username = username,
            resource = resource,
            action = action,
            permissions = listOf("READ_PORTFOLIO")
        )

        every { authenticationService.authorizeUser(username, resource, action) } returns authorizationResult

        // When
        val response = authenticationController.authorizeUser(username, resource, action)

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(authorizationResult, response.body)
        assertTrue(response.body!!.isAuthorized)
        assertEquals(username, response.body!!.username)
        assertEquals(resource, response.body!!.resource)
        assertEquals(action, response.body!!.action)

        verify(exactly = 1) { authenticationService.authorizeUser(username, resource, action) }
    }

    @Test
    fun `should return forbidden when authorization fails`() {
        // Given
        val username = "testuser"
        val resource = "portfolio"
        val action = "delete"
        val authorizationResult = AuthorizationResult.unauthorized(
            username = username,
            resource = resource,
            action = action,
            errorMessage = "Insufficient permissions"
        )

        every { authenticationService.authorizeUser(username, resource, action) } returns authorizationResult

        // When
        val response = authenticationController.authorizeUser(username, resource, action)

        // Then
        assertEquals(HttpStatus.FORBIDDEN, response.statusCode)
        assertEquals(authorizationResult, response.body)
        assertFalse(response.body!!.isAuthorized)
        assertEquals("Insufficient permissions", response.body!!.errorMessage)

        verify(exactly = 1) { authenticationService.authorizeUser(username, resource, action) }
    }

    @Test
    fun `should return health check status`() {
        // When
        val response = authenticationController.health()

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(mapOf("status" to "UP", "service" to "authentication"), response.body)
    }

    @Test
    fun `should handle empty authorization header for token validation`() {
        // Given
        val authorization = "Bearer " // Empty token after prefix
        val token = ""
        val validationResult = TokenValidationResult.invalid("Empty token")

        every { authenticationService.validateToken(token) } returns validationResult

        // When
        val response = authenticationController.validateToken(authorization)

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)
        assertFalse(response.body!!.isValid)

        verify(exactly = 1) { authenticationService.validateToken(token) }
    }

    @Test
    fun `should handle empty authorization header for token refresh`() {
        // Given
        val authorization = "Bearer " // Empty token after prefix
        val token = ""

        every { authenticationService.refreshToken(token) } returns null

        // When
        val response = authenticationController.refreshToken(authorization)

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)
        assertEquals(mapOf("error" to "Token refresh failed"), response.body)

        verify(exactly = 1) { authenticationService.refreshToken(token) }
    }

    @Test
    fun `should handle authorization with empty parameters`() {
        // Given
        val username = ""
        val resource = ""
        val action = ""
        val authorizationResult = AuthorizationResult.unauthorized(
            username = username,
            resource = resource,
            action = action,
            errorMessage = "Invalid parameters"
        )

        every { authenticationService.authorizeUser(username, resource, action) } returns authorizationResult

        // When
        val response = authenticationController.authorizeUser(username, resource, action)

        // Then
        assertEquals(HttpStatus.FORBIDDEN, response.statusCode)
        assertFalse(response.body!!.isAuthorized)

        verify(exactly = 1) { authenticationService.authorizeUser(username, resource, action) }
    }

    @Test
    fun `should handle special characters in authorization parameters`() {
        // Given
        val username = "test@user.com"
        val resource = "portfolio/sub-resource"
        val action = "read:write"
        val authorizationResult = AuthorizationResult.authorized(
            username = username,
            resource = resource,
            action = action,
            permissions = listOf("READ_PORTFOLIO", "WRITE_PORTFOLIO")
        )

        every { authenticationService.authorizeUser(username, resource, action) } returns authorizationResult

        // When
        val response = authenticationController.authorizeUser(username, resource, action)

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertTrue(response.body!!.isAuthorized)
        assertEquals(username, response.body!!.username)
        assertEquals(resource, response.body!!.resource)
        assertEquals(action, response.body!!.action)

        verify(exactly = 1) { authenticationService.authorizeUser(username, resource, action) }
    }
} 