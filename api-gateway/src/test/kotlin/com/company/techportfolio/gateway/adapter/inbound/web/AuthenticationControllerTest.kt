package com.company.techportfolio.gateway.adapter.inbound.web

import com.company.techportfolio.gateway.domain.model.AuthenticationResult
import com.company.techportfolio.gateway.domain.model.TokenValidationResult
import com.company.techportfolio.gateway.domain.port.AuthorizationResult
import com.company.techportfolio.gateway.domain.service.AuthenticationService
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import reactor.test.StepVerifier
import java.time.LocalDateTime

/**
 * Unit test class for the AuthenticationController (REACTIVE).
 *
 * This test class verifies the behavior of the reactive AuthenticationController REST endpoints
 * using MockK for mocking dependencies and StepVerifier for testing reactive streams.
 * It tests all HTTP endpoints including authentication, token validation, token refresh,
 * authorization, and health checks.
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
 * - Uses StepVerifier for testing reactive Mono<ResponseEntity<T>> returns
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
        val responseMono = authenticationController.authenticateUser(mockAuthentication)

        // Then
        StepVerifier.create(responseMono)
            .expectNextMatches { response ->
                response.statusCode == HttpStatus.OK &&
                        response.body == authResult &&
                        response.body!!.isAuthenticated &&
                        response.body!!.username == "testuser" &&
                        response.body!!.token == "jwt-token-123"
            }
            .verifyComplete()

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
        val responseMono = authenticationController.authenticateUser(mockAuthentication)

        // Then
        StepVerifier.create(responseMono)
            .expectNextMatches { response ->
                response.statusCode == HttpStatus.BAD_REQUEST &&
                        response.body == authResult &&
                        !response.body!!.isAuthenticated &&
                        response.body!!.errorMessage == "Authentication failed"
            }
            .verifyComplete()

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
        val responseMono = authenticationController.validateToken(authorization)

        // Then
        StepVerifier.create(responseMono)
            .expectNextMatches { response ->
                response.statusCode == HttpStatus.OK &&
                        response.body == validationResult &&
                        response.body!!.isValid &&
                        response.body!!.username == "testuser"
            }
            .verifyComplete()

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
        val responseMono = authenticationController.validateToken(authorization)

        // Then
        StepVerifier.create(responseMono)
            .expectNextMatches { response ->
                response.statusCode == HttpStatus.UNAUTHORIZED &&
                        response.body == validationResult &&
                        !response.body!!.isValid &&
                        response.body!!.errorMessage == "Invalid token signature"
            }
            .verifyComplete()

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
        val responseMono = authenticationController.validateToken(authorization)

        // Then
        StepVerifier.create(responseMono)
            .expectNextMatches { response ->
                response.statusCode == HttpStatus.OK &&
                        response.body!!.isValid
            }
            .verifyComplete()

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
        val responseMono = authenticationController.refreshToken(authorization)

        // Then
        StepVerifier.create(responseMono)
            .expectNextMatches { response ->
                response.statusCode == HttpStatus.OK &&
                        response.body == mapOf("token" to newToken)
            }
            .verifyComplete()

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
        val responseMono = authenticationController.refreshToken(authorization)

        // Then
        StepVerifier.create(responseMono)
            .expectNextMatches { response ->
                response.statusCode == HttpStatus.UNAUTHORIZED &&
                        response.body == mapOf("error" to "Token refresh failed")
            }
            .verifyComplete()

        verify(exactly = 1) { authenticationService.refreshToken(expiredToken) }
    }

    /**
     * Tests successful user authorization.
     *
     * Verifies that the controller correctly authorizes users for specific
     * resource-action combinations and returns appropriate results.
     *
     * Expected behavior:
     * - Returns HTTP 200 OK for authorized users
     * - Returns authorization result with user details
     * - Calls authorization service with correct parameters
     */
    @Test
    fun `should authorize user successfully`() {
        // Given
        val username = "testuser"
        val resource = "portfolios"
        val action = "read"
        val authResult = AuthorizationResult.authorized(
            username = username,
            resource = resource,
            action = action,
            permissions = listOf("portfolios:read")
        )

        every { authenticationService.authorizeUser(username, resource, action) } returns authResult

        // When
        val responseMono = authenticationController.authorizeUser(username, resource, action)

        // Then
        StepVerifier.create(responseMono)
            .expectNextMatches { response ->
                response.statusCode == HttpStatus.OK &&
                        response.body == authResult &&
                        response.body!!.isAuthorized &&
                        response.body!!.username == username &&
                        response.body!!.resource == resource &&
                        response.body!!.action == action
            }
            .verifyComplete()

        verify(exactly = 1) { authenticationService.authorizeUser(username, resource, action) }
    }

    /**
     * Tests authorization failure scenarios.
     *
     * Verifies that the controller returns HTTP 403 Forbidden when
     * authorization fails due to insufficient permissions or roles.
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
        val resource = "admin"
        val action = "delete"
        val authResult = AuthorizationResult.unauthorized(
            username = username,
            resource = resource,
            action = action,
            errorMessage = "Insufficient permissions"
        )

        every { authenticationService.authorizeUser(username, resource, action) } returns authResult

        // When
        val responseMono = authenticationController.authorizeUser(username, resource, action)

        // Then
        StepVerifier.create(responseMono)
            .expectNextMatches { response ->
                response.statusCode == HttpStatus.FORBIDDEN &&
                        response.body == authResult &&
                        !response.body!!.isAuthorized &&
                        response.body!!.errorMessage == "Insufficient permissions"
            }
            .verifyComplete()

        verify(exactly = 1) { authenticationService.authorizeUser(username, resource, action) }
    }

    /**
     * Tests health check endpoint functionality.
     *
     * Verifies that the health check endpoint returns proper service
     * status information for monitoring and load balancing purposes.
     *
     * Expected behavior:
     * - Returns HTTP 200 OK status
     * - Returns health status map with service information
     * - Indicates service is UP and running
     */
    @Test
    fun `should return health status`() {
        // When
        val responseMono = authenticationController.health()

        // Then
        StepVerifier.create(responseMono)
            .expectNextMatches { response ->
                response.statusCode == HttpStatus.OK &&
                        response.body == mapOf("status" to "UP", "service" to "authentication")
            }
            .verifyComplete()
    }
} 