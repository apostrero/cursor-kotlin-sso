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

/**
 * JWT-based implementation of the AuthenticationPort for token operations.
 *
 * This adapter handles JWT token generation, validation, and management operations.
 * It implements the AuthenticationPort interface as an outbound adapter in the
 * hexagonal architecture, providing concrete JWT functionality for authentication.
 *
 * Key features:
 * - JWT token generation with configurable expiration
 * - Token validation with signature and expiration checking
 * - Token refresh functionality
 * - Claims extraction for username and authorities
 * - Secure HMAC-SHA512 signing algorithm
 *
 * Configuration properties:
 * - jwt.secret: Secret key for token signing (defaults to development key)
 * - jwt.expiration: Token expiration time in seconds (defaults to 3600)
 *
 * @property jwtSecret Secret key for JWT signing
 * @property jwtExpiration Token expiration time in seconds
 * @property signingKey Computed signing key from the secret
 *
 * @author Technology Portfolio Team
 * @since 1.0.0
 */
@Component
class JwtAuthenticationAdapter : AuthenticationPort {

    @Value("\${jwt.secret:default-secret-key-for-development-only}")
    private lateinit var jwtSecret: String

    @Value("\${jwt.expiration:3600}")
    private var jwtExpiration: Long = 3600

    private val signingKey: Key by lazy {
        Keys.hmacShaKeyFor(jwtSecret.toByteArray())
    }

    /**
     * Not implemented in JWT adapter as SAML authentication is handled elsewhere.
     *
     * This method is not used in the JWT adapter since authentication is handled
     * by SAML components. The domain service calls generateToken directly instead.
     *
     * @param authentication Spring Security Authentication object
     * @throws UnsupportedOperationException Always thrown as this operation is not supported
     */
    override fun authenticateUser(authentication: org.springframework.security.core.Authentication): com.company.techportfolio.gateway.domain.model.AuthenticationResult {
        // This method is not used in the JWT adapter as authentication is handled by SAML
        // The domain service will call generateToken instead
        throw UnsupportedOperationException("JWT adapter does not handle SAML authentication")
    }

    /**
     * Validates a JWT token and extracts user information.
     *
     * Performs comprehensive token validation including signature verification,
     * expiration checking, and claims extraction. Returns detailed validation
     * results with user information if the token is valid.
     *
     * @param token JWT token string to validate
     * @return TokenValidationResult containing validation status and extracted user information
     */
    override fun validateToken(token: String): TokenValidationResult {
        return try {
            val claims = getClaimsFromToken(token)
            val username = claims.subject
            @Suppress("UNCHECKED_CAST")
            val authorities = claims["authorities"] as? List<String> ?: emptyList()
            val sessionIndex = claims["sessionIndex"] as? String
            val issuedAt = claims.issuedAt.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
            val expiresAt = claims.expiration.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()

            if (LocalDateTime.now().isAfter(expiresAt)) {
                TokenValidationResult.expired(username, authorities, sessionIndex)
            } else {
                TokenValidationResult.valid(username, authorities, sessionIndex, issuedAt, expiresAt)
            }
        } catch (e: io.jsonwebtoken.ExpiredJwtException) {
            // Handle expired token specifically - extract claims from the exception
            val claims = e.claims
            val username = claims.subject
            @Suppress("UNCHECKED_CAST")
            val authorities = claims["authorities"] as? List<String> ?: emptyList()
            val sessionIndex = claims["sessionIndex"] as? String
            TokenValidationResult.expired(username, authorities, sessionIndex)
        } catch (e: Exception) {
            TokenValidationResult.invalid("Token validation failed: ${e.message}")
        }
    }

    /**
     * Refreshes an existing JWT token with extended expiration time.
     *
     * Extracts claims from the current token and generates a new token with
     * the same user information but extended expiration time.
     *
     * @param token Current JWT token to refresh
     * @return New JWT token string, or null if refresh fails
     */
    override fun refreshToken(token: String): String? {
        return try {
            val claims = getClaimsFromToken(token)
            val username = claims.subject
            @Suppress("UNCHECKED_CAST")
            val authorities = claims["authorities"] as? List<String> ?: emptyList()
            val sessionIndex = claims["sessionIndex"] as? String

            generateToken(username, authorities, sessionIndex)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Generates a new JWT token for the specified user with authorities and session information.
     *
     * Creates a signed JWT token containing user information, authorities,
     * and session details with appropriate expiration time using HMAC-SHA512 algorithm.
     *
     * @param username User's username to include in the token
     * @param authorities List of user authorities/roles
     * @param sessionIndex Optional session identifier for SAML sessions
     * @return Generated JWT token string
     */
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

    /**
     * Extracts the username from a JWT token without full validation.
     *
     * Performs basic token parsing to extract the username claim.
     * Used for logging and auditing purposes where full validation is not required.
     *
     * @param token JWT token string
     * @return Username extracted from token, or null if extraction fails
     */
    override fun extractUsernameFromToken(token: String): String? {
        return try {
            val claims = getClaimsFromToken(token)
            claims.subject
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Extracts the authorities/roles from a JWT token without full validation.
     *
     * Performs basic token parsing to extract the authorities claim.
     * Used for authorization checks where full validation is not required.
     *
     * @param token JWT token string
     * @return List of authorities extracted from token, or null if extraction fails
     */
    override fun extractAuthoritiesFromToken(token: String): List<String>? {
        return try {
            val claims = getClaimsFromToken(token)
            @Suppress("UNCHECKED_CAST")
            claims["authorities"] as? List<String>
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Checks if a JWT token has expired based on its expiration claim.
     *
     * Compares the token's expiration time with the current system time.
     * This is a lightweight check that doesn't perform full token validation.
     *
     * @param token JWT token string to check
     * @return true if token is expired, false otherwise
     */
    override fun isTokenExpired(token: String): Boolean {
        return try {
            val claims = getClaimsFromToken(token)
            val expiration = claims.expiration
            expiration.before(Date())
        } catch (e: Exception) {
            true
        }
    }

    /**
     * Extracts and validates claims from a JWT token.
     *
     * Parses the JWT token using the configured signing key and extracts
     * the claims. Throws exceptions if the token is invalid or expired.
     *
     * @param token JWT token string to parse
     * @return Claims object containing token data
     * @throws Exception if token parsing or validation fails
     */
    private fun getClaimsFromToken(token: String): Claims {
        return Jwts.parserBuilder()
            .setSigningKey(signingKey)
            .build()
            .parseClaimsJws(token)
            .body
    }
} 