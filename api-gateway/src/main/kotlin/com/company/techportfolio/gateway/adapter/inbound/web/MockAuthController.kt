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
import java.time.LocalDateTime

@Controller
@Profile("mock-auth")
class MockAuthController(
    private val authenticationPort: AuthenticationPort
) {

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

    @GetMapping("/api/auth/mock-success")
    @ResponseBody
    fun mockAuthSuccess(): ResponseEntity<Map<String, Any>> {
        val authentication = SecurityContextHolder.getContext().authentication
        
        if (authentication == null || !authentication.isAuthenticated) {
            return ResponseEntity.badRequest().body(
                mapOf("error" to "Not authenticated")
            )
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

        return ResponseEntity.ok(
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
        )
    }

    @PostMapping("/api/auth/mock-login")
    @ResponseBody
    fun mockApiLogin(
        @RequestBody loginRequest: MockLoginRequest
    ): ResponseEntity<Map<String, Any>> {
        // This endpoint allows programmatic login for testing
        val validCredentials = mapOf(
            "user1" to "password",
            "user2" to "password", 
            "admin" to "secret"
        )

        if (validCredentials[loginRequest.username] != loginRequest.password) {
            return ResponseEntity.badRequest().body(
                mapOf("error" to "Invalid credentials")
            )
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

        return ResponseEntity.ok(
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
        )
    }

    @GetMapping("/api/auth/mock-users")
    @ResponseBody
    fun getMockUsers(): ResponseEntity<Map<String, Any>> {
        return ResponseEntity.ok(
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
        )
    }
}

data class MockLoginRequest(
    val username: String,
    val password: String
) 