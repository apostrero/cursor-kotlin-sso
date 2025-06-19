package com.company.techportfolio.authorization.adapter.inbound.web

import com.company.techportfolio.authorization.domain.model.AuthorizationRequest
import com.company.techportfolio.authorization.domain.model.AuthorizationResponse
import com.company.techportfolio.authorization.domain.model.UserPermissions
import com.company.techportfolio.authorization.domain.service.AuthorizationService
import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import com.ninjasquad.springmockk.MockkBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(AuthorizationController::class)
@ContextConfiguration(classes = [AuthorizationController::class, AuthorizationControllerTest.TestSecurityConfig::class])
class AuthorizationControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var authorizationService: AuthorizationService

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Configuration
    @EnableWebSecurity
    class TestSecurityConfig {
        @Bean
        fun filterChain(http: HttpSecurity): SecurityFilterChain {
            http.csrf { it.disable() }
                .authorizeHttpRequests { it.anyRequest().permitAll() }
            return http.build()
        }
    }

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
        mockMvc.perform(
            post("/api/authorization/check")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.isAuthorized").value(true))
            .andExpect(jsonPath("$.username").value("testuser"))
            .andExpect(jsonPath("$.resource").value("portfolio"))
            .andExpect(jsonPath("$.action").value("read"))
            .andExpect(jsonPath("$.permissions[0]").value("portfolio:read"))
            .andExpect(jsonPath("$.roles[0]").value("USER"))

        verify { authorizationService.authorizeUser(any()) }
    }

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
        mockMvc.perform(
            post("/api/authorization/check")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isForbidden)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.isAuthorized").value(false))
            .andExpect(jsonPath("$.username").value("testuser"))
            .andExpect(jsonPath("$.resource").value("admin"))
            .andExpect(jsonPath("$.action").value("delete"))
            .andExpect(jsonPath("$.errorMessage").value("Access denied"))

        verify { authorizationService.authorizeUser(any()) }
    }

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
        mockMvc.perform(
            post("/api/authorization/check")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.isAuthorized").value(true))

        verify { authorizationService.authorizeUser(any()) }
    }

    @Test
    fun `should return user permissions successfully`() {
        // Given
        val username = "testuser"
        val userPermissions = UserPermissions(
            username = username,
            permissions = listOf("portfolio:read", "portfolio:write"),
            roles = listOf("USER", "PORTFOLIO_MANAGER"),
            organizationId = 1L,
            isActive = true
        )

        every { authorizationService.getUserPermissions(username) } returns userPermissions

        // When & Then
        mockMvc.perform(
            get("/api/authorization/permissions")
                .param("username", username)
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.username").value(username))
            .andExpect(jsonPath("$.permissions[0]").value("portfolio:read"))
            .andExpect(jsonPath("$.permissions[1]").value("portfolio:write"))
            .andExpect(jsonPath("$.roles[0]").value("USER"))
            .andExpect(jsonPath("$.roles[1]").value("PORTFOLIO_MANAGER"))
            .andExpect(jsonPath("$.organizationId").value(1))
            .andExpect(jsonPath("$.isActive").value(true))

        verify { authorizationService.getUserPermissions(username) }
    }

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
        mockMvc.perform(
            get("/api/authorization/permissions")
                .param("username", username)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.username").value(username))
            .andExpect(jsonPath("$.permissions").isEmpty)
            .andExpect(jsonPath("$.roles").isEmpty)
            .andExpect(jsonPath("$.isActive").value(false))

        verify { authorizationService.getUserPermissions(username) }
    }

    @Test
    fun `should check if user has specific role successfully`() {
        // Given
        val username = "testuser"
        val role = "PORTFOLIO_MANAGER"

        every { authorizationService.hasRole(username, role) } returns true

        // When & Then
        mockMvc.perform(
            get("/api/authorization/has-role")
                .param("username", username)
                .param("role", role)
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().string("true"))

        verify { authorizationService.hasRole(username, role) }
    }

    @Test
    fun `should return false when user does not have specific role`() {
        // Given
        val username = "testuser"
        val role = "ADMIN"

        every { authorizationService.hasRole(username, role) } returns false

        // When & Then
        mockMvc.perform(
            get("/api/authorization/has-role")
                .param("username", username)
                .param("role", role)
        )
            .andExpect(status().isOk)
            .andExpect(content().string("false"))

        verify { authorizationService.hasRole(username, role) }
    }

    @Test
    fun `should check if user has any of the specified roles`() {
        // Given
        val requestBody = mapOf(
            "username" to "testuser",
            "roles" to listOf("ADMIN", "PORTFOLIO_MANAGER")
        )

        every { authorizationService.hasAnyRole(any(), any()) } returns true

        // When & Then
        mockMvc.perform(
            post("/api/authorization/has-any-role")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody))
        )
            .andExpect(status().isOk)
            .andExpect(content().string("true"))

        verify { authorizationService.hasAnyRole(any(), any()) }
    }

    @Test
    fun `should return false when user does not have any of the specified roles`() {
        // Given
        val requestBody = mapOf(
            "username" to "testuser",
            "roles" to listOf("ADMIN", "SUPER_USER")
        )

        every { authorizationService.hasAnyRole(any(), any()) } returns false

        // When & Then
        mockMvc.perform(
            post("/api/authorization/has-any-role")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody))
        )
            .andExpect(status().isOk)
            .andExpect(content().string("false"))

        verify { authorizationService.hasAnyRole(any(), any()) }
    }

    @Test
    fun `should check if user has specific permission`() {
        // Given
        val username = "testuser"
        val resource = "portfolio"
        val action = "read"

        every { authorizationService.hasPermission(username, resource, action) } returns true

        // When & Then
        mockMvc.perform(
            get("/api/authorization/has-permission")
                .param("username", username)
                .param("resource", resource)
                .param("action", action)
        )
            .andExpect(status().isOk)
            .andExpect(content().string("true"))

        verify { authorizationService.hasPermission(username, resource, action) }
    }

    @Test
    fun `should return false when user does not have specific permission`() {
        // Given
        val username = "testuser"
        val resource = "admin"
        val action = "delete"

        every { authorizationService.hasPermission(username, resource, action) } returns false

        // When & Then
        mockMvc.perform(
            get("/api/authorization/has-permission")
                .param("username", username)
                .param("resource", resource)
                .param("action", action)
        )
            .andExpect(status().isOk)
            .andExpect(content().string("false"))

        verify { authorizationService.hasPermission(username, resource, action) }
    }

    @Test
    fun `should return health status`() {
        // When & Then
        mockMvc.perform(get("/api/authorization/health"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value("UP"))
            .andExpect(jsonPath("$.service").value("authorization"))
    }

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
        mockMvc.perform(
            get("/api/authorization/permissions")
                .param("username", username)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.username").value(""))
            .andExpect(jsonPath("$.isActive").value(false))

        verify { authorizationService.getUserPermissions(username) }
    }

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
        mockMvc.perform(
            get("/api/authorization/permissions")
                .param("username", username)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.username").value(username))

        verify { authorizationService.getUserPermissions(username) }
    }

    @Test
    fun `should handle Unicode characters in parameters`() {
        // Given
        val username = "tëstüser"
        val role = "RÔLE_ÜSER"

        every { authorizationService.hasRole(username, role) } returns true

        // When & Then
        mockMvc.perform(
            get("/api/authorization/has-role")
                .param("username", username)
                .param("role", role)
        )
            .andExpect(status().isOk)
            .andExpect(content().string("true"))

        verify { authorizationService.hasRole(username, role) }
    }

    @Test
    fun `should handle invalid JSON in authorization check`() {
        // Given
        val invalidJson = "{ invalid json }"

        // When & Then
        mockMvc.perform(
            post("/api/authorization/check")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson)
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `should handle missing request body in authorization check`() {
        // When & Then
        mockMvc.perform(
            post("/api/authorization/check")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `should handle missing username parameter in permissions request`() {
        // When & Then
        mockMvc.perform(get("/api/authorization/permissions"))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `should handle missing role parameter in has-role request`() {
        // When & Then
        mockMvc.perform(
            get("/api/authorization/has-role")
                .param("username", "testuser")
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `should handle missing username parameter in has-role request`() {
        // When & Then
        mockMvc.perform(
            get("/api/authorization/has-role")
                .param("role", "USER")
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `should handle missing parameters in has-permission request`() {
        // When & Then
        mockMvc.perform(
            get("/api/authorization/has-permission")
                .param("username", "testuser")
                .param("resource", "portfolio")
        )
            .andExpect(status().isBadRequest)
    }

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
        mockMvc.perform(
            post("/api/authorization/check")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)

        verify { authorizationService.authorizeUser(any()) }
    }
} 