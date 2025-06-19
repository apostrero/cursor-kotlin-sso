package com.company.techportfolio.gateway.adapter.outbound.authorization

import com.company.techportfolio.gateway.adapter.out.authorization.AuthorizationServiceAdapter
import com.company.techportfolio.gateway.domain.port.AuthorizationResult
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate

/**
 * Unit test class for the AuthorizationServiceAdapter.
 * 
 * This test class verifies the behavior of the AuthorizationServiceAdapter which handles
 * communication with the external authorization microservice via REST API calls.
 * It tests role-based access control (RBAC) operations and permission management.
 * 
 * Test coverage includes:
 * - User authorization checks via REST API
 * - Permission retrieval and validation
 * - Role-based access control operations
 * - Error handling when authorization service is unavailable
 * - REST API communication patterns and error responses
 * - Fallback behavior for service failures
 * 
 * Testing approach:
 * - Uses MockK for mocking RestTemplate dependencies
 * - Tests successful authorization scenarios
 * - Verifies error handling and graceful degradation
 * - Validates REST API call parameters and URLs
 * - Ensures authorization failures return appropriate responses
 * - Tests various user permission scenarios
 * 
 * @author Technology Portfolio Team
 * @since 1.0.0
 */

class AuthorizationServiceAdapterTest {

    private val restTemplate = mockk<RestTemplate>()
    private lateinit var authorizationServiceAdapter: AuthorizationServiceAdapter
    private val authorizationServiceUrl = "http://localhost:8081"

    @BeforeEach
    fun setUp() {
        clearAllMocks()
        authorizationServiceAdapter = AuthorizationServiceAdapter(restTemplate)
        ReflectionTestUtils.setField(authorizationServiceAdapter, "authorizationServiceUrl", authorizationServiceUrl)
    }

    @Test
    fun `should authorize user successfully`() {
        // Given
        val username = "testuser"
        val resource = "portfolio"
        val action = "read"
        val expectedUrl = "$authorizationServiceUrl/api/authorization/check"
        val expectedRequest = mapOf(
            "username" to username,
            "resource" to resource,
            "action" to action
        )
        val expectedResult = AuthorizationResult.authorized(username, resource, action, listOf("READ_PORTFOLIO"))

        every { restTemplate.postForObject(expectedUrl, expectedRequest, AuthorizationResult::class.java) } returns expectedResult

        // When
        val result = authorizationServiceAdapter.authorizeUser(username, resource, action)

        // Then
        assertTrue(result.isAuthorized)
        assertEquals(username, result.username)
        assertEquals(resource, result.resource)
        assertEquals(action, result.action)
        assertEquals(listOf("READ_PORTFOLIO"), result.permissions)

        verify(exactly = 1) { restTemplate.postForObject(expectedUrl, expectedRequest, AuthorizationResult::class.java) }
    }

    @Test
    fun `should handle authorization failure from service`() {
        // Given
        val username = "testuser"
        val resource = "portfolio"
        val action = "delete"
        val expectedUrl = "$authorizationServiceUrl/api/authorization/check"
        val expectedRequest = mapOf(
            "username" to username,
            "resource" to resource,
            "action" to action
        )
        val expectedResult = AuthorizationResult.unauthorized(username, resource, action, "Insufficient permissions")

        every { restTemplate.postForObject(expectedUrl, expectedRequest, AuthorizationResult::class.java) } returns expectedResult

        // When
        val result = authorizationServiceAdapter.authorizeUser(username, resource, action)

        // Then
        assertFalse(result.isAuthorized)
        assertEquals(username, result.username)
        assertEquals(resource, result.resource)
        assertEquals(action, result.action)
        assertEquals("Insufficient permissions", result.errorMessage)

        verify(exactly = 1) { restTemplate.postForObject(expectedUrl, expectedRequest, AuthorizationResult::class.java) }
    }

    @Test
    fun `should handle null response from authorization service`() {
        // Given
        val username = "testuser"
        val resource = "portfolio"
        val action = "read"
        val expectedUrl = "$authorizationServiceUrl/api/authorization/check"
        val expectedRequest = mapOf(
            "username" to username,
            "resource" to resource,
            "action" to action
        )

        every { restTemplate.postForObject(expectedUrl, expectedRequest, AuthorizationResult::class.java) } returns null

        // When
        val result = authorizationServiceAdapter.authorizeUser(username, resource, action)

        // Then
        assertFalse(result.isAuthorized)
        assertEquals(username, result.username)
        assertEquals(resource, result.resource)
        assertEquals(action, result.action)
        assertEquals("No response from authorization service", result.errorMessage)

        verify(exactly = 1) { restTemplate.postForObject(expectedUrl, expectedRequest, AuthorizationResult::class.java) }
    }

    @Test
    fun `should handle authorization service exception`() {
        // Given
        val username = "testuser"
        val resource = "portfolio"
        val action = "read"
        val expectedUrl = "$authorizationServiceUrl/api/authorization/check"
        val expectedRequest = mapOf(
            "username" to username,
            "resource" to resource,
            "action" to action
        )

        every { restTemplate.postForObject(expectedUrl, expectedRequest, AuthorizationResult::class.java) } throws RestClientException("Service unavailable")

        // When
        val result = authorizationServiceAdapter.authorizeUser(username, resource, action)

        // Then
        assertFalse(result.isAuthorized)
        assertEquals(username, result.username)
        assertEquals(resource, result.resource)
        assertEquals(action, result.action)
        assertTrue(result.errorMessage!!.contains("Authorization service error: Service unavailable"))

        verify(exactly = 1) { restTemplate.postForObject(expectedUrl, expectedRequest, AuthorizationResult::class.java) }
    }

    @Test
    fun `should get user permissions successfully`() {
        // Given
        val username = "testuser"
        val expectedUrl = "$authorizationServiceUrl/api/authorization/permissions?username=$username"
        val expectedPermissions = listOf("READ_PORTFOLIO", "WRITE_PORTFOLIO")

        every { restTemplate.getForObject(expectedUrl, Array<String>::class.java) } returns expectedPermissions.toTypedArray()

        // When
        val permissions = authorizationServiceAdapter.getUserPermissions(username)

        // Then
        assertEquals(expectedPermissions, permissions)

        verify(exactly = 1) { restTemplate.getForObject(expectedUrl, Array<String>::class.java) }
    }

    @Test
    fun `should handle null response when getting user permissions`() {
        // Given
        val username = "testuser"
        val expectedUrl = "$authorizationServiceUrl/api/authorization/permissions?username=$username"

        every { restTemplate.getForObject(expectedUrl, Array<String>::class.java) } returns null

        // When
        val permissions = authorizationServiceAdapter.getUserPermissions(username)

        // Then
        assertEquals(emptyList<String>(), permissions)

        verify(exactly = 1) { restTemplate.getForObject(expectedUrl, Array<String>::class.java) }
    }

    @Test
    fun `should handle exception when getting user permissions`() {
        // Given
        val username = "testuser"
        val expectedUrl = "$authorizationServiceUrl/api/authorization/permissions?username=$username"

        every { restTemplate.getForObject(expectedUrl, Array<String>::class.java) } throws RestClientException("Service unavailable")

        // When
        val permissions = authorizationServiceAdapter.getUserPermissions(username)

        // Then
        assertEquals(emptyList<String>(), permissions)

        verify(exactly = 1) { restTemplate.getForObject(expectedUrl, Array<String>::class.java) }
    }

    @Test
    fun `should check if user has role successfully`() {
        // Given
        val username = "testuser"
        val role = "ADMIN"
        val expectedUrl = "$authorizationServiceUrl/api/authorization/has-role?username=$username&role=$role"

        every { restTemplate.getForObject(expectedUrl, Boolean::class.java) } returns true

        // When
        val hasRole = authorizationServiceAdapter.hasRole(username, role)

        // Then
        assertTrue(hasRole)

        verify(exactly = 1) { restTemplate.getForObject(expectedUrl, Boolean::class.java) }
    }

    @Test
    fun `should return false when user does not have role`() {
        // Given
        val username = "testuser"
        val role = "ADMIN"
        val expectedUrl = "$authorizationServiceUrl/api/authorization/has-role?username=$username&role=$role"

        every { restTemplate.getForObject(expectedUrl, Boolean::class.java) } returns false

        // When
        val hasRole = authorizationServiceAdapter.hasRole(username, role)

        // Then
        assertFalse(hasRole)

        verify(exactly = 1) { restTemplate.getForObject(expectedUrl, Boolean::class.java) }
    }

    @Test
    fun `should check if user has any role successfully`() {
        // Given
        val username = "testuser"
        val roles = listOf("ADMIN", "MANAGER")
        val expectedUrl = "$authorizationServiceUrl/api/authorization/has-any-role"
        val expectedRequest = mapOf(
            "username" to username,
            "roles" to roles
        )

        every { restTemplate.postForObject(expectedUrl, expectedRequest, Boolean::class.java) } returns true

        // When
        val hasAnyRole = authorizationServiceAdapter.hasAnyRole(username, roles)

        // Then
        assertTrue(hasAnyRole)

        verify(exactly = 1) { restTemplate.postForObject(expectedUrl, expectedRequest, Boolean::class.java) }
    }
} 