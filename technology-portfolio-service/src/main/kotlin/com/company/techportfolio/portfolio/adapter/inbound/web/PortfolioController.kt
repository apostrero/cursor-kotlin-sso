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
import reactor.core.publisher.Mono
import reactor.core.publisher.Flux

/**
 * Portfolio REST Controller - Web Adapter Layer (REACTIVE)
 * 
 * This controller provides RESTful HTTP endpoints for technology portfolio management
 * operations using reactive programming patterns. It serves as the primary web adapter 
 * in the hexagonal architecture, translating HTTP requests to domain service calls 
 * and responses back to HTTP using reactive streams.
 * 
 * ## API Overview:
 * - **Base Path**: `/api/v1/portfolios`
 * - **Authentication**: JWT-based with role-based access control
 * - **Content Type**: JSON for all request/response bodies
 * - **Validation**: Jakarta Bean Validation on request bodies
 * - **Reactive**: All endpoints return Mono<ResponseEntity<T>> for single items or Flux<T> for collections
 * 
 * ## Endpoints:
 * - `POST /` - Create new portfolio (Mono)
 * - `GET /{id}` - Get portfolio details (Mono)
 * - `PUT /{id}` - Update portfolio (Mono)
 * - `DELETE /{id}` - Delete portfolio (Mono)
 * - `GET /my` - Get user's portfolios (Flux)
 * - `GET /organization/{id}` - Get organization portfolios (Flux)
 * - `GET /search` - Search portfolios with filters (Flux)
 * - `POST /{id}/technologies` - Add technology to portfolio (Mono)
 * - `GET /technologies/{id}` - Get technology details (Mono)
 * - `PUT /technologies/{id}` - Update technology (Mono)
 * - `DELETE /{portfolioId}/technologies/{techId}` - Remove technology (Mono)
 * - `GET /{id}/technologies` - Get portfolio technologies (Flux)
 * - `GET /stream` - Stream all portfolios (Flux with Server-Sent Events)
 * - `GET /technologies/stream` - Stream all technologies (Flux with Server-Sent Events)
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
 * - Reactive error handling with onErrorMap and onErrorResume
 * 
 * ## Flux<T> Usage:
 * - Use Flux for endpoints that return collections
 * - Flux provides backpressure handling for large datasets
 * - Supports streaming with Server-Sent Events
 * - Enables reactive pagination and filtering
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
     * **Reactive**: Returns Mono<ResponseEntity<PortfolioResponse>>
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
     * @return Mono<ResponseEntity<PortfolioResponse>> with created portfolio (201) or error status
     * @throws IllegalArgumentException if JWT subject is invalid
     */
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    fun createPortfolio(
        @Valid @RequestBody request: CreatePortfolioRequest,
        @AuthenticationPrincipal jwt: Jwt
    ): Mono<ResponseEntity<PortfolioResponse>> {
        val userId = jwt.subject?.toLongOrNull() ?: 
            return Mono.just(ResponseEntity.badRequest().build())
        
        val portfolioRequest = request.copy(ownerId = userId)
        
        return portfolioService.createPortfolio(portfolioRequest)
            .map { portfolio -> ResponseEntity.status(HttpStatus.CREATED).body(portfolio) }
            .onErrorResume { error ->
                when (error) {
                    is IllegalArgumentException -> 
                        Mono.just(ResponseEntity.badRequest().build())
                    else -> 
                        Mono.just(ResponseEntity.internalServerError().build())
                }
            }
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
     * **Reactive**: Returns Mono<ResponseEntity<PortfolioResponse>>
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
     * @return Mono<ResponseEntity<PortfolioResponse>> with portfolio details (200) or not found (404)
     */
    @GetMapping("/{portfolioId}")
    @PreAuthorize("hasRole('USER')")
    fun getPortfolio(@PathVariable portfolioId: Long): Mono<ResponseEntity<PortfolioResponse>> {
        return portfolioService.getPortfolio(portfolioId)
            .map { portfolio -> ResponseEntity.ok(portfolio) }
            .onErrorResume { error ->
                when (error) {
                    is IllegalArgumentException -> 
                        Mono.just(ResponseEntity.notFound().build())
                    else -> 
                        Mono.just(ResponseEntity.internalServerError().build())
                }
            }
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
     * **Reactive**: Returns Mono<ResponseEntity<PortfolioResponse>>
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
     * @return Mono<ResponseEntity<PortfolioResponse>> with updated portfolio (200) or error status
     */
    @PutMapping("/{portfolioId}")
    @PreAuthorize("hasRole('USER')")
    fun updatePortfolio(
        @PathVariable portfolioId: Long,
        @Valid @RequestBody request: UpdatePortfolioRequest
    ): Mono<ResponseEntity<PortfolioResponse>> {
        return portfolioService.updatePortfolio(portfolioId, request)
            .map { portfolio -> ResponseEntity.ok(portfolio) }
            .onErrorResume { error ->
                when (error) {
                    is IllegalArgumentException -> 
                        Mono.just(ResponseEntity.badRequest().build())
                    else -> 
                        Mono.just(ResponseEntity.internalServerError().build())
                }
            }
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
     * **Reactive**: Returns Mono<ResponseEntity<Void>>
     * 
     * ## Business Rules:
     * - Portfolio must exist
     * - Portfolio must be empty (no technologies)
     * - Deletion is permanent and cannot be undone
     * 
     * @param portfolioId The unique identifier of the portfolio to delete
     * @return Mono<ResponseEntity<Void>> with no content (204) if successful, not found (404) if failed
     */
    @DeleteMapping("/{portfolioId}")
    @PreAuthorize("hasRole('USER')")
    fun deletePortfolio(@PathVariable portfolioId: Long): Mono<ResponseEntity<Void>> {
        return portfolioService.deletePortfolio(portfolioId)
            .map<ResponseEntity<Void>> { deleted ->
                if (deleted) {
                    ResponseEntity.noContent().build()
                } else {
                    ResponseEntity.notFound().build()
                }
            }
            .onErrorResume { error ->
                when (error) {
                    is IllegalArgumentException -> 
                        Mono.just(ResponseEntity.badRequest().build())
                    else -> 
                        Mono.just(ResponseEntity.internalServerError().build())
                }
            }
    }

    /**
     * Retrieves all portfolios owned by the authenticated user as a reactive stream.
     * 
     * Returns a Flux stream of portfolio summaries for the authenticated user.
     * This endpoint demonstrates Flux<T> usage for collections with reactive
     * backpressure handling and streaming capabilities.
     * 
     * **HTTP Method**: GET  
     * **Path**: `/api/v1/portfolios/my`  
     * **Security**: Requires USER role
     * **Reactive**: Returns Flux<PortfolioSummary> for streaming collections
     * 
     * ## Response Example (200 OK):
     * ```json
     * [
     *   {
     *     "id": 1,
     *     "name": "Enterprise Architecture Portfolio",
     *     "type": "ENTERPRISE",
     *     "technologyCount": 5,
     *     "totalAnnualCost": 50000.00
     *   },
     *   {
     *     "id": 2,
     *     "name": "Development Tools Portfolio",
     *     "type": "DEVELOPMENT",
     *     "technologyCount": 3,
     *     "totalAnnualCost": 25000.00
     *   }
     * ]
     * ```
     * 
     * @param jwt The JWT token containing user authentication information
     * @return Flux<PortfolioSummary> with user's portfolios as a reactive stream
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('USER')")
    fun getMyPortfolios(@AuthenticationPrincipal jwt: Jwt): Flux<PortfolioSummary> {
        val userId = jwt.subject?.toLongOrNull() ?: 
            return Flux.empty()
        
        return portfolioService.getPortfoliosByOwner(userId)
            .onErrorResume { error ->
                // Log error and return empty flux
                println("Error retrieving portfolios for user $userId: ${error.message}")
                Flux.empty()
            }
    }

    /**
     * Retrieves all portfolios for a specific organization as a reactive stream.
     * 
     * Returns a Flux stream of portfolio summaries for the specified organization.
     * This endpoint demonstrates Flux<T> usage for organizational data with
     * reactive filtering and error handling.
     * 
     * **HTTP Method**: GET  
     * **Path**: `/api/v1/portfolios/organization/{organizationId}`  
     * **Security**: Requires ADMIN role
     * **Reactive**: Returns Flux<PortfolioSummary> for streaming collections
     * 
     * @param organizationId The ID of the organization
     * @return Flux<PortfolioSummary> with organization's portfolios as a reactive stream
     */
    @GetMapping("/organization/{organizationId}")
    @PreAuthorize("hasRole('ADMIN')")
    fun getPortfoliosByOrganization(@PathVariable organizationId: Long): Flux<PortfolioSummary> {
        return portfolioService.getPortfoliosByOrganization(organizationId)
            .onErrorResume { error ->
                println("Error retrieving portfolios for organization $organizationId: ${error.message}")
                Flux.empty()
            }
    }

    /**
     * Searches portfolios with flexible filtering criteria as a reactive stream.
     * 
     * Returns a Flux stream of portfolio summaries matching the search criteria.
     * This endpoint demonstrates Flux<T> usage with reactive filtering and
     * parameter-based search capabilities.
     * 
     * **HTTP Method**: GET  
     * **Path**: `/api/v1/portfolios/search`  
     * **Security**: Requires USER role
     * **Reactive**: Returns Flux<PortfolioSummary> for streaming search results
     * 
     * ## Query Parameters:
     * - `name` (optional): Portfolio name filter (partial matching)
     * - `type` (optional): Portfolio type filter
     * - `status` (optional): Portfolio status filter
     * - `organizationId` (optional): Organization scope filter
     * 
     * ## Example Request:
     * ```
     * GET /api/v1/portfolios/search?name=Enterprise&type=ENTERPRISE&status=ACTIVE
     * ```
     * 
     * @param name Optional name filter
     * @param type Optional portfolio type filter
     * @param status Optional portfolio status filter
     * @param organizationId Optional organization scope filter
     * @return Flux<PortfolioSummary> with matching portfolios as a reactive stream
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('USER')")
    fun searchPortfolios(
        @RequestParam(required = false) name: String?,
        @RequestParam(required = false) type: PortfolioType?,
        @RequestParam(required = false) status: PortfolioStatus?,
        @RequestParam(required = false) organizationId: Long?
    ): Flux<PortfolioSummary> {
        return portfolioService.searchPortfolios(name, type, status, organizationId)
            .onErrorResume { error ->
                println("Error searching portfolios: ${error.message}")
                Flux.empty()
            }
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
     * **Reactive**: Returns Mono<ResponseEntity<TechnologyResponse>>
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
     * @return Mono<ResponseEntity<TechnologyResponse>> with created technology (201) or error status
     */
    @PostMapping("/{portfolioId}/technologies")
    @PreAuthorize("hasRole('USER')")
    fun addTechnology(
        @PathVariable portfolioId: Long,
        @Valid @RequestBody request: AddTechnologyRequest
    ): Mono<ResponseEntity<TechnologyResponse>> {
        return portfolioService.addTechnology(portfolioId, request)
            .map { technology -> ResponseEntity.status(HttpStatus.CREATED).body(technology) }
            .onErrorResume { error ->
                when (error) {
                    is IllegalArgumentException -> 
                        Mono.just(ResponseEntity.badRequest().build())
                    else -> 
                        Mono.just(ResponseEntity.internalServerError().build())
                }
            }
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
     * **Reactive**: Returns Mono<ResponseEntity<TechnologyResponse>>
     * 
     * @param technologyId The unique identifier of the technology
     * @return Mono<ResponseEntity<TechnologyResponse>> with technology details (200) or not found (404)
     */
    @GetMapping("/technologies/{technologyId}")
    @PreAuthorize("hasRole('USER')")
    fun getTechnology(@PathVariable technologyId: Long): Mono<ResponseEntity<TechnologyResponse>> {
        return portfolioService.getTechnology(technologyId)
            .map { technology -> ResponseEntity.ok(technology) }
            .onErrorResume { error ->
                when (error) {
                    is IllegalArgumentException -> 
                        Mono.just(ResponseEntity.notFound().build())
                    else -> 
                        Mono.just(ResponseEntity.internalServerError().build())
                }
            }
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
     * **Reactive**: Returns Mono<ResponseEntity<TechnologyResponse>>
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
     * @return Mono<ResponseEntity<TechnologyResponse>> with updated technology (200) or error status
     */
    @PutMapping("/technologies/{technologyId}")
    @PreAuthorize("hasRole('USER')")
    fun updateTechnology(
        @PathVariable technologyId: Long,
        @Valid @RequestBody request: UpdateTechnologyRequest
    ): Mono<ResponseEntity<TechnologyResponse>> {
        return portfolioService.updateTechnology(technologyId, request)
            .map { technology -> ResponseEntity.ok(technology) }
            .onErrorResume { error ->
                when (error) {
                    is IllegalArgumentException -> 
                        Mono.just(ResponseEntity.badRequest().build())
                    else -> 
                        Mono.just(ResponseEntity.internalServerError().build())
                }
            }
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
     * **Reactive**: Returns Mono<ResponseEntity<Void>>
     * 
     * ## Business Rules:
     * - Both portfolio and technology must exist
     * - Technology must belong to the specified portfolio
     * - Removal is permanent and cannot be undone
     * 
     * @param portfolioId The unique identifier of the portfolio
     * @param technologyId The unique identifier of the technology to remove
     * @return Mono<ResponseEntity<Void>> with no content (204) if successful, not found (404) if failed
     */
    @DeleteMapping("/{portfolioId}/technologies/{technologyId}")
    @PreAuthorize("hasRole('USER')")
    fun removeTechnology(
        @PathVariable portfolioId: Long,
        @PathVariable technologyId: Long
    ): Mono<ResponseEntity<Void>> {
        return portfolioService.removeTechnology(portfolioId, technologyId)
            .map<ResponseEntity<Void>> { removed ->
                if (removed) {
                    ResponseEntity.noContent().build()
                } else {
                    ResponseEntity.notFound().build()
                }
            }
            .onErrorResume { error ->
                when (error) {
                    is IllegalArgumentException -> 
                        Mono.just(ResponseEntity.badRequest().build())
                    else -> 
                        Mono.just(ResponseEntity.internalServerError().build())
                }
            }
    }

    /**
     * Retrieves all technologies for a specific portfolio as a reactive stream.
     * 
     * Returns a Flux stream of technology summaries for the specified portfolio.
     * This endpoint demonstrates Flux<T> usage for technology collections with
     * reactive error handling and portfolio-scoped filtering.
     * 
     * **HTTP Method**: GET  
     * **Path**: `/api/v1/portfolios/{portfolioId}/technologies`  
     * **Security**: Requires USER role
     * **Reactive**: Returns Flux<TechnologySummary> for streaming technology collections
     * 
     * ## Response Example (200 OK):
     * ```json
     * [
     *   {
     *     "id": 1,
     *     "name": "Spring Boot",
     *     "category": "Framework",
     *     "version": "3.4.0",
     *     "type": "OPEN_SOURCE",
     *     "annualCost": 0.00
     *   },
     *   {
     *     "id": 2,
     *     "name": "PostgreSQL",
     *     "category": "Database",
     *     "version": "15.0",
     *     "type": "OPEN_SOURCE",
     *     "annualCost": 5000.00
     *   }
     * ]
     * ```
     * 
     * @param portfolioId The ID of the portfolio containing the technologies
     * @return Flux<TechnologySummary> with portfolio's technologies as a reactive stream
     */
    @GetMapping("/{portfolioId}/technologies")
    @PreAuthorize("hasRole('USER')")
    fun getPortfolioTechnologies(@PathVariable portfolioId: Long): Flux<TechnologySummary> {
        return portfolioService.getTechnologiesByPortfolio(portfolioId)
            .onErrorResume { error ->
                println("Error retrieving technologies for portfolio $portfolioId: ${error.message}")
                Flux.empty()
            }
    }

    /**
     * Streams all portfolios as Server-Sent Events (SSE).
     * 
     * Returns a Flux stream of all portfolios as Server-Sent Events for real-time
     * streaming. This endpoint demonstrates advanced Flux<T> usage with SSE
     * for live data streaming and reactive backpressure handling.
     * 
     * **HTTP Method**: GET  
     * **Path**: `/api/v1/portfolios/stream`  
     * **Security**: Requires ADMIN role
     * **Content-Type**: text/event-stream
     * **Reactive**: Returns Flux<PortfolioSummary> as Server-Sent Events
     * 
     * ## SSE Response Format:
     * ```
     * data: {"id":1,"name":"Portfolio 1","type":"ENTERPRISE"}
     * 
     * data: {"id":2,"name":"Portfolio 2","type":"DEVELOPMENT"}
     * 
     * ```
     * 
     * @return Flux<PortfolioSummary> as Server-Sent Events stream
     */
    @GetMapping(value = ["/stream"], produces = ["text/event-stream"])
    @PreAuthorize("hasRole('ADMIN')")
    fun streamAllPortfolios(): Flux<PortfolioSummary> {
        return portfolioService.searchPortfolios(null, null, null, null)
            .onErrorResume { error ->
                println("Error streaming portfolios: ${error.message}")
                Flux.empty()
            }
    }

    /**
     * Streams all technologies as Server-Sent Events (SSE).
     * 
     * Returns a Flux stream of all technologies as Server-Sent Events for real-time
     * streaming. This endpoint demonstrates advanced Flux<T> usage with SSE
     * for live technology data streaming.
     * 
     * **HTTP Method**: GET  
     * **Path**: `/api/v1/portfolios/technologies/stream`  
     * **Security**: Requires ADMIN role
     * **Content-Type**: text/event-stream
     * **Reactive**: Returns Flux<TechnologySummary> as Server-Sent Events
     * 
     * @return Flux<TechnologySummary> as Server-Sent Events stream
     */
    @GetMapping(value = ["/technologies/stream"], produces = ["text/event-stream"])
    @PreAuthorize("hasRole('ADMIN')")
    fun streamAllTechnologies(): Flux<TechnologySummary> {
        // This would typically come from a reactive repository
        // For now, we'll return empty flux since we don't have a global technology query
        return Flux.empty<TechnologySummary>()
            .onErrorResume { error ->
                println("Error streaming technologies: ${error.message}")
                Flux.empty()
            }
    }
} 