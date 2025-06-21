package com.company.techportfolio.gateway.adapter.inbound.web

import com.company.techportfolio.gateway.domain.port.AuthenticationPort
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.ui.Model
import reactor.test.StepVerifier

/**
 * Unit test class for the MockAuthController (REACTIVE).
 *
 * This test class verifies the behavior of the reactive MockAuthController which provides
 * mock authentication functionality for development and testing environments.
 * It tests web-based login pages, API authentication endpoints, and user management.
 *
 * Test coverage includes:
 * - Mock login page rendering with error/logout parameters
 * - Mock authentication success scenarios
 * - API-based authentication with different user types
 * - JWT token generation and user information handling
 * - Security context management and authentication state
 * - Edge cases and error handling
 *
 * Testing approach:
 * - Uses MockK for mocking dependencies
 * - Uses StepVerifier for testing reactive Mono<ResponseEntity<T>> returns
 * - Follows Given-When-Then test structure
 * - Tests Spring Security context integration
 * - Verifies HTTP status codes and response bodies
 * - Validates service method interactions
 *
 * @author Technology Portfolio Team
 * @since 1.0.0
 */
class MockAuthControllerTest {

    private val authenticationPort = mockk<AuthenticationPort>()
    private lateinit var mockAuthController: MockAuthController

    /**
     * Sets up test fixtures before each test method.
     *
     * Initializes the MockAuthController with a mocked AuthenticationPort
     * and clears all mocks to ensure test isolation.
     */
    @BeforeEach
    fun setUp() {
        clearAllMocks()
        mockAuthController = MockAuthController(authenticationPort)
    }

    /**
     * Tests mock login page rendering without any parameters.
     *
     * Verifies that the controller returns the correct view name and
     * sets appropriate model attributes when no error or logout parameters
     * are provided.
     *
     * Expected behavior:
     * - Returns "mock-login" view name
     * - Sets error attribute to false
     * - Sets logout attribute to false
     */
    @Test
    fun `should return mock login page without parameters`() {
        // Given
        val model = mockk<Model>(relaxed = true)

        // When
        val viewName = mockAuthController.mockLoginPage(null, null, model)

        // Then
        assertEquals("mock-login", viewName)
        verify { model.addAttribute("error", false) }
        verify { model.addAttribute("logout", false) }
    }

    /**
     * Tests mock login page rendering with error parameter.
     *
     * Verifies that the controller correctly handles error parameters
     * and sets the appropriate model attributes for displaying error messages.
     *
     * Expected behavior:
     * - Returns "mock-login" view name
     * - Sets error attribute to true when error parameter is present
     * - Sets logout attribute to false
     */
    @Test
    fun `should return mock login page with error parameter`() {
        // Given
        val model = mockk<Model>(relaxed = true)

        // When
        val viewName = mockAuthController.mockLoginPage("true", null, model)

        // Then
        assertEquals("mock-login", viewName)
        verify { model.addAttribute("error", true) }
        verify { model.addAttribute("logout", false) }
    }

    /**
     * Tests mock login page rendering with logout parameter.
     *
     * Verifies that the controller correctly handles logout parameters
     * and sets the appropriate model attributes for displaying logout confirmation.
     *
     * Expected behavior:
     * - Returns "mock-login" view name
     * - Sets error attribute to false
     * - Sets logout attribute to true when logout parameter is present
     */
    @Test
    fun `should return mock login page with logout parameter`() {
        // Given
        val model = mockk<Model>(relaxed = true)

        // When
        val viewName = mockAuthController.mockLoginPage(null, "true", model)

        // Then
        assertEquals("mock-login", viewName)
        verify { model.addAttribute("error", false) }
        verify { model.addAttribute("logout", true) }
    }

    /**
     * Tests mock login page rendering with both error and logout parameters.
     *
     * Verifies that the controller can handle multiple parameters simultaneously
     * and sets both model attributes correctly.
     *
     * Expected behavior:
     * - Returns "mock-login" view name
     * - Sets error attribute to true
     * - Sets logout attribute to true
     */
    @Test
    fun `should return mock login page with both error and logout parameters`() {
        // Given
        val model = mockk<Model>(relaxed = true)

        // When
        val viewName = mockAuthController.mockLoginPage("true", "true", model)

        // Then
        assertEquals("mock-login", viewName)
        verify { model.addAttribute("error", true) }
        verify { model.addAttribute("logout", true) }
    }

    /**
     * Tests successful mock authentication with authenticated user in security context.
     *
     * Verifies that the controller can extract user information from Spring Security
     * context, generate JWT tokens, and return comprehensive user details.
     *
     * Expected behavior:
     * - Returns HTTP 200 OK status
     * - Generates JWT token via AuthenticationPort
     * - Returns user details with formatted information
     * - Includes success message and token in response
     */
    @Test
    fun `should handle mock auth success with authenticated user`() {
        // Given
        val username = "testuser"
        val authorities = listOf(SimpleGrantedAuthority("ROLE_USER"), SimpleGrantedAuthority("READ_PORTFOLIO"))
        val jwtToken = "mock-jwt-token-123"

        val mockAuthentication = mockk<Authentication> {
            every { isAuthenticated } returns true
            every { name } returns username
            every { getAuthorities() } returns authorities
        }

        val mockSecurityContext = mockk<SecurityContext> {
            every { authentication } returns mockAuthentication
        }

        SecurityContextHolder.setContext(mockSecurityContext)

        every {
            authenticationPort.generateToken(
                username,
                listOf("ROLE_USER", "READ_PORTFOLIO"),
                null
            )
        } returns jwtToken

        // When
        val responseMono = mockAuthController.mockAuthSuccess()

        // Then
        StepVerifier.create(responseMono)
            .expectNextMatches { response ->
                response.statusCode == HttpStatus.OK &&
                        response.body is Map<*, *> &&
                        (response.body as Map<*, *>)["success"] == true &&
                        (response.body as Map<*, *>)["token"] == jwtToken &&
                        (response.body as Map<*, *>)["message"] == "Mock authentication successful"
            }
            .verifyComplete()

        verify { authenticationPort.generateToken(username, listOf("ROLE_USER", "READ_PORTFOLIO"), null) }
    }

    /**
     * Tests mock authentication failure with unauthenticated user.
     *
     * Verifies that the controller handles unauthenticated users gracefully
     * and returns appropriate error responses.
     *
     * Expected behavior:
     * - Returns HTTP 401 Unauthorized status
     * - Returns error message indicating authentication failure
     * - Does not call token generation service
     */
    @Test
    fun `should handle mock auth failure with unauthenticated user`() {
        // Given
        val mockAuthentication = mockk<Authentication> {
            every { isAuthenticated } returns false
        }

        val mockSecurityContext = mockk<SecurityContext> {
            every { authentication } returns mockAuthentication
        }

        SecurityContextHolder.setContext(mockSecurityContext)

        // When
        val responseMono = mockAuthController.mockAuthSuccess()

        // Then
        StepVerifier.create(responseMono)
            .expectNextMatches { response ->
                response.statusCode == HttpStatus.UNAUTHORIZED &&
                        response.body is Map<*, *> &&
                        (response.body as Map<*, *>)["error"] == "User not authenticated"
            }
            .verifyComplete()

        verify(exactly = 0) { authenticationPort.generateToken(any(), any(), any()) }
    }

    /**
     * Tests mock authentication with null security context.
     *
     * Verifies that the controller handles null security context gracefully
     * and returns appropriate error responses.
     *
     * Expected behavior:
     * - Returns HTTP 401 Unauthorized status
     * - Returns error message indicating authentication failure
     * - Does not call token generation service
     */
    @Test
    fun `should handle mock auth with null security context`() {
        // Given
        SecurityContextHolder.clearContext()

        // When
        val responseMono = mockAuthController.mockAuthSuccess()

        // Then
        StepVerifier.create(responseMono)
            .expectNextMatches { response ->
                response.statusCode == HttpStatus.UNAUTHORIZED &&
                        response.body is Map<*, *> &&
                        (response.body as Map<*, *>)["error"] == "User not authenticated"
            }
            .verifyComplete()

        verify(exactly = 0) { authenticationPort.generateToken(any(), any(), any()) }
    }

    /**
     * Tests API-based mock authentication for regular user.
     *
     * Verifies that the controller can handle API authentication requests
     * for regular users and return appropriate JWT tokens and user information.
     *
     * Expected behavior:
     * - Returns HTTP 200 OK status
     * - Generates JWT token for regular user
     * - Returns user details with standard permissions
     * - Includes formatted user information
     */
    @Test
    fun `should handle API mock auth for regular user`() {
        // Given
        val loginRequest = MockLoginRequest("user2", "password")
        val jwtToken = "regular-user-jwt-token"

        every {
            authenticationPort.generateToken(
                "user2",
                listOf("ROLE_VIEWER", "READ_PORTFOLIO"),
                null
            )
        } returns jwtToken

        // When
        val responseMono = mockAuthController.mockApiLogin(loginRequest)

        // Then
        StepVerifier.create(responseMono)
            .expectNextMatches { response ->
                response.statusCode == HttpStatus.OK &&
                        response.body is Map<*, *> &&
                        (response.body as Map<*, *>)["success"] == true &&
                        (response.body as Map<*, *>)["token"] == jwtToken &&
                        ((response.body as Map<*, *>)["user"] as Map<*, *>)["username"] == "user2"
            }
            .verifyComplete()

        verify { authenticationPort.generateToken("user2", listOf("ROLE_VIEWER", "READ_PORTFOLIO"), null) }
    }

    /**
     * Tests API-based mock authentication for admin user.
     *
     * Verifies that the controller can handle API authentication requests
     * for admin users and return appropriate JWT tokens with elevated permissions.
     *
     * Expected behavior:
     * - Returns HTTP 200 OK status
     * - Generates JWT token for admin user
     * - Returns user details with admin permissions
     * - Includes comprehensive permission set
     */
    @Test
    fun `should handle API mock auth for admin user`() {
        // Given
        val loginRequest = MockLoginRequest("admin", "secret")
        val jwtToken = "admin-user-jwt-token"

        every {
            authenticationPort.generateToken(
                "admin",
                listOf(
                    "ROLE_ADMIN",
                    "READ_PORTFOLIO",
                    "WRITE_PORTFOLIO",
                    "DELETE_PORTFOLIO",
                    "MANAGE_USERS",
                    "VIEW_ANALYTICS"
                ),
                null
            )
        } returns jwtToken

        // When
        val responseMono = mockAuthController.mockApiLogin(loginRequest)

        // Then
        StepVerifier.create(responseMono)
            .expectNextMatches { response ->
                response.statusCode == HttpStatus.OK &&
                        response.body is Map<*, *> &&
                        (response.body as Map<*, *>)["success"] == true &&
                        (response.body as Map<*, *>)["token"] == jwtToken &&
                        ((response.body as Map<*, *>)["user"] as Map<*, *>)["username"] == "admin"
            }
            .verifyComplete()

        verify {
            authenticationPort.generateToken(
                "admin",
                listOf(
                    "ROLE_ADMIN",
                    "READ_PORTFOLIO",
                    "WRITE_PORTFOLIO",
                    "DELETE_PORTFOLIO",
                    "MANAGE_USERS",
                    "VIEW_ANALYTICS"
                ),
                null
            )
        }
    }

    /**
     * Tests API-based mock authentication for manager user.
     *
     * Verifies that the controller can handle API authentication requests
     * for manager users and return appropriate JWT tokens with manager permissions.
     *
     * Expected behavior:
     * - Returns HTTP 200 OK status
     * - Generates JWT token for manager user
     * - Returns user details with manager permissions
     * - Includes read and write permissions
     */
    @Test
    fun `should handle API mock auth for manager user`() {
        // Given
        val loginRequest = MockLoginRequest("user1", "password")
        val jwtToken = "manager-user-jwt-token"

        every {
            authenticationPort.generateToken(
                "user1",
                listOf("ROLE_PORTFOLIO_MANAGER", "READ_PORTFOLIO", "WRITE_PORTFOLIO", "VIEW_ANALYTICS"),
                null
            )
        } returns jwtToken

        // When
        val responseMono = mockAuthController.mockApiLogin(loginRequest)

        // Then
        StepVerifier.create(responseMono)
            .expectNextMatches { response ->
                response.statusCode == HttpStatus.OK &&
                        response.body is Map<*, *> &&
                        (response.body as Map<*, *>)["success"] == true &&
                        (response.body as Map<*, *>)["token"] == jwtToken &&
                        ((response.body as Map<*, *>)["user"] as Map<*, *>)["username"] == "user1"
            }
            .verifyComplete()

        verify {
            authenticationPort.generateToken(
                "user1",
                listOf("ROLE_PORTFOLIO_MANAGER", "READ_PORTFOLIO", "WRITE_PORTFOLIO", "VIEW_ANALYTICS"),
                null
            )
        }
    }

    /**
     * Tests API-based mock authentication with invalid credentials.
     *
     * Verifies that the controller handles invalid credentials gracefully
     * and returns appropriate error responses.
     *
     * Expected behavior:
     * - Returns HTTP 400 Bad Request status
     * - Returns error message indicating invalid credentials
     * - Does not call token generation service
     */
    @Test
    fun `should handle API mock auth with invalid credentials`() {
        // Given
        val loginRequest = MockLoginRequest("invaliduser", "wrongpassword")

        // When
        val responseMono = mockAuthController.mockApiLogin(loginRequest)

        // Then
        StepVerifier.create(responseMono)
            .expectNextMatches { response ->
                response.statusCode == HttpStatus.BAD_REQUEST &&
                        response.body is Map<*, *> &&
                        (response.body as Map<*, *>)["error"] == "Invalid credentials"
            }
            .verifyComplete()

        verify(exactly = 0) { authenticationPort.generateToken(any(), any(), any()) }
    }
} 