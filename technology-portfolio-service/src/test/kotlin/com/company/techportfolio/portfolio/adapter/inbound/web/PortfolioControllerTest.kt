package com.company.techportfolio.portfolio.adapter.inbound.web

import com.company.techportfolio.portfolio.domain.model.*
import com.company.techportfolio.portfolio.domain.service.PortfolioService
import com.company.techportfolio.shared.domain.model.*
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.hamcrest.Matchers.containsString
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.beans.factory.annotation.Autowired
import org.mockito.Mockito.*
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.mockito.kotlin.verify

/**
 * Unit tests for PortfolioController.
 * 
 * This test class verifies the functionality of the PortfolioController, which
 * is the web adapter layer in the hexagonal architecture. It tests the controller's
 * ability to handle HTTP requests, validate input, delegate to the domain service,
 * and return appropriate HTTP responses.
 * 
 * ## Test Coverage:
 * - Portfolio CRUD operations (Create, Read, Update, Delete)
 * - Technology management operations
 * - Authentication and authorization handling
 * - Input validation and error handling
 * - HTTP status code verification
 * - Response body validation
 * - JWT token processing
 * 
 * ## Testing Approach:
 * - Uses @WebMvcTest for proper Spring Security configuration
 * - Uses Mockito for mocking the PortfolioService
 * - Tests both successful and error scenarios
 * - Verifies HTTP status codes, headers, and response bodies
 * - Tests JWT token extraction and validation
 * 
 * @author Technology Portfolio Team
 * @since 1.0.0
 * @see PortfolioController
 * @see PortfolioService
 */
@WebMvcTest(PortfolioController::class)
@Import(PortfolioControllerTestExceptionHandler::class)
@ActiveProfiles("test")
class PortfolioControllerTest {

    /**
     * Mock of the domain service for portfolio operations.
     */
    @MockBean
    private lateinit var portfolioService: PortfolioService

    /**
     * MockMvc instance for HTTP request testing.
     */
    @Autowired
    private lateinit var mockMvc: MockMvc

    /**
     * ObjectMapper for JSON serialization/deserialization.
     */
    private val objectMapper: ObjectMapper = jacksonObjectMapper()

    /**
     * Fixed test date/time for consistent testing.
     */
    private val testDateTime = LocalDateTime.of(2024, 1, 15, 10, 30, 0)

    /**
     * Test JWT token for authenticated requests.
     * Uses a valid token value and includes the 'scope' claim for USER role.
     */
    private val testJwt = Jwt.withTokenValue("eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjMiLCJzY29wZSI6IlVTRVIiLCJhdWQiOiJ0ZXN0LWF1ZGllbmNlIiwiaXNzIjoiaHR0cHM6Ly90ZXN0Lmlzc3Vlci5jb20iLCJpYXQiOjE2NzM4OTYwMDAsImV4cCI6MTY3Mzg5OTYwMH0.test-signature")
        .subject("123")
        .claim("scope", "USER")
        .header("alg", "RS256")
        .build()

    /**
     * Invalid JWT token for testing error scenarios.
     * Uses an invalid subject but still includes the 'scope' claim for USER role.
     */
    private val invalidJwt = Jwt.withTokenValue("eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJpbnZhbGlkLXVzZXItaWQiLCJzY29wZSI6IlVTRVIiLCJhdWQiOiJ0ZXN0LWF1ZGllbmNlIiwiaXNzIjoiaHR0cHM6Ly90ZXN0Lmlzc3Vlci5jb20iLCJpYXQiOjE2NzM4OTYwMDAsImV4cCI6MTY3Mzg5OTYwMH0.test-signature")
        .subject("invalid-user-id")
        .claim("scope", "USER")
        .header("alg", "RS256")
        .build()

    /**
     * Admin JWT token for endpoints requiring ADMIN role.
     */
    private val adminJwt = Jwt.withTokenValue("eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIiwic2NvcGUiOiJBRE1JTiIsImF1ZCI6InRlc3QtYXVkaWVuY2UiLCJpc3MiOiJodHRwczovL3Rlc3QuaXNzdWVyLmNvbSIsImlhdCI6MTY3Mzg5NjAwMCwiZXhwIjoxNjczODk5NjAwfQ.admin-signature")
        .subject("1")
        .claim("scope", "ADMIN")
        .header("alg", "RS256")
        .build()

    /**
     * Set up the test environment before each test.
     * 
     * Clears all mocks to ensure test isolation.
     */
    @BeforeEach
    fun setUp() {
        reset(portfolioService)
    }

    /**
     * Tests successful portfolio creation.
     * 
     * Verifies that:
     * 1. The controller accepts a valid POST request
     * 2. The service is called with the correct request parameters
     * 3. The response has the correct HTTP status (201 Created)
     * 4. The response body contains the created portfolio data
     */
    @Test
    fun `createPortfolio should create portfolio successfully`() {
        // Given
        val request = CreatePortfolioRequest(
            name = "Test Portfolio",
            description = "Test Description",
            type = PortfolioType.ENTERPRISE,
            ownerId = 123L,
            organizationId = 200L
        )
        val expectedResponse = createTestPortfolioResponse(1L, "Test Portfolio")
        
        whenever(portfolioService.createPortfolio(any())).thenReturn(expectedResponse)

        // When & Then
        mockMvc.perform(
            post("/api/v1/portfolios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(jwt().jwt(testJwt))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Test Portfolio"))
            .andExpect(jsonPath("$.description").value("Test Description"))
            .andExpect(jsonPath("$.type").value("ENTERPRISE"))
            .andExpect(jsonPath("$.ownerId").value(123))

        verify(portfolioService).createPortfolio(any())
    }

    /**
     * Tests portfolio creation with invalid JWT token.
     * 
     * Verifies that:
     * 1. The controller properly handles JWT tokens with invalid subject
     * 2. An appropriate error response is returned
     * 3. The service is not called
     */
    @Test
    fun `createPortfolio should handle invalid JWT token`() {
        // Given
        val request = CreatePortfolioRequest(
            name = "Test Portfolio",
            type = PortfolioType.ENTERPRISE,
            ownerId = 123L
        )

        // When & Then
        mockMvc.perform(
            post("/api/v1/portfolios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(jwt().jwt(invalidJwt))
        )
            .andExpect(status().isBadRequest)
            .andExpect(content().string(containsString("Invalid user ID")))

        verify(portfolioService, org.mockito.kotlin.never()).createPortfolio(any())
    }

    /**
     * Tests successful portfolio retrieval by ID.
     * 
     * Verifies that:
     * 1. The controller accepts a valid GET request
     * 2. The service is called with the correct portfolio ID
     * 3. The response has the correct HTTP status (200 OK)
     * 4. The response body contains the portfolio data
     */
    @Test
    fun `getPortfolio should return portfolio when found`() {
        // Given
        val portfolioId = 1L
        val expectedResponse = createTestPortfolioResponse(portfolioId, "Test Portfolio")
        
        `when`(portfolioService.getPortfolio(portfolioId)).thenReturn(expectedResponse)

        // When & Then
        mockMvc.perform(
            get("/api/v1/portfolios/$portfolioId")
                .with(jwt().jwt(testJwt))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(portfolioId))
            .andExpect(jsonPath("$.name").value("Test Portfolio"))
            .andExpect(jsonPath("$.technologyCount").value(0))

        verify(portfolioService).getPortfolio(portfolioId)
    }

    /**
     * Tests portfolio retrieval when portfolio doesn't exist.
     * 
     * Verifies that:
     * 1. The controller properly handles service exceptions
     * 2. An appropriate error response is returned
     * 3. The service is called with the correct parameters
     */
    @Test
    fun `getPortfolio should handle portfolio not found`() {
        // Given
        val portfolioId = 999L
        
        `when`(portfolioService.getPortfolio(portfolioId)).thenThrow(IllegalArgumentException("Portfolio not found"))

        // When & Then
        mockMvc.perform(
            get("/api/v1/portfolios/$portfolioId")
                .with(jwt().jwt(testJwt))
        )
            .andExpect(status().isNotFound)

        verify(portfolioService).getPortfolio(portfolioId)
    }

    /**
     * Tests successful portfolio update.
     * 
     * Verifies that:
     * 1. The controller accepts a valid PUT request
     * 2. The service is called with the correct parameters
     * 3. The response has the correct HTTP status (200 OK)
     * 4. The response body contains the updated portfolio data
     */
    @Test
    fun `updatePortfolio should update portfolio successfully`() {
        // Given
        val portfolioId = 1L
        val request = UpdatePortfolioRequest(
            name = "Updated Portfolio",
            description = "Updated Description",
            status = PortfolioStatus.ARCHIVED
        )
        val expectedResponse = createTestPortfolioResponse(portfolioId, "Updated Portfolio")
        
        whenever(portfolioService.updatePortfolio(eq(portfolioId), any())).thenReturn(expectedResponse)

        // When & Then
        mockMvc.perform(
            put("/api/v1/portfolios/$portfolioId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(jwt().jwt(testJwt))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(portfolioId))
            .andExpect(jsonPath("$.name").value("Updated Portfolio"))

        verify(portfolioService).updatePortfolio(eq(portfolioId), any())
    }

    /**
     * Tests successful portfolio deletion.
     * 
     * Verifies that:
     * 1. The controller accepts a valid DELETE request
     * 2. The service is called with the correct portfolio ID
     * 3. The response has the correct HTTP status (204 No Content)
     */
    @Test
    fun `deletePortfolio should delete portfolio successfully`() {
        // Given
        val portfolioId = 1L
        
        `when`(portfolioService.deletePortfolio(portfolioId)).thenReturn(true)

        // When & Then
        mockMvc.perform(
            delete("/api/v1/portfolios/$portfolioId")
                .with(jwt().jwt(testJwt))
        )
            .andExpect(status().isNoContent)

        verify(portfolioService).deletePortfolio(portfolioId)
    }

    /**
     * Tests portfolio deletion when portfolio cannot be deleted.
     * 
     * Verifies that:
     * 1. The controller properly handles unsuccessful deletion
     * 2. The response has the correct HTTP status (404 Not Found)
     * 3. The service is called with the correct parameters
     */
    @Test
    fun `deletePortfolio should handle deletion failure`() {
        // Given
        val portfolioId = 1L
        
        `when`(portfolioService.deletePortfolio(portfolioId)).thenReturn(false)

        // When & Then
        mockMvc.perform(
            delete("/api/v1/portfolios/$portfolioId")
                .with(jwt().jwt(testJwt))
        )
            .andExpect(status().isNotFound)

        verify(portfolioService).deletePortfolio(portfolioId)
    }

    /**
     * Tests successful retrieval of user's portfolios.
     * 
     * Verifies that:
     * 1. The controller accepts a valid GET request
     * 2. The JWT token is properly processed to extract user ID
     * 3. The service is called with the correct user ID
     * 4. The response has the correct HTTP status (200 OK)
     * 5. The response body contains the list of portfolio summaries
     */
    @Test
    fun `getMyPortfolios should return user portfolios`() {
        // Given
        val userId = 123L
        val expectedResponse = listOf(
            createTestPortfolioSummary(1L, "Portfolio 1"),
            createTestPortfolioSummary(2L, "Portfolio 2")
        )
        
        `when`(portfolioService.getPortfoliosByOwner(userId)).thenReturn(expectedResponse)

        // When & Then
        mockMvc.perform(
            get("/api/v1/portfolios/my")
                .with(jwt().jwt(testJwt))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].name").value("Portfolio 1"))
            .andExpect(jsonPath("$[1].id").value(2))
            .andExpect(jsonPath("$[1].name").value("Portfolio 2"))

        verify(portfolioService).getPortfoliosByOwner(userId)
    }

    /**
     * Tests successful retrieval of organization portfolios.
     * 
     * Verifies that:
     * 1. The controller accepts a valid GET request
     * 2. The service is called with the correct organization ID
     * 3. The response has the correct HTTP status (200 OK)
     * 4. The response body contains the list of portfolio summaries
     */
    @Test
    fun `getOrganizationPortfolios should return organization portfolios`() {
        // Given
        val organizationId = 200L
        val expectedResponse = listOf(createTestPortfolioSummary(1L, "Org Portfolio"))
        
        `when`(portfolioService.getPortfoliosByOrganization(organizationId)).thenReturn(expectedResponse)

        // When & Then
        mockMvc.perform(
            get("/api/v1/portfolios/organization/$organizationId")
                .with(jwt().jwt(testJwt))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].name").value("Org Portfolio"))

        verify(portfolioService).getPortfoliosByOrganization(organizationId)
    }

    /**
     * Tests successful portfolio search with filters.
     * 
     * Verifies that:
     * 1. The controller accepts a valid GET request with query parameters
     * 2. The service is called with the correct search parameters
     * 3. The response has the correct HTTP status (200 OK)
     * 4. The response body contains the filtered portfolio summaries
     */
    @Test
    fun `searchPortfolios should return filtered portfolios`() {
        // Given
        val name = "enterprise"
        val type = PortfolioType.ENTERPRISE
        val status = PortfolioStatus.ACTIVE
        val organizationId = 200L
        val expectedResponse = listOf(createTestPortfolioSummary(1L, "Enterprise Portfolio"))
        
        `when`(portfolioService.searchPortfolios(eq(name), eq(type), eq(status), eq(organizationId))).thenReturn(expectedResponse)

        // When & Then
        mockMvc.perform(
            get("/api/v1/portfolios/search")
                .param("name", name)
                .param("type", type.name)
                .param("status", status.name)
                .param("organizationId", organizationId.toString())
                .with(jwt().jwt(testJwt))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].name").value("Enterprise Portfolio"))

        verify(portfolioService).searchPortfolios(eq(name), eq(type), eq(status), eq(organizationId))
    }

    /**
     * Tests successful technology addition to portfolio.
     * 
     * Verifies that:
     * 1. The controller accepts a valid POST request
     * 2. The service is called with the correct parameters
     * 3. The response has the correct HTTP status (201 Created)
     * 4. The response body contains the created technology data
     */
    @Test
    fun `addTechnology should add technology successfully`() {
        // Given
        val portfolioId = 1L
        val request = AddTechnologyRequest(
            name = "Spring Boot",
            description = "Java framework",
            category = "Framework",
            version = "3.2.0",
            type = TechnologyType.FRAMEWORK,
            maturityLevel = MaturityLevel.MATURE,
            riskLevel = RiskLevel.LOW,
            annualCost = BigDecimal("5000.00"),
            vendorName = "VMware"
        )
        val expectedResponse = createTestTechnologyResponse(1L, "Spring Boot")
        
        whenever(portfolioService.addTechnology(eq(portfolioId), any())).thenReturn(expectedResponse)

        // When & Then
        mockMvc.perform(
            post("/api/v1/portfolios/$portfolioId/technologies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(jwt().jwt(testJwt))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Spring Boot"))
            .andExpect(jsonPath("$.category").value("Framework"))

        verify(portfolioService).addTechnology(eq(portfolioId), any())
    }

    /**
     * Tests successful technology retrieval by ID.
     * 
     * Verifies that:
     * 1. The controller accepts a valid GET request
     * 2. The service is called with the correct technology ID
     * 3. The response has the correct HTTP status (200 OK)
     * 4. The response body contains the technology data
     */
    @Test
    fun `getTechnology should return technology when found`() {
        // Given
        val technologyId = 1L
        val expectedResponse = createTestTechnologyResponse(technologyId, "Spring Boot")
        
        `when`(portfolioService.getTechnology(technologyId)).thenReturn(expectedResponse)

        // When & Then
        mockMvc.perform(
            get("/api/v1/portfolios/technologies/$technologyId")
                .with(jwt().jwt(testJwt))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(technologyId))
            .andExpect(jsonPath("$.name").value("Spring Boot"))

        verify(portfolioService).getTechnology(technologyId)
    }

    /**
     * Tests successful technology update.
     * 
     * Verifies that:
     * 1. The controller accepts a valid PUT request
     * 2. The service is called with the correct parameters
     * 3. The response has the correct HTTP status (200 OK)
     * 4. The response body contains the updated technology data
     */
    @Test
    fun `updateTechnology should update technology successfully`() {
        // Given
        val technologyId = 1L
        val request = UpdateTechnologyRequest(
            name = "Updated Spring Boot",
            version = "3.3.0",
            annualCost = BigDecimal("6000.00")
        )
        val expectedResponse = createTestTechnologyResponse(technologyId, "Updated Spring Boot")
        
        whenever(portfolioService.updateTechnology(eq(technologyId), any())).thenReturn(expectedResponse)

        // When & Then
        mockMvc.perform(
            put("/api/v1/portfolios/technologies/$technologyId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(jwt().jwt(testJwt))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(technologyId))
            .andExpect(jsonPath("$.name").value("Updated Spring Boot"))

        verify(portfolioService).updateTechnology(eq(technologyId), any())
    }

    /**
     * Tests successful technology removal from portfolio.
     * 
     * Verifies that:
     * 1. The controller accepts a valid DELETE request
     * 2. The service is called with the correct parameters
     * 3. The response has the correct HTTP status (204 No Content)
     */
    @Test
    fun `removeTechnology should remove technology successfully`() {
        // Given
        val portfolioId = 1L
        val technologyId = 10L
        
        `when`(portfolioService.removeTechnology(portfolioId, technologyId)).thenReturn(true)

        // When & Then
        mockMvc.perform(
            delete("/api/v1/portfolios/$portfolioId/technologies/$technologyId")
                .with(jwt().jwt(testJwt))
        )
            .andExpect(status().isNoContent)

        verify(portfolioService).removeTechnology(portfolioId, technologyId)
    }

    /**
     * Tests technology removal when removal fails.
     * 
     * Verifies that:
     * 1. The controller properly handles unsuccessful removal
     * 2. The response has the correct HTTP status (404 Not Found)
     * 3. The service is called with the correct parameters
     */
    @Test
    fun `removeTechnology should handle removal failure`() {
        // Given
        val portfolioId = 1L
        val technologyId = 10L
        
        `when`(portfolioService.removeTechnology(portfolioId, technologyId)).thenReturn(false)

        // When & Then
        mockMvc.perform(
            delete("/api/v1/portfolios/$portfolioId/technologies/$technologyId")
                .with(jwt().jwt(testJwt))
        )
            .andExpect(status().isNotFound)

        verify(portfolioService).removeTechnology(portfolioId, technologyId)
    }

    /**
     * Tests successful retrieval of portfolio technologies.
     * 
     * Verifies that:
     * 1. The controller accepts a valid GET request
     * 2. The service is called with the correct portfolio ID
     * 3. The response has the correct HTTP status (200 OK)
     * 4. The response body contains the list of technology summaries
     */
    @Test
    fun `getTechnologiesByPortfolio should return portfolio technologies`() {
        // Given
        val portfolioId = 1L
        val expectedResponse = listOf(
            createTestTechnologySummary(1L, "Spring Boot"),
            createTestTechnologySummary(2L, "PostgreSQL")
        )
        
        `when`(portfolioService.getTechnologiesByPortfolio(portfolioId)).thenReturn(expectedResponse)

        // When & Then
        mockMvc.perform(
            get("/api/v1/portfolios/$portfolioId/technologies")
                .with(jwt().jwt(testJwt))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].name").value("Spring Boot"))
            .andExpect(jsonPath("$[1].id").value(2))
            .andExpect(jsonPath("$[1].name").value("PostgreSQL"))

        verify(portfolioService).getTechnologiesByPortfolio(portfolioId)
    }

    // Helper Methods
    /**
     * Creates a test PortfolioResponse instance with the specified ID and name.
     * 
     * @param id The portfolio ID
     * @param name The portfolio name
     * @return A test PortfolioResponse instance
     */
    private fun createTestPortfolioResponse(id: Long, name: String) = PortfolioResponse(
        id = id,
        name = name,
        description = "Test Description",
        type = PortfolioType.ENTERPRISE,
        status = PortfolioStatus.ACTIVE,
        isActive = true,
        createdAt = testDateTime,
        updatedAt = testDateTime,
        ownerId = 123L,
        organizationId = 200L,
        technologyCount = 0,
        totalAnnualCost = null,
        technologies = emptyList()
    )

    /**
     * Creates a test PortfolioSummary instance with the specified ID and name.
     * 
     * @param id The portfolio ID
     * @param name The portfolio name
     * @return A test PortfolioSummary instance
     */
    private fun createTestPortfolioSummary(id: Long, name: String) = PortfolioSummary(
        id = id,
        name = name,
        type = PortfolioType.ENTERPRISE,
        status = PortfolioStatus.ACTIVE,
        ownerId = 123L,
        organizationId = 200L,
        technologyCount = 5,
        totalAnnualCost = BigDecimal("10000.00"),
        lastUpdated = testDateTime
    )

    /**
     * Creates a test TechnologyResponse instance with the specified ID and name.
     * 
     * @param id The technology ID
     * @param name The technology name
     * @return A test TechnologyResponse instance
     */
    private fun createTestTechnologyResponse(id: Long, name: String) = TechnologyResponse(
        id = id,
        name = name,
        description = "Test Technology",
        category = "Framework",
        version = "3.2.0",
        type = TechnologyType.FRAMEWORK,
        maturityLevel = MaturityLevel.MATURE,
        riskLevel = RiskLevel.LOW,
        annualCost = BigDecimal("5000.00"),
        licenseCost = BigDecimal("1000.00"),
        maintenanceCost = BigDecimal("500.00"),
        vendorName = "VMware",
        vendorContact = "support@vmware.com",
        supportContractExpiry = testDateTime.plusYears(1),
        isActive = true,
        createdAt = testDateTime,
        updatedAt = testDateTime
    )

    /**
     * Creates a test TechnologySummary instance with the specified ID and name.
     * 
     * @param id The technology ID
     * @param name The technology name
     * @return A test TechnologySummary instance
     */
    private fun createTestTechnologySummary(id: Long, name: String) = TechnologySummary(
        id = id,
        name = name,
        category = "Framework",
        type = TechnologyType.FRAMEWORK,
        maturityLevel = MaturityLevel.MATURE,
        riskLevel = RiskLevel.LOW,
        annualCost = BigDecimal("5000.00"),
        vendorName = "VMware"
    )
} 