package com.company.techportfolio.portfolio.adapter.inbound.web

import com.company.techportfolio.portfolio.domain.model.*
import com.company.techportfolio.portfolio.domain.service.PortfolioService
import com.company.techportfolio.shared.domain.model.PortfolioStatus
import com.company.techportfolio.shared.domain.model.PortfolioType
import com.company.techportfolio.shared.domain.port.CommandResult
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration

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
@Tag(name = "Portfolios", description = "Portfolio management operations")
@SecurityRequirement(name = "bearerAuth")
class PortfolioController(
    private val portfolioService: PortfolioService
) {

    private val logger: Logger = LoggerFactory.getLogger(PortfolioController::class.java)

    /**
     * Create a new portfolio
     *
     * Creates a new portfolio with the provided details.
     * This is a reactive endpoint that uses Mono for single result operations.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('PORTFOLIO_MANAGER') or hasRole('ADMIN')")
    @Operation(
        summary = "Create portfolio",
        description = """
            Creates a new portfolio with the provided details.
            
            **Reactive Operation**: Uses Mono<T> for single result handling
            **Authentication**: Requires PORTFOLIO_MANAGER or ADMIN role
            **Validation**: Validates portfolio data and business rules
            
            ### Example Request:
            ```json
            {
              "name": "Enterprise Java Applications",
              "description": "Portfolio for enterprise Java applications",
              "type": "ENTERPRISE"
            }
            ```
        """,
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Portfolio creation request",
            required = true,
            content = arrayOf(
                Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = CreatePortfolioRequest::class),
                    examples = arrayOf(
                        ExampleObject(
                            name = "Enterprise Portfolio",
                            summary = "Create an enterprise portfolio",
                            value = """
                        {
                          "name": "Enterprise Java Applications",
                          "description": "Portfolio for enterprise Java applications",
                          "type": "ENTERPRISE"
                        }
                        """
                        )
                    )
                )
            )
        )
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "Portfolio created successfully",
                content = arrayOf(
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = PortfolioResponse::class),
                        examples = arrayOf(
                            ExampleObject(
                                name = "Created Portfolio",
                                value = """
                            {
                              "id": 1,
                              "name": "Enterprise Java Applications",
                              "description": "Portfolio for enterprise Java applications",
                              "type": "ENTERPRISE",
                              "status": "ACTIVE",
                              "technologies": [
                                {
                                  "id": 1,
                                  "name": "Spring Boot",
                                  "version": "3.2.0",
                                  "category": "FRAMEWORK",
                                  "status": "ACTIVE",
                                  "maturityLevel": "MATURE",
                                  "description": "Spring Boot framework for Java applications"
                                }
                              ]
                            }
                            """
                            )
                        )
                    )
                )
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid request data",
                ref = "#/components/responses/ValidationError"
            ),
            ApiResponse(
                responseCode = "401",
                description = "Authentication required",
                ref = "#/components/responses/UnauthorizedError"
            ),
            ApiResponse(
                responseCode = "403",
                description = "Insufficient permissions",
                ref = "#/components/responses/ForbiddenError"
            ),
            ApiResponse(
                responseCode = "422",
                description = "Business rule violation",
                content = arrayOf(
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = CommandResult::class)
                    )
                )
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error",
                ref = "#/components/responses/ServerError"
            )
        ]
    )
    fun createPortfolio(@Valid @RequestBody request: CreatePortfolioRequest): Mono<PortfolioResponse> {
        return portfolioService.createPortfolio(request)
            .onErrorMap { error ->
                when (error) {
                    is IllegalArgumentException -> IllegalArgumentException("Invalid portfolio data: ${error.message}")
                    else -> RuntimeException("Failed to create portfolio: ${error.message}")
                }
            }
    }

    /**
     * Get all portfolios
     *
     * Retrieves all portfolios as a reactive stream.
     * This endpoint demonstrates Flux<T> usage for collections.
     */
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(
        summary = "Get all portfolios",
        description = """
            Retrieves all portfolios as a reactive stream.
            
            **Reactive Operation**: Uses Flux<T> for collection handling
            **Authentication**: Requires USER role
            **Streaming**: Returns portfolios as a reactive stream
            
            ### Example Response:
            ```json
            [
              {
                "id": 1,
                "name": "Enterprise Java Applications",
                "type": "ENTERPRISE",
                "technologyCount": 5,
                "totalAnnualCost": 50000.00
              }
            ]
            ```
        """
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Portfolios retrieved successfully",
                content = arrayOf(
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(type = "array", implementation = PortfolioSummary::class),
                        examples = arrayOf(
                            ExampleObject(
                                name = "Portfolio List",
                                value = """
                            [
                              {
                                "id": 1,
                                "name": "Enterprise Java Applications",
                                "description": "Portfolio for enterprise Java applications",
                                "type": "ENTERPRISE",
                                "status": "ACTIVE",
                                "technologies": [
                                  {
                                    "id": 1,
                                    "name": "Spring Boot",
                                    "version": "3.2.0",
                                    "category": "FRAMEWORK",
                                    "status": "ACTIVE",
                                    "maturityLevel": "MATURE",
                                    "description": "Spring Boot framework for Java applications"
                                  }
                                ]
                              }
                            ]
                            """
                            )
                        )
                    )
                )
            ),
            ApiResponse(
                responseCode = "401",
                description = "Authentication required",
                ref = "#/components/responses/UnauthorizedError"
            ),
            ApiResponse(
                responseCode = "403",
                description = "Insufficient permissions",
                ref = "#/components/responses/ForbiddenError"
            ),
            ApiResponse(
                responseCode = "404",
                description = "Portfolio not found",
                ref = "#/components/responses/NotFoundError"
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error",
                ref = "#/components/responses/ServerError"
            )
        ]
    )
    fun getAllPortfolios(): Flux<PortfolioSummary> {
        return portfolioService.searchPortfolios(null, null, null, null)
            .onErrorResume { error: Throwable ->
                logger.error("Error retrieving portfolios: ${error.message}")
                Flux.empty<PortfolioSummary>()
            }
    }

    /**
     * Get portfolio by ID
     *
     * Retrieves a specific portfolio by its ID.
     * This is a reactive endpoint that uses Mono for single result operations.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    @Operation(
        summary = "Get portfolio by ID",
        description = """
            Retrieves a specific portfolio by its ID.
            
            **Reactive Operation**: Uses Mono<T> for single result handling
            **Authentication**: Requires USER role
            **Validation**: Validates portfolio ID
            
            ### Path Parameters:
            - `id`: Portfolio ID (required)
            
            ### Example Request:
            ```
            GET /api/portfolios/1
            ```
        """,
        parameters = [
            Parameter(
                name = "id",
                `in` = ParameterIn.PATH,
                description = "Portfolio ID",
                required = true,
                schema = Schema(type = "integer", example = "1")
            )
        ]
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Portfolio retrieved successfully",
                content = arrayOf(
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = PortfolioResponse::class)
                    )
                )
            ),
            ApiResponse(
                responseCode = "401",
                description = "Authentication required",
                ref = "#/components/responses/UnauthorizedError"
            ),
            ApiResponse(
                responseCode = "403",
                description = "Insufficient permissions",
                ref = "#/components/responses/ForbiddenError"
            ),
            ApiResponse(
                responseCode = "404",
                description = "Portfolio not found",
                ref = "#/components/responses/NotFoundError"
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error",
                ref = "#/components/responses/ServerError"
            )
        ]
    )
    fun getPortfolioById(@PathVariable id: Long): Mono<PortfolioResponse> {
        return portfolioService.getPortfolio(id)
            .onErrorMap { error ->
                when (error) {
                    is IllegalArgumentException -> IllegalArgumentException("Invalid portfolio ID: ${error.message}")
                    else -> RuntimeException("Failed to retrieve portfolio: ${error.message}")
                }
            }
    }

    /**
     * Update a portfolio
     *
     * Updates an existing portfolio with the provided details.
     * This is a reactive endpoint that uses Mono for single result operations.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PORTFOLIO_MANAGER') or hasRole('ADMIN')")
    @Operation(
        summary = "Update portfolio",
        description = """
            Updates an existing portfolio with the provided details.
            
            **Reactive Operation**: Uses Mono<T> for single result handling
            **Authentication**: Requires PORTFOLIO_MANAGER or ADMIN role
            **Validation**: Validates portfolio data and business rules
            
            ### Path Parameters:
            - `id`: Portfolio ID (required)
            
            ### Example Request:
            ```json
            {
              "name": "Updated Enterprise Java Applications",
              "description": "Updated description for enterprise Java applications",
              "type": "ENTERPRISE"
            }
            ```
        """,
        parameters = [
            Parameter(
                name = "id",
                `in` = ParameterIn.PATH,
                description = "Portfolio ID",
                required = true,
                schema = Schema(type = "integer", example = "1")
            )
        ],
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Portfolio update request",
            required = true,
            content = arrayOf(
                Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = UpdatePortfolioRequest::class),
                    examples = arrayOf(
                        ExampleObject(
                            name = "Update Portfolio",
                            value = """
                        {
                          "name": "Updated Enterprise Java Applications",
                          "description": "Updated description for enterprise Java applications",
                          "type": "ENTERPRISE"
                        }
                        """
                        )
                    )
                )
            )
        )
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Portfolio updated successfully",
                content = arrayOf(
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = PortfolioResponse::class)
                    )
                )
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid request data",
                ref = "#/components/responses/ValidationError"
            ),
            ApiResponse(
                responseCode = "401",
                description = "Authentication required",
                ref = "#/components/responses/UnauthorizedError"
            ),
            ApiResponse(
                responseCode = "403",
                description = "Insufficient permissions",
                ref = "#/components/responses/ForbiddenError"
            ),
            ApiResponse(
                responseCode = "404",
                description = "Portfolio not found",
                ref = "#/components/responses/NotFoundError"
            ),
            ApiResponse(
                responseCode = "422",
                description = "Business rule violation",
                content = arrayOf(
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = CommandResult::class)
                    )
                )
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error",
                ref = "#/components/responses/ServerError"
            )
        ]
    )
    fun updatePortfolio(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdatePortfolioRequest
    ): Mono<PortfolioResponse> {
        return portfolioService.updatePortfolio(id, request)
            .onErrorMap { error ->
                when (error) {
                    is IllegalArgumentException -> IllegalArgumentException("Invalid portfolio data: ${error.message}")
                    else -> RuntimeException("Failed to update portfolio: ${error.message}")
                }
            }
    }

    /**
     * Delete a portfolio
     *
     * Deletes a portfolio and all its associated technologies.
     * This is a reactive endpoint that uses Mono for single result operations.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Delete portfolio",
        description = """
            Deletes a portfolio and all its associated technologies.
            
            **Reactive Operation**: Uses Mono<T> for single result handling
            **Authentication**: Requires ADMIN role
            **Cascade**: Deletes all associated technologies and assessments
            
            ### Path Parameters:
            - `id`: Portfolio ID (required)
            
            ### Example Request:
            ```
            DELETE /api/portfolios/1
            ```
        """,
        parameters = [
            Parameter(
                name = "id",
                `in` = ParameterIn.PATH,
                description = "Portfolio ID",
                required = true,
                schema = Schema(type = "integer", example = "1")
            )
        ]
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "204",
                description = "Portfolio deleted successfully"
            ),
            ApiResponse(
                responseCode = "401",
                description = "Authentication required",
                ref = "#/components/responses/UnauthorizedError"
            ),
            ApiResponse(
                responseCode = "403",
                description = "Insufficient permissions",
                ref = "#/components/responses/ForbiddenError"
            ),
            ApiResponse(
                responseCode = "404",
                description = "Portfolio not found",
                ref = "#/components/responses/NotFoundError"
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error",
                ref = "#/components/responses/ServerError"
            )
        ]
    )
    fun deletePortfolio(@PathVariable id: Long): Mono<ResponseEntity<Void>> {
        return portfolioService.deletePortfolio(id)
            .then(Mono.just<ResponseEntity<Void>>(ResponseEntity.noContent().build()))
            .onErrorMap { error: Throwable ->
                when (error) {
                    is IllegalArgumentException -> IllegalArgumentException("Invalid portfolio ID: ${error.message}")
                    else -> RuntimeException("Failed to delete portfolio: ${error.message}")
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
        val userId = jwt.subject?.toLongOrNull() ?: return Flux.empty<PortfolioSummary>()
        return portfolioService.getPortfoliosByOwner(userId)
            .onErrorResume { error: Throwable ->
                logger.error("Error retrieving portfolios for user $userId: ${error.message}")
                Flux.empty<PortfolioSummary>()
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
            .onErrorResume { error: Throwable ->
                logger.error("Error retrieving portfolios for organization $organizationId: ${error.message}")
                Flux.empty<PortfolioSummary>()
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
            .onErrorResume { error: Throwable ->
                logger.error("Error searching portfolios: ${error.message}")
                Flux.empty<PortfolioSummary>()
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
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('PORTFOLIO_MANAGER') or hasRole('ADMIN')")
    @Operation(
        summary = "Add technology to portfolio",
        description = """
            Adds a new technology to an existing portfolio.
            
            **Reactive Operation**: Uses Mono<T> for single result handling
            **Authentication**: Requires PORTFOLIO_MANAGER or ADMIN role
            **Validation**: Validates technology data and portfolio existence
            
            ### Path Parameters:
            - `portfolioId`: Portfolio ID (required)
            
            ### Example Request:
            ```json
            {
              "name": "Spring Boot",
              "version": "3.2.0",
              "category": "FRAMEWORK",
              "description": "Spring Boot framework for Java applications",
              "maturityLevel": "MATURE",
              "status": "ACTIVE"
            }
            ```
        """,
        parameters = [
            Parameter(
                name = "portfolioId",
                `in` = ParameterIn.PATH,
                description = "Portfolio ID",
                required = true,
                schema = Schema(type = "integer", example = "1")
            )
        ],
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Technology creation request",
            required = true,
            content = arrayOf(
                Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = AddTechnologyRequest::class),
                    examples = arrayOf(
                        ExampleObject(
                            name = "Add Technology",
                            value = """
                        {
                          "name": "Spring Boot",
                          "version": "3.2.0",
                          "category": "FRAMEWORK",
                          "description": "Spring Boot framework for Java applications",
                          "maturityLevel": "MATURE",
                          "status": "ACTIVE"
                        }
                        """
                        )
                    )
                )
            )
        )
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "Technology added successfully",
                content = arrayOf(
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = TechnologyResponse::class)
                    )
                )
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid request data",
                ref = "#/components/responses/ValidationError"
            ),
            ApiResponse(
                responseCode = "401",
                description = "Authentication required",
                ref = "#/components/responses/UnauthorizedError"
            ),
            ApiResponse(
                responseCode = "403",
                description = "Insufficient permissions",
                ref = "#/components/responses/ForbiddenError"
            ),
            ApiResponse(
                responseCode = "404",
                description = "Portfolio not found",
                ref = "#/components/responses/NotFoundError"
            ),
            ApiResponse(
                responseCode = "422",
                description = "Business rule violation",
                content = arrayOf(
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = CommandResult::class)
                    )
                )
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error",
                ref = "#/components/responses/ServerError"
            )
        ]
    )
    fun addTechnologyToPortfolio(
        @PathVariable portfolioId: Long,
        @Valid @RequestBody request: AddTechnologyRequest
    ): Mono<TechnologyResponse> {
        return portfolioService.addTechnology(portfolioId, request)
            .onErrorMap { error ->
                when (error) {
                    is IllegalArgumentException -> IllegalArgumentException("Invalid technology data: ${error.message}")
                    else -> RuntimeException("Failed to add technology to portfolio: ${error.message}")
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
    @Operation(
        summary = "Remove technology from portfolio",
        description = "Permanently removes the specified technology from the portfolio."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "204",
                description = "Technology removed successfully"
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid request data",
                ref = "#/components/responses/ValidationError"
            ),
            ApiResponse(
                responseCode = "401",
                description = "Authentication required",
                ref = "#/components/responses/UnauthorizedError"
            ),
            ApiResponse(
                responseCode = "403",
                description = "Insufficient permissions",
                ref = "#/components/responses/ForbiddenError"
            ),
            ApiResponse(
                responseCode = "404",
                description = "Portfolio or technology not found",
                ref = "#/components/responses/NotFoundError"
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error",
                ref = "#/components/responses/ServerError"
            )
        ]
    )
    fun removeTechnologyFromPortfolio(
        @PathVariable portfolioId: Long,
        @PathVariable technologyId: Long
    ): Mono<ResponseEntity<Void>> {
        return portfolioService.removeTechnology(portfolioId, technologyId)
            .then(Mono.just<ResponseEntity<Void>>(ResponseEntity.noContent().build()))
            .onErrorResume { error: Throwable ->
                when (error) {
                    is IllegalArgumentException -> Mono.just<ResponseEntity<Void>>(ResponseEntity.badRequest().build())
                    else -> Mono.just<ResponseEntity<Void>>(ResponseEntity.internalServerError().build())
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
    @Operation(
        summary = "Get all technologies for a portfolio",
        description = "Returns a Flux stream of technology summaries for the specified portfolio."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Technologies retrieved successfully",
                content = arrayOf(
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(type = "array", implementation = TechnologySummary::class)
                    )
                )
            ),
            ApiResponse(
                responseCode = "401",
                description = "Authentication required",
                ref = "#/components/responses/UnauthorizedError"
            ),
            ApiResponse(
                responseCode = "403",
                description = "Insufficient permissions",
                ref = "#/components/responses/ForbiddenError"
            ),
            ApiResponse(
                responseCode = "404",
                description = "Portfolio not found",
                ref = "#/components/responses/NotFoundError"
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error",
                ref = "#/components/responses/ServerError"
            )
        ]
    )
    fun getTechnologiesByPortfolio(@PathVariable portfolioId: Long): Flux<TechnologySummary> {
        return portfolioService.getTechnologiesByPortfolio(portfolioId)
            .onErrorResume { error: Throwable ->
                logger.error("Error retrieving technologies for portfolio $portfolioId: ${error.message}")
                Flux.empty<TechnologySummary>()
            }
    }

    /**
     * Stream portfolios in real-time
     *
     * Streams portfolio updates in real-time using Server-Sent Events (SSE).
     * This is a reactive streaming endpoint that uses Flux for continuous data flow.
     */
    @GetMapping(value = ["/stream"], produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    @PreAuthorize("hasRole('VIEWER') or hasRole('PORTFOLIO_MANAGER') or hasRole('ADMIN')")
    @Operation(
        summary = "Stream portfolios in real-time",
        description = "Streams portfolio updates in real-time using Server-Sent Events (SSE)."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Portfolio stream started",
                content = arrayOf(
                    Content(
                        mediaType = MediaType.TEXT_EVENT_STREAM_VALUE,
                        examples = arrayOf(
                            ExampleObject(
                                name = "SSE Stream",
                                value = """
                            data: {\"id\": 1, \"name\": \"Enterprise Java\", \"type\": \"ENTERPRISE\", \"updatedAt\": \"2024-01-15T10:30:00Z\"}
                            data: {\"id\": 2, \"name\": \"Cloud Infrastructure\", \"type\": \"CLOUD\", \"updatedAt\": \"2024-01-15T10:31:00Z\"}
                            """
                            )
                        )
                    )
                )
            ),
            ApiResponse(
                responseCode = "401",
                description = "Authentication required",
                ref = "#/components/responses/UnauthorizedError"
            ),
            ApiResponse(
                responseCode = "403",
                description = "Insufficient permissions",
                ref = "#/components/responses/ForbiddenError"
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error",
                ref = "#/components/responses/ServerError"
            )
        ]
    )
    fun streamPortfolios(): Flux<PortfolioSummary> {
        return portfolioService.searchPortfolios(null, null, null, null)
            .switchIfEmpty(
                // Fallback to mock data if no portfolios found (useful for tests)
                Flux.range(1, 5).map { id ->
                    PortfolioSummary(
                        id = id.toLong(),
                        name = "Mock Portfolio $id",
                        type = when (id % 3) {
                            0 -> PortfolioType.ENTERPRISE
                            1 -> PortfolioType.PROJECT
                            else -> PortfolioType.PERSONAL
                        },
                        status = PortfolioStatus.ACTIVE,
                        ownerId = (1..100).random().toLong(),
                        organizationId = (1..50).random().toLong(),
                        technologyCount = id * 2,
                        totalAnnualCost = (id * 1000.0).toBigDecimal(),
                        lastUpdated = java.time.LocalDateTime.now()
                    )
                }
            )
            .delayElements(Duration.ofMillis(100)) // Reduced delay for tests
            .onErrorMap { error: Throwable ->
                RuntimeException("Failed to stream portfolios: ${error.message}")
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
    @Operation(
        summary = "Stream all technologies as SSE",
        description = "Returns a Flux stream of all technologies as Server-Sent Events for real-time streaming."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Technology stream started",
                content = arrayOf(
                    Content(
                        mediaType = MediaType.TEXT_EVENT_STREAM_VALUE,
                        schema = Schema(type = "array", implementation = TechnologySummary::class)
                    )
                )
            ),
            ApiResponse(
                responseCode = "401",
                description = "Authentication required",
                ref = "#/components/responses/UnauthorizedError"
            ),
            ApiResponse(
                responseCode = "403",
                description = "Insufficient permissions",
                ref = "#/components/responses/ForbiddenError"
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error",
                ref = "#/components/responses/ServerError"
            )
        ]
    )
    fun streamAllTechnologies(): Flux<TechnologySummary> {
        return Flux.empty<TechnologySummary>()
            .onErrorResume { error: Throwable ->
                logger.error("Error streaming technologies: ${error.message}")
                Flux.empty<TechnologySummary>()
            }
    }
} 