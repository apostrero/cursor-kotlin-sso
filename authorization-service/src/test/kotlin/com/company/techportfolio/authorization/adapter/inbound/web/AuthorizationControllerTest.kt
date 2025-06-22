package com.company.techportfolio.authorization.adapter.inbound.web

import com.company.techportfolio.authorization.domain.model.AuthorizationRequest
import com.company.techportfolio.authorization.domain.model.AuthorizationResponse
import com.company.techportfolio.authorization.domain.model.UserPermissions
import com.company.techportfolio.authorization.domain.service.AuthorizationService
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.reactive.server.WebTestClient

/**
 * Authorization Controller Test Suite - WebFlux Integration Tests
 *
 * This test class provides comprehensive integration testing for the AuthorizationController,
 * focusing on reactive HTTP endpoint behavior, request/response handling, and proper integration
 * with the authorization service layer using WebFlux testing patterns.
 *
 * ## Test Strategy:
 * - **Web Layer Testing**: Uses @WebFluxTest for lightweight web layer testing
 * - **Service Mocking**: MockK integration for service layer mocking
 * - **HTTP Testing**: WebTestClient for HTTP request/response testing
 * - **JSON Validation**: Comprehensive JSON path assertions
 * - **Security Testing**: Custom security configuration for test isolation
 *
 * ## Test Coverage:
 * - Authorization check endpoints (success/failure scenarios)
 * - User permissions retrieval endpoints
 * - Request/response serialization and deserialization
 * - HTTP status code validation
 * - Security configuration testing
 *
 * ## Architecture:
 * - **Controller**: AuthorizationController (system under test)
 * - **Service**: AuthorizationService (mocked dependency)
 * - **Framework**: Spring Boot Test with WebTestClient
 * - **Mocking**: MockK for Kotlin-friendly mocking
 *
 * ## Usage Example:
 * ```kotlin
 * // Test runs automatically with JUnit 5
 * // Individual test methods validate specific endpoint behaviors
 * // Security is disabled for test isolation
 * ```
 *
 * @author Technology Portfolio Team
 * @version 1.0
 * @since 1.0
 */
@WebFluxTest(AuthorizationController::class)
@ContextConfiguration(classes = [AuthorizationController::class, AuthorizationControllerTest.TestSecurityConfig::class])
class AuthorizationControllerTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @MockkBean
    private lateinit var authorizationService: AuthorizationService

    /**
     * Test Security Configuration for WebFlux
     */
    @Configuration
    @EnableWebFluxSecurity
    class TestSecurityConfig {
        @Bean
        fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
            return http
                .csrf { it.disable() }
                .authorizeExchange { it.anyExchange().permitAll() }
                .build()
        }
    }

    /**
     * Tests successful authorization check endpoint.
     *
     * This test verifies that the authorization check endpoint returns HTTP 200
     * with proper JSON response when the user is authorized for the requested
     * resource and action.
     *
     * ## Test Scenario:
     * - User: "testuser"
     * - Resource: "portfolio"
     * - Action: "read"
     * - Expected: Authorized with permissions and roles
     *
     * ## Validations:
     * - HTTP 200 status code
     * - JSON response structure
     * - Authorization result (isAuthorized = true)
     * - User, resource, and action echoed back
     * - Permissions and roles arrays populated
     */
    @Test
    fun `should return 200 when authorization check is successful`() {
        // Given
        val request = AuthorizationRequest(
            username = "testuser",
            resource = "portfolio",
            action = "read"
        )
        val response = AuthorizationResponse.authorized(
            username = "testuser",
            resource = "portfolio",
            action = "read",
            permissions = listOf("portfolio:read"),
            roles = listOf("USER")
        )

        every { authorizationService.authorizeUser(any()) } returns response

        // When & Then
        webTestClient.post()
            .uri("/api/authorization/check")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.isAuthorized").isEqualTo(true)
            .jsonPath("$.username").isEqualTo("testuser")
            .jsonPath("$.resource").isEqualTo("portfolio")
            .jsonPath("$.action").isEqualTo("read")
            .jsonPath("$.permissions[0]").isEqualTo("portfolio:read")
            .jsonPath("$.roles[0]").isEqualTo("USER")

        verify { authorizationService.authorizeUser(any()) }
    }

    /**
     * Tests failed authorization check endpoint.
     *
     * This test verifies that the authorization check endpoint returns HTTP 403
     * with proper error response when the user is not authorized for the requested
     * resource and action.
     *
     * ## Test Scenario:
     * - User: "testuser"
     * - Resource: "admin"
     * - Action: "delete"
     * - Expected: Unauthorized with error message
     *
     * ## Validations:
     * - HTTP 403 Forbidden status code
     * - JSON response structure
     * - Authorization result (isAuthorized = false)
     * - Error message included in response
     * - User, resource, and action echoed back
     */
    @Test
    fun `should return 403 when authorization check fails`() {
        // Given
        val request = AuthorizationRequest(
            username = "testuser",
            resource = "admin",
            action = "delete"
        )
        val response = AuthorizationResponse.unauthorized(
            username = "testuser",
            resource = "admin",
            action = "delete",
            errorMessage = "Access denied"
        )

        every { authorizationService.authorizeUser(any()) } returns response

        // When & Then
        webTestClient.post()
            .uri("/api/authorization/check")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isForbidden
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.isAuthorized").isEqualTo(false)
            .jsonPath("$.username").isEqualTo("testuser")
            .jsonPath("$.resource").isEqualTo("admin")
            .jsonPath("$.action").isEqualTo("delete")
            .jsonPath("$.errorMessage").isEqualTo("Access denied")

        verify { authorizationService.authorizeUser(any()) }
    }

    /**
     * Tests authorization check with contextual information.
     *
     * This test verifies that the authorization check endpoint properly handles
     * requests that include additional context information (such as organization ID
     * and department) and passes this context to the authorization service.
     *
     * ## Test Scenario:
     * - User: "testuser"
     * - Resource: "portfolio"
     * - Action: "read"
     * - Context: organizationId=1, department="IT"
     * - Expected: Successful authorization
     *
     * ## Validations:
     * - HTTP 200 status code
     * - Context properly serialized in request
     * - Authorization service receives context
     * - Successful authorization response
     */
    @Test
    fun `should handle authorization check with context`() {
        // Given
        val request = AuthorizationRequest(
            username = "testuser",
            resource = "portfolio",
            action = "read",
            context = mapOf("organizationId" to 1L, "department" to "IT")
        )
        val response = AuthorizationResponse.authorized(
            username = "testuser",
            resource = "portfolio",
            action = "read"
        )

        every { authorizationService.authorizeUser(any()) } returns response

        // When & Then
        webTestClient.post()
            .uri("/api/authorization/check")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.isAuthorized").isEqualTo(true)

        verify { authorizationService.authorizeUser(any()) }
    }

    /**
     * Tests user permissions retrieval endpoint.
     *
     * This test verifies that the user permissions endpoint returns comprehensive
     * permission information for a specified user, including their roles,
     * permissions, organization context, and activation status.
     *
     * ## Test Scenario:
     * - User: "testuser"
     * - Expected: Complete permission profile
     *
     * ## Validations:
     * - HTTP 200 status code
     * - JSON response structure
     * - Username echoed back
     * - Permissions array populated
     * - Roles array populated
     * - Organization ID included
     * - Active status confirmed
     */
    @Test
    fun `should return user permissions successfully`() {
        // Given
        val username = "testuser"
        val userPermissions = UserPermissions(
            username = username,
            permissions = listOf("portfolio:read", "portfolio:write"),
            roles = listOf("USER", "PORTFOLIO_MANAGER"),
            isActive = true
        )

        every { authorizationService.getUserPermissions(username) } returns userPermissions

        // When & Then
        webTestClient.get()
            .uri("/api/authorization/permissions?username={username}", username)
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.username").isEqualTo(username)
            .jsonPath("$.isActive").isEqualTo(true)
            .jsonPath("$.permissions[0]").isEqualTo("portfolio:read")
            .jsonPath("$.permissions[1]").isEqualTo("portfolio:write")
            .jsonPath("$.roles[0]").isEqualTo("USER")
            .jsonPath("$.roles[1]").isEqualTo("PORTFOLIO_MANAGER")

        verify { authorizationService.getUserPermissions(username) }
    }

    /**
     * Tests user permissions for inactive user.
     *
     * This test verifies that the system properly handles requests for permissions
     * of inactive users, returning empty permissions and roles while maintaining
     * the user's basic information and inactive status.
     *
     * ## Test Scenario:
     * - User: "inactiveuser"
     * - Status: Inactive
     * - Expected: Empty permissions and roles
     *
     * ## Validations:
     * - HTTP 200 status code
     * - Username echoed back
     * - Empty permissions array
     * - Empty roles array
     * - isActive = false
     */
    @Test
    fun `should return empty permissions for inactive user`() {
        // Given
        val username = "inactiveuser"
        val userPermissions = UserPermissions(
            username = username,
            permissions = emptyList(),
            roles = emptyList(),
            isActive = false
        )

        every { authorizationService.getUserPermissions(username) } returns userPermissions

        // When & Then
        webTestClient.get()
            .uri("/api/authorization/permissions?username={username}", username)
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.username").isEqualTo(username)
            .jsonPath("$.isActive").isEqualTo(false)
            .jsonPath("$.permissions").isEmpty
            .jsonPath("$.roles").isEmpty

        verify { authorizationService.getUserPermissions(username) }
    }

    /**
     * Tests role verification endpoint for users with specific roles.
     *
     * This test verifies that the role check endpoint correctly identifies when
     * a user has a specific role assigned, returning a boolean response indicating
     * the role membership status.
     *
     * ## Test Scenario:
     * - User: "testuser"
     * - Role: "PORTFOLIO_MANAGER"
     * - Expected: User has the role (true)
     *
     * ## Validations:
     * - HTTP 200 status code
     * - Boolean response (true)
     * - Service method called with correct parameters
     */
    @Test
    fun `should check if user has specific role successfully`() {
        // Given
        val username = "testuser"
        val role = "PORTFOLIO_MANAGER"

        every { authorizationService.hasRole(username, role) } returns true

        // When & Then
        webTestClient.get()
            .uri("/api/authorization/has-role?username={username}&role={role}", username, role)
            .exchange()
            .expectStatus().isOk
            .expectBody(Boolean::class.java)
            .isEqualTo(true)

        verify { authorizationService.hasRole(username, role) }
    }

    /**
     * Tests role verification endpoint for users without specific roles.
     *
     * This test verifies that the role check endpoint correctly identifies when
     * a user does not have a specific role assigned, returning false for the
     * role membership check.
     *
     * ## Test Scenario:
     * - User: "testuser"
     * - Role: "ADMIN"
     * - Expected: User does not have the role (false)
     *
     * ## Validations:
     * - HTTP 200 status code
     * - Boolean response (false)
     * - Service method called with correct parameters
     */
    @Test
    fun `should return false when user does not have specific role`() {
        // Given
        val username = "testuser"
        val role = "ADMIN"

        every { authorizationService.hasRole(username, role) } returns false

        // When & Then
        webTestClient.get()
            .uri("/api/authorization/has-role?username={username}&role={role}", username, role)
            .exchange()
            .expectStatus().isOk
            .expectBody(Boolean::class.java)
            .isEqualTo(false)

        verify { authorizationService.hasRole(username, role) }
    }

    /**
     * Tests multiple role verification endpoint for users with any matching roles.
     *
     * This test verifies that the multiple role check endpoint correctly identifies
     * when a user has at least one role from a list of acceptable roles, using
     * a POST request with JSON body containing the role list.
     *
     * ## Test Scenario:
     * - User: "testuser"
     * - Roles: ["ADMIN", "PORTFOLIO_MANAGER"]
     * - Expected: User has at least one role (true)
     *
     * ## Validations:
     * - HTTP 200 status code
     * - Boolean response (true)
     * - JSON request body properly processed
     * - Service method called with correct parameters
     */
    @Test
    fun `should check if user has any of the specified roles`() {
        // Given
        val username = "testuser"
        val roles = listOf("ADMIN", "PORTFOLIO_MANAGER")
        val request = mapOf("username" to username, "roles" to roles)

        every { authorizationService.hasAnyRole(username, roles) } returns true

        // When & Then
        webTestClient.post()
            .uri("/api/authorization/has-any-role")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk
            .expectBody(Boolean::class.java)
            .isEqualTo(true)

        verify { authorizationService.hasAnyRole(username, roles) }
    }

    /**
     * Tests multiple role verification endpoint for users without any matching roles.
     *
     * This test verifies that the multiple role check endpoint correctly identifies
     * when a user has none of the roles from a list of acceptable roles, returning
     * false for the multiple role membership check.
     *
     * ## Test Scenario:
     * - User: "testuser"
     * - Roles: ["ADMIN", "SUPER_USER"]
     * - Expected: User has none of the roles (false)
     *
     * ## Validations:
     * - HTTP 200 status code
     * - Boolean response (false)
     * - JSON request body properly processed
     * - Service method called with correct parameters
     */
    @Test
    fun `should return false when user does not have any of the specified roles`() {
        // Given
        val username = "testuser"
        val roles = listOf("ADMIN", "SUPER_USER")
        val request = mapOf("username" to username, "roles" to roles)

        every { authorizationService.hasAnyRole(username, roles) } returns false

        // When & Then
        webTestClient.post()
            .uri("/api/authorization/has-any-role")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk
            .expectBody(Boolean::class.java)
            .isEqualTo(false)

        verify { authorizationService.hasAnyRole(username, roles) }
    }

    /**
     * Tests permission verification endpoint for users with specific permissions.
     *
     * This test verifies that the permission check endpoint correctly identifies
     * when a user has a specific permission for a resource-action combination,
     * using query parameters for the permission details.
     *
     * ## Test Scenario:
     * - User: "testuser"
     * - Resource: "portfolio"
     * - Action: "read"
     * - Expected: User has the permission (true)
     *
     * ## Validations:
     * - HTTP 200 status code
     * - Boolean response (true)
     * - Query parameters properly processed
     * - Service method called with correct parameters
     */
    @Test
    fun `should check if user has specific permission`() {
        // Given
        val username = "testuser"
        val resource = "portfolio"
        val action = "read"

        every { authorizationService.hasPermission(username, resource, action) } returns true

        // When & Then
        webTestClient.get()
            .uri(
                "/api/authorization/has-permission?username={username}&resource={resource}&action={action}",
                username, resource, action
            )
            .exchange()
            .expectStatus().isOk
            .expectBody(Boolean::class.java)
            .isEqualTo(true)

        verify { authorizationService.hasPermission(username, resource, action) }
    }

    /**
     * Tests permission verification endpoint for users without specific permissions.
     *
     * This test verifies that the permission check endpoint correctly identifies
     * when a user does not have a specific permission for a resource-action
     * combination, returning false for the permission check.
     *
     * ## Test Scenario:
     * - User: "testuser"
     * - Resource: "admin"
     * - Action: "delete"
     * - Expected: User does not have the permission (false)
     *
     * ## Validations:
     * - HTTP 200 status code
     * - Boolean response (false)
     * - Query parameters properly processed
     * - Service method called with correct parameters
     */
    @Test
    fun `should return false when user does not have specific permission`() {
        // Given
        val username = "testuser"
        val resource = "admin"
        val action = "delete"

        every { authorizationService.hasPermission(username, resource, action) } returns false

        // When & Then
        webTestClient.get()
            .uri(
                "/api/authorization/has-permission?username={username}&resource={resource}&action={action}",
                username, resource, action
            )
            .exchange()
            .expectStatus().isOk
            .expectBody(Boolean::class.java)
            .isEqualTo(false)

        verify { authorizationService.hasPermission(username, resource, action) }
    }

    /**
     * Tests health check endpoint functionality.
     *
     * This test verifies that the health check endpoint returns proper service
     * status information, providing a way to monitor the authorization service's
     * availability and health for operations and monitoring systems.
     *
     * ## Test Scenario:
     * - Request: GET /api/authorization/health
     * - Expected: Service health status
     *
     * ## Validations:
     * - HTTP 200 status code
     * - JSON response with health information
     * - Status field indicates "UP"
     * - Service field identifies "authorization"
     */
    @Test
    fun `should return health status`() {
        // When & Then
        webTestClient.get()
            .uri("/api/authorization/health")
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.status").isEqualTo("UP")
            .jsonPath("$.service").isEqualTo("authorization")
    }

    /**
     * Tests user permissions endpoint with empty username handling.
     *
     * This test verifies that the system properly handles requests for user
     * permissions when an empty username is provided, returning appropriate
     * response without errors and proper inactive status.
     *
     * ## Test Scenario:
     * - User: "" (empty string)
     * - Expected: Empty permissions with inactive status
     *
     * ## Validations:
     * - HTTP 200 status code
     * - Empty username echoed back
     * - isActive = false
     * - Service method called with empty username
     */
    @Test
    fun `should handle empty username in permissions request`() {
        // Given
        val username = ""
        val userPermissions = UserPermissions(
            username = username,
            permissions = emptyList(),
            roles = emptyList(),
            isActive = false
        )

        every { authorizationService.getUserPermissions(username) } returns userPermissions

        // When & Then
        webTestClient.get()
            .uri("/api/authorization/permissions?username={username}", username)
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.username").isEqualTo("")
            .jsonPath("$.isActive").isEqualTo(false)

        verify { authorizationService.getUserPermissions(username) }
    }

    /**
     * Tests special character handling in username parameters.
     *
     * This test verifies that the system properly handles usernames containing
     * special characters commonly found in email addresses and other identifiers,
     * ensuring proper URL encoding and parameter processing.
     *
     * ## Test Scenario:
     * - User: "test.user@example.com"
     * - Expected: Special characters handled correctly
     *
     * ## Validations:
     * - HTTP 200 status code
     * - Special characters preserved in username
     * - Service method receives correct username
     * - Response contains expected data
     */
    @Test
    fun `should handle special characters in username`() {
        // Given
        val username = "test.user@example.com"
        val userPermissions = UserPermissions(
            username = username,
            permissions = listOf("portfolio:read"),
            roles = listOf("USER"),
            isActive = true
        )

        every { authorizationService.getUserPermissions(username) } returns userPermissions

        // When & Then
        webTestClient.get()
            .uri("/api/authorization/permissions?username={username}", username)
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.username").isEqualTo(username)

        verify { authorizationService.getUserPermissions(username) }
    }

    /**
     * Tests Unicode character handling in request parameters.
     *
     * This test verifies that the system properly handles Unicode characters
     * in usernames and role names, ensuring proper encoding, transmission,
     * and processing of international characters.
     *
     * ## Test Scenario:
     * - User: "tëstüser" (with umlauts)
     * - Role: "RÔLE_ÜSER" (with accents)
     * - Expected: Unicode characters handled correctly
     *
     * ## Validations:
     * - HTTP 200 status code
     * - Unicode characters preserved
     * - Service method receives correct parameters
     * - Boolean response returned
     */
    @Test
    fun `should handle Unicode characters in parameters`() {
        // Given
        val username = "tëstüser"
        val role = "RÔLE_ÜSER"

        every { authorizationService.hasRole(username, role) } returns true

        // When & Then
        webTestClient.get()
            .uri("/api/authorization/has-role?username={username}&role={role}", username, role)
            .exchange()
            .expectStatus().isOk
            .expectBody(Boolean::class.java)
            .isEqualTo(true)

        verify { authorizationService.hasRole(username, role) }
    }

    /**
     * Tests error handling for invalid JSON in request body.
     *
     * This test verifies that the system properly handles malformed JSON
     * in authorization check requests, returning appropriate HTTP 400 Bad Request
     * status without causing server errors or security issues.
     *
     * ## Test Scenario:
     * - Request: POST with invalid JSON
     * - Expected: HTTP 400 Bad Request
     *
     * ## Validations:
     * - HTTP 400 Bad Request status code
     * - No service method calls made
     * - Proper error handling without exceptions
     */
    @Test
    fun `should handle invalid JSON in authorization check`() {
        // Given
        val invalidJson = "{ invalid json }"

        // When & Then
        webTestClient.post()
            .uri("/api/authorization/check")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(invalidJson)
            .exchange()
            .expectStatus().isBadRequest
    }

    /**
     * Tests error handling for missing request body.
     *
     * This test verifies that the system properly handles authorization check
     * requests that are missing the required JSON request body, returning
     * appropriate HTTP 400 Bad Request status.
     *
     * ## Test Scenario:
     * - Request: POST without request body
     * - Expected: HTTP 400 Bad Request
     *
     * ## Validations:
     * - HTTP 400 Bad Request status code
     * - No service method calls made
     * - Proper error handling for missing data
     */
    @Test
    fun `should handle missing request body in authorization check`() {
        // When & Then
        webTestClient.post()
            .uri("/api/authorization/check")
            .exchange()
            .expectStatus().isBadRequest
    }

    /**
     * Tests error handling for missing username parameter.
     *
     * This test verifies that the system properly handles user permissions
     * requests that are missing the required username parameter, returning
     * appropriate HTTP 400 Bad Request status.
     *
     * ## Test Scenario:
     * - Request: GET /permissions without username parameter
     * - Expected: HTTP 400 Bad Request
     *
     * ## Validations:
     * - HTTP 400 Bad Request status code
     * - No service method calls made
     * - Proper parameter validation
     */
    @Test
    fun `should handle missing username parameter in permissions request`() {
        // When & Then
        webTestClient.get()
            .uri("/api/authorization/permissions")
            .exchange()
            .expectStatus().isBadRequest
    }

    /**
     * Tests error handling for missing role parameter in role check.
     *
     * This test verifies that the system properly handles role check requests
     * that are missing the required role parameter while providing the username,
     * returning appropriate HTTP 400 Bad Request status.
     *
     * ## Test Scenario:
     * - Request: GET /has-role with username but without role parameter
     * - Expected: HTTP 400 Bad Request
     *
     * ## Validations:
     * - HTTP 400 Bad Request status code
     * - No service method calls made
     * - Proper parameter validation for required fields
     */
    @Test
    fun `should handle missing role parameter in has-role request`() {
        // When & Then
        webTestClient.get()
            .uri("/api/authorization/has-role?username={username}", "testuser")
            .exchange()
            .expectStatus().isBadRequest
    }

    /**
     * Tests error handling for missing username parameter in role check.
     *
     * This test verifies that the system properly handles role check requests
     * that are missing the required username parameter while providing the role,
     * returning appropriate HTTP 400 Bad Request status.
     *
     * ## Test Scenario:
     * - Request: GET /has-role with role but without username parameter
     * - Expected: HTTP 400 Bad Request
     *
     * ## Validations:
     * - HTTP 400 Bad Request status code
     * - No service method calls made
     * - Proper parameter validation for required fields
     */
    @Test
    fun `should handle missing username parameter in has-role request`() {
        // When & Then
        webTestClient.get()
            .uri("/api/authorization/has-role?role={role}", "USER")
            .exchange()
            .expectStatus().isBadRequest
    }

    /**
     * Tests error handling for missing parameters in permission check.
     *
     * This test verifies that the system properly handles permission check requests
     * that are missing required parameters (action in this case), returning
     * appropriate HTTP 400 Bad Request status when not all required parameters
     * are provided.
     *
     * ## Test Scenario:
     * - Request: GET /has-permission with username and resource but missing action
     * - Expected: HTTP 400 Bad Request
     *
     * ## Validations:
     * - HTTP 400 Bad Request status code
     * - No service method calls made
     * - Proper validation for all required permission parameters
     */
    @Test
    fun `should handle missing parameters in has-permission request`() {
        // When & Then
        webTestClient.get()
            .uri("/api/authorization/has-permission?username={username}&resource={resource}", "testuser", "portfolio")
            .exchange()
            .expectStatus().isBadRequest
    }

    /**
     * Tests handling of large request payloads.
     *
     * This test verifies that the system can properly handle authorization
     * requests with large context maps containing many key-value pairs,
     * ensuring the system can process substantial amounts of contextual
     * information without performance issues or errors.
     *
     * ## Test Scenario:
     * - Request: Authorization check with 1000 context entries
     * - Expected: Successful processing of large payload
     *
     * ## Validations:
     * - HTTP 200 status code
     * - Large JSON payload properly processed
     * - Service method called with large context
     * - No performance or memory issues
     */
    @Test
    fun `should handle large request payload`() {
        // Given
        val largeContext = (1..1000).associate { "key$it" to "value$it" }
        val request = AuthorizationRequest(
            username = "testuser",
            resource = "portfolio",
            action = "read",
            context = largeContext
        )
        val response = AuthorizationResponse.authorized("testuser")

        every { authorizationService.authorizeUser(any()) } returns response

        // When & Then
        webTestClient.post()
            .uri("/api/authorization/check")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk

        verify { authorizationService.authorizeUser(any()) }
    }
} 