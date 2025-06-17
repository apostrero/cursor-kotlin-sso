package com.company.techportfolio.gateway.domain.port

import com.company.techportfolio.gateway.domain.model.AuthenticationResult
import com.company.techportfolio.gateway.domain.model.TokenValidationResult
import org.springframework.security.core.Authentication

interface AuthenticationPort {
    fun authenticateUser(authentication: Authentication): AuthenticationResult
    fun validateToken(token: String): TokenValidationResult
    fun refreshToken(token: String): String?
    fun generateToken(username: String, authorities: List<String>, sessionIndex: String?): String
    fun extractUsernameFromToken(token: String): String?
    fun extractAuthoritiesFromToken(token: String): List<String>?
    fun isTokenExpired(token: String): Boolean
} 