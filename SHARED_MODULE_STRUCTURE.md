# Shared Module - Separated File Structure

This document describes the new separated file structure of the shared module, where each class, interface, and enum has been moved to its own individual file for better organization and maintainability.

## Overview

The shared module has been refactored to follow the single responsibility principle, with each file containing only one class, interface, or enum. This improves:

- **Maintainability**: Easier to find and modify specific components
- **Readability**: Clear file names indicate the purpose of each component
- **Version Control**: Better tracking of changes to individual components
- **Reusability**: Individual components can be imported without pulling in unnecessary dependencies

## File Structure

### Domain Models (`domain/model/`)

All domain models are already separated into individual files:

```
domain/model/
├── User.kt                           # User domain model
├── TechnologyPortfolio.kt            # Technology portfolio domain model
├── Technology.kt                     # Technology domain model
├── TechnologyDependency.kt           # Technology dependency domain model
├── TechnologyAssessment.kt           # Technology assessment domain model
├── PortfolioAssessment.kt            # Portfolio assessment domain model
├── Organization.kt                   # Organization domain model
├── Role.kt                          # Role domain model
├── Permission.kt                     # Permission domain model
├── AssessmentStatus.kt               # Assessment status enum
├── AssessmentType.kt                 # Assessment type enum
├── DependencyStrength.kt             # Dependency strength enum
├── DependencyType.kt                 # Dependency type enum
├── MaturityLevel.kt                  # Maturity level enum
├── PortfolioStatus.kt                # Portfolio status enum
├── PortfolioType.kt                  # Portfolio type enum
├── RiskLevel.kt                      # Risk level enum
└── TechnologyType.kt                 # Technology type enum
```

### Ports (`domain/port/`)

Base classes and interfaces for CQRS pattern:

```
domain/port/
├── Command.kt                        # Base Command abstract class
├── Query.kt                          # Base Query abstract class
├── CommandResult.kt                  # Command result wrapper
├── CommandHandler.kt                 # CQRS interfaces and buses
├── UserCommands.kt                   # User-related commands
├── BasicPortfolioCommands.kt         # Basic portfolio commands
├── BasicQueries.kt                   # Basic queries
├── UserRepository.kt                 # User repository interface
├── PortfolioRepository.kt            # Portfolio repository interface
└── EventPublisher.kt                 # Event publisher interface
```

### Domain Events (`domain/event/`)

Events are organized by domain area:

```
domain/event/
├── DomainEvent.kt                    # Base domain event class
├── AuthenticationEvents.kt           # Authentication-related events
├── AuthorizationEvents.kt            # Authorization-related events
├── AssessmentEvents.kt               # Assessment-related events
├── PortfolioLifecycleEvents.kt       # Portfolio lifecycle events
├── TechnologyEvents.kt               # Technology-related events
├── PortfolioCostEvents.kt            # Portfolio cost events
├── UserLifecycleEvents.kt            # User lifecycle events
├── UserSessionEvents.kt              # User session events
├── UserRoleEvents.kt                 # User role events
└── UserPasswordEvents.kt             # User password events
```

### CQRS (`domain/cqrs/`)

Commands, queries, and handlers organized by functionality:

```
domain/cqrs/
├── PortfolioCommands.kt              # Advanced portfolio commands
├── PortfolioQueries.kt               # Advanced portfolio queries
├── PortfolioLifecycleCommandHandlers.kt  # Portfolio lifecycle handlers
├── TechnologyCommandHandlers.kt      # Technology command handlers
└── BulkOperationCommandHandlers.kt   # Bulk operation handlers
```

## Benefits of Separation

### 1. **Single Responsibility Principle**
Each file has a single, well-defined purpose:
- `User.kt` - Only contains the User domain model
- `PortfolioCreatedEvent.kt` - Only contains the portfolio created event
- `CreatePortfolioCommandHandler.kt` - Only handles portfolio creation

### 2. **Improved Navigation**
Developers can quickly find specific components:
- Looking for user-related events? Check `UserLifecycleEvents.kt`
- Need to modify portfolio commands? Check `PortfolioCommands.kt`
- Want to see all authentication events? Check `AuthenticationEvents.kt`

### 3. **Better Version Control**
- Changes to individual components are clearly tracked
- Merge conflicts are reduced when multiple developers work on different components
- Git history shows exactly which components were modified

### 4. **Selective Imports**
Services can import only the components they need:
```kotlin
// Only import what's needed
import com.company.techportfolio.shared.domain.model.User
import com.company.techportfolio.shared.domain.event.UserCreatedEvent
import com.company.techportfolio.shared.domain.port.UserRepository
```

### 5. **Easier Testing**
- Individual components can be tested in isolation
- Mock implementations are easier to create
- Test files can be organized to match the source structure

## Migration Guide

### Before (Monolithic Files)
```kotlin
// Command.kt - Multiple classes in one file
abstract class Command(...)
abstract class Query(...)
data class CreateUserCommand(...)
data class UpdateUserCommand(...)
// ... many more classes
```

### After (Separated Files)
```kotlin
// Command.kt - Only base class
abstract class Command(...)

// Query.kt - Only base class  
abstract class Query(...)

// UserCommands.kt - Only user commands
data class CreateUserCommand(...)
data class UpdateUserCommand(...)
```

### Import Updates
When using these components, imports remain the same:
```kotlin
import com.company.techportfolio.shared.domain.port.Command
import com.company.techportfolio.shared.domain.event.UserCreatedEvent
import com.company.techportfolio.shared.domain.model.User
```

## Best Practices

### 1. **File Naming**
- Use descriptive names that clearly indicate the purpose
- Follow Kotlin naming conventions
- Group related components with consistent prefixes

### 2. **Documentation**
- Each file should have a clear purpose documented in comments
- Use KDoc comments for public APIs
- Include usage examples where appropriate

### 3. **Organization**
- Keep related components in the same directory
- Use subdirectories for complex domains
- Maintain consistent structure across similar components

### 4. **Dependencies**
- Minimize dependencies between files
- Use interfaces to decouple implementations
- Avoid circular dependencies

## Future Considerations

### 1. **Package Organization**
Consider organizing by feature rather than layer:
```
domain/
├── user/
│   ├── model/
│   ├── events/
│   └── commands/
├── portfolio/
│   ├── model/
│   ├── events/
│   └── commands/
└── technology/
    ├── model/
    ├── events/
    └── commands/
```

### 2. **Module Splitting**
For very large shared modules, consider splitting into multiple modules:
- `shared-core` - Basic domain models and interfaces
- `shared-events` - Domain events
- `shared-cqrs` - Commands, queries, and handlers
- `shared-repositories` - Repository interfaces

### 3. **Code Generation**
Consider using code generation tools for:
- Boilerplate code (getters, setters, builders)
- Event classes from domain models
- Repository interfaces from domain models

## Conclusion

The separated file structure provides significant benefits in terms of maintainability, readability, and developer productivity. While it may result in more files, the improved organization and clarity make it easier to work with the codebase as it grows in complexity.

This structure follows industry best practices and makes the shared module more scalable and maintainable for future development. 