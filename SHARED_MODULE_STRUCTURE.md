# Shared Module - Correct Architecture Structure

This document describes the corrected structure of the shared module, ensuring it only contains truly shared objects that are used across multiple microservices.

## Architecture Principle

The shared module follows the **Single Responsibility Principle** for shared components:
- ✅ **Include**: Objects used by multiple microservices
- ❌ **Exclude**: Objects specific to a single microservice

## Corrected File Structure

### Domain Models (`shared/src/main/kotlin/.../model/`)

**Truly Shared Domain Models** - Used across multiple services:

```
model/
├── User.kt                           # User domain model (used by authorization + api-gateway)
├── Organization.kt                   # Organization model (used by authorization + portfolio)
├── Role.kt                          # Role model (used by authorization + api-gateway)
├── Permission.kt                     # Permission model (used by authorization + api-gateway)
├── Technology.kt                     # Technology model (used by portfolio + shared events)
├── TechnologyPortfolio.kt            # Portfolio model (used by portfolio + shared events)
├── TechnologyDependency.kt           # Dependency model (used by portfolio + shared events)
├── TechnologyAssessment.kt           # Assessment model (used by portfolio + shared events)
├── PortfolioAssessment.kt            # Assessment model (used by portfolio + shared events)
├── AssessmentStatus.kt               # Shared assessment status enum
├── AssessmentType.kt                 # Shared assessment type enum
├── DependencyStrength.kt             # Shared dependency strength enum
├── DependencyType.kt                 # Shared dependency type enum
├── MaturityLevel.kt                  # Shared maturity level enum
├── PortfolioStatus.kt                # Shared portfolio status enum
├── PortfolioType.kt                  # Shared portfolio type enum
├── RiskLevel.kt                      # Shared risk level enum
└── TechnologyType.kt                 # Shared technology type enum
```

### Shared Ports (`shared/src/main/kotlin/.../domain/port/`)

**Only Truly Shared Interfaces** - Used across multiple services:

```
domain/port/
├── Command.kt                        # Base Command abstract class (CQRS)
├── Query.kt                          # Base Query abstract class (CQRS)
├── CommandResult.kt                  # Command result wrapper (CQRS)
├── CommandHandler.kt                 # CQRS interfaces and buses
├── EventPublisher.kt                 # Event publisher interface (shared infrastructure)
├── UserRepository.kt                 # User repository (used by authorization + api-gateway)
├── BasicPortfolioCommands.kt         # Basic portfolio commands (shared CQRS)
├── BasicQueries.kt                   # Basic queries (shared CQRS)
└── CreateUserCommand.kt              # User creation command (shared CQRS)
```

### Shared Events (`shared/src/main/kotlin/.../domain/event/`)

**Cross-Service Domain Events** - Events published by one service and consumed by others:

```
domain/event/
├── DomainEvent.kt                    # Base domain event class
├── AuthenticationEvents.kt           # Authentication events (api-gateway → others)
├── AuthorizationEvents.kt            # Authorization events (authorization → others)
├── AssessmentEvents.kt               # Assessment events (portfolio → others)
├── PortfolioLifecycleEvents.kt       # Portfolio events (portfolio → others)
├── TechnologyEvents.kt               # Technology events (portfolio → others)
├── PortfolioCostEvents.kt            # Cost events (portfolio → others)
├── UserLifecycleEvents.kt            # User events (authorization → others)
├── UserSessionEvents.kt              # Session events (api-gateway → others)
├── UserRoleEvents.kt                 # Role events (authorization → others)
└── UserPasswordEvents.kt             # Password events (authorization → others)
```

### Shared CQRS (`shared/src/main/kotlin/.../domain/cqrs/`)

**Cross-Service Commands and Handlers** - Complex operations that span multiple services:

```
domain/cqrs/
├── PortfolioCommands.kt              # Advanced portfolio commands
├── PortfolioQueries.kt               # Advanced portfolio queries
├── PortfolioLifecycleCommandHandlers.kt  # Portfolio lifecycle handlers
├── TechnologyCommandHandlers.kt      # Technology command handlers
└── BulkOperationCommandHandlers.kt   # Bulk operation handlers
```

## What Was Moved Out of Shared Module

### ❌ **Moved to Technology Portfolio Service**:
The following repository interfaces were incorrectly placed in shared and have been moved to `technology-portfolio-service/src/main/kotlin/.../domain/port/`:

- `TechnologyPortfolioRepository.kt` - Only used by portfolio service
- `PortfolioAssessmentRepository.kt` - Assessment operations specific to portfolio service
- `TechnologyAssessmentRepository.kt` - Assessment operations specific to portfolio service  
- `TechnologyDependencyRepository.kt` - Dependency operations specific to portfolio service
- `TechnologyRepository.kt` - Duplicate removed (already exists in portfolio service)

### ✅ **Correctly Remains in Shared Module**:

- `UserRepository.kt` - Used by both authorization-service and api-gateway
- `EventPublisher.kt` - Infrastructure interface used by all services
- CQRS base classes - Used by all services implementing CQRS pattern

## Service-Specific Repository Interfaces

### Authorization Service
```
authorization-service/src/main/kotlin/.../domain/port/
├── UserRepository.kt                 # Authorization-specific user operations
├── RoleRepository.kt                 # Role management operations
└── PermissionRepository.kt           # Permission management operations
```

### Technology Portfolio Service
```
technology-portfolio-service/src/main/kotlin/.../domain/port/
├── PortfolioRepository.kt            # Portfolio CRUD operations
├── PortfolioQueryRepository.kt       # Portfolio query operations
├── TechnologyRepository.kt           # Technology CRUD operations
├── TechnologyQueryRepository.kt      # Technology query operations
├── TechnologyPortfolioRepository.kt  # Portfolio-specific operations
├── PortfolioAssessmentRepository.kt  # Portfolio assessment operations
├── TechnologyAssessmentRepository.kt # Technology assessment operations
└── TechnologyDependencyRepository.kt # Technology dependency operations
```

## Benefits of Correct Architecture

### 1. **True Separation of Concerns**
- Shared module contains only cross-service dependencies
- Each service owns its specific repository interfaces
- Clear boundaries between shared and service-specific code

### 2. **Reduced Coupling**
- Services don't depend on interfaces they don't use
- Changes to service-specific interfaces don't affect other services
- Shared module has minimal, stable API surface

### 3. **Better Maintainability**
- Service teams can evolve their repository interfaces independently
- Shared module changes are rare and well-considered
- Clear ownership of interfaces and implementations

### 4. **Improved Testability**
- Services can mock only the interfaces they actually use
- Shared interfaces have clear, stable contracts
- Less complex dependency graphs in tests

## Architecture Validation Rules

### ✅ **Should Be in Shared Module**:
1. **Domain Models** used by 2+ services
2. **Events** published by one service and consumed by others
3. **Base Classes** for CQRS pattern (Command, Query, etc.)
4. **Infrastructure Interfaces** used by all services (EventPublisher)
5. **Cross-Service Repository Interfaces** (UserRepository used by auth + gateway)

### ❌ **Should NOT Be in Shared Module**:
1. **Service-Specific Repository Interfaces** (PortfolioRepository only used by portfolio service)
2. **Service-Specific Commands/Queries** that don't cross service boundaries
3. **Implementation Classes** (adapters, entities, etc.)
4. **Service-Specific Configuration**
5. **Service-Specific Utilities**

## Migration Impact

### Before (Incorrect)
```kotlin
// Portfolio service importing from shared module
import com.company.techportfolio.shared.domain.port.TechnologyPortfolioRepository
```

### After (Correct)  
```kotlin
// Portfolio service using its own interfaces
import com.company.techportfolio.portfolio.domain.port.TechnologyPortfolioRepository
```

### No Impact Areas
```kotlin
// These imports remain unchanged - truly shared
import com.company.techportfolio.shared.model.User
import com.company.techportfolio.shared.domain.event.UserCreatedEvent
import com.company.techportfolio.shared.domain.port.EventPublisher
```

## Future Considerations

### 1. **Shared Module Stability**
The shared module should have a very stable API. Changes should be:
- Backward compatible when possible
- Well-communicated across teams
- Thoroughly tested across all consuming services

### 2. **Service Evolution**
Services can now evolve their repository interfaces independently:
- Add service-specific methods without affecting others
- Optimize for service-specific use cases
- Implement service-specific performance optimizations

### 3. **Cross-Service Communication**
For cross-service data access:
- Use domain events for eventual consistency
- Use REST APIs for synchronous queries
- Avoid direct database access across service boundaries

## Conclusion

The corrected shared module structure ensures:
- **Clear Ownership**: Each service owns its specific interfaces
- **Minimal Coupling**: Shared module contains only truly shared objects
- **Better Maintainability**: Services can evolve independently
- **Architectural Integrity**: Proper separation of concerns maintained

This structure follows microservices best practices and makes the system more maintainable and scalable. 