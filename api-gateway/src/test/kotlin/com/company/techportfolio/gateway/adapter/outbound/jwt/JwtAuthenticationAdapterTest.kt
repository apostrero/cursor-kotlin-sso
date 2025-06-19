package com.company.techportfolio.gateway.adapter.outbound.jwt

import com.company.techportfolio.gateway.adapter.out.jwt.JwtAuthenticationAdapter
import com.company.techportfolio.gateway.domain.model.TokenValidationResult
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.springframework.test.util.ReflectionTestUtils
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

class JwtAuthenticationAdapterTest {

    private lateinit var jwtAuthenticationAdapter: JwtAuthenticationAdapter
    private val jwtSecret = "test-secret-key-for-jwt-testing-purposes-must-be-long-enough"
    private val jwtExpiration = 3600L

    @BeforeEach
    fun setUp() {
        jwtAuthenticationAdapter = JwtAuthenticationAdapter()
        ReflectionTestUtils.setField(jwtAuthenticationAdapter, "jwtSecret", jwtSecret)
        ReflectionTestUtils.setField(jwtAuthenticationAdapter, "jwtExpiration", jwtExpiration)
    }

    @Test
    fun `should generate valid JWT token`() {
        // Given
        val username = "testuser"
        val authorities = listOf("ROLE_USER", "READ_PORTFOLIO")
        val sessionIndex = "session-123"

        // When
        val token = jwtAuthenticationAdapter.generateToken(username, authorities, sessionIndex)

        // Then
        assertNotNull(token)
        assertTrue(token.isNotEmpty())
        
        // Verify token structure (should have 3 parts separated by dots)
        val tokenParts = token.split(".")
        assertEquals(3, tokenParts.size)
        
        // Verify token content by parsing it
        val signingKey = Keys.hmacShaKeyFor(jwtSecret.toByteArray())
        val claims = Jwts.parserBuilder()
            .setSigningKey(signingKey)
            .build()
            .parseClaimsJws(token)
            .body

        assertEquals(username, claims.subject)
        assertEquals(authorities, claims["authorities"])
        assertEquals(sessionIndex, claims["sessionIndex"])
        assertNotNull(claims.issuedAt)
        assertNotNull(claims.expiration)
    }

    @Test
    fun `should generate token with null session index`() {
        // Given
        val username = "testuser"
        val authorities = listOf("ROLE_USER")
        val sessionIndex: String? = null

        // When
        val token = jwtAuthenticationAdapter.generateToken(username, authorities, sessionIndex)

        // Then
        assertNotNull(token)
        
        val signingKey = Keys.hmacShaKeyFor(jwtSecret.toByteArray())
        val claims = Jwts.parserBuilder()
            .setSigningKey(signingKey)
            .build()
            .parseClaimsJws(token)
            .body

        assertEquals(username, claims.subject)
        assertEquals(authorities, claims["authorities"])
        assertNull(claims["sessionIndex"])
    }

    @Test
    fun `should generate token with empty authorities`() {
        // Given
        val username = "testuser"
        val authorities = emptyList<String>()
        val sessionIndex = "session-123"

        // When
        val token = jwtAuthenticationAdapter.generateToken(username, authorities, sessionIndex)

        // Then
        assertNotNull(token)
        
        val signingKey = Keys.hmacShaKeyFor(jwtSecret.toByteArray())
        val claims = Jwts.parserBuilder()
            .setSigningKey(signingKey)
            .build()
            .parseClaimsJws(token)
            .body

        assertEquals(username, claims.subject)
        assertEquals(authorities, claims["authorities"])
        assertEquals(sessionIndex, claims["sessionIndex"])
    }

    @Test
    fun `should validate valid token successfully`() {
        // Given
        val username = "testuser"
        val authorities = listOf("ROLE_USER", "READ_PORTFOLIO")
        val sessionIndex = "session-123"
        val token = jwtAuthenticationAdapter.generateToken(username, authorities, sessionIndex)

        // When
        val result = jwtAuthenticationAdapter.validateToken(token)

        // Then
        assertTrue(result.isValid)
        assertEquals(username, result.username)
        assertEquals(authorities, result.authorities)
        assertEquals(sessionIndex, result.sessionIndex)
        assertNotNull(result.issuedAt)
        assertNotNull(result.expiresAt)
        assertFalse(result.isExpired) // Should not be expired for newly generated token
        assertNull(result.errorMessage)
    }

    @Test
    fun `should validate expired token correctly`() {
        // Given - Create a token that's already expired
        val username = "testuser"
        val authorities = listOf("ROLE_USER")
        val sessionIndex = "session-123"
        
        // Create an expired token manually
        val signingKey = Keys.hmacShaKeyFor(jwtSecret.toByteArray())
        val expiredToken = Jwts.builder()
            .setSubject(username)
            .claim("authorities", authorities)
            .claim("sessionIndex", sessionIndex)
            .setIssuedAt(Date.from(java.time.Instant.now().minusSeconds(7200))) // 2 hours ago
            .setExpiration(Date.from(java.time.Instant.now().minusSeconds(3600))) // 1 hour ago (expired)
            .signWith(signingKey, io.jsonwebtoken.SignatureAlgorithm.HS512)
            .compact()

        // When
        val result = jwtAuthenticationAdapter.validateToken(expiredToken)

        // Then
        assertFalse(result.isValid) // Should be invalid due to expiration
        assertEquals(username, result.username)
        assertEquals(authorities, result.authorities)
        assertEquals(sessionIndex, result.sessionIndex)
        assertTrue(result.isExpired)
        assertEquals("Token has expired", result.errorMessage)
    }

    @Test
    fun `should handle invalid token signature`() {
        // Given
        val invalidToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImF1dGhvcml0aWVzIjpbIlJPTEVfVVNFUiJdLCJpYXQiOjE2MzQ1NjE0NzIsImV4cCI6MTYzNDU2NTA3Mn0.invalid-signature"

        // When
        val result = jwtAuthenticationAdapter.validateToken(invalidToken)

        // Then
        assertFalse(result.isValid)
        assertNull(result.username)
        assertEquals(emptyList<String>(), result.authorities)
        assertNull(result.sessionIndex)
        assertNull(result.issuedAt)
        assertNull(result.expiresAt)
        assertFalse(result.isExpired)
        assertTrue(result.errorMessage!!.contains("Token validation failed"))
    }

    @Test
    fun `should handle malformed token`() {
        // Given
        val malformedToken = "not.a.valid.jwt.token"

        // When
        val result = jwtAuthenticationAdapter.validateToken(malformedToken)

        // Then
        assertFalse(result.isValid)
        assertTrue(result.errorMessage!!.contains("Token validation failed"))
    }

    @Test
    fun `should extract username from valid token`() {
        // Given
        val username = "testuser"
        val token = jwtAuthenticationAdapter.generateToken(username, listOf("ROLE_USER"), "session")

        // When
        val extractedUsername = jwtAuthenticationAdapter.extractUsernameFromToken(token)

        // Then
        assertEquals(username, extractedUsername)
    }

    @Test
    fun `should return null when extracting username from invalid token`() {
        // Given
        val invalidToken = "invalid.jwt.token"

        // When
        val extractedUsername = jwtAuthenticationAdapter.extractUsernameFromToken(invalidToken)

        // Then
        assertNull(extractedUsername)
    }

    @Test
    fun `should extract authorities from valid token`() {
        // Given
        val authorities = listOf("ROLE_USER", "READ_PORTFOLIO", "WRITE_PORTFOLIO")
        val token = jwtAuthenticationAdapter.generateToken("testuser", authorities, "session")

        // When
        val extractedAuthorities = jwtAuthenticationAdapter.extractAuthoritiesFromToken(token)

        // Then
        assertEquals(authorities, extractedAuthorities)
    }

    @Test
    fun `should return null when extracting authorities from invalid token`() {
        // Given
        val invalidToken = "invalid.jwt.token"

        // When
        val extractedAuthorities = jwtAuthenticationAdapter.extractAuthoritiesFromToken(invalidToken)

        // Then
        assertNull(extractedAuthorities)
    }

    @Test
    fun `should detect expired token correctly`() {
        // Given - Create an expired token
        val signingKey = Keys.hmacShaKeyFor(jwtSecret.toByteArray())
        val expiredToken = Jwts.builder()
            .setSubject("testuser")
            .claim("authorities", listOf("ROLE_USER"))
            .setIssuedAt(Date.from(java.time.Instant.now().minusSeconds(7200))) // 2 hours ago
            .setExpiration(Date.from(java.time.Instant.now().minusSeconds(3600))) // 1 hour ago (expired)
            .signWith(signingKey, io.jsonwebtoken.SignatureAlgorithm.HS512)
            .compact()

        // When
        val isExpired = jwtAuthenticationAdapter.isTokenExpired(expiredToken)

        // Then
        assertTrue(isExpired)
    }

    @Test
    fun `should detect non-expired token correctly`() {
        // Given
        val validToken = jwtAuthenticationAdapter.generateToken("testuser", listOf("ROLE_USER"), "session")

        // When
        val isExpired = jwtAuthenticationAdapter.isTokenExpired(validToken)

        // Then
        assertFalse(isExpired)
    }

    @Test
    fun `should return true for expired when token is invalid`() {
        // Given
        val invalidToken = "invalid.jwt.token"

        // When
        val isExpired = jwtAuthenticationAdapter.isTokenExpired(invalidToken)

        // Then
        assertTrue(isExpired) // Invalid tokens are considered expired
    }

    @Test
    fun `should refresh token successfully`() {
        // Given
        val username = "testuser"
        val authorities = listOf("ROLE_USER", "READ_PORTFOLIO")
        val sessionIndex = "session-123"
        val originalToken = jwtAuthenticationAdapter.generateToken(username, authorities, sessionIndex)

        // Wait a moment to ensure different timestamps
        Thread.sleep(1000)

        // When
        val refreshedToken = jwtAuthenticationAdapter.refreshToken(originalToken)

        // Then
        assertNotNull(refreshedToken)
        assertNotEquals(originalToken, refreshedToken) // Should be different token
        
        // Verify refreshed token has same claims
        val result = jwtAuthenticationAdapter.validateToken(refreshedToken!!)
        assertTrue(result.isValid)
        assertEquals(username, result.username)
        assertEquals(authorities, result.authorities)
        assertEquals(sessionIndex, result.sessionIndex)
    }

    @Test
    fun `should return null when refreshing invalid token`() {
        // Given
        val invalidToken = "invalid.jwt.token"

        // When
        val refreshedToken = jwtAuthenticationAdapter.refreshToken(invalidToken)

        // Then
        assertNull(refreshedToken)
    }

    @Test
    fun `should throw UnsupportedOperationException for authenticateUser method`() {
        // Given
        val mockAuthentication = org.mockito.Mockito.mock(org.springframework.security.core.Authentication::class.java)

        // When & Then
        assertThrows(UnsupportedOperationException::class.java) {
            jwtAuthenticationAdapter.authenticateUser(mockAuthentication)
        }
    }

    @Test
    fun `should handle token validation with missing authorities claim`() {
        // Given - Create token without authorities claim
        val signingKey = Keys.hmacShaKeyFor(jwtSecret.toByteArray())
        val tokenWithoutAuthorities = Jwts.builder()
            .setSubject("testuser")
            .claim("sessionIndex", "session-123")
            .setIssuedAt(Date())
            .setExpiration(Date(System.currentTimeMillis() + jwtExpiration * 1000))
            .signWith(signingKey, io.jsonwebtoken.SignatureAlgorithm.HS512)
            .compact()

        // When
        val result = jwtAuthenticationAdapter.validateToken(tokenWithoutAuthorities)

        // Then
        assertTrue(result.isValid)
        assertEquals("testuser", result.username)
        assertEquals(emptyList<String>(), result.authorities) // Should default to empty list
        assertEquals("session-123", result.sessionIndex)
    }

    @Test
    fun `should handle token validation with missing sessionIndex claim`() {
        // Given - Create token without sessionIndex claim
        val signingKey = Keys.hmacShaKeyFor(jwtSecret.toByteArray())
        val tokenWithoutSessionIndex = Jwts.builder()
            .setSubject("testuser")
            .claim("authorities", listOf("ROLE_USER"))
            .setIssuedAt(Date())
            .setExpiration(Date(System.currentTimeMillis() + jwtExpiration * 1000))
            .signWith(signingKey, io.jsonwebtoken.SignatureAlgorithm.HS512)
            .compact()

        // When
        val result = jwtAuthenticationAdapter.validateToken(tokenWithoutSessionIndex)

        // Then
        assertTrue(result.isValid)
        assertEquals("testuser", result.username)
        assertEquals(listOf("ROLE_USER"), result.authorities)
        assertNull(result.sessionIndex) // Should be null when not present
    }
} 