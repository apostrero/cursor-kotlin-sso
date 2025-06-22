package com.company.techportfolio.gateway.adapter.outbound.jwt

import com.company.techportfolio.gateway.adapter.out.jwt.JwtAuthenticationAdapter
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.util.ReflectionTestUtils
import java.util.*

/**
 * Unit test class for the JwtAuthenticationAdapter.
 *
 * This test class verifies the behavior of the JwtAuthenticationAdapter which handles
 * JWT token generation, validation, and management operations. It tests all aspects
 * of JWT token lifecycle including creation, validation, expiration, and error handling.
 *
 * Test coverage includes:
 * - JWT token generation with various parameter combinations
 * - Token validation for valid, expired, and invalid tokens
 * - Token refresh operations
 * - Username and authorities extraction from tokens
 * - Token expiration detection and handling
 * - Error handling for malformed and invalid tokens
 * - Edge cases with null values and empty collections
 *
 * Testing approach:
 * - Uses reflection to set private fields for testing
 * - Tests with known JWT secret for deterministic results
 * - Validates JWT token structure and claims
 * - Tests temporal aspects with manual token creation
 * - Verifies error handling and edge cases
 *
 * @author Technology Portfolio Team
 * @since 1.0.0
 */
class JwtAuthenticationAdapterTest {

    private lateinit var jwtAuthenticationAdapter: JwtAuthenticationAdapter
    private val jwtSecret = "test-secret-key-for-jwt-testing-purposes-must-be-long-enough-for-hs512-algorithm-which-requires-at-least-512-bits"
    private val jwtExpiration = 3600L

    /**
     * Sets up test fixtures before each test method.
     *
     * Initializes the JwtAuthenticationAdapter and configures it with test values
     * using reflection to set private fields. This ensures consistent test environment.
     */
    @BeforeEach
    fun setUp() {
        jwtAuthenticationAdapter = JwtAuthenticationAdapter()
        ReflectionTestUtils.setField(jwtAuthenticationAdapter, "jwtSecret", jwtSecret)
        ReflectionTestUtils.setField(jwtAuthenticationAdapter, "jwtExpiration", jwtExpiration)
    }

    /**
     * Tests JWT token generation with complete parameter set.
     *
     * Verifies that the adapter can generate valid JWT tokens with all required
     * claims including username, authorities, and session index. Also validates
     * the token structure and content by parsing the generated token.
     *
     * Expected behavior:
     * - Generates non-empty JWT token
     * - Token has correct 3-part structure (header.payload.signature)
     * - Token contains all provided claims
     * - Token is properly signed and parseable
     */
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

    /**
     * Tests JWT token generation with null session index.
     *
     * Verifies that the adapter can handle null session index values gracefully
     * and still generate valid tokens. This is important for authentication flows
     * that don't use session-based tracking.
     *
     * Expected behavior:
     * - Generates valid token despite null session index
     * - Token contains null for sessionIndex claim
     * - Other claims are populated correctly
     */
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

    /**
     * Tests JWT token generation with empty authorities list.
     *
     * Verifies that the adapter can handle users with no authorities/roles
     * and still generate valid tokens. This supports scenarios where users
     * may have minimal or no permissions.
     *
     * Expected behavior:
     * - Generates valid token with empty authorities
     * - Token contains empty list for authorities claim
     * - Other claims are populated correctly
     */
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

    /**
     * Tests successful validation of a valid JWT token.
     *
     * Verifies that the adapter can validate tokens it generates and return
     * appropriate validation results with all user information extracted.
     *
     * Expected behavior:
     * - Returns valid TokenValidationResult
     * - Extracts all user information correctly
     * - Detects token is not expired
     * - Provides issued and expiration timestamps
     */
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

    /**
     * Tests validation of an expired JWT token.
     *
     * Verifies that the adapter correctly detects expired tokens and returns
     * appropriate validation results. This test manually creates an expired token
     * to test the expiration detection logic.
     *
     * Expected behavior:
     * - Returns invalid TokenValidationResult due to expiration
     * - Extracts user information from expired token
     * - Correctly identifies token as expired
     * - Provides appropriate error message
     */
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

    /**
     * Tests handling of tokens with invalid signatures.
     *
     * Verifies that the adapter properly rejects tokens with invalid signatures
     * and returns appropriate error responses. This is critical for security.
     *
     * Expected behavior:
     * - Returns invalid TokenValidationResult
     * - Does not extract any user information
     * - Provides appropriate error message
     * - Handles security exception gracefully
     */
    @Test
    fun `should handle invalid token signature`() {
        // Given
        val invalidToken =
            "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImF1dGhvcml0aWVzIjpbIlJPTEVfVVNFUiJdLCJpYXQiOjE2MzQ1NjE0NzIsImV4cCI6MTYzNDU2NTA3Mn0.invalid-signature"

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

    /**
     * Tests handling of malformed JWT tokens.
     *
     * Verifies that the adapter properly handles tokens that don't follow
     * JWT format and returns appropriate error responses.
     *
     * Expected behavior:
     * - Returns invalid TokenValidationResult
     * - Handles parsing exception gracefully
     * - Provides appropriate error message
     */
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

    /**
     * Tests username extraction from valid JWT tokens.
     *
     * Verifies that the adapter can extract username information from tokens
     * without performing full validation. This is useful for logging and auditing.
     *
     * Expected behavior:
     * - Extracts username from valid token
     * - Returns null for invalid tokens
     * - Handles extraction without full validation
     */
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

    /**
     * Tests username extraction from invalid JWT tokens.
     *
     * Verifies that the adapter gracefully handles invalid tokens when
     * attempting to extract username information, returning null instead
     * of throwing exceptions.
     *
     * Expected behavior:
     * - Returns null for invalid tokens
     * - Handles parsing errors gracefully
     * - Does not throw exceptions
     * - Provides safe extraction for invalid input
     */
    @Test
    fun `should return null when extracting username from invalid token`() {
        // Given
        val invalidToken = "invalid.jwt.token"

        // When
        val extractedUsername = jwtAuthenticationAdapter.extractUsernameFromToken(invalidToken)

        // Then
        assertNull(extractedUsername)
    }

    /**
     * Tests authorities extraction from valid JWT tokens.
     *
     * Verifies that the adapter can extract authorities/roles information
     * from valid tokens. This is useful for authorization decisions and
     * user context establishment.
     *
     * Expected behavior:
     * - Extracts authorities list from valid token
     * - Preserves authority order and content
     * - Handles multiple authorities correctly
     * - Provides accurate role information
     */
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

    /**
     * Tests authorities extraction from invalid JWT tokens.
     *
     * Verifies that the adapter gracefully handles invalid tokens when
     * attempting to extract authorities information, returning null instead
     * of throwing exceptions.
     *
     * Expected behavior:
     * - Returns null for invalid tokens
     * - Handles parsing errors gracefully
     * - Does not throw exceptions
     * - Provides safe extraction for invalid input
     */
    @Test
    fun `should return null when extracting authorities from invalid token`() {
        // Given
        val invalidToken = "invalid.jwt.token"

        // When
        val extractedAuthorities = jwtAuthenticationAdapter.extractAuthoritiesFromToken(invalidToken)

        // Then
        assertNull(extractedAuthorities)
    }

    /**
     * Tests expiration detection for expired JWT tokens.
     *
     * Verifies that the adapter can correctly identify tokens that have
     * passed their expiration time. This test creates a manually expired
     * token to test the expiration detection logic.
     *
     * Expected behavior:
     * - Correctly identifies expired tokens
     * - Returns true for tokens past expiration time
     * - Handles temporal comparisons accurately
     * - Supports token lifecycle management
     */
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

    /**
     * Tests expiration detection for non-expired JWT tokens.
     *
     * Verifies that the adapter correctly identifies tokens that are still
     * valid and have not reached their expiration time.
     *
     * Expected behavior:
     * - Correctly identifies non-expired tokens
     * - Returns false for tokens within validity period
     * - Handles temporal comparisons accurately
     * - Supports active token validation
     */
    @Test
    fun `should detect non-expired token correctly`() {
        // Given
        val validToken = jwtAuthenticationAdapter.generateToken("testuser", listOf("ROLE_USER"), "session")

        // When
        val isExpired = jwtAuthenticationAdapter.isTokenExpired(validToken)

        // Then
        assertFalse(isExpired)
    }

    /**
     * Tests expiration detection for invalid JWT tokens.
     *
     * Verifies that the adapter treats invalid tokens as expired for
     * security purposes. Invalid tokens should not be considered valid
     * regardless of their expiration status.
     *
     * Expected behavior:
     * - Treats invalid tokens as expired
     * - Returns true for malformed tokens
     * - Provides secure default behavior
     * - Handles invalid input gracefully
     */
    @Test
    fun `should return true for expired when token is invalid`() {
        // Given
        val invalidToken = "invalid.jwt.token"

        // When
        val isExpired = jwtAuthenticationAdapter.isTokenExpired(invalidToken)

        // Then
        assertTrue(isExpired) // Invalid tokens are considered expired
    }

    /**
     * Tests successful JWT token refresh functionality.
     *
     * Verifies that the adapter can refresh valid tokens by creating new
     * tokens with updated timestamps while preserving the original claims.
     * This is essential for maintaining user sessions.
     *
     * Expected behavior:
     * - Creates new token with fresh timestamps
     * - Preserves original token claims
     * - Returns different token than original
     * - Maintains user context and permissions
     */
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

    /**
     * Tests token refresh failure for invalid tokens.
     *
     * Verifies that the adapter gracefully handles refresh attempts on
     * invalid tokens by returning null instead of throwing exceptions.
     *
     * Expected behavior:
     * - Returns null for invalid tokens
     * - Does not throw exceptions
     * - Handles invalid input gracefully
     * - Provides safe refresh operation
     */
    @Test
    fun `should return null when refreshing invalid token`() {
        // Given
        val invalidToken = "invalid.jwt.token"

        // When
        val refreshedToken = jwtAuthenticationAdapter.refreshToken(invalidToken)

        // Then
        assertNull(refreshedToken)
    }

    /**
     * Tests unsupported authenticateUser method behavior.
     *
     * Verifies that the adapter throws UnsupportedOperationException for
     * the authenticateUser method, as this adapter focuses on JWT operations
     * rather than user authentication.
     *
     * Expected behavior:
     * - Throws UnsupportedOperationException
     * - Indicates method is not supported
     * - Maintains clear interface boundaries
     * - Prevents misuse of adapter
     */
    @Test
    fun `should throw UnsupportedOperationException for authenticateUser method`() {
        // Given
        val mockAuthentication = org.mockito.Mockito.mock(org.springframework.security.core.Authentication::class.java)

        // When & Then
        assertThrows(UnsupportedOperationException::class.java) {
            jwtAuthenticationAdapter.authenticateUser(mockAuthentication)
        }
    }

    /**
     * Tests token validation with missing authorities claim.
     *
     * Verifies that the adapter gracefully handles tokens that don't contain
     * the authorities claim by defaulting to an empty authorities list.
     *
     * Expected behavior:
     * - Validates token successfully despite missing claim
     * - Defaults to empty authorities list
     * - Handles optional claims gracefully
     * - Maintains backward compatibility
     */
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

    /**
     * Tests token validation with missing sessionIndex claim.
     *
     * Verifies that the adapter gracefully handles tokens that don't contain
     * the sessionIndex claim by setting it to null in the validation result.
     *
     * Expected behavior:
     * - Validates token successfully despite missing claim
     * - Sets sessionIndex to null when not present
     * - Handles optional claims gracefully
     * - Supports stateless authentication scenarios
     */
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