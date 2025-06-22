# Shared Module

The shared module contains components that are used across multiple microservices in the Technology Portfolio SSO system.

## Overview

The shared module follows the **Single Responsibility Principle** for shared components:
- ✅ **Include**: Objects used by multiple microservices
- ❌ **Exclude**: Objects specific to a single microservice

## Components

### Domain Models
- **User**: User domain model used by authorization service and API gateway
- **Organization**: Organization model used by authorization and portfolio services
- **Role**: Role model used by authorization service and API gateway
- **Permission**: Permission model used by authorization service and API gateway
- **Technology**: Technology model used by portfolio service and shared events
- **TechnologyPortfolio**: Portfolio model used by portfolio service and shared events
- **TechnologyDependency**: Dependency model used by portfolio service and shared events
- **TechnologyAssessment**: Assessment model used by portfolio service and shared events
- **PortfolioAssessment**: Assessment model used by portfolio service and shared events

### Shared Enums
- **AssessmentStatus**: Assessment status enumeration
- **AssessmentType**: Assessment type enumeration
- **DependencyStrength**: Dependency strength enumeration
- **DependencyType**: Dependency type enumeration
- **MaturityLevel**: Technology maturity level enumeration
- **PortfolioStatus**: Portfolio status enumeration
- **PortfolioType**: Portfolio type enumeration
- **RiskLevel**: Risk level enumeration
- **TechnologyType**: Technology type enumeration

### Shared Ports
- **Command**: Base Command abstract class for CQRS pattern
- **Query**: Base Query abstract class for CQRS pattern
- **CommandResult**: Command result wrapper for CQRS pattern
- **CommandHandler**: CQRS interfaces and buses
- **EventPublisher**: Event publisher interface for shared infrastructure
- **UserRepository**: User repository interface used by authorization service and API gateway
- **BasicPortfolioCommands**: Basic portfolio commands for shared CQRS
- **BasicQueries**: Basic queries for shared CQRS
- **CreateUserCommand**: User creation command for shared CQRS

### Shared Events
- **DomainEvent**: Base domain event class
- **AuthenticationEvents**: Authentication events (API gateway → others)
- **AuthorizationEvents**: Authorization events (authorization → others)
- **AssessmentEvents**: Assessment events (portfolio → others)
- **PortfolioLifecycleEvents**: Portfolio events (portfolio → others)
- **TechnologyEvents**: Technology events (portfolio → others)
- **PortfolioCostEvents**: Cost events (portfolio → others)
- **UserLifecycleEvents**: User events (authorization → others)
- **UserSessionEvents**: Session events (API gateway → others)
- **UserRoleEvents**: Role events (authorization → others)
- **UserPasswordEvents**: Password events (authorization → others)

### Shared CQRS
- **PortfolioCommands**: Advanced portfolio commands
- **PortfolioQueries**: Advanced portfolio queries
- **PortfolioLifecycleCommandHandlers**: Portfolio lifecycle handlers
- **TechnologyCommandHandlers**: Technology command handlers
- **BulkOperationCommandHandlers**: Bulk operation handlers

## Architecture

For detailed information about the shared module structure, see:
- [Shared Module Structure](../../architecture/shared-module-structure.md)

## Usage

### Including in Services

Add the shared module as a dependency in your service's `build.gradle.kts`:

```kotlin
dependencies {
    implementation(project(":shared"))
}
```

### Importing Components

```kotlin
// Import shared models
import com.company.techportfolio.shared.model.User
import com.company.techportfolio.shared.model.Technology

// Import shared events
import com.company.techportfolio.shared.domain.event.UserCreatedEvent

// Import shared ports
import com.company.techportfolio.shared.domain.port.EventPublisher
import com.company.techportfolio.shared.domain.port.Command
```

## Guidelines

### What Should Be in Shared Module
1. **Domain Models** used by 2+ services
2. **Events** published by one service and consumed by others
3. **Base Classes** for CQRS pattern (Command, Query, etc.)
4. **Infrastructure Interfaces** used by all services (EventPublisher)
5. **Cross-Service Repository Interfaces** (UserRepository used by auth + gateway)
6. **Shared Enums** used across multiple services

### What Should NOT Be in Shared Module
1. **Service-Specific Repository Interfaces** (PortfolioRepository only used by portfolio service)
2. **Service-Specific Commands/Queries** that don't cross service boundaries
3. **Implementation Classes** (adapters, entities, etc.)
4. **Service-Specific Configuration**
5. **Service-Specific Utilities**

## Maintenance

- Keep the shared module API stable
- Changes should be backward compatible when possible
- Test changes across all consuming services
- Document breaking changes clearly 