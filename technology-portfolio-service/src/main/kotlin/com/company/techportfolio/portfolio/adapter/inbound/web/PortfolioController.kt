package com.company.techportfolio.portfolio.adapter.inbound.web

import com.company.techportfolio.portfolio.domain.model.*
import com.company.techportfolio.portfolio.domain.service.PortfolioService
import com.company.techportfolio.shared.domain.model.PortfolioType
import com.company.techportfolio.shared.domain.model.PortfolioStatus
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt

@RestController
@RequestMapping("/api/v1/portfolios")
class PortfolioController(
    private val portfolioService: PortfolioService
) {

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    fun createPortfolio(
        @Valid @RequestBody request: CreatePortfolioRequest,
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<PortfolioResponse> {
        val userId = jwt.subject?.toLongOrNull() ?: throw IllegalArgumentException("Invalid user ID")
        val portfolioRequest = request.copy(ownerId = userId)
        
        val portfolio = portfolioService.createPortfolio(portfolioRequest)
        return ResponseEntity.status(HttpStatus.CREATED).body(portfolio)
    }

    @GetMapping("/{portfolioId}")
    @PreAuthorize("hasRole('USER')")
    fun getPortfolio(@PathVariable portfolioId: Long): ResponseEntity<PortfolioResponse> {
        val portfolio = portfolioService.getPortfolio(portfolioId)
        return ResponseEntity.ok(portfolio)
    }

    @PutMapping("/{portfolioId}")
    @PreAuthorize("hasRole('USER')")
    fun updatePortfolio(
        @PathVariable portfolioId: Long,
        @Valid @RequestBody request: UpdatePortfolioRequest
    ): ResponseEntity<PortfolioResponse> {
        val portfolio = portfolioService.updatePortfolio(portfolioId, request)
        return ResponseEntity.ok(portfolio)
    }

    @DeleteMapping("/{portfolioId}")
    @PreAuthorize("hasRole('USER')")
    fun deletePortfolio(@PathVariable portfolioId: Long): ResponseEntity<Void> {
        val deleted = portfolioService.deletePortfolio(portfolioId)
        return if (deleted) {
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('USER')")
    fun getMyPortfolios(@AuthenticationPrincipal jwt: Jwt): ResponseEntity<List<PortfolioSummary>> {
        val userId = jwt.subject?.toLongOrNull() ?: throw IllegalArgumentException("Invalid user ID")
        val portfolios = portfolioService.getPortfoliosByOwner(userId)
        return ResponseEntity.ok(portfolios)
    }

    @GetMapping("/organization/{organizationId}")
    @PreAuthorize("hasRole('ADMIN')")
    fun getOrganizationPortfolios(@PathVariable organizationId: Long): ResponseEntity<List<PortfolioSummary>> {
        val portfolios = portfolioService.getPortfoliosByOrganization(organizationId)
        return ResponseEntity.ok(portfolios)
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('USER')")
    fun searchPortfolios(
        @RequestParam(required = false) name: String?,
        @RequestParam(required = false) type: PortfolioType?,
        @RequestParam(required = false) status: PortfolioStatus?,
        @RequestParam(required = false) organizationId: Long?
    ): ResponseEntity<List<PortfolioSummary>> {
        val portfolios = portfolioService.searchPortfolios(name, type, status, organizationId)
        return ResponseEntity.ok(portfolios)
    }

    @PostMapping("/{portfolioId}/technologies")
    @PreAuthorize("hasRole('USER')")
    fun addTechnology(
        @PathVariable portfolioId: Long,
        @Valid @RequestBody request: AddTechnologyRequest
    ): ResponseEntity<TechnologyResponse> {
        val technology = portfolioService.addTechnology(portfolioId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(technology)
    }

    @GetMapping("/technologies/{technologyId}")
    @PreAuthorize("hasRole('USER')")
    fun getTechnology(@PathVariable technologyId: Long): ResponseEntity<TechnologyResponse> {
        val technology = portfolioService.getTechnology(technologyId)
        return ResponseEntity.ok(technology)
    }

    @PutMapping("/technologies/{technologyId}")
    @PreAuthorize("hasRole('USER')")
    fun updateTechnology(
        @PathVariable technologyId: Long,
        @Valid @RequestBody request: UpdateTechnologyRequest
    ): ResponseEntity<TechnologyResponse> {
        val technology = portfolioService.updateTechnology(technologyId, request)
        return ResponseEntity.ok(technology)
    }

    @DeleteMapping("/{portfolioId}/technologies/{technologyId}")
    @PreAuthorize("hasRole('USER')")
    fun removeTechnology(
        @PathVariable portfolioId: Long,
        @PathVariable technologyId: Long
    ): ResponseEntity<Void> {
        val removed = portfolioService.removeTechnology(portfolioId, technologyId)
        return if (removed) {
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/{portfolioId}/technologies")
    @PreAuthorize("hasRole('USER')")
    fun getTechnologiesByPortfolio(@PathVariable portfolioId: Long): ResponseEntity<List<TechnologySummary>> {
        val technologies = portfolioService.getTechnologiesByPortfolio(portfolioId)
        return ResponseEntity.ok(technologies)
    }
} 