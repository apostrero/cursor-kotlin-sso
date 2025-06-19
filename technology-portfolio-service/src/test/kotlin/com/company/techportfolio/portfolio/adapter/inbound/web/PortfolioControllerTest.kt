package com.company.techportfolio.portfolio.adapter.inbound.web

import com.company.techportfolio.portfolio.domain.model.*
import com.company.techportfolio.portfolio.domain.service.PortfolioService
import com.company.techportfolio.shared.domain.model.PortfolioType
import com.company.techportfolio.shared.domain.model.PortfolioStatus
import com.company.techportfolio.shared.domain.model.TechnologyType
import com.company.techportfolio.shared.domain.model.MaturityLevel
import com.company.techportfolio.shared.domain.model.RiskLevel
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.web.SecurityFilterChain
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import java.math.BigDecimal
import java.time.LocalDateTime
import org.junit.jupiter.api.Assertions.*
import org.mockito.Mockito.*
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(PortfolioController::class)
@ContextConfiguration(classes = [PortfolioController::class, PortfolioControllerTest.TestSecurityConfig::class])
class PortfolioControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var portfolioService: PortfolioService

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Configuration
    @EnableWebSecurity
    class TestSecurityConfig {
        @Bean
        fun filterChain(http: HttpSecurity): SecurityFilterChain {
            http.csrf { it.disable() }
                .authorizeHttpRequests { it.anyRequest().permitAll() }
            return http.build()
        }
    }

    @Test
    fun `getPortfolio should return 200 when portfolio exists`() {
        // Given
        val portfolioId = 1L
        val response = PortfolioResponse(
            id = portfolioId,
            name = "Test Portfolio",
            description = "Test Description",
            type = PortfolioType.ENTERPRISE,
            status = PortfolioStatus.ACTIVE,
            isActive = true,
            createdAt = LocalDateTime.now(),
            updatedAt = null,
            ownerId = 1L,
            organizationId = 100L,
            technologyCount = 0,
            totalAnnualCost = null,
            technologies = emptyList()
        )

        `when`(portfolioService.getPortfolio(portfolioId)).thenReturn(response)

        // When & Then
        mockMvc.perform(get("/api/v1/portfolios/$portfolioId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(portfolioId))
            .andExpect(jsonPath("$.name").value("Test Portfolio"))

        verify(portfolioService).getPortfolio(portfolioId)
    }

    @Test
    fun `deletePortfolio should return 204 when portfolio is deleted successfully`() {
        // Given
        val portfolioId = 1L
        `when`(portfolioService.deletePortfolio(portfolioId)).thenReturn(true)

        // When & Then
        mockMvc.perform(delete("/api/v1/portfolios/$portfolioId"))
            .andExpect(status().isNoContent)

        verify(portfolioService).deletePortfolio(portfolioId)
    }

    @Test
    fun `addTechnology should return 201 when technology is added successfully`() {
        // Given
        val portfolioId = 1L
        val request = AddTechnologyRequest(
            name = "Test Technology",
            description = "Test Technology Description",
            category = "Database",
            version = "1.0.0",
            type = TechnologyType.DATABASE,
            maturityLevel = MaturityLevel.PRODUCTION,
            riskLevel = RiskLevel.LOW,
            annualCost = BigDecimal("1000.00"),
            vendorName = "Test Vendor"
        )

        val response = TechnologyResponse(
            id = 1L,
            name = request.name,
            description = request.description,
            category = request.category,
            version = request.version,
            type = request.type,
            maturityLevel = request.maturityLevel,
            riskLevel = request.riskLevel,
            annualCost = request.annualCost,
            licenseCost = null,
            maintenanceCost = null,
            vendorName = request.vendorName,
            vendorContact = null,
            supportContractExpiry = null,
            isActive = true,
            createdAt = LocalDateTime.now(),
            updatedAt = null
        )

        `when`(portfolioService.addTechnology(portfolioId, request)).thenReturn(response)

        // When & Then
        mockMvc.perform(
            post("/api/v1/portfolios/$portfolioId/technologies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Test Technology"))

        verify(portfolioService).addTechnology(portfolioId, request)
    }

    @Test
    fun `removeTechnology should return 204 when technology is removed successfully`() {
        // Given
        val portfolioId = 1L
        val technologyId = 1L

        `when`(portfolioService.removeTechnology(portfolioId, technologyId)).thenReturn(true)

        // When & Then
        mockMvc.perform(delete("/api/v1/portfolios/$portfolioId/technologies/$technologyId"))
            .andExpect(status().isNoContent)

        verify(portfolioService).removeTechnology(portfolioId, technologyId)
    }

    @Test
    fun `getTechnologiesByPortfolio should return 200 with technology list`() {
        // Given
        val portfolioId = 1L
        val technologies = listOf(
            TechnologySummary(
                id = 1L,
                name = "Test Technology",
                category = "Database",
                type = TechnologyType.DATABASE,
                maturityLevel = MaturityLevel.PRODUCTION,
                riskLevel = RiskLevel.LOW,
                annualCost = BigDecimal("1000.00"),
                vendorName = "Test Vendor"
            )
        )

        `when`(portfolioService.getTechnologiesByPortfolio(portfolioId)).thenReturn(technologies)

        // When & Then
        mockMvc.perform(get("/api/v1/portfolios/$portfolioId/technologies"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].name").value("Test Technology"))

        verify(portfolioService).getTechnologiesByPortfolio(portfolioId)
    }
} 