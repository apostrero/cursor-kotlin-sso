package com.company.techportfolio.gateway.adapter.inbound.web

import com.company.techportfolio.gateway.domain.model.AuthenticationResult
import com.company.techportfolio.gateway.domain.model.TokenValidationResult
import com.company.techportfolio.gateway.domain.port.AuthorizationResult
import com.company.techportfolio.gateway.domain.service.AuthenticationService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthenticationController(
    private val authenticationService: AuthenticationService
) {

    @PostMapping("/authenticate")
    fun authenticateUser(authentication: Authentication): ResponseEntity<AuthenticationResult> {
        val result = authenticationService.authenticateUser(authentication)
        return if (result.isAuthenticated) {
            ResponseEntity.ok(result)
        } else {
            ResponseEntity.badRequest().body(result)
        }
    }

    @PostMapping("/validate")
    fun validateToken(@RequestHeader("Authorization") authorization: String): ResponseEntity<TokenValidationResult> {
        val token = authorization.removePrefix("Bearer ")
        val result = authenticationService.validateToken(token)
        return if (result.isValid) {
            ResponseEntity.ok(result)
        } else {
            ResponseEntity.status(401).body(result)
        }
    }

    @PostMapping("/refresh")
    fun refreshToken(@RequestHeader("Authorization") authorization: String): ResponseEntity<Map<String, String>> {
        val token = authorization.removePrefix("Bearer ")
        val refreshedToken = authenticationService.refreshToken(token)
        return if (refreshedToken != null) {
            ResponseEntity.ok(mapOf("token" to refreshedToken))
        } else {
            ResponseEntity.status(401).body(mapOf("error" to "Token refresh failed"))
        }
    }

    @PostMapping("/authorize")
    fun authorizeUser(
        @RequestParam username: String,
        @RequestParam resource: String,
        @RequestParam action: String
    ): ResponseEntity<AuthorizationResult> {
        val result = authenticationService.authorizeUser(username, resource, action)
        return if (result.isAuthorized) {
            ResponseEntity.ok(result)
        } else {
            ResponseEntity.status(403).body(result)
        }
    }

    @GetMapping("/health")
    fun health(): ResponseEntity<Map<String, String>> {
        return ResponseEntity.ok(mapOf("status" to "UP", "service" to "authentication"))
    }
} 