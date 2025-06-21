package com.company.techportfolio.portfolio.config

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import java.security.Key
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

/**
 * JWT Test Utilities for Technology Portfolio Service
 *
 * This utility class provides helper methods for generating and validating
 * JWT tokens during testing. It supports creating test tokens with custom
 * claims, authorities, and expiration times for security testing scenarios.
 *
 * Key features:
 * - JWT token generation with custom claims
 * - Authority/role injection for testing
 * - Configurable expiration times
 * - Test-specific JWT decoder
 * - Support for different signing algorithms
 *
 * Usage:
 * - Generate tokens for authenticated test scenarios
 * - Test different authority combinations
 * - Validate JWT authentication flow
 * - Test token expiration scenarios
 *
 * @author Technology Portfolio Team
 * @since 1.0.0
 */
object JwtTestUtils {

    private const val TEST_SECRET = "test-secret-key-for-unit-tests-only-not-for-production-must-be-long-enough"
    private val signingKey: Key = Keys.hmacShaKeyFor(TEST_SECRET.toByteArray())

    /**
     * Generates a JWT token for testing purposes.
     *
     * Creates a signed JWT token with the specified username, authorities,
     * and optional expiration time. The token uses HMAC-SHA512 signing
     * for consistency with the production configuration.
     *
     * @param username The username to include in the token subject
     * @param authorities List of authorities/roles for the user
     * @param expirationMinutes Minutes until token expiration (default: 60)
     * @return Generated JWT token string
     */
    fun generateTestToken(
        username: String,
        authorities: List<String> = listOf("ROLE_USER"),
        expirationMinutes: Long = 60
    ): String {
        val now = Instant.now()
        val expiryDate = now.plus(expirationMinutes, ChronoUnit.MINUTES)

        return Jwts.builder()
            .setSubject(username)
            .claim("authorities", authorities)
            .claim("sessionIndex", "test-session-${UUID.randomUUID()}")
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(expiryDate))
            .signWith(signingKey, SignatureAlgorithm.HS512)
            .compact()
    }

    /**
     * Generates an expired JWT token for testing expiration scenarios.
     *
     * Creates a JWT token that is already expired, useful for testing
     * token validation and expiration handling.
     *
     * @param username The username to include in the token subject
     * @param authorities List of authorities/roles for the user
     * @return Generated expired JWT token string
     */
    fun generateExpiredTestToken(
        username: String,
        authorities: List<String> = listOf("ROLE_USER")
    ): String {
        val now = Instant.now()
        val expiryDate = now.minus(1, ChronoUnit.HOURS) // Expired 1 hour ago

        return Jwts.builder()
            .setSubject(username)
            .claim("authorities", authorities)
            .claim("sessionIndex", "test-session-${UUID.randomUUID()}")
            .setIssuedAt(Date.from(now.minus(2, ChronoUnit.HOURS)))
            .setExpiration(Date.from(expiryDate))
            .signWith(signingKey, SignatureAlgorithm.HS512)
            .compact()
    }

    /**
     * Generates a JWT token with ADMIN role for testing admin scenarios.
     *
     * Creates a JWT token with ADMIN role, useful for testing endpoints
     * that require administrative privileges.
     *
     * @param username The username to include in the token subject
     * @return Generated JWT token string with ADMIN role
     */
    fun generateAdminTestToken(username: String): String {
        return generateTestToken(username, listOf("ROLE_ADMIN", "ROLE_USER"))
    }

    /**
     * Generates a JWT token with multiple roles for testing complex scenarios.
     *
     * Creates a JWT token with multiple authorities, useful for testing
     * endpoints with complex authorization requirements.
     *
     * @param username The username to include in the token subject
     * @param roles List of roles to include in the token
     * @return Generated JWT token string with multiple roles
     */
    fun generateMultiRoleTestToken(username: String, roles: List<String>): String {
        return generateTestToken(username, roles)
    }

    /**
     * Creates a JWT decoder for testing purposes.
     *
     * Returns a JWT decoder configured with the test signing key,
     * useful for validating tokens in test scenarios.
     *
     * @return JwtDecoder configured for test environment
     */
    fun createTestJwtDecoder(): JwtDecoder {
        return NimbusJwtDecoder.withSecretKey(signingKey as javax.crypto.SecretKey).build()
    }

    /**
     * Validates a JWT token and returns the decoded JWT object.
     *
     * Decodes and validates a JWT token using the test signing key,
     * returning the JWT object if valid or throwing an exception if invalid.
     *
     * @param token JWT token string to validate
     * @return Decoded JWT object
     * @throws Exception if token is invalid or expired
     */
    fun validateTestToken(token: String): Jwt {
        val decoder = createTestJwtDecoder()
        return decoder.decode(token)
    }

    /**
     * Extracts username from a JWT token without full validation.
     *
     * Performs basic token parsing to extract the username claim.
     * This is useful for logging and testing purposes.
     *
     * @param token JWT token string
     * @return Username extracted from token, or null if extraction fails
     */
    fun extractUsernameFromToken(token: String): String? {
        return try {
            val claims = Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .body
            claims.subject
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Extracts authorities from a JWT token without full validation.
     *
     * Performs basic token parsing to extract the authorities claim.
     * This is useful for testing authorization scenarios.
     *
     * @param token JWT token string
     * @return List of authorities extracted from token, or null if extraction fails
     */
    fun extractAuthoritiesFromToken(token: String): List<String>? {
        return try {
            val claims = Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .body
            @Suppress("UNCHECKED_CAST")
            claims["authorities"] as? List<String>
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Checks if a JWT token has expired.
     *
     * Compares the token's expiration time with the current system time.
     * This is a lightweight check that doesn't perform full token validation.
     *
     * @param token JWT token string to check
     * @return true if token is expired, false otherwise
     */
    fun isTokenExpired(token: String): Boolean {
        return try {
            val claims = Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .body
            val expiration = claims.expiration
            expiration.before(Date())
        } catch (e: Exception) {
            true
        }
    }

    /**
     * Creates an Authorization header value with a JWT token.
     *
     * Formats a JWT token as a proper Authorization header value
     * for use in HTTP requests during testing.
     *
     * @param token JWT token string
     * @return Authorization header value (e.g., "Bearer <token>")
     */
    fun createAuthorizationHeader(token: String): String {
        return "Bearer $token"
    }
} 