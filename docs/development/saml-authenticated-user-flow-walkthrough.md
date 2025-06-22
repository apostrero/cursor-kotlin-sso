# SAML Authenticated User Flow - Code Walk-Through

This document provides a detailed walk-through of the actual classes and functions involved in the SAML authenticated user flow, tracing the execution path when a user with a valid JWT token from SAML authentication accesses protected resources.

## Overview

The SAML authenticated user flow handles users who already have valid JWT tokens from previous SAML authentication, allowing them to directly access protected resources without going through the full SAML authentication process again.

## Flow Sequence

### 1. User Already Authenticated with SAML

**Prerequisites**: User has valid JWT token from previous SAML authentication stored in browser cookies

**JWT Token Structure**:
```json
{
  "sub": "user1",
  "authorities": ["ROLE_PORTFOLIO_MANAGER"],
  "sessionIndex": "session-123",
  "iat": 1640995200,
  "exp": 1640998800
}
```

### 2. Direct Access to Protected Resource

**User Action**: Access protected resource (e.g., `/api/portfolios`)

**Browser → API Gateway**: `GET /api/portfolios` with JWT token

**Classes Involved**:
- **Entry Point**: `PortfolioController.getAllPortfolios()` (Technology Portfolio Service)
- **JWT Validation**: `JwtAuthenticationAdapter.validateToken()`
- **Authorization**: `AuthorizationServiceAdapter.authorizeUser()`

**Code Path**:
```kotlin
// PortfolioController.kt - Portfolio Retrieval Endpoint
@GetMapping
@PreAuthorize("hasRole('USER')")
fun getAllPortfolios(): Flux<PortfolioSummary> {
    return portfolioService.getAllPortfolios()
        .onErrorResume { error: Throwable ->
            logger.error("Error retrieving portfolios: ${error.message}")
            Flux.empty<PortfolioSummary>()
        }
}
```

### 3. JWT Token Extraction and Validation

**API Gateway**: Extract JWT token from Authorization header or cookies

**Classes Involved**:
- **JWT Adapter**: `JwtAuthenticationAdapter.validateToken()`
- **Token Model**: `TokenValidationResult`

**Code Path**:
```kotlin
// JwtAuthenticationAdapter.kt - JWT Token Validation
override fun validateToken(token: String): TokenValidationResult {
    return try {
        val claims = getClaimsFromToken(token)
        val username = claims.subject
        @Suppress("UNCHECKED_CAST")
        val authorities = claims["authorities"] as? List<String> ?: emptyList()
        val sessionIndex = claims["sessionIndex"] as? String
        val issuedAt = claims.issuedAt.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
        val expiresAt = claims.expiration.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()

        if (LocalDateTime.now().isAfter(expiresAt)) {
            TokenValidationResult.expired(username, authorities, sessionIndex)
        } else {
            TokenValidationResult.valid(username, authorities, sessionIndex, issuedAt, expiresAt)
        }
    } catch (e: io.jsonwebtoken.ExpiredJwtException) {
        // Handle expired token specifically
        val claims = e.claims
        val username = claims.subject
        @Suppress("UNCHECKED_CAST")
        val authorities = claims["authorities"] as? List<String> ?: emptyList()
        val sessionIndex = claims["sessionIndex"] as? String
        TokenValidationResult.expired(username, authorities, sessionIndex)
    } catch (e: Exception) {
        TokenValidationResult.invalid("Token validation failed: ${e.message}")
    }
}
```

**Token Claims Extraction**:
```kotlin
// JwtAuthenticationAdapter.kt - Claims Extraction
private fun getClaimsFromToken(token: String): Claims {
    return Jwts.parserBuilder()
        .setSigningKey(signingKey)
        .build()
        .parseClaimsJws(token)
        .body
}
```

**Validation Result**:
```kotlin
// TokenValidationResult.kt - Validation Result Model
data class TokenValidationResult(
    val isValid: Boolean,
    val username: String? = null,
    val authorities: List<String> = emptyList(),
    val sessionIndex: String? = null,
    val issuedAt: LocalDateTime? = null,
    val expiresAt: LocalDateTime? = null,
    val isExpired: Boolean = false,
    val errorMessage: String? = null
)
```

### 4. SAML User Authorization Check

**API Gateway → Authorization Service**: `POST /api/auth/authorize`

**Classes Involved**:
- **Gateway Adapter**: `AuthorizationServiceAdapter.authorizeUser()`
- **Authorization Controller**: `AuthorizationController.checkAuthorization()`
- **Authorization Service**: `AuthorizationService.authorizeUser()`

**Code Path**:
```kotlin
// AuthorizationServiceAdapter.kt - Gateway to Authorization Service
override fun authorizeUser(username: String, resource: String, action: String): AuthorizationResult {
    return try {
        val url = "$authorizationServiceUrl/api/authorization/check"
        val request = mapOf(
            "username" to username,
            "resource" to resource,
            "action" to action
        )

        val response = restTemplate.postForObject(url, request, AuthorizationResult::class.java)
        response ?: AuthorizationResult.unauthorized(username, resource, action, "No response from authorization service")
    } catch (e: Exception) {
        AuthorizationResult.unauthorized(username, resource, action, "Authorization service error: ${e.message}")
    }
}
```

**Authorization Service Processing**:
```kotlin
// AuthorizationService.kt - Authorization Decision
fun authorizeUser(request: AuthorizationRequest): AuthorizationResponse {
    return try {
        val username = request.username
        val resource = request.resource
        val action = request.action

        // Check if user exists and is active
        if (!userRepository.isUserActive(username)) {
            return AuthorizationResponse.unauthorized(
                username = username,
                resource = resource,
                action = action,
                errorMessage = "User is not active or does not exist"
            )
        }

        // Check if user has the required permission
        val hasPermission = permissionRepository.hasPermission(username, resource, action)

        if (hasPermission) {
            val permissions = userRepository.findUserPermissions(username)
            val roles = userRepository.findUserRoles(username)
            val organizationId = userRepository.findUserOrganization(username)

            AuthorizationResponse.authorized(
                username = username,
                resource = resource,
                action = action,
                permissions = permissions,
                roles = roles,
                organizationId = organizationId
            )
        } else {
            AuthorizationResponse.unauthorized(
                username = username,
                resource = resource,
                action = action,
                errorMessage = "User does not have permission for $resource:$action"
            )
        }
    } catch (e: Exception) {
        AuthorizationResponse.unauthorized(
            username = request.username,
            resource = request.resource,
            action = request.action,
            errorMessage = "Authorization failed: ${e.message}"
        )
    }
}
```

**Database Query for SAML User**:
```sql
-- Query user permissions with SAML data
SELECT u.username, u.email, u.saml_name_id, r.name as role
FROM users u
JOIN user_roles ur ON u.id = ur.user_id
JOIN roles r ON ur.role_id = r.id
WHERE u.id = 1 AND u.saml_name_id IS NOT NULL
```

**Response**: `200 OK` with authorization decision and user details

### 5. Service Communication with SAML User

**API Gateway → Technology Portfolio Service**: `GET /api/portfolios`

**Classes Involved**:
- **Portfolio Controller**: `PortfolioController.getAllPortfolios()`
- **Portfolio Service**: `PortfolioService.getAllPortfolios()`
- **Portfolio Repository**: `PortfolioRepository.findAll()`

**Code Path**:
```kotlin
// PortfolioController.kt - Portfolio Retrieval
@GetMapping
@PreAuthorize("hasRole('USER')")
fun getAllPortfolios(): Flux<PortfolioSummary> {
    return portfolioService.getAllPortfolios()
        .onErrorResume { error: Throwable ->
            logger.error("Error retrieving portfolios: ${error.message}")
            Flux.empty<PortfolioSummary>()
        }
}
```

**Portfolio Service Processing**:
```kotlin
// PortfolioService.kt - Business Logic
fun getAllPortfolios(): Flux<PortfolioSummary> {
    return portfolioRepository.findAll()
        .flatMap { portfolio ->
            technologyRepository.countByPortfolioId(portfolio.id!!)
                .map { techCount ->
                    PortfolioSummary(
                        id = portfolio.id!!,
                        name = portfolio.name,
                        description = portfolio.description,
                        type = portfolio.type,
                        status = portfolio.status,
                        technologyCount = techCount.toInt(),
                        totalAnnualCost = calculateTotalAnnualCost(portfolio.id!!),
                        createdAt = portfolio.createdAt,
                        updatedAt = portfolio.updatedAt
                    )
                }
        }
        .onErrorResume { error ->
            logger.error("Error retrieving portfolios: ${error.message}")
            Flux.empty()
        }
}
```

**Database Query**:
```sql
-- Query portfolios for SAML user
SELECT * FROM portfolios
WHERE owner_id = 1 OR is_public = true
ORDER BY created_at DESC
```

**Response**: `200 OK` with portfolio data filtered for the SAML user

### 6. Alternative: Create New Portfolio (SAML User)

**User Action**: Create new portfolio via `POST /api/portfolios`

**Classes Involved**:
- **Portfolio Controller**: `PortfolioController.createPortfolio()`
- **Portfolio Service**: `PortfolioService.createPortfolio()`
- **Portfolio Repository**: `PortfolioRepository.save()`

**Code Path**:
```kotlin
// PortfolioController.kt - Portfolio Creation
@PostMapping
@ResponseStatus(HttpStatus.CREATED)
@PreAuthorize("hasRole('PORTFOLIO_MANAGER') or hasRole('ADMIN')")
fun createPortfolio(@Valid @RequestBody request: CreatePortfolioRequest): Mono<PortfolioResponse> {
    return portfolioService.createPortfolio(request)
        .onErrorMap { error ->
            when (error) {
                is IllegalArgumentException -> IllegalArgumentException("Invalid portfolio data: ${error.message}")
                else -> RuntimeException("Failed to create portfolio: ${error.message}")
            }
        }
}
```

**Portfolio Service Creation Logic**:
```kotlin
// PortfolioService.kt - Portfolio Creation
fun createPortfolio(request: CreatePortfolioRequest): Mono<PortfolioResponse> {
    return portfolioRepository.findByName(request.name)
        .flatMap { _ ->
            Mono.error<PortfolioResponse>(
                IllegalArgumentException("Portfolio with name '${request.name}' already exists")
            )
        }
        .switchIfEmpty(
            Mono.defer {
                val portfolio = TechnologyPortfolio(
                    name = request.name,
                    description = request.description,
                    type = request.type,
                    status = PortfolioStatus.ACTIVE,
                    isActive = true,
                    createdAt = LocalDateTime.now(),
                    ownerId = request.ownerId,
                    organizationId = request.organizationId
                )

                portfolioRepository.save(portfolio)
                    .flatMap { savedPortfolio ->
                        // Publish event
                        eventPublisher.publish(
                            PortfolioCreatedEvent(
                                portfolioId = savedPortfolio.id!!,
                                name = savedPortfolio.name,
                                ownerId = savedPortfolio.ownerId,
                                organizationId = savedPortfolio.organizationId
                            )
                        ).then(Mono.just(savedPortfolio))
                    }
                    .flatMap { toPortfolioResponse(it) }
            }
        )
        .onErrorMap { error ->
            when (error) {
                is IllegalArgumentException -> error
                else -> RuntimeException("Failed to create portfolio: ${error.message}", error)
            }
        }
}
```

**Database Insert**:
```sql
-- Insert new portfolio for SAML user
INSERT INTO portfolios (name, description, type, owner_id, status, created_at)
VALUES (?, ?, ?, ?, 'ACTIVE', NOW())
```

**Event Publishing**:
```kotlin
// PortfolioCreatedEvent.kt - Domain Event
data class PortfolioCreatedEvent(
    val portfolioId: Long,
    val name: String,
    val ownerId: Long,
    val organizationId: Long?
) : DomainEvent()
```

**Response**: `201 Created` with created portfolio details

## Key Classes and Their Responsibilities

### API Gateway Layer

1. **`JwtAuthenticationAdapter`** - JWT token validation and management
   - Validates JWT tokens with signature verification
   - Extracts user claims and authorities
   - Handles token expiration scenarios
   - Supports SAML session index tracking

2. **`AuthorizationServiceAdapter`** - Authorization service communication
   - Makes HTTP calls to authorization service
   - Handles authorization decisions for SAML users
   - Manages service communication errors
   - Supports SAML-specific authorization checks

3. **`AuthenticationService`** - Authentication business logic
   - Orchestrates JWT validation
   - Logs authentication events for SAML users
   - Manages token refresh scenarios
   - Handles SAML session management

### Authorization Service Layer

1. **`AuthorizationController`** - REST API endpoints
   - Provides authorization check endpoints
   - Handles SAML user permission queries
   - Manages role verification for SAML users

2. **`AuthorizationService`** - Authorization business logic
   - Makes authorization decisions for SAML users
   - Evaluates SAML user permissions
   - Handles role-based access control
   - Supports SAML NameID-based user lookup

3. **`UserRepository`** - User data access
   - Queries SAML user information
   - Checks SAML user active status
   - Retrieves SAML user permissions and roles
   - Supports SAML NameID-based user identification

### Technology Portfolio Service Layer

1. **`PortfolioController`** - REST API endpoints
   - Handles portfolio CRUD operations for SAML users
   - Manages technology operations
   - Provides reactive streaming endpoints
   - Supports SAML user context

2. **`PortfolioService`** - Business logic
   - Orchestrates portfolio operations for SAML users
   - Manages technology relationships
   - Publishes domain events with SAML context
   - Handles SAML user-specific business rules

3. **`PortfolioRepository`** - Data access
   - Performs database operations for SAML users
   - Handles reactive data queries
   - Manages portfolio persistence
   - Supports SAML user ownership filtering

## SAML User Role Scenarios

### 1. Portfolio Manager (user1)
```kotlin
// SAML User Configuration
val samlUser = SamlUser(
    nameId = "user1@company.com",
    username = "user1",
    email = "user1@company.com",
    groups = ["portfolio-managers"]
)

// Role Mapping
val roles = listOf("ROLE_PORTFOLIO_MANAGER")
val permissions = listOf("PORTFOLIO_READ", "PORTFOLIO_WRITE", "PORTFOLIO_CREATE")

// JWT Claims
val claims = mapOf(
    "sub" to "user1",
    "authorities" to roles,
    "sessionIndex" to "session-123",
    "samlNameId" to "user1@company.com"
)
```

### 2. Viewer (user2)
```kotlin
// SAML User Configuration
val samlUser = SamlUser(
    nameId = "user2@company.com",
    username = "user2",
    email = "user2@company.com",
    groups = ["viewers"]
)

// Role Mapping
val roles = listOf("ROLE_VIEWER")
val permissions = listOf("PORTFOLIO_READ")

// JWT Claims
val claims = mapOf(
    "sub" to "user2",
    "authorities" to roles,
    "sessionIndex" to "session-456",
    "samlNameId" to "user2@company.com"
)
```

### 3. Administrator (admin)
```kotlin
// SAML User Configuration
val samlUser = SamlUser(
    nameId = "admin@company.com",
    username = "admin",
    email = "admin@company.com",
    groups = ["admins"]
)

// Role Mapping
val roles = listOf("ROLE_ADMIN")
val permissions = listOf("PORTFOLIO_READ", "PORTFOLIO_WRITE", "PORTFOLIO_CREATE", "PORTFOLIO_DELETE", "USER_MANAGE")

// JWT Claims
val claims = mapOf(
    "sub" to "admin",
    "authorities" to roles,
    "sessionIndex" to "session-789",
    "samlNameId" to "admin@company.com"
)
```

## SAML Token Refresh (Optional)

**Classes Involved**:
- **JWT Adapter**: `JwtAuthenticationAdapter.refreshToken()`
- **Authentication Service**: `AuthenticationService.refreshToken()`

**Code Path**:
```kotlin
// JwtAuthenticationAdapter.kt - Token Refresh
override fun refreshToken(token: String): String? {
    return try {
        val claims = getClaimsFromToken(token)
        val username = claims.subject
        @Suppress("UNCHECKED_CAST")
        val authorities = claims["authorities"] as? List<String> ?: emptyList()
        val sessionIndex = claims["sessionIndex"] as? String

        generateToken(username, authorities, sessionIndex)
    } catch (e: Exception) {
        null
    }
}
```

**Refresh Flow**:
1. Gateway detects JWT is close to expiration
2. Gateway checks if user has valid SAML session
3. If SAML session valid, refresh JWT with same SAML claims
4. If SAML session expired, redirect to SAML login
5. Gateway updates response with new token
6. Browser stores updated token

## SAML Session Management

**Classes Involved**:
- **SAML Configuration**: `SamlConfig`
- **Session Management**: Spring Security SAML2 session handling

**Session Management Features**:
1. SAML sessions managed by SimpleSAMLphp IdP
2. JWT tokens have shorter expiration than SAML sessions
3. SAML logout invalidates both SAML session and JWT
4. Single Sign-Out (SLO) supported
5. Session timeout configurable in IdP

## Error Handling

### Authentication Errors
- **Expired JWT**: 401 Unauthorized + redirect to SAML login
- **Invalid JWT signature**: 401 Unauthorized + redirect to SAML login
- **SAML user not found in database**: 401 Unauthorized + redirect to SAML login
- **SAML session expired**: 401 Unauthorized + redirect to SAML login

### Authorization Errors
- **Insufficient permissions**: 403 Forbidden
- **Resource not found**: 404 Not Found
- **SAML IdP unavailable**: 503 Service Unavailable
- **SAML metadata changed**: 500 Internal Server Error

### Service Errors
- **Portfolio not found**: 404 Not Found
- **Validation errors**: 400 Bad Request
- **Database errors**: 500 Internal Server Error

## Security Considerations

1. **JWT Token Security**: HMAC-SHA512 signing with configurable secret
2. **SAML Security**: XML signature validation, timestamp validation, destination validation
3. **Authorization**: Role-based access control with fine-grained permissions
4. **Audit Logging**: Comprehensive logging of authentication and authorization events
5. **Session Management**: Secure session handling with SAML integration
6. **Token Refresh**: Secure token refresh with SAML session validation

## Configuration

### SAML Configuration (docker-compose.yml)
```yaml
services:
  simplesamlphp:
    image: kristophjunge/test-saml-idp
    ports:
      - "8081:8080"
    environment:
      - SIMPLESAMLPHP_SP_ENTITY_ID=http://localhost:8080
      - SIMPLESAMLPHP_SP_ASSERTION_CONSUMER_SERVICE=http://localhost:8080/saml/acs
      - SIMPLESAMLPHP_SP_SINGLE_LOGOUT_SERVICE=http://localhost:8080/saml/logout
      - SIMPLESAMLPHP_IDP_ENTITY_ID=http://localhost:8081/simplesaml/saml2/idp/metadata.php
```

### JWT Configuration (application.yml)
```yaml
jwt:
  secret: ${JWT_SECRET:default-secret-key-for-development-only}
  expiration: 3600
```

### Service URLs (application.yml)
```yaml
services:
  authorization:
    url: http://localhost:8082
  portfolio:
    url: http://localhost:8083
```

## Testing

### Unit Tests
- `JwtAuthenticationAdapterTest` - Tests JWT token validation for SAML users
- `AuthorizationServiceTest` - Tests authorization decisions for SAML users
- `PortfolioControllerTest` - Tests portfolio endpoints with SAML authentication
- `PortfolioServiceTest` - Tests portfolio business logic with SAML context

### Integration Tests
- `ReactiveIntegrationTest` - Tests service integration with SAML users
- `ReactiveEndToEndTest` - Tests complete SAML authenticated flow
- `ReactiveLoadTest` - Tests performance under load with SAML users

### SAML-Specific Tests
- SAML user authentication flow tests
- SAML token refresh tests
- SAML session management tests
- SAML user role mapping tests

## Monitoring and Observability

1. **Health Checks**: Each service provides health check endpoints
2. **Metrics**: Spring Boot Actuator metrics for SAML authentication monitoring
3. **Logging**: Structured logging with SAML correlation IDs
4. **Audit Trail**: Complete audit logging of SAML authentication and authorization events
5. **Distributed Tracing**: Request tracing across service boundaries with SAML context

## Performance Considerations

1. **JWT Token Caching**: Cache validated JWT tokens to reduce validation overhead
2. **SAML Session Caching**: Cache SAML session information for faster lookups
3. **Authorization Caching**: Cache authorization decisions for frequently accessed resources
4. **Database Optimization**: Optimize queries for SAML user data access
5. **Reactive Streams**: Use reactive programming for efficient resource handling

This walk-through provides a comprehensive understanding of how the SAML authenticated user flow is implemented in the actual codebase, from JWT token validation through to portfolio data access and creation. 