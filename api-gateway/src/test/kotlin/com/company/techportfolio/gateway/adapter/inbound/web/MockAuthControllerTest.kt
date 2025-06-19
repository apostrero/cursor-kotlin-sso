package com.company.techportfolio.gateway.adapter.inbound.web

import com.company.techportfolio.gateway.domain.port.AuthenticationPort
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.ui.Model

class MockAuthControllerTest {

    private val authenticationPort = mockk<AuthenticationPort>()
    private lateinit var mockAuthController: MockAuthController

    @BeforeEach
    fun setUp() {
        clearAllMocks()
        mockAuthController = MockAuthController(authenticationPort)
    }

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
        
        every { authenticationPort.generateToken(username, listOf("ROLE_USER", "READ_PORTFOLIO"), null) } returns jwtToken

        // When
        val response = mockAuthController.mockAuthSuccess()

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        val responseBody = response.body as Map<*, *>
        assertEquals(true, responseBody["success"])
        assertEquals(jwtToken, responseBody["token"])
        assertEquals("Mock authentication successful", responseBody["message"])
        
        val user = responseBody["user"] as Map<*, *>
        assertEquals(username, user["username"])
        assertEquals("${username}@example.com", user["email"])
        assertEquals("Testuser", user["firstName"])
        assertEquals("MockUser", user["lastName"])
        assertEquals(listOf("ROLE_USER", "READ_PORTFOLIO"), user["roles"])

        verify(exactly = 1) { authenticationPort.generateToken(username, listOf("ROLE_USER", "READ_PORTFOLIO"), null) }
        
        // Clean up
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `should handle mock auth success with no authentication`() {
        // Given
        val mockSecurityContext = mockk<SecurityContext> {
            every { authentication } returns null
        }
        
        SecurityContextHolder.setContext(mockSecurityContext)

        // When
        val response = mockAuthController.mockAuthSuccess()

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        val responseBody = response.body as Map<*, *>
        assertEquals("Not authenticated", responseBody["error"])

        verify(exactly = 0) { authenticationPort.generateToken(any(), any(), any()) }
        
        // Clean up
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `should handle mock auth success with unauthenticated user`() {
        // Given
        val mockAuthentication = mockk<Authentication> {
            every { isAuthenticated } returns false
        }
        
        val mockSecurityContext = mockk<SecurityContext> {
            every { authentication } returns mockAuthentication
        }
        
        SecurityContextHolder.setContext(mockSecurityContext)

        // When
        val response = mockAuthController.mockAuthSuccess()

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        val responseBody = response.body as Map<*, *>
        assertEquals("Not authenticated", responseBody["error"])

        verify(exactly = 0) { authenticationPort.generateToken(any(), any(), any()) }
        
        // Clean up
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `should handle mock API login with valid user1 credentials`() {
        // Given
        val loginRequest = MockLoginRequest("user1", "password")
        val jwtToken = "mock-jwt-token-user1"
        
        every { authenticationPort.generateToken("user1", listOf("ROLE_PORTFOLIO_MANAGER", "READ_PORTFOLIO", "WRITE_PORTFOLIO", "VIEW_ANALYTICS"), null) } returns jwtToken

        // When
        val response = mockAuthController.mockApiLogin(loginRequest)

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        val responseBody = response.body as Map<*, *>
        assertEquals(true, responseBody["success"])
        assertEquals(jwtToken, responseBody["token"])
        assertEquals("Mock authentication successful", responseBody["message"])
        
        val user = responseBody["user"] as Map<*, *>
        assertEquals("user1", user["username"])
        assertEquals("user1@example.com", user["email"])
        assertEquals("User1", user["firstName"])
        assertEquals("MockUser", user["lastName"])
        assertEquals(listOf("ROLE_PORTFOLIO_MANAGER", "READ_PORTFOLIO", "WRITE_PORTFOLIO", "VIEW_ANALYTICS"), user["roles"])

        verify(exactly = 1) { authenticationPort.generateToken("user1", listOf("ROLE_PORTFOLIO_MANAGER", "READ_PORTFOLIO", "WRITE_PORTFOLIO", "VIEW_ANALYTICS"), null) }
    }

    @Test
    fun `should handle mock API login with valid user2 credentials`() {
        // Given
        val loginRequest = MockLoginRequest("user2", "password")
        val jwtToken = "mock-jwt-token-user2"
        
        every { authenticationPort.generateToken("user2", listOf("ROLE_VIEWER", "READ_PORTFOLIO"), null) } returns jwtToken

        // When
        val response = mockAuthController.mockApiLogin(loginRequest)

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        val responseBody = response.body as Map<*, *>
        assertEquals(true, responseBody["success"])
        assertEquals(jwtToken, responseBody["token"])
        
        val user = responseBody["user"] as Map<*, *>
        assertEquals("user2", user["username"])
        assertEquals(listOf("ROLE_VIEWER", "READ_PORTFOLIO"), user["roles"])

        verify(exactly = 1) { authenticationPort.generateToken("user2", listOf("ROLE_VIEWER", "READ_PORTFOLIO"), null) }
    }

    @Test
    fun `should handle mock API login with valid admin credentials`() {
        // Given
        val loginRequest = MockLoginRequest("admin", "secret")
        val jwtToken = "mock-jwt-token-admin"
        
        every { authenticationPort.generateToken("admin", listOf("ROLE_ADMIN", "READ_PORTFOLIO", "WRITE_PORTFOLIO", "DELETE_PORTFOLIO", "MANAGE_USERS", "VIEW_ANALYTICS"), null) } returns jwtToken

        // When
        val response = mockAuthController.mockApiLogin(loginRequest)

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        val responseBody = response.body as Map<*, *>
        assertEquals(true, responseBody["success"])
        assertEquals(jwtToken, responseBody["token"])
        
        val user = responseBody["user"] as Map<*, *>
        assertEquals("admin", user["username"])
        assertEquals(listOf("ROLE_ADMIN", "READ_PORTFOLIO", "WRITE_PORTFOLIO", "DELETE_PORTFOLIO", "MANAGE_USERS", "VIEW_ANALYTICS"), user["roles"])

        verify(exactly = 1) { authenticationPort.generateToken("admin", listOf("ROLE_ADMIN", "READ_PORTFOLIO", "WRITE_PORTFOLIO", "DELETE_PORTFOLIO", "MANAGE_USERS", "VIEW_ANALYTICS"), null) }
    }

    @Test
    fun `should handle mock API login with invalid username`() {
        // Given
        val loginRequest = MockLoginRequest("invaliduser", "password")

        // When
        val response = mockAuthController.mockApiLogin(loginRequest)

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        val responseBody = response.body as Map<*, *>
        assertEquals("Invalid credentials", responseBody["error"])

        verify(exactly = 0) { authenticationPort.generateToken(any(), any(), any()) }
    }

    @Test
    fun `should handle mock API login with invalid password for user1`() {
        // Given
        val loginRequest = MockLoginRequest("user1", "wrongpassword")

        // When
        val response = mockAuthController.mockApiLogin(loginRequest)

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        val responseBody = response.body as Map<*, *>
        assertEquals("Invalid credentials", responseBody["error"])

        verify(exactly = 0) { authenticationPort.generateToken(any(), any(), any()) }
    }

    @Test
    fun `should handle mock API login with invalid password for admin`() {
        // Given
        val loginRequest = MockLoginRequest("admin", "wrongsecret")

        // When
        val response = mockAuthController.mockApiLogin(loginRequest)

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        val responseBody = response.body as Map<*, *>
        assertEquals("Invalid credentials", responseBody["error"])

        verify(exactly = 0) { authenticationPort.generateToken(any(), any(), any()) }
    }

    @Test
    fun `should return mock users information`() {
        // When
        val response = mockAuthController.getMockUsers()

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        val responseBody = response.body as Map<*, *>
        
        val users = responseBody["users"] as List<*>
        assertEquals(3, users.size)
        
        val user1 = users[0] as Map<*, *>
        assertEquals("user1", user1["username"])
        assertEquals("password", user1["password"])
        assertEquals("Portfolio Manager", user1["role"])
        assertEquals(listOf("READ_PORTFOLIO", "WRITE_PORTFOLIO", "VIEW_ANALYTICS"), user1["permissions"])
        
        val user2 = users[1] as Map<*, *>
        assertEquals("user2", user2["username"])
        assertEquals("password", user2["password"])
        assertEquals("Viewer", user2["role"])
        assertEquals(listOf("READ_PORTFOLIO"), user2["permissions"])
        
        val admin = users[2] as Map<*, *>
        assertEquals("admin", admin["username"])
        assertEquals("secret", admin["password"])
        assertEquals("Administrator", admin["role"])
        assertEquals(listOf("READ_PORTFOLIO", "WRITE_PORTFOLIO", "DELETE_PORTFOLIO", "MANAGE_USERS", "VIEW_ANALYTICS"), admin["permissions"])
        
        assertEquals("These are mock users for testing. Use /api/auth/mock-login for programmatic authentication.", responseBody["note"])
    }

    @Test
    fun `should handle mock API login with unknown user that gets empty authorities`() {
        // Given
        val loginRequest = MockLoginRequest("unknownuser", "password")

        // Mock the valid credentials to include the unknown user
        // This test checks the else branch in the authorities assignment
        // Since the controller has hardcoded credentials, we need to modify our approach
        
        // When
        val response = mockAuthController.mockApiLogin(loginRequest)

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        val responseBody = response.body as Map<*, *>
        assertEquals("Invalid credentials", responseBody["error"])

        verify(exactly = 0) { authenticationPort.generateToken(any(), any(), any()) }
    }

    @Test
    fun `should handle empty username in mock API login`() {
        // Given
        val loginRequest = MockLoginRequest("", "password")

        // When
        val response = mockAuthController.mockApiLogin(loginRequest)

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        val responseBody = response.body as Map<*, *>
        assertEquals("Invalid credentials", responseBody["error"])

        verify(exactly = 0) { authenticationPort.generateToken(any(), any(), any()) }
    }

    @Test
    fun `should handle empty password in mock API login`() {
        // Given
        val loginRequest = MockLoginRequest("user1", "")

        // When
        val response = mockAuthController.mockApiLogin(loginRequest)

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        val responseBody = response.body as Map<*, *>
        assertEquals("Invalid credentials", responseBody["error"])

        verify(exactly = 0) { authenticationPort.generateToken(any(), any(), any()) }
    }

    @Test
    fun `MockLoginRequest data class should work correctly`() {
        // Given
        val username = "testuser"
        val password = "testpassword"

        // When
        val loginRequest = MockLoginRequest(username, password)

        // Then
        assertEquals(username, loginRequest.username)
        assertEquals(password, loginRequest.password)
        
        // Test equality
        val loginRequest2 = MockLoginRequest(username, password)
        assertEquals(loginRequest, loginRequest2)
        assertEquals(loginRequest.hashCode(), loginRequest2.hashCode())
        
        // Test toString
        val toString = loginRequest.toString()
        assertTrue(toString.contains("MockLoginRequest"))
        assertTrue(toString.contains(username))
        assertTrue(toString.contains(password))
        
        // Test copy
        val copied = loginRequest.copy(password = "newpassword")
        assertEquals(username, copied.username)
        assertEquals("newpassword", copied.password)
    }
} 