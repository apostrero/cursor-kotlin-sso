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
import com.company.techportfolio.shared.domain.model.CommandResult
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import io.swagger.v3.oas.annotations.enums.ParameterIn
import org.springframework.http.MediaType
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

    /**
     * Create a new technology portfolio
     * 
     * Creates a new portfolio with the provided details and returns the created portfolio.
     * This is a reactive endpoint that uses Mono for single result operations.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('PORTFOLIO_MANAGER') or hasRole('ADMIN')")
    @Operation(
        summary = "Create a new portfolio",
        description = """
            Creates a new technology portfolio with the provided details.
            
            **Reactive Operation**: Uses Mono<T> for single result handling
            **Authentication**: Requires PORTFOLIO_MANAGER or ADMIN role
            **Validation**: Validates portfolio name, type, and description
            
            ### Example Request:
            ```json
            {
              "name": "Enterprise Java Applications",
              "description": "Portfolio for enterprise Java applications and frameworks",
              "type": "ENTERPRISE",
              "organizationId": 1
            }
            ```
        """,
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Portfolio creation request",
            required = true,
            content = Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = Schema(implementation = CreatePortfolioRequest::class),
                examples = arrayOf(
                    ExampleObject(
                        name = "Enterprise Portfolio",
                        summary = "Create an enterprise portfolio",
                        value = """
                        {
                          "name": "Enterprise Java Applications",
                          "description": "Portfolio for enterprise Java applications and frameworks",
                          "type": "ENTERPRISE",
                          "organizationId": 1
                        }
                        """
                    ),
                    ExampleObject(
                        name = "Cloud Portfolio",
                        summary = "Create a cloud portfolio",
                        value = """
                        {
                          "name": "Cloud Infrastructure",
                          "description": "Portfolio for cloud infrastructure and services",
                          "type": "CLOUD",
                          "organizationId": 1
                        }
                        """
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
                content = Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = PortfolioResponse::class),
                    examples = arrayOf(
                        ExampleObject(
                            name = "Created Portfolio",
                            value = """
                            {
                              "id": 1,
                              "name": "Enterprise Java Applications",
                              "description": "Portfolio for enterprise Java applications and frameworks",
                              "type": "ENTERPRISE",
                              "organizationId": 1,
                              "createdAt": "2024-01-15T10:30:00Z",
                              "updatedAt": "2024-01-15T10:30:00Z",
                              "technologies": []
                            }
                            """
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
                content = Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = CommandResult::class)
                )
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error",
                ref = "#/components/responses/ServerError"
            )
        ]
    )
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
     * Get all portfolios with pagination and filtering
     * 
     * Retrieves a paginated list of portfolios with optional filtering.
     * This is a reactive endpoint that uses Flux for multiple results.
     */
    @GetMapping
    @PreAuthorize("hasRole('VIEWER') or hasRole('PORTFOLIO_MANAGER') or hasRole('ADMIN')")
    @Operation(
        summary = "Get all portfolios",
        description = """
            Retrieves a paginated list of portfolios with optional filtering.
            
            **Reactive Operation**: Uses Flux<T> for multiple result handling
            **Authentication**: Requires VIEWER, PORTFOLIO_MANAGER, or ADMIN role
            **Pagination**: Supports page, size, and sort parameters
            **Filtering**: Supports filtering by type, organization, and name
            
            ### Query Parameters:
            - `page`: Page number (0-based, default: 0)
            - `size`: Page size (default: 20, max: 100)
            - `sort`: Sort field and direction (e.g., `name,asc`, `createdAt,desc`)
            - `type`: Filter by portfolio type (ENTERPRISE, CLOUD, MOBILE, etc.)
            - `organizationId`: Filter by organization ID
            - `name`: Filter by portfolio name (partial match)
            
            ### Example Requests:
            ```
            GET /api/portfolios?page=0&size=10&sort=name,asc
            GET /api/portfolios?type=ENTERPRISE&organizationId=1
            GET /api/portfolios?name=Java&sort=createdAt,desc
            ```
        """,
        parameters = [
            Parameter(
                name = "page",
                `in` = ParameterIn.QUERY,
                description = "Page number (0-based)",
                schema = Schema(type = "integer", defaultValue = "0", minimum = "0")
            ),
            Parameter(
                name = "size",
                `in` = ParameterIn.QUERY,
                description = "Page size",
                schema = Schema(type = "integer", defaultValue = "20", minimum = "1", maximum = "100")
            ),
            Parameter(
                name = "sort",
                `in` = ParameterIn.QUERY,
                description = "Sort field and direction (e.g., name,asc)",
                schema = Schema(type = "string", example = "name,asc")
            ),
            Parameter(
                name = "type",
                `in` = ParameterIn.QUERY,
                description = "Filter by portfolio type",
                schema = Schema(type = "string", allowableValues = ["ENTERPRISE", "CLOUD", "MOBILE", "WEB", "DATA"])
            ),
            Parameter(
                name = "organizationId",
                `in` = ParameterIn.QUERY,
                description = "Filter by organization ID",
                schema = Schema(type = "integer")
            ),
            Parameter(
                name = "name",
                `in` = ParameterIn.QUERY,
                description = "Filter by portfolio name (partial match)",
                schema = Schema(type = "string")
            )
        ]
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Portfolios retrieved successfully",
                content = Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = ArraySchema::class).items(Schema(implementation = PortfolioSummary::class)),
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
                                "technologyCount": 5,
                                "createdAt": "2024-01-15T10:30:00Z"
                              },
                              {
                                "id": 2,
                                "name": "Cloud Infrastructure",
                                "description": "Portfolio for cloud infrastructure",
                                "type": "CLOUD",
                                "technologyCount": 3,
                                "createdAt": "2024-01-15T11:00:00Z"
                              }
                            ]
                            """
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
    fun getAllPortfolios(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) sort: String?,
        @RequestParam(required = false) type: String?,
        @RequestParam(required = false) organizationId: Long?,
        @RequestParam(required = false) name: String?
    ): Flux<PortfolioSummary> {
        return portfolioService.getAllPortfolios(page, size, sort, type, organizationId, name)
            .onErrorMap { error ->
                RuntimeException("Failed to retrieve portfolios: ${error.message}")
            }
    }

    /**
     * Get a specific portfolio by ID
     * 
     * Retrieves detailed information about a specific portfolio including its technologies.
     * This is a reactive endpoint that uses Mono for single result operations.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('VIEWER') or hasRole('PORTFOLIO_MANAGER') or hasRole('ADMIN')")
    @Operation(
        summary = "Get portfolio by ID",
        description = """
            Retrieves detailed information about a specific portfolio including its technologies.
            
            **Reactive Operation**: Uses Mono<T> for single result handling
            **Authentication**: Requires VIEWER, PORTFOLIO_MANAGER, or ADMIN role
            **Caching**: Response is cached for 5 minutes
            
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
                content = Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = PortfolioResponse::class),
                    examples = arrayOf(
                        ExampleObject(
                            name = "Portfolio with Technologies",
                            value = """
                            {
                              "id": 1,
                              "name": "Enterprise Java Applications",
                              "description": "Portfolio for enterprise Java applications and frameworks",
                              "type": "ENTERPRISE",
                              "organizationId": 1,
                              "createdAt": "2024-01-15T10:30:00Z",
                              "updatedAt": "2024-01-15T10:30:00Z",
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
        return portfolioService.getPortfolioById(id)
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
            content = Content(
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
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Portfolio updated successfully",
                content = Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = PortfolioResponse::class)
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
                content = Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = CommandResult::class)
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
            .then(Mono.just(ResponseEntity.noContent().build()))
            .onErrorMap { error ->
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
            content = Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = Schema(implementation = AddTechnologyRequest::class),
                examples = arrayOf(
                    ExampleObject(
                        name = "Add Framework",
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
                    ),
                    ExampleObject(
                        name = "Add Database",
                        value = """
                        {
                          "name": "PostgreSQL",
                          "version": "15.0",
                          "category": "DATABASE",
                          "description": "PostgreSQL relational database",
                          "maturityLevel": "MATURE",
                          "status": "ACTIVE"
                        }
                        """
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
                content = Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = TechnologyResponse::class)
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
                content = Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = CommandResult::class)
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
        return portfolioService.addTechnologyToPortfolio(portfolioId, request)
            .onErrorMap { error ->
                when (error) {
                    is IllegalArgumentException -> IllegalArgumentException("Invalid technology data: ${error.message}")
                    else -> RuntimeException("Failed to add technology: ${error.message}")
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
     * Stream portfolios in real-time
     * 
     * Streams portfolio updates in real-time using Server-Sent Events (SSE).
     * This is a reactive streaming endpoint that uses Flux for continuous data flow.
     */
    @GetMapping(value = ["/stream"], produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    @PreAuthorize("hasRole('VIEWER') or hasRole('PORTFOLIO_MANAGER') or hasRole('ADMIN')")
    @Operation(
        summary = "Stream portfolios in real-time",
        description = """
            Streams portfolio updates in real-time using Server-Sent Events (SSE).
            
            **Reactive Streaming**: Uses Flux<T> for continuous data flow
            **Authentication**: Requires VIEWER, PORTFOLIO_MANAGER, or ADMIN role
            **Real-time**: Provides live updates as portfolios change
            **SSE**: Uses Server-Sent Events for browser compatibility
            
            ### Headers:
            - `Accept: text/event-stream`
            - `Cache-Control: no-cache`
            - `Connection: keep-alive`
            
            ### Example Response:
            ```
            data: {"id": 1, "name": "Enterprise Java", "type": "ENTERPRISE", "updatedAt": "2024-01-15T10:30:00Z"}
            
            data: {"id": 2, "name": "Cloud Infrastructure", "type": "CLOUD", "updatedAt": "2024-01-15T10:31:00Z"}
            ```
        """
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Portfolio stream started",
                content = Content(
                    mediaType = MediaType.TEXT_EVENT_STREAM_VALUE,
                    examples = arrayOf(
                        ExampleObject(
                            name = "SSE Stream",
                            value = """
                            data: {"id": 1, "name": "Enterprise Java", "type": "ENTERPRISE", "updatedAt": "2024-01-15T10:30:00Z"}
                            
                            data: {"id": 2, "name": "Cloud Infrastructure", "type": "CLOUD", "updatedAt": "2024-01-15T10:31:00Z"}
                            """
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
        return portfolioService.streamPortfolios()
            .delayElements(Duration.ofSeconds(1)) // Simulate real-time updates
            .onErrorMap { error ->
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