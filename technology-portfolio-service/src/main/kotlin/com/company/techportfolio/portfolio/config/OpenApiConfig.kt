package com.company.techportfolio.portfolio.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.media.ArraySchema
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.responses.ApiResponse
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import io.swagger.v3.oas.models.tags.Tag
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * OpenAPI 3.0 Configuration for Technology Portfolio Service
 *
 * Provides comprehensive API documentation with reactive examples,
 * security definitions, and detailed response schemas.
 */
@Configuration
class OpenApiConfig {

    @Bean
    fun customOpenAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("Technology Portfolio Service API")
                    .description(
                        """
                        ## Overview
                        
                        The Technology Portfolio Service provides comprehensive portfolio management capabilities
                        with reactive programming support using Spring WebFlux.
                        
                        ### Key Features
                        - **Reactive Portfolio Management**: Create, update, and manage technology portfolios
                        - **Technology Management**: Manage individual technologies within portfolios
                        - **Assessment Management**: Handle portfolio and technology assessments
                        - **Dependency Management**: Manage technology dependencies and relationships
                        - **Event-Driven Communication**: Publish domain events for other services
                        - **CQRS Implementation**: Separate read and write operations for better performance
                        
                        ### Reactive Programming
                        
                        This API uses Spring WebFlux with reactive types:
                        - `Mono<T>`: Single result operations
                        - `Flux<T>`: Multiple result operations and streaming
                        - Server-Sent Events (SSE) for real-time updates
                        
                        ### Authentication
                        
                        All endpoints require JWT authentication. Include the JWT token in the Authorization header:
                        ```
                        Authorization: Bearer <your-jwt-token>
                        ```
                        
                        ### Rate Limiting
                        
                        API endpoints are rate-limited to ensure fair usage:
                        - Standard endpoints: 100 requests per minute
                        - Streaming endpoints: 10 connections per minute
                        - Admin endpoints: 50 requests per minute
                        
                        ### Error Handling
                        
                        The API uses standard HTTP status codes and provides detailed error responses:
                        - `400 Bad Request`: Invalid request data
                        - `401 Unauthorized`: Missing or invalid authentication
                        - `403 Forbidden`: Insufficient permissions
                        - `404 Not Found`: Resource not found
                        - `422 Unprocessable Entity`: Business rule violations
                        - `500 Internal Server Error`: Server errors
                        
                        ### Pagination
                        
                        List endpoints support pagination with the following parameters:
                        - `page`: Page number (0-based, default: 0)
                        - `size`: Page size (default: 20, max: 100)
                        - `sort`: Sort field and direction (e.g., `name,asc`)
                        
                        ### Streaming
                        
                        Streaming endpoints use Server-Sent Events (SSE) for real-time data:
                        - Content-Type: `text/event-stream`
                        - Connection: `keep-alive`
                        - Cache-Control: `no-cache`
                    """.trimIndent()
                    )
                    .version("1.0.0")
                    .contact(
                        Contact()
                            .name("Technology Portfolio Team")
                            .email("tech-portfolio@company.com")
                            .url("https://techportfolio.company.com")
                    )
                    .license(
                        License()
                            .name("MIT License")
                            .url("https://opensource.org/licenses/MIT")
                    )
            )
            .servers(
                listOf(
                    Server()
                        .url("http://localhost:8083")
                        .description("Local Development Server"),
                    Server()
                        .url("https://dev.techportfolio.company.com")
                        .description("Development Environment"),
                    Server()
                        .url("https://test.techportfolio.company.com")
                        .description("Test Environment"),
                    Server()
                        .url("https://techportfolio.company.com")
                        .description("Production Environment")
                )
            )
            .tags(
                listOf(
                    Tag()
                        .name("Portfolios")
                        .description("Portfolio management operations"),
                    Tag()
                        .name("Technologies")
                        .description("Technology management within portfolios"),
                    Tag()
                        .name("Assessments")
                        .description("Portfolio and technology assessments"),
                    Tag()
                        .name("Dependencies")
                        .description("Technology dependency management"),
                    Tag()
                        .name("Streaming")
                        .description("Real-time data streaming endpoints"),
                    Tag()
                        .name("Health")
                        .description("Service health and monitoring")
                )
            )
            .components(
                Components()
                    .securitySchemes(
                        mapOf(
                            "bearerAuth" to SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT token for authentication")
                        )
                    )
                    .responses(
                        mapOf(
                            "UnauthorizedError" to ApiResponse()
                                .description("Authentication required")
                                .content(
                                    Content()
                                        .addMediaType(
                                            "application/json",
                                            MediaType()
                                                .schema(
                                                    Schema<Any>()
                                                        .type("object")
                                                        .properties(
                                                            mapOf(
                                                                "error" to Schema<Any>().type("string").example("Unauthorized"),
                                                                "message" to Schema<Any>().type("string").example("Authentication required"),
                                                                "timestamp" to Schema<Any>().type("string").example("2024-01-15T10:30:00Z")
                                                            )
                                                        )
                                                )
                                        )
                                ),
                            "ForbiddenError" to ApiResponse()
                                .description("Insufficient permissions")
                                .content(
                                    Content()
                                        .addMediaType(
                                            "application/json",
                                            MediaType()
                                                .schema(
                                                    Schema<Any>()
                                                        .type("object")
                                                        .properties(
                                                            mapOf(
                                                                "error" to Schema<Any>().type("string").example("Forbidden"),
                                                                "message" to Schema<Any>().type("string").example("Insufficient permissions"),
                                                                "timestamp" to Schema<Any>().type("string").example("2024-01-15T10:30:00Z")
                                                            )
                                                        )
                                                )
                                        )
                                ),
                            "NotFoundError" to ApiResponse()
                                .description("Resource not found")
                                .content(
                                    Content()
                                        .addMediaType(
                                            "application/json",
                                            MediaType()
                                                .schema(
                                                    Schema<Any>()
                                                        .type("object")
                                                        .properties(
                                                            mapOf(
                                                                "error" to Schema<Any>().type("string").example("Not Found"),
                                                                "message" to Schema<Any>().type("string").example("Resource not found"),
                                                                "timestamp" to Schema<Any>().type("string").example("2024-01-15T10:30:00Z")
                                                            )
                                                        )
                                                )
                                        )
                                ),
                            "ValidationError" to ApiResponse()
                                .description("Validation error")
                                .content(
                                    Content()
                                        .addMediaType(
                                            "application/json",
                                            MediaType()
                                                .schema(
                                                    Schema<Any>()
                                                        .type("object")
                                                        .properties(
                                                            mapOf(
                                                                "error" to Schema<Any>().type("string").example("Bad Request"),
                                                                "message" to Schema<Any>().type("string").example("Validation failed"),
                                                                "timestamp" to Schema<Any>().type("string").example("2024-01-15T10:30:00Z"),
                                                                "errors" to ArraySchema().items(Schema<Any>().type("string")).example(
                                                                    listOf(
                                                                        "Name is required",
                                                                        "Description cannot be empty"
                                                                    )
                                                                )
                                                            )
                                                        )
                                                )
                                        )
                                ),
                            "ServerError" to ApiResponse()
                                .description("Internal server error")
                                .content(
                                    Content()
                                        .addMediaType(
                                            "application/json",
                                            MediaType()
                                                .schema(
                                                    Schema<Any>()
                                                        .type("object")
                                                        .properties(
                                                            mapOf(
                                                                "error" to Schema<Any>().type("string").example("Internal Server Error"),
                                                                "message" to Schema<Any>().type("string").example("An unexpected error occurred"),
                                                                "timestamp" to Schema<Any>().type("string").example("2024-01-15T10:30:00Z")
                                                            )
                                                        )
                                                )
                                        )
                                )
                        )
                    )
            )
            .addSecurityItem(
                SecurityRequirement()
                    .addList("bearerAuth")
            )
    }
} 