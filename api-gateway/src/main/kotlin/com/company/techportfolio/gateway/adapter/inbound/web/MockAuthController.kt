package com.company.techportfolio.gateway.adapter.inbound.web

import com.company.techportfolio.gateway.domain.model.AuthenticationResult
import com.company.techportfolio.gateway.domain.port.AuthenticationPort
import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.time.LocalDateTime

/**
 * Mock authentication controller for development and testing purposes (REACTIVE).
 * 
 * This controller provides mock authentication functionality when the application
 * is running in 'mock-auth' profile. It simulates SAML SSO authentication flows
 * without requiring external identity providers, making it ideal for development
 * and testing environments using reactive programming patterns.
 * 
 * Key features:
 * - Web-based mock login page (synchronous view rendering)
 * - Programmatic API authentication endpoints (reactive)
 * - Predefined test users with different roles
 * - JWT token generation for authenticated users
 * - User information and permission management
 * - Reactive error handling with onErrorMap and onErrorResume
 * 
 * Available test users:
 * - user1/password (Portfolio Manager)
 * - user2/password (Viewer)
 * - admin/secret (Administrator)
 * 
 * @property authenticationPort Port for JWT token generation and authentication operations
 * 
 * @author Technology Portfolio Team
 * @since 1.0.0
 */
@Controller
@Profile("mock-auth")
class MockAuthController(
    private val authenticationPort: AuthenticationPort
) {

    /**
     * Displays the mock authentication login page.
     * 
     * This endpoint renders a web-based login form for interactive authentication
     * during development. It supports error and logout message display.
     * 
     * Note: This method remains synchronous as it returns a view name for template rendering.
     * 
     * @param error Optional error parameter to display authentication errors
     * @param logout Optional logout parameter to display logout confirmation
     * @param model Spring MVC model for passing data to the view
     * @return View name for the mock login template
     */
    @GetMapping("/mock-login")
    fun mockLoginPage(
        @RequestParam(required = false) error: String?,
        @RequestParam(required = false) logout: String?,
        model: Model
    ): String {
        model.addAttribute("error", error != null)
        model.addAttribute("logout", logout != null)
        return "mock-login"
    }

    /**
     * Handles successful mock authentication and returns user details with JWT token.
     * 
     * This endpoint is called after successful form-based authentication to generate
     * JWT tokens and return user information. It extracts authentication details
     * from the Spring Security context.
     * 
     * **Reactive**: Returns Mono<ResponseEntity<Map<String, Any>>>
     * 
     * @return Mono<ResponseEntity<Map<String, Any>>> with authentication result including JWT token and user details
     */
    @GetMapping("/api/auth/mock-success")
    @ResponseBody
    fun mockAuthSuccess(): Mono<ResponseEntity<Map<String, Any>>> {
        val authentication = SecurityContextHolder.getContext().authentication
        
        if (authentication == null || !authentication.isAuthenticated) {
            return Mono.just(ResponseEntity.badRequest().body(
                mapOf("error" to "Not authenticated")
            ))
        }

        val authorities = authentication.authorities.map { it.authority }
        val username = authentication.name
        
        // Generate JWT token
        val jwtToken = authenticationPort.generateToken(username, authorities, null)

        // Create successful authentication result
        val authResult = AuthenticationResult.success(
            username = username,
            authorities = authorities,
            token = jwtToken,
            sessionIndex = null,
            expiresAt = LocalDateTime.now().plusHours(1)
        )

        return Mono.just(ResponseEntity.ok(
            mapOf(
                "success" to true,
                "token" to jwtToken,
                "user" to mapOf(
                    "username" to username,
                    "email" to "${username}@example.com",
                    "firstName" to username.replaceFirstChar { it.uppercase() },
                    "lastName" to "MockUser",
                    "roles" to authorities
                ),
                "message" to "Mock authentication successful"
            )
        ))
    }

    /**
     * Handles programmatic mock authentication via API.
     * 
     * This endpoint allows direct authentication using username/password credentials
     * for automated testing and API integration. It validates credentials against
     * predefined test users and generates JWT tokens for successful authentication.
     * 
     * **Reactive**: Returns Mono<ResponseEntity<Map<String, Any>>>
     * 
     * @param loginRequest Request body containing username and password
     * @return Mono<ResponseEntity<Map<String, Any>>> with authentication result (200 OK if successful, 400 Bad Request if failed)
     */
    @PostMapping("/api/auth/mock-login")
    @ResponseBody
    fun mockApiLogin(
        @RequestBody loginRequest: MockLoginRequest
    ): Mono<ResponseEntity<Map<String, Any>>> {
        // This endpoint allows programmatic login for testing
        val validCredentials = mapOf(
            "user1" to "password",
            "user2" to "password", 
            "admin" to "secret"
        )

        if (validCredentials[loginRequest.username] != loginRequest.password) {
            return Mono.just(ResponseEntity.badRequest().body(
                mapOf("error" to "Invalid credentials")
            ))
        }

        // Create mock authorities based on username
        val authorities = when (loginRequest.username) {
            "admin" -> listOf("ROLE_ADMIN", "READ_PORTFOLIO", "WRITE_PORTFOLIO", "DELETE_PORTFOLIO", "MANAGE_USERS", "VIEW_ANALYTICS")
            "user1" -> listOf("ROLE_PORTFOLIO_MANAGER", "READ_PORTFOLIO", "WRITE_PORTFOLIO", "VIEW_ANALYTICS")
            "user2" -> listOf("ROLE_VIEWER", "READ_PORTFOLIO")
            else -> emptyList()
        }

        // Generate JWT token
        val jwtToken = authenticationPort.generateToken(loginRequest.username, authorities, null)

        // Create successful authentication result
        val authResult = AuthenticationResult.success(
            username = loginRequest.username,
            authorities = authorities,
            token = jwtToken,
            sessionIndex = null,
            expiresAt = LocalDateTime.now().plusHours(1)
        )

        return Mono.just(ResponseEntity.ok(
            mapOf(
                "success" to true,
                "token" to jwtToken,
                "user" to mapOf(
                    "username" to loginRequest.username,
                    "email" to "${loginRequest.username}@example.com",
                    "firstName" to loginRequest.username.replaceFirstChar { it.uppercase() },
                    "lastName" to "MockUser",
                    "roles" to authorities
                ),
                "message" to "Mock authentication successful"
            )
        ))
    }

    /**
     * Returns information about available mock test users.
     * 
     * This endpoint provides a list of predefined test users with their credentials,
     * roles, and permissions. Useful for testing and development to understand
     * available user accounts and their capabilities.
     * 
     * **Reactive**: Returns Mono<ResponseEntity<Map<String, Any>>>
     * 
     * @return Mono<ResponseEntity<Map<String, Any>>> with list of mock users and their details
     */
    @GetMapping("/api/auth/mock-users")
    @ResponseBody
    fun getMockUsers(): Mono<ResponseEntity<Map<String, Any>>> {
        return Mono.just(ResponseEntity.ok(
            mapOf(
                "users" to listOf(
                    mapOf(
                        "username" to "user1",
                        "password" to "password",
                        "role" to "Portfolio Manager",
                        "permissions" to listOf("READ_PORTFOLIO", "WRITE_PORTFOLIO", "VIEW_ANALYTICS")
                    ),
                    mapOf(
                        "username" to "user2", 
                        "password" to "password",
                        "role" to "Viewer",
                        "permissions" to listOf("READ_PORTFOLIO")
                    ),
                    mapOf(
                        "username" to "admin",
                        "password" to "secret",
                        "role" to "Administrator", 
                        "permissions" to listOf("READ_PORTFOLIO", "WRITE_PORTFOLIO", "DELETE_PORTFOLIO", "MANAGE_USERS", "VIEW_ANALYTICS")
                    )
                ),
                "note" to "These are mock users for testing. Use /api/auth/mock-login for programmatic authentication."
            )
        ))
    }
}

/**
 * Request data class for mock authentication API login.
 * 
 * This data class represents the request body structure for programmatic
 * authentication via the mock login API endpoint. It contains the necessary
 * credentials for user authentication.
 * 
 * @property username The username for authentication
 * @property password The password for authentication
 * 
 * @author Technology Portfolio Team
 * @since 1.0.0
 */
data class MockLoginRequest(
    val username: String,
    val password: String
) 