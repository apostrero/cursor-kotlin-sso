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

/**
 * Portfolio REST Controller - Web Adapter Layer
 * 
 * This controller provides RESTful HTTP endpoints for technology portfolio management
 * operations. It serves as the primary web adapter in the hexagonal architecture,
 * translating HTTP requests to domain service calls and responses back to HTTP.
 * 
 * ## API Overview:
 * - **Base Path**: `/api/v1/portfolios`
 * - **Authentication**: JWT-based with role-based access control
 * - **Content Type**: JSON for all request/response bodies
 * - **Validation**: Jakarta Bean Validation on request bodies
 * 
 * ## Endpoints:
 * - `POST /` - Create new portfolio
 * - `GET /{id}` - Get portfolio details
 * - `PUT /{id}` - Update portfolio
 * - `DELETE /{id}` - Delete portfolio
 * - `GET /my` - Get user's portfolios
 * - `GET /organization/{id}` - Get organization portfolios
 * - `GET /search` - Search portfolios with filters
 * - `POST /{id}/technologies` - Add technology to portfolio
 * - `GET /technologies/{id}` - Get technology details
 * - `PUT /technologies/{id}` - Update technology
 * - `DELETE /{portfolioId}/technologies/{techId}` - Remove technology
 * - `GET /{id}/technologies` - Get portfolio technologies
 * 
 * ## Security:
 * - All endpoints require authentication
 * - Most endpoints require USER role
 * - Organization endpoints require ADMIN role
 * - JWT subject is used as user ID for ownership validation
 * 
 * ## Error Handling:
 * - Returns appropriate HTTP status codes
 * - Validation errors return 400 Bad Request
 * - Authorization errors return 403 Forbidden
 * - Not found errors return 404 Not Found
 * - Server errors return 500 Internal Server Error
 * 
 * @param portfolioService The domain service for portfolio operations
 * 
 * @author Technology Portfolio Team
 * @since 1.0.0
 * @see PortfolioService
 * @see CreatePortfolioRequest
 * @see PortfolioResponse
 */
@RestController
@RequestMapping("/api/v1/portfolios")
class PortfolioController(
    private val portfolioService: PortfolioService
) {

    /**
     * Creates a new technology portfolio.
     * 
     * Creates a new portfolio owned by the authenticated user. The owner ID
     * is automatically extracted from the JWT token to ensure users can only
     * create portfolios for themselves.
     * 
     * **HTTP Method**: POST  
     * **Path**: `/api/v1/portfolios`  
     * **Security**: Requires USER role  
     * **Content-Type**: application/json
     * 
     * ## Request Body Example:
     * ```json
     * {
     *   "name": "Enterprise Architecture Portfolio",
     *   "description": "Technologies used in enterprise architecture",
     *   "type": "ENTERPRISE",
     *   "organizationId": 100
     * }
     * ```
     * 
     * ## Response Example (201 Created):
     * ```json
     * {
     *   "id": 1,
     *   "name": "Enterprise Architecture Portfolio",
     *   "description": "Technologies used in enterprise architecture",
     *   "type": "ENTERPRISE",
     *   "status": "ACTIVE",
     *   "ownerId": 42,
     *   "organizationId": 100,
     *   "technologyCount": 0,
     *   "totalAnnualCost": null,
     *   "createdAt": "2024-01-15T10:30:00",
     *   "technologies": []
     * }
     * ```
     * 
     * @param request The portfolio creation request (validated)
     * @param jwt The JWT token containing user authentication information
     * @return ResponseEntity with created portfolio (201) or error status
     * @throws IllegalArgumentException if JWT subject is invalid
     */
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

    /**
     * Retrieves a portfolio by its unique identifier.
     * 
     * Returns comprehensive portfolio information including all associated
     * technologies, calculated costs, and metadata.
     * 
     * **HTTP Method**: GET  
     * **Path**: `/api/v1/portfolios/{portfolioId}`  
     * **Security**: Requires USER role
     * 
     * ## Response Example (200 OK):
     * ```json
     * {
     *   "id": 1,
     *   "name": "Enterprise Architecture Portfolio",
     *   "technologyCount": 5,
     *   "totalAnnualCost": 50000.00,
     *   "technologies": [...]
     * }
     * ```
     * 
     * @param portfolioId The unique identifier of the portfolio
     * @return ResponseEntity with portfolio details (200) or not found (404)
     */
    @GetMapping("/{portfolioId}")
    @PreAuthorize("hasRole('USER')")
    fun getPortfolio(@PathVariable portfolioId: Long): ResponseEntity<PortfolioResponse> {
        val portfolio = portfolioService.getPortfolio(portfolioId)
        return ResponseEntity.ok(portfolio)
    }

    /**
     * Updates an existing portfolio with partial data.
     * 
     * Allows partial updates to portfolio information. Only fields provided
     * in the request body will be updated. The updated timestamp is
     * automatically set to the current time.
     * 
     * **HTTP Method**: PUT  
     * **Path**: `/api/v1/portfolios/{portfolioId}`  
     * **Security**: Requires USER role  
     * **Content-Type**: application/json
     * 
     * ## Request Body Example:
     * ```json
     * {
     *   "name": "Updated Portfolio Name",
     *   "status": "ARCHIVED"
     * }
     * ```
     * 
     * @param portfolioId The unique identifier of the portfolio to update
     * @param request The update request with optional field changes (validated)
     * @return ResponseEntity with updated portfolio (200) or error status
     */
    @PutMapping("/{portfolioId}")
    @PreAuthorize("hasRole('USER')")
    fun updatePortfolio(
        @PathVariable portfolioId: Long,
        @Valid @RequestBody request: UpdatePortfolioRequest
    ): ResponseEntity<PortfolioResponse> {
        val portfolio = portfolioService.updatePortfolio(portfolioId, request)
        return ResponseEntity.ok(portfolio)
    }

    /**
     * Deletes a portfolio by its unique identifier.
     * 
     * Permanently deletes a portfolio. The portfolio must be empty (no technologies)
     * before it can be deleted to maintain data integrity.
     * 
     * **HTTP Method**: DELETE  
     * **Path**: `/api/v1/portfolios/{portfolioId}`  
     * **Security**: Requires USER role
     * 
     * ## Business Rules:
     * - Portfolio must exist
     * - Portfolio must be empty (no technologies)
     * - Deletion is permanent and cannot be undone
     * 
     * @param portfolioId The unique identifier of the portfolio to delete
     * @return ResponseEntity with no content (204) if successful, not found (404) if failed
     */
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

    /**
     * Retrieves portfolios owned by the authenticated user.
     * 
     * Returns a list of portfolio summaries for the current user. This endpoint
     * automatically filters portfolios based on the authenticated user's ID
     * extracted from the JWT token.
     * 
     * **HTTP Method**: GET  
     * **Path**: `/api/v1/portfolios/my`  
     * **Security**: Requires USER role
     * 
     * ## Response Example (200 OK):
     * ```json
     * [
     *   {
     *     "id": 1,
     *     "name": "My Portfolio",
     *     "type": "PERSONAL",
     *     "status": "ACTIVE",
     *     "technologyCount": 3,
     *     "totalAnnualCost": 15000.00
     *   }
     * ]
     * ```
     * 
     * @param jwt The JWT token containing user authentication information
     * @return ResponseEntity with list of user's portfolio summaries (200)
     * @throws IllegalArgumentException if JWT subject is invalid
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('USER')")
    fun getMyPortfolios(@AuthenticationPrincipal jwt: Jwt): ResponseEntity<List<PortfolioSummary>> {
        val userId = jwt.subject?.toLongOrNull() ?: throw IllegalArgumentException("Invalid user ID")
        val portfolios = portfolioService.getPortfoliosByOwner(userId)
        return ResponseEntity.ok(portfolios)
    }

    /**
     * Retrieves portfolios for a specific organization.
     * 
     * Returns all portfolios belonging to the specified organization. This
     * endpoint is restricted to administrators and supports multi-tenant
     * organizational portfolio management.
     * 
     * **HTTP Method**: GET  
     * **Path**: `/api/v1/portfolios/organization/{organizationId}`  
     * **Security**: Requires ADMIN role
     * 
     * @param organizationId The unique identifier of the organization
     * @return ResponseEntity with list of organization's portfolio summaries (200)
     */
    @GetMapping("/organization/{organizationId}")
    @PreAuthorize("hasRole('ADMIN')")
    fun getOrganizationPortfolios(@PathVariable organizationId: Long): ResponseEntity<List<PortfolioSummary>> {
        val portfolios = portfolioService.getPortfoliosByOrganization(organizationId)
        return ResponseEntity.ok(portfolios)
    }

    /**
     * Searches portfolios with flexible filtering criteria.
     * 
     * Provides advanced search capabilities with multiple optional query parameters.
     * All parameters are optional, allowing for flexible search combinations.
     * Results are returned as portfolio summaries for efficient listing.
     * 
     * **HTTP Method**: GET  
     * **Path**: `/api/v1/portfolios/search`  
     * **Security**: Requires USER role
     * 
     * ## Query Parameters:
     * - `name` (optional): Portfolio name filter (partial matching)
     * - `type` (optional): Portfolio type filter (PERSONAL, TEAM, ENTERPRISE, etc.)
     * - `status` (optional): Portfolio status filter (ACTIVE, ARCHIVED, etc.)
     * - `organizationId` (optional): Organization scope filter
     * 
     * ## Example Request:
     * `GET /api/v1/portfolios/search?name=enterprise&type=ENTERPRISE&status=ACTIVE`
     * 
     * @param name Optional name filter for partial matching
     * @param type Optional portfolio type filter
     * @param status Optional portfolio status filter
     * @param organizationId Optional organization scope filter
     * @return ResponseEntity with list of matching portfolio summaries (200)
     */
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

    /**
     * Adds a new technology to an existing portfolio.
     * 
     * Creates a new technology entry and associates it with the specified portfolio.
     * The technology includes comprehensive metadata for lifecycle management
     * including cost tracking and vendor information.
     * 
     * **HTTP Method**: POST  
     * **Path**: `/api/v1/portfolios/{portfolioId}/technologies`  
     * **Security**: Requires USER role  
     * **Content-Type**: application/json
     * 
     * ## Request Body Example:
     * ```json
     * {
     *   "name": "Spring Boot",
     *   "description": "Java application framework",
     *   "category": "Framework",
     *   "version": "3.2.0",
     *   "type": "FRAMEWORK",
     *   "maturityLevel": "MATURE",
     *   "riskLevel": "LOW",
     *   "annualCost": 5000.00,
     *   "vendorName": "VMware"
     * }
     * ```
     * 
     * @param portfolioId The unique identifier of the portfolio
     * @param request The technology creation request (validated)
     * @return ResponseEntity with created technology (201) or error status
     */
    @PostMapping("/{portfolioId}/technologies")
    @PreAuthorize("hasRole('USER')")
    fun addTechnology(
        @PathVariable portfolioId: Long,
        @Valid @RequestBody request: AddTechnologyRequest
    ): ResponseEntity<TechnologyResponse> {
        val technology = portfolioService.addTechnology(portfolioId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(technology)
    }

    /**
     * Retrieves a technology by its unique identifier.
     * 
     * Returns comprehensive technology information including all metadata,
     * cost information, vendor details, and lifecycle data.
     * 
     * **HTTP Method**: GET  
     * **Path**: `/api/v1/portfolios/technologies/{technologyId}`  
     * **Security**: Requires USER role
     * 
     * @param technologyId The unique identifier of the technology
     * @return ResponseEntity with technology details (200) or not found (404)
     */
    @GetMapping("/technologies/{technologyId}")
    @PreAuthorize("hasRole('USER')")
    fun getTechnology(@PathVariable technologyId: Long): ResponseEntity<TechnologyResponse> {
        val technology = portfolioService.getTechnology(technologyId)
        return ResponseEntity.ok(technology)
    }

    /**
     * Updates an existing technology with partial data.
     * 
     * Allows selective updates to technology information including costs,
     * vendor details, and lifecycle metadata. Only non-null fields in the
     * request are updated, enabling partial updates.
     * 
     * **HTTP Method**: PUT  
     * **Path**: `/api/v1/portfolios/technologies/{technologyId}`  
     * **Security**: Requires USER role  
     * **Content-Type**: application/json
     * 
     * ## Request Body Example:
     * ```json
     * {
     *   "version": "3.3.0",
     *   "annualCost": 5500.00,
     *   "riskLevel": "MEDIUM"
     * }
     * ```
     * 
     * @param technologyId The unique identifier of the technology to update
     * @param request The update request with optional field changes (validated)
     * @return ResponseEntity with updated technology (200) or error status
     */
    @PutMapping("/technologies/{technologyId}")
    @PreAuthorize("hasRole('USER')")
    fun updateTechnology(
        @PathVariable technologyId: Long,
        @Valid @RequestBody request: UpdateTechnologyRequest
    ): ResponseEntity<TechnologyResponse> {
        val technology = portfolioService.updateTechnology(technologyId, request)
        return ResponseEntity.ok(technology)
    }

    /**
     * Removes a technology from a portfolio.
     * 
     * Permanently removes the specified technology from the portfolio. The operation
     * includes validation to ensure the technology belongs to the specified portfolio
     * before removal.
     * 
     * **HTTP Method**: DELETE  
     * **Path**: `/api/v1/portfolios/{portfolioId}/technologies/{technologyId}`  
     * **Security**: Requires USER role
     * 
     * ## Business Rules:
     * - Both portfolio and technology must exist
     * - Technology must belong to the specified portfolio
     * - Removal is permanent and cannot be undone
     * 
     * @param portfolioId The unique identifier of the portfolio
     * @param technologyId The unique identifier of the technology to remove
     * @return ResponseEntity with no content (204) if successful, not found (404) if failed
     */
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

    /**
     * Retrieves all technologies for a specific portfolio.
     * 
     * Returns a list of technology summaries associated with the specified portfolio.
     * This endpoint provides an efficient way to view all technologies in a portfolio
     * without the full detailed information.
     * 
     * **HTTP Method**: GET  
     * **Path**: `/api/v1/portfolios/{portfolioId}/technologies`  
     * **Security**: Requires USER role
     * 
     * ## Response Example (200 OK):
     * ```json
     * [
     *   {
     *     "id": 1,
     *     "name": "Spring Boot",
     *     "category": "Framework",
     *     "version": "3.2.0",
     *     "type": "FRAMEWORK",
     *     "maturityLevel": "MATURE",
     *     "riskLevel": "LOW",
     *     "annualCost": 5000.00
     *   }
     * ]
     * ```
     * 
     * @param portfolioId The unique identifier of the portfolio
     * @return ResponseEntity with list of technology summaries (200)
     */
    @GetMapping("/{portfolioId}/technologies")
    @PreAuthorize("hasRole('USER')")
    fun getTechnologiesByPortfolio(@PathVariable portfolioId: Long): ResponseEntity<List<TechnologySummary>> {
        val technologies = portfolioService.getTechnologiesByPortfolio(portfolioId)
        return ResponseEntity.ok(technologies)
    }
} 