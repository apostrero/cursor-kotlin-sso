# Hexagonal Architecture Implementation

This document explains the implementation of Hexagonal Architecture (Ports and Adapters) across all microservices in the Technology Portfolio SSO application.

## Overview

Hexagonal Architecture, also known as Ports and Adapters, is implemented across all microservices to provide:
- **Separation of Concerns**: Clear boundaries between business logic and infrastructure
- **Testability**: Easy unit testing with mock adapters
- **Flexibility**: Easy to swap implementations (e.g., different databases, external services)
- **Maintainability**: Changes in one layer don't affect others
- **Consistency**: Uniform architecture across all services

## Microservices Architecture

The application consists of multiple microservices, each following hexagonal architecture:

1. **API Gateway** - Entry point with SSO authentication
2. **Authorization Service** - User authorization and role management
3. **Technology Portfolio Service** - Portfolio and technology management
4. **Shared Module** - Common domain models and interfaces

## Architecture Layers

### 1. Domain Layer (Core)
The innermost layer containing business logic and domain models.

#### Domain Models
Each service has its own domain models:
- **API Gateway**: AuthenticationResult, TokenValidationResult
- **Authorization Service**: AuthorizationRequest, AuthorizationResult, User
- **Technology Portfolio Service**: PortfolioRequest, TechnologyRequest, PortfolioResponse
- **Shared Module**: User, TechnologyPortfolio, Technology (shared across services)

#### Domain Services
- **API Gateway**: AuthenticationService
- **Authorization Service**: AuthorizationService
- **Technology Portfolio Service**: PortfolioService

#### Ports (Interfaces)
- **Repository Ports**: Data access contracts
- **Service Ports**: External service communication contracts
- **Event Ports**: Event publishing contracts

### 2. Application Layer (Use Cases)
Contains application-specific business logic and orchestrates domain services.

### 3. Adapters Layer (Infrastructure)
Implements the ports defined in the domain layer.

#### Inbound Adapters (Driving/Input)
- **REST Controllers**: HTTP endpoints for each service
- **Event Listeners**: Handle incoming domain events

#### Outbound Adapters (Driven/Output)
- **Database Adapters**: JPA repositories and entities
- **External Service Adapters**: Communication with other services
- **Event Publishers**: Publish domain events

## Complete Directory Structure

```
CursorKotlinSSO/
├── api-gateway/                          # API Gateway Service
│   ├── src/main/kotlin/com/company/techportfolio/gateway/
│   │   ├── domain/                       # Domain Layer
│   │   │   ├── model/                    # Domain Models
│   │   │   │   ├── AuthenticationResult.kt
│   │   │   │   └── TokenValidationResult.kt
│   │   │   ├── port/                     # Ports (Interfaces)
│   │   │   │   ├── AuthenticationPort.kt
│   │   │   │   ├── AuthorizationPort.kt
│   │   │   │   └── AuditPort.kt
│   │   │   └── service/                  # Domain Services
│   │   │       └── AuthenticationService.kt
│   │   ├── adapter/                      # Adapters Layer
│   │   │   ├── in/                       # Inbound Adapters
│   │   │   │   └── web/
│   │   │   │       └── AuthenticationController.kt
│   │   │   └── out/                      # Outbound Adapters
│   │   │       ├── jwt/
│   │   │       │   └── JwtAuthenticationAdapter.kt
│   │   │       ├── authorization/
│   │   │       │   └── AuthorizationServiceAdapter.kt
│   │   │       └── audit/
│   │   │           └── AuditServiceAdapter.kt
│   │   └── config/                       # Configuration
│   │       ├── SamlConfig.kt
│   │       ├── GatewayConfig.kt
│   │       └── ApplicationConfig.kt
│   └── src/main/resources/
│       ├── application.yml
│       └── saml/                         # SAML certificates
│
├── authorization-service/                 # Authorization Service
│   ├── src/main/kotlin/com/company/techportfolio/authorization/
│   │   ├── domain/                       # Domain Layer
│   │   │   ├── model/                    # Domain Models
│   │   │   │   ├── AuthorizationRequest.kt
│   │   │   │   └── AuthorizationResult.kt
│   │   │   ├── port/                     # Ports (Interfaces)
│   │   │   │   ├── UserRepository.kt
│   │   │   │   ├── RoleRepository.kt
│   │   │   │   └── PermissionRepository.kt
│   │   │   └── service/                  # Domain Services
│   │   │       └── AuthorizationService.kt
│   │   ├── adapter/                      # Adapters Layer
│   │   │   ├── in/                       # Inbound Adapters
│   │   │   │   └── web/
│   │   │   │       └── AuthorizationController.kt
│   │   │   └── out/                      # Outbound Adapters
│   │   │       ├── persistence/          # Database Adapters
│   │   │       │   ├── entity/
│   │   │       │   │   ├── UserEntity.kt
│   │   │       │   │   └── UserRoleEntity.kt
│   │   │       │   ├── repository/
│   │   │       │   │   ├── UserJpaRepository.kt
│   │   │       │   │   └── UserRoleJpaRepository.kt
│   │   │       │   └── UserRepositoryAdapter.kt
│   │   │       └── event/                # Event Adapters
│   │   │           └── EventPublisherAdapter.kt
│   │   └── AuthorizationServiceApplication.kt
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── db/migration/                 # Database migrations
│   │       ├── V1__Create_users_table.sql
│   │       └── V2__Create_user_roles_table.sql
│   └── src/test/kotlin/                  # Comprehensive tests
│       └── com/company/techportfolio/authorization/
│           └── domain/service/
│               └── AuthorizationServiceTest.kt
│
├── technology-portfolio-service/          # Technology Portfolio Service
│   ├── src/main/kotlin/com/company/techportfolio/portfolio/
│   │   ├── domain/                       # Domain Layer
│   │   │   ├── model/                    # Domain Models
│   │   │   │   ├── PortfolioRequest.kt
│   │   │   │   ├── TechnologyRequest.kt
│   │   │   │   ├── PortfolioResponse.kt
│   │   │   │   └── TechnologyResponse.kt
│   │   │   ├── port/                     # Ports (Interfaces)
│   │   │   │   ├── PortfolioRepository.kt
│   │   │   │   ├── TechnologyRepository.kt
│   │   │   │   ├── PortfolioQueryRepository.kt
│   │   │   │   └── TechnologyQueryRepository.kt
│   │   │   └── service/                  # Domain Services
│   │   │       └── PortfolioService.kt
│   │   ├── adapter/                      # Adapters Layer
│   │   │   ├── in/                       # Inbound Adapters
│   │   │   │   └── web/
│   │   │   │       └── PortfolioController.kt
│   │   │   └── out/                      # Outbound Adapters
│   │   │       ├── persistence/          # Database Adapters
│   │   │       │   ├── entity/
│   │   │       │   │   ├── PortfolioEntity.kt
│   │   │       │   │   └── TechnologyEntity.kt
│   │   │       │   ├── repository/
│   │   │       │   │   ├── PortfolioJpaRepository.kt
│   │   │       │   │   └── TechnologyJpaRepository.kt
│   │   │       │   └── PortfolioRepositoryAdapter.kt
│   │   │       └── event/                # Event Adapters
│   │   │           └── EventPublisherAdapter.kt
│   │   └── TechnologyPortfolioServiceApplication.kt
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── db/migration/                 # Database migrations
│   │       ├── V1__Create_portfolios_table.sql
│   │       └── V2__Create_technologies_table.sql
│   └── src/test/kotlin/                  # Comprehensive tests
│       └── com/company/techportfolio/portfolio/
│           ├── domain/service/
│           │   └── PortfolioServiceTest.kt
│           └── adapter/in/web/
│               └── PortfolioControllerTest.kt
│
├── shared/                               # Shared Module
│   ├── src/main/kotlin/com/company/techportfolio/shared/
│   │   ├── domain/                       # Shared Domain Layer
│   │   │   ├── model/                    # Shared Domain Models
│   │   │   │   ├── User.kt
│   │   │   │   ├── TechnologyPortfolio.kt
│   │   │   │   ├── Technology.kt
│   │   │   │   ├── TechnologyDependency.kt
│   │   │   │   ├── TechnologyAssessment.kt
│   │   │   │   ├── PortfolioAssessment.kt
│   │   │   │   ├── Organization.kt
│   │   │   │   ├── Role.kt
│   │   │   │   ├── Permission.kt
│   │   │   │   └── enums/                # Shared Enums (Individual Files)
│   │   │   │       ├── PortfolioType.kt
│   │   │   │       ├── PortfolioStatus.kt
│   │   │   │       ├── TechnologyType.kt
│   │   │   │       ├── MaturityLevel.kt
│   │   │   │       ├── RiskLevel.kt
│   │   │   │       ├── AssessmentType.kt
│   │   │   │       ├── AssessmentStatus.kt
│   │   │   │       ├── DependencyType.kt
│   │   │   │       └── DependencyStrength.kt
│   │   │   ├── port/                     # Shared Repository Ports & CQRS Base
│   │   │   │   ├── UserRepository.kt
│   │   │   │   ├── PortfolioRepository.kt
│   │   │   │   ├── EventPublisher.kt
│   │   │   │   ├── Command.kt            # Base Command class
│   │   │   │   ├── Query.kt              # Base Query class
│   │   │   │   ├── CommandResult.kt      # Command result wrapper
│   │   │   │   ├── CommandHandler.kt     # CQRS interfaces & buses
│   │   │   │   ├── UserCommands.kt       # User-related commands
│   │   │   │   ├── BasicPortfolioCommands.kt # Basic portfolio commands
│   │   │   │   └── BasicQueries.kt       # Basic queries
│   │   │   ├── event/                    # Domain Events (Individual Files)
│   │   │   │   ├── DomainEvent.kt        # Base event class
│   │   │   │   ├── AuthenticationEvents.kt
│   │   │   │   ├── AuthorizationEvents.kt
│   │   │   │   ├── AssessmentEvents.kt
│   │   │   │   ├── PortfolioLifecycleEvents.kt
│   │   │   │   ├── TechnologyEvents.kt
│   │   │   │   ├── PortfolioCostEvents.kt
│   │   │   │   ├── UserLifecycleEvents.kt
│   │   │   │   ├── UserSessionEvents.kt
│   │   │   │   ├── UserRoleEvents.kt
│   │   │   │   └── UserPasswordEvents.kt
│   │   │   └── cqrs/                     # CQRS Commands and Queries (Individual Files)
│   │   │       ├── PortfolioCommands.kt  # Advanced portfolio commands
│   │   │       ├── PortfolioQueries.kt   # Advanced portfolio queries
│   │   │       ├── PortfolioLifecycleCommandHandlers.kt
│   │   │       ├── TechnologyCommandHandlers.kt
│   │   │       └── BulkOperationCommandHandlers.kt
│   │   └── port/                         # Event Publishing
│   │       └── EventPublisher.kt
│   └── build.gradle.kts
│
├── build.gradle.kts                      # Root build configuration
├── settings.gradle.kts                   # Project settings
├── README.md                             # Project documentation
├── SSO.md                                # SSO Architecture Guide
├── HEXAGONAL_ARCHITECTURE.md             # This file
└── IMPLEMENTATION_GUIDE.md               # Implementation Guide
```

## Service-Specific Implementations

### API Gateway Service

**Purpose**: Entry point for all client requests with SSO authentication

**Key Components**:
- **Domain Models**: AuthenticationResult, TokenValidationResult
- **Domain Service**: AuthenticationService
- **Ports**: AuthenticationPort, AuthorizationPort, AuditPort
- **Inbound Adapters**: AuthenticationController (REST endpoints)
- **Outbound Adapters**: JWT adapter, Authorization service adapter, Audit service adapter

**Hexagonal Implementation**:
```kotlin
// Domain Service
@Service
class AuthenticationService(
    private val authenticationPort: AuthenticationPort,
    private val authorizationPort: AuthorizationPort,
    private val auditPort: AuditPort
) {
    fun authenticateUser(authentication: Authentication): AuthenticationResult {
        // Business logic implementation
    }
}

// Port Interface
interface AuthenticationPort {
    fun authenticateUser(authentication: Authentication): AuthenticationResult
    fun validateToken(token: String): TokenValidationResult
    fun generateToken(username: String, authorities: List<String>, sessionIndex: String?): String
}

// Outbound Adapter
@Component
class JwtAuthenticationAdapter : AuthenticationPort {
    override fun validateToken(token: String): TokenValidationResult {
        // JWT-specific implementation
    }
}
```

### Authorization Service

**Purpose**: User authorization and role-based access control

**Key Components**:
- **Domain Models**: AuthorizationRequest, AuthorizationResult, User
- **Domain Service**: AuthorizationService
- **Ports**: UserRepository, RoleRepository, PermissionRepository
- **Inbound Adapters**: AuthorizationController (REST endpoints)
- **Outbound Adapters**: Database adapters, Event publisher

**Hexagonal Implementation**:
```kotlin
// Domain Service
@Service
class AuthorizationService(
    private val userRepository: UserRepository,
    private val eventPublisher: EventPublisher
) {
    fun authorize(request: AuthorizationRequest): AuthorizationResult {
        // Business logic implementation
    }
}

// Port Interface
interface UserRepository {
    fun findById(id: Long): User?
    fun findByUsername(username: String): User?
    fun save(user: User): User
}

// Outbound Adapter
@Repository
class UserRepositoryAdapter(
    private val userJpaRepository: UserJpaRepository
) : UserRepository {
    override fun findById(id: Long): User? {
        return userJpaRepository.findById(id).orElse(null)?.toDomain()
    }
}
```

### Technology Portfolio Service

**Purpose**: Portfolio and technology management

**Key Components**:
- **Domain Models**: PortfolioRequest, TechnologyRequest, PortfolioResponse, TechnologyResponse
- **Domain Service**: PortfolioService
- **Ports**: PortfolioRepository, TechnologyRepository, PortfolioQueryRepository, TechnologyQueryRepository
- **Inbound Adapters**: PortfolioController (REST endpoints)
- **Outbound Adapters**: Database adapters, Event publisher

**Hexagonal Implementation**:
```kotlin
// Domain Service
@Service
class PortfolioService(
    private val portfolioRepository: PortfolioRepository,
    private val technologyRepository: TechnologyRepository,
    private val portfolioQueryRepository: PortfolioQueryRepository,
    private val technologyQueryRepository: TechnologyQueryRepository,
    private val eventPublisher: EventPublisher
) {
    fun createPortfolio(request: CreatePortfolioRequest): PortfolioResponse {
        // Business logic implementation
    }
}

// Port Interface
interface PortfolioRepository {
    fun findById(id: Long): TechnologyPortfolio?
    fun save(portfolio: TechnologyPortfolio): TechnologyPortfolio
    fun update(portfolio: TechnologyPortfolio): TechnologyPortfolio
    fun delete(id: Long): Boolean
}

// Outbound Adapter
@Repository
class PortfolioRepositoryAdapter(
    private val portfolioJpaRepository: PortfolioJpaRepository,
    private val technologyJpaRepository: TechnologyJpaRepository
) : PortfolioRepository, TechnologyRepository, PortfolioQueryRepository, TechnologyQueryRepository {
    override fun findById(id: Long): TechnologyPortfolio? {
        return portfolioJpaRepository.findById(id).orElse(null)?.toDomain()
    }
}
```

## Shared Module

**Purpose**: Common domain models, interfaces, and utilities shared across services

**Key Components**:
- **Domain Models**: User, TechnologyPortfolio, Technology
- **Enums**: PortfolioType, PortfolioStatus, TechnologyType, MaturityLevel, RiskLevel
- **Events**: DomainEvent, PortfolioEvents, UserEvents
- **CQRS**: Commands, Queries, CommandHandlers, QueryHandlers
- **Ports**: Repository interfaces, EventPublisher

## Benefits Across All Services

### 1. **Consistency**
- Uniform architecture across all microservices
- Consistent naming conventions and patterns
- Shared domain models and interfaces

### 2. **Testability**
- Each service can be tested independently
- Mock adapters for external dependencies
- Isolated business logic testing

### 3. **Flexibility**
- Easy to swap implementations (databases, external services)
- Independent deployment and scaling
- Technology-specific optimizations

### 4. **Maintainability**
- Clear separation of concerns
- Changes isolated to specific layers
- Easy to understand and modify

### 5. **Scalability**
- Each service can be optimized independently
- Easy to add new services following the same pattern
- Event-driven communication between services

## Testing Strategy

### Unit Tests
- **Domain Services**: Test business logic with mock adapters
- **Adapters**: Test adapter implementations
- **Ports**: Test interface contracts

### Integration Tests
- **Controllers**: Test REST endpoints
- **Repository Adapters**: Test database operations
- **Event Publishing**: Test event flow

### Test Examples

```kotlin
// Domain Service Test
@Test
fun `createPortfolio should create portfolio successfully`() {
    // Given
    val request = CreatePortfolioRequest(name = "Test Portfolio", ...)
    every { portfolioRepository.findByName(request.name) } returns null
    every { portfolioRepository.save(any()) } returns portfolio
    
    // When
    val result = portfolioService.createPortfolio(request)
    
    // Then
    assertNotNull(result)
    assertEquals(request.name, result.name)
    verify { eventPublisher.publish(any<PortfolioCreatedEvent>()) }
}

// Controller Test
@Test
@WithMockUser(roles = ["USER"])
fun `createPortfolio should return 201 when portfolio is created successfully`() {
    // Given
    val request = CreatePortfolioRequest(...)
    every { portfolioService.createPortfolio(any()) } returns response
    
    // When & Then
    mockMvc.perform(post("/api/v1/portfolios")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated)
}
```

## Event-Driven Communication

All services implement event-driven patterns:

```kotlin
// Domain Event
data class PortfolioCreatedEvent(
    override val id: String = UUID.randomUUID().toString(),
    override val timestamp: LocalDateTime = LocalDateTime.now(),
    override val eventType: String = "PortfolioCreated",
    val portfolioId: Long,
    val name: String,
    val ownerId: Long,
    val organizationId: Long?
) : DomainEvent

// Event Publishing
@Component
class EventPublisherAdapter : EventPublisher {
    override fun publish(event: DomainEvent) {
        // Publish to message broker, event store, etc.
    }
}
```

## CQRS Implementation

Commands and Queries are separated for optimal performance:

```kotlin
// Commands (Write Operations)
data class CreatePortfolioCommand(
    val name: String,
    val description: String?,
    val type: PortfolioType,
    val ownerId: Long,
    val organizationId: Long?
) : Command

// Queries (Read Operations)
data class GetPortfolioQuery(
    val portfolioId: Long
) : Query

// Command Handler
@Component
class CreatePortfolioCommandHandler(
    private val portfolioService: PortfolioService,
    private val eventPublisher: EventPublisher
) : CommandHandler<CreatePortfolioCommand> {
    override fun handle(command: CreatePortfolioCommand) {
        // Handle command and publish events
    }
}
```

This hexagonal architecture implementation provides a solid, scalable, and maintainable foundation for the Technology Portfolio SSO application across all microservices. 