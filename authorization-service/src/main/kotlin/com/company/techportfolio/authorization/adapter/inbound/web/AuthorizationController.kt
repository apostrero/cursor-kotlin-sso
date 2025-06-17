package com.company.techportfolio.authorization.adapter.inbound.web

import com.company.techportfolio.authorization.domain.model.AuthorizationRequest
import com.company.techportfolio.authorization.domain.model.AuthorizationResponse
import com.company.techportfolio.authorization.domain.model.UserPermissions
import com.company.techportfolio.authorization.domain.service.AuthorizationService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import jakarta.validation.Valid

@RestController
@RequestMapping("/api/authorization")
class AuthorizationController(
    private val authorizationService: AuthorizationService
) {

    @PostMapping("/check")
    fun checkAuthorization(@RequestBody @Valid request: AuthorizationRequest): ResponseEntity<AuthorizationResponse> {
        val response = authorizationService.authorizeUser(request)
        return if (response.isAuthorized) {
            ResponseEntity.ok(response)
        } else {
            ResponseEntity.status(403).body(response)
        }
    }

    @GetMapping("/permissions")
    fun getUserPermissions(@RequestParam username: String): ResponseEntity<UserPermissions> {
        val permissions = authorizationService.getUserPermissions(username)
        return ResponseEntity.ok(permissions)
    }

    @GetMapping("/has-role")
    fun hasRole(
        @RequestParam username: String,
        @RequestParam role: String
    ): ResponseEntity<Boolean> {
        val hasRole = authorizationService.hasRole(username, role)
        return ResponseEntity.ok(hasRole)
    }

    @PostMapping("/has-any-role")
    fun hasAnyRole(
        @RequestBody request: Map<String, Any>
    ): ResponseEntity<Boolean> {
        val username = request["username"] as String
        val roles = request["roles"] as List<String>
        val hasAnyRole = authorizationService.hasAnyRole(username, roles)
        return ResponseEntity.ok(hasAnyRole)
    }

    @GetMapping("/has-permission")
    fun hasPermission(
        @RequestParam username: String,
        @RequestParam resource: String,
        @RequestParam action: String
    ): ResponseEntity<Boolean> {
        val hasPermission = authorizationService.hasPermission(username, resource, action)
        return ResponseEntity.ok(hasPermission)
    }

    @GetMapping("/health")
    fun health(): ResponseEntity<Map<String, String>> {
        return ResponseEntity.ok(mapOf("status" to "UP", "service" to "authorization"))
    }
} 