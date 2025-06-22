# SAML Non-Authenticated User Flow - Code Walk-Through

This document provides a detailed walk-through of the actual classes and functions involved in the SAML non-authenticated user flow, tracing the execution path from initial request to final response.

## Overview

The SAML non-authenticated user flow handles users who access protected resources without valid JWT tokens, redirecting them through SAML authentication and ultimately granting access to the requested resource.

## Flow Sequence

### 1. Initial Request to Protected Resource

**User Action**: Navigate to protected resource (e.g., `/api/portfolios`)

**Browser → API Gateway**: `GET /api/portfolios`

**Classes Involved**:
- **Entry Point**: `PortfolioController.getAllPortfolios()` (Technology Portfolio Service)
- **Gateway Filter**: Spring Security filter chain configured in `SamlConfig.securityFilterChain()`

**Code Path**:
```kotlin
// SamlConfig.kt - Security Filter Chain Configuration
@Bean
fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
    http
        .authorizeHttpRequests { authorize ->
            authorize
                .requestMatchers("/actuator/**", "/health", "/metrics", "/api/auth/health").permitAll()
                .anyRequest().authenticated() // ← This triggers authentication check
        }
        .saml2Login { saml2 ->
            saml2
                .loginPage("/saml/login")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/saml/login?error=true")
        }
        // ... other configuration
}
```

**Authentication Check**: Spring Security detects no JWT token in request headers or cookies

**Response**: `302 Redirect to /saml/login`

### 2. SAML Authentication Flow

**Browser → API Gateway**: `GET /saml/login`

**Classes Involved**:
- **Controller**: Spring Security SAML2 login endpoint (auto-generated)
- **Configuration**: `SamlConfig.saml2Login()` configuration

**Code Path**:
```kotlin
// SamlConfig.kt - SAML Login Configuration
.saml2Login { saml2 ->
    saml2
        .loginPage("/saml/login") // ← Custom login page endpoint
        .defaultSuccessUrl("/dashboard", true)
        .failureUrl("/saml/login?error=true")
}
```

**SAML AuthnRequest Generation**: Spring Security SAML2 generates authentication request

**Response**: `302 Redirect to SimpleSAMLphp IdP` with SAML request parameters

### 3. SAML Identity Provider Processing

**Browser → SimpleSAMLphp IdP**: `GET /simplesaml/saml2/idp/SSOService.php`

**Classes Involved**:
- **External Service**: SimpleSAMLphp IdP (Docker container)
- **Configuration**: Defined in `docker-compose.yml`

**Code Path**:
```yaml
# docker-compose.yml - SimpleSAMLphp Configuration
services:
  simplesamlphp:
    image: kristophjunge/test-saml-idp
    ports:
      - "8081:8080"
    environment:
      - SIMPLESAMLPHP_SP_ENTITY_ID=http://localhost:8080
      - SIMPLESAMLPHP_SP_ASSERTION_CONSUMER_SERVICE=http://localhost:8080/saml/acs
      - SIMPLESAMLPHP_SP_SINGLE_LOGOUT_SERVICE=http://localhost:8080/saml/logout
```

**IdP Processing**:
1. Decode and validate SAML AuthnRequest
2. Display login form to user
3. User enters credentials (e.g., `user1/password`)
4. Validate credentials against configured users
5. Generate SAML Response with user attributes

**Response**: `302 Redirect with SAML Response` to `/saml/acs`

### 4. SAML Response Processing

**Browser → API Gateway**: `POST /saml/acs`

**Classes Involved**:
- **Controller**: Spring Security SAML2 Assertion Consumer Service (auto-generated)
- **Authentication**: `AuthenticationService.authenticateUser()`
- **JWT Generation**: `JwtAuthenticationAdapter.generateToken()`

**Code Path**:
```kotlin
// AuthenticationService.kt - SAML Authentication Processing
fun authenticateUser(authentication: Authentication): AuthenticationResult {
    return try {
        val samlAuth = authentication as Saml2Authentication
        val username = samlAuth.principal as String
        val authorities = samlAuth.authorities.map { it.authority }
        val sessionIndex = samlAuth.credentials?.toString()

        // Generate JWT token
        val token = authenticationPort.generateToken(username, authorities, sessionIndex)

        // Log successful authentication
        auditPort.logAuthenticationEvent(
            AuthenticationEvent(
                username = username,
                eventType = AuthenticationEventType.LOGIN_SUCCESS,
                sessionIndex = sessionIndex,
                success = true
            )
        )

        // Log token generation
        auditPort.logTokenEvent(
            TokenEvent(
                username = username,
                eventType = TokenEventType.TOKEN_GENERATED,
                sessionIndex = sessionIndex
            )
        )

        AuthenticationResult(
            isAuthenticated = true,
            username = username,
            authorities = authorities,
            token = token,
            sessionIndex = sessionIndex,
            expiresAt = LocalDateTime.now().plusSeconds(jwtExpiration)
        )
    } catch (e: Exception) {
        // Error handling...
    }
}
```

**JWT Token Generation**:
```kotlin
// JwtAuthenticationAdapter.kt - JWT Token Generation
override fun generateToken(username: String, authorities: List<String>, sessionIndex: String?): String {
    val now = Instant.now()
    val expiration = now.plusSeconds(jwtExpiration)
    
    val claims = mutableMapOf<String, Any>(
        "sub" to username,
        "iat" to now,
        "exp" to expiration,
        "authorities" to authorities
    )
    
    if (sessionIndex != null) {
        claims["sessionIndex"] = sessionIndex
    }
    
    return Jwts.builder()
        .setClaims(claims)
        .signWith(signingKey, SignatureAlgorithm.HS512)
        .compact()
}
```

**Response**: `302 Redirect to original resource` with JWT cookie set

### 5. Authenticated Request with SAML JWT

**User → Browser**: Access protected resource again

**Browser → API Gateway**: `GET /api/portfolios` with JWT token

**Classes Involved**:
- **JWT Validation**: `JwtAuthenticationAdapter.validateToken()`
- **Authorization**: `AuthorizationServiceAdapter.authorizeUser()`

**Code Path**:
```kotlin
// JwtAuthenticationAdapter.kt - JWT Token Validation
override fun validateToken(token: String): TokenValidationResult {
    return try {
        val claims = Jwts.parserBuilder()
            .setSigningKey(signingKey)
            .build()
            .parseClaimsJws(token)
            .body

        val username = claims["sub"] as String
        @Suppress("UNCHECKED_CAST")
        val authorities = claims["authorities"] as List<String>
        val sessionIndex = claims["sessionIndex"] as String?

        TokenValidationResult(
            isValid = true,
            username = username,
            authorities = authorities,
            sessionIndex = sessionIndex,
            expiresAt = LocalDateTime.ofInstant(
                Instant.ofEpochSecond(claims["exp"] as Long),
                ZoneOffset.UTC
            )
        )
    } catch (e: Exception) {
        TokenValidationResult(
            isValid = false,
            errorMessage = "Token validation failed: ${e.message}"
        )
    }
}
```

### 6. Authorization Check

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

**Response**: `200 OK` with authorization decision

### 7. Service Communication

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
```kotlin
// PortfolioRepository.kt - Data Access
fun findAll(): Flux<TechnologyPortfolio> {
    return databaseClient.sql("SELECT * FROM portfolios WHERE is_active = true ORDER BY created_at DESC")
        .map { row ->
            TechnologyPortfolio(
                id = row.get("id", Long::class.java),
                name = row.get("name", String::class.java),
                description = row.get("description", String::class.java),
                type = PortfolioType.valueOf(row.get("type", String::class.java)),
                status = PortfolioStatus.valueOf(row.get("status", String::class.java)),
                isActive = row.get("is_active", Boolean::class.java),
                createdAt = row.get("created_at", LocalDateTime::class.java),
                updatedAt = row.get("updated_at", LocalDateTime::class.java),
                ownerId = row.get("owner_id", Long::class.java),
                organizationId = row.get("organization_id", Long::class.java)
            )
        }
        .all()
}
```

**Response**: `200 OK` with portfolio data

## Key Classes and Their Responsibilities

### API Gateway Layer

1. **`SamlConfig`** - SAML2 authentication configuration
   - Configures Spring Security filter chain
   - Sets up SAML login/logout endpoints
   - Defines authorization rules

2. **`AuthenticationService`** - Core authentication business logic
   - Processes SAML authentication responses
   - Generates JWT tokens
   - Logs authentication events

3. **`JwtAuthenticationAdapter`** - JWT token management
   - Generates JWT tokens with SAML claims
   - Validates JWT tokens
   - Extracts user information from tokens

4. **`AuthorizationServiceAdapter`** - Authorization service communication
   - Makes HTTP calls to authorization service
   - Handles authorization decisions
   - Manages service communication errors

### Authorization Service Layer

1. **`AuthorizationController`** - REST API endpoints
   - Provides authorization check endpoints
   - Handles permission queries
   - Manages role verification

2. **`AuthorizationService`** - Authorization business logic
   - Makes authorization decisions
   - Evaluates user permissions
   - Handles role-based access control

3. **`UserRepository`** - User data access
   - Queries user information
   - Checks user active status
   - Retrieves user permissions and roles

### Technology Portfolio Service Layer

1. **`PortfolioController`** - REST API endpoints
   - Handles portfolio CRUD operations
   - Manages technology operations
   - Provides reactive streaming endpoints

2. **`PortfolioService`** - Business logic
   - Orchestrates portfolio operations
   - Manages technology relationships
   - Publishes domain events

3. **`PortfolioRepository`** - Data access
   - Performs database operations
   - Handles reactive data queries
   - Manages portfolio persistence

## Error Handling

### Authentication Errors
- **Invalid SAML credentials**: 401 Unauthorized
- **SAML Response signature invalid**: 401 Unauthorized
- **SAML Response expired**: 401 Unauthorized
- **SAML IdP not available**: 503 Service Unavailable

### Authorization Errors
- **User not found**: 401 Unauthorized
- **Insufficient permissions**: 403 Forbidden
- **Authorization service unavailable**: 503 Service Unavailable

### Service Errors
- **Portfolio not found**: 404 Not Found
- **Validation errors**: 400 Bad Request
- **Database errors**: 500 Internal Server Error

## Security Considerations

1. **JWT Token Security**: HMAC-SHA512 signing with configurable secret
2. **SAML Security**: XML signature validation, timestamp validation, destination validation
3. **Authorization**: Role-based access control with fine-grained permissions
4. **Audit Logging**: Comprehensive logging of authentication and authorization events
5. **Error Handling**: Secure error responses that don't leak sensitive information

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
- `AuthenticationServiceTest` - Tests SAML authentication processing
- `JwtAuthenticationAdapterTest` - Tests JWT token generation and validation
- `AuthorizationServiceTest` - Tests authorization decisions
- `PortfolioControllerTest` - Tests portfolio endpoints

### Integration Tests
- `ReactiveIntegrationTest` - Tests service integration
- `ReactiveEndToEndTest` - Tests complete flow
- `ReactiveLoadTest` - Tests performance under load

## Monitoring and Observability

1. **Health Checks**: Each service provides health check endpoints
2. **Metrics**: Spring Boot Actuator metrics for monitoring
3. **Logging**: Structured logging with correlation IDs
4. **Audit Trail**: Complete audit logging of authentication and authorization events
5. **Distributed Tracing**: Request tracing across service boundaries

This walk-through provides a comprehensive understanding of how the SAML non-authenticated user flow is implemented in the actual codebase, from the initial request through to the final response. 