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

/**
 * Unit test class for the AuthenticationController.
 * 
 * This test class verifies the behavior of the AuthenticationController REST endpoints
 * using MockK for mocking dependencies. It tests all HTTP endpoints including
 * authentication, token validation, token refresh, authorization, and health checks.
 * 
 * Test coverage includes:
 * - Successful authentication scenarios
 * - Authentication failure scenarios
 * - Token validation (valid and invalid tokens)
 * - Token refresh operations
 * - User authorization checks
 * - Health check endpoint
 * - Edge cases and error handling
 * 
 * Testing approach:
 * - Uses MockK for mocking the AuthenticationService
 * - Follows Given-When-Then test structure
 * - Verifies HTTP status codes and response bodies
 * - Validates service method interactions
 * 
 * @author Technology Portfolio Team
 * @since 1.0.0
 */
class AuthenticationControllerTest {

    private val authenticationService = mockk<AuthenticationService>()
    private lateinit var authenticationController: AuthenticationController

    /**
     * Sets up test fixtures before each test method.
     * 
     * Initializes the AuthenticationController with a mocked AuthenticationService
     * and clears all mocks to ensure test isolation.
     */
    @BeforeEach
    fun setUp() {
        clearAllMocks()
        authenticationController = AuthenticationController(authenticationService)
    }

    /**
     * Tests successful user authentication with valid credentials.
     * 
     * Verifies that the controller returns HTTP 200 OK with authentication
     * result when the authentication service successfully authenticates a user.
     * 
     * Expected behavior:
     * - Returns HTTP 200 OK status
     * - Returns authentication result with user details and JWT token
     * - Calls authentication service exactly once
     */
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

    /**
     * Tests authentication failure with invalid credentials.
     * 
     * Verifies that the controller returns HTTP 400 Bad Request when
     * authentication fails due to invalid credentials or other errors.
     * 
     * Expected behavior:
     * - Returns HTTP 400 Bad Request status
     * - Returns authentication result with error message
     * - Indicates authentication failure in response
     */
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

    /**
     * Tests successful JWT token validation.
     * 
     * Verifies that the controller validates JWT tokens correctly and returns
     * HTTP 200 OK with validation result for valid tokens.
     * 
     * Expected behavior:
     * - Strips "Bearer " prefix from Authorization header
     * - Returns HTTP 200 OK status for valid tokens
     * - Returns validation result with user information
     */
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

    /**
     * Tests JWT token validation failure.
     * 
     * Verifies that the controller returns HTTP 401 Unauthorized when
     * token validation fails due to invalid signature, expiration, or format.
     * 
     * Expected behavior:
     * - Returns HTTP 401 Unauthorized status
     * - Returns validation result with error message
     * - Indicates validation failure in response
     */
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

    /**
     * Tests handling of authorization header without Bearer prefix.
     * 
     * Verifies that the controller can handle authorization headers that
     * don't include the "Bearer " prefix by processing the token directly.
     * 
     * Expected behavior:
     * - Processes token without Bearer prefix
     * - Returns successful validation for valid tokens
     * - Maintains normal validation behavior
     */
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

    /**
     * Tests successful JWT token refresh operation.
     * 
     * Verifies that the controller can refresh valid JWT tokens and return
     * new tokens with extended expiration times.
     * 
     * Expected behavior:
     * - Strips "Bearer " prefix from Authorization header
     * - Returns HTTP 200 OK with new token
     * - Provides token in response body
     */
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

    /**
     * Tests token refresh failure scenarios.
     * 
     * Verifies that the controller returns HTTP 401 Unauthorized when
     * token refresh fails due to expired or invalid tokens.
     * 
     * Expected behavior:
     * - Returns HTTP 401 Unauthorized status
     * - Returns error message in response body
     * - Handles null return from service gracefully
     */
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

    /**
     * Tests successful user authorization for resource access.
     * 
     * Verifies that the controller can authorize users to perform specific
     * actions on resources and return HTTP 200 OK for authorized requests.
     * 
     * Expected behavior:
     * - Returns HTTP 200 OK for authorized users
     * - Returns authorization result with permissions
     * - Validates all authorization parameters
     */
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

    /**
     * Tests authorization failure scenarios.
     * 
     * Verifies that the controller returns HTTP 403 Forbidden when
     * users don't have sufficient permissions for requested actions.
     * 
     * Expected behavior:
     * - Returns HTTP 403 Forbidden status
     * - Returns authorization result with error message
     * - Indicates authorization failure in response
     */
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

    /**
     * Tests the health check endpoint functionality.
     * 
     * Verifies that the health check endpoint returns proper status
     * information for monitoring and load balancer health checks.
     * 
     * Expected behavior:
     * - Returns HTTP 200 OK status
     * - Returns service status and name
     * - No external dependencies required
     */
    @Test
    fun `should return health check status`() {
        // When
        val response = authenticationController.health()

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(mapOf("status" to "UP", "service" to "authentication"), response.body)
    }

    /**
     * Tests handling of empty authorization header for token validation.
     * 
     * Verifies that the controller handles empty tokens gracefully and
     * returns appropriate error responses.
     * 
     * Expected behavior:
     * - Returns HTTP 401 Unauthorized for empty tokens
     * - Handles empty string after Bearer prefix
     * - Provides meaningful error responses
     */
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

    /**
     * Tests handling of empty authorization header for token refresh.
     * 
     * Verifies that the controller handles empty tokens gracefully during
     * refresh operations and returns appropriate error responses.
     * 
     * Expected behavior:
     * - Returns HTTP 401 Unauthorized for empty tokens
     * - Handles null return from service
     * - Provides meaningful error messages
     */
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

    /**
     * Tests authorization with empty parameters.
     * 
     * Verifies that the controller handles empty or null authorization
     * parameters gracefully and returns appropriate error responses.
     * 
     * Expected behavior:
     * - Returns HTTP 403 Forbidden for invalid parameters
     * - Handles empty strings for all parameters
     * - Provides meaningful error responses
     */
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

    /**
     * Tests authorization with special characters in parameters.
     * 
     * Verifies that the controller can handle special characters in
     * authorization parameters such as email addresses and complex resource paths.
     * 
     * Expected behavior:
     * - Handles email addresses as usernames
     * - Processes resource paths with special characters
     * - Supports complex action strings
     * - Returns successful authorization when appropriate
     */
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