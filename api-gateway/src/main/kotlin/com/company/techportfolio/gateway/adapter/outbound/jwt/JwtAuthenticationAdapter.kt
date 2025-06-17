package com.company.techportfolio.gateway.adapter.out.jwt

import com.company.techportfolio.gateway.domain.model.TokenValidationResult
import com.company.techportfolio.gateway.domain.port.AuthenticationPort
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.security.Key
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.*

@Component
class JwtAuthenticationAdapter : AuthenticationPort {

    @Value("\${jwt.secret:default-secret-key-for-development-only}")
    private lateinit var jwtSecret: String

    @Value("\${jwt.expiration:3600}")
    private var jwtExpiration: Long = 3600

    private val signingKey: Key by lazy {
        Keys.hmacShaKeyFor(jwtSecret.toByteArray())
    }

    override fun authenticateUser(authentication: org.springframework.security.core.Authentication): com.company.techportfolio.gateway.domain.model.AuthenticationResult {
        // This method is not used in the JWT adapter as authentication is handled by SAML
        // The domain service will call generateToken instead
        throw UnsupportedOperationException("JWT adapter does not handle SAML authentication")
    }

    override fun validateToken(token: String): TokenValidationResult {
        return try {
            val claims = getClaimsFromToken(token)
            val username = claims.subject
            val authorities = claims["authorities"] as? List<String> ?: emptyList()
            val sessionIndex = claims["sessionIndex"] as? String
            val issuedAt = claims.issuedAt.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
            val expiresAt = claims.expiration.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
            
            if (LocalDateTime.now().isAfter(expiresAt)) {
                TokenValidationResult.expired(username, authorities, sessionIndex)
            } else {
                TokenValidationResult.valid(username, authorities, sessionIndex, issuedAt, expiresAt)
            }
        } catch (e: Exception) {
            TokenValidationResult.invalid("Token validation failed: ${e.message}")
        }
    }

    override fun refreshToken(token: String): String? {
        return try {
            val claims = getClaimsFromToken(token)
            val username = claims.subject
            val authorities = claims["authorities"] as? List<String> ?: emptyList()
            val sessionIndex = claims["sessionIndex"] as? String
            
            generateToken(username, authorities, sessionIndex)
        } catch (e: Exception) {
            null
        }
    }

    override fun generateToken(username: String, authorities: List<String>, sessionIndex: String?): String {
        val now = Instant.now()
        val expiryDate = now.plus(jwtExpiration, ChronoUnit.SECONDS)

        return Jwts.builder()
            .setSubject(username)
            .claim("authorities", authorities)
            .claim("sessionIndex", sessionIndex)
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(expiryDate))
            .signWith(signingKey, SignatureAlgorithm.HS512)
            .compact()
    }

    override fun extractUsernameFromToken(token: String): String? {
        return try {
            val claims = getClaimsFromToken(token)
            claims.subject
        } catch (e: Exception) {
            null
        }
    }

    override fun extractAuthoritiesFromToken(token: String): List<String>? {
        return try {
            val claims = getClaimsFromToken(token)
            @Suppress("UNCHECKED_CAST")
            claims["authorities"] as? List<String>
        } catch (e: Exception) {
            null
        }
    }

    override fun isTokenExpired(token: String): Boolean {
        return try {
            val claims = getClaimsFromToken(token)
            val expiration = claims.expiration
            expiration.before(Date())
        } catch (e: Exception) {
            true
        }
    }

    private fun getClaimsFromToken(token: String): Claims {
        return Jwts.parserBuilder()
            .setSigningKey(signingKey)
            .build()
            .parseClaimsJws(token)
            .body
    }
} 