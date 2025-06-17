# Technology Portfolio Service

The Technology Portfolio Service is the core business service responsible for managing technology portfolios, technologies, assessments, and dependencies. It follows hexagonal architecture principles and provides comprehensive portfolio management capabilities.

## Architecture Overview

The Technology Portfolio Service provides:

- **Portfolio Management**: Create, update, and manage technology portfolios
- **Technology Management**: Manage individual technologies within portfolios
- **Assessment Management**: Handle portfolio and technology assessments
- **Dependency Management**: Manage technology dependencies and relationships
- **Event-Driven Communication**: Publish domain events for other services
- **CQRS Implementation**: Separate read and write operations for better performance

## Technology Stack

- **Framework**: Spring Boot 3.4
- **Language**: Kotlin
- **Build Tool**: Gradle
- **Database**: PostgreSQL with Flyway migrations
- **ORM**: Spring Data JPA
- **Event Publishing**: Spring Events
- **CQRS**: Custom command and query handlers
- **API Documentation**: OpenAPI 3.0

## Project Structure

```
technology-portfolio-service/
├── src/main/kotlin/com/company/techportfolio/portfolio/
│   ├── adapter/
│   │   ├── in/
│   │   │   └── web/
│   │   │       └── PortfolioController.kt
│   │   └── out/
│   │       ├── persistence/
│   │       │   ├── entity/
│   │       │   │   ├── TechnologyPortfolioEntity.kt
│   │       │   │   ├── TechnologyEntity.kt
│   │       │   │   ├── PortfolioAssessmentEntity.kt
│   │       │   │   ├── TechnologyAssessmentEntity.kt
│   │       │   │   └── TechnologyDependencyEntity.kt
│   │       │   ├── repository/
│   │       │   │   ├── TechnologyPortfolioRepository.kt
│   │       │   │   ├── TechnologyRepository.kt
│   │       │   │   ├── PortfolioAssessmentRepository.kt
│   │       │   │   ├── TechnologyAssessmentRepository.kt
│   │       │   │   └── TechnologyDependencyRepository.kt
│   │       │   └── adapter/
│   │       │       ├── TechnologyPortfolioPersistenceAdapter.kt
│   │       │       ├── TechnologyPersistenceAdapter.kt
│   │       │       └── AssessmentPersistenceAdapter.kt
│   │       └── event/
│   │           └── PortfolioEventPublisher.kt
│   ├── domain/
│   │   ├── model/
│   │   │   ├── PortfolioRequest.kt
│   │   │   ├── PortfolioResponse.kt
│   │   │   └── TechnologyRequest.kt
│   │   ├── port/
│   │   │   ├── PortfolioRepository.kt
│   │   │   ├── TechnologyRepository.kt
│   │   │   ├── AssessmentRepository.kt
│   │   │   └── EventPublisher.kt
│   │   └── service/
│   │       └── PortfolioService.kt
│   └── TechnologyPortfolioServiceApplication.kt
├── src/main/resources/
│   ├── application.yml
│   └── db/migration/
│       ├── V1__Create_portfolios_table.sql
│       ├── V2__Create_technologies_table.sql
│       ├── V3__Create_portfolio_assessments_table.sql
│       ├── V4__Create_technology_assessments_table.sql
│       └── V5__Create_technology_dependencies_table.sql
└── src/test/kotlin/
    └── com/company/techportfolio/portfolio/
        ├── adapter/in/web/
        │   └── PortfolioControllerTest.kt
        └── domain/service/
            └── PortfolioServiceTest.kt
```

## Setup and Configuration

### Prerequisites

- Java 21 or higher
- Gradle 8.0 or higher
- PostgreSQL 14 or higher

### Environment Variables

Create a `.env` file in the project root:

```bash
# Server Configuration
SERVER_PORT=8082
SERVER_SERVLET_CONTEXT_PATH=/portfolio

# Database Configuration
DB_HOST=localhost
DB_PORT=5432
DB_NAME=portfolio_db
DB_USERNAME=postgres
DB_PASSWORD=password

# JPA Configuration
JPA_HIBERNATE_DDL_AUTO=validate
JPA_SHOW_SQL=false

# Event Configuration
EVENT_PUBLISHING_ENABLED=true

# Logging Configuration
LOGGING_LEVEL=INFO
```

### Configuration Files

#### application.yml

```yaml
server:
  port: ${SERVER_PORT:8082}
  servlet:
    context-path: ${SERVER_SERVLET_CONTEXT_PATH:/portfolio}

spring:
  application:
    name: technology-portfolio-service
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:portfolio_db}
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:password}
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: ${JPA_HIBERNATE_DDL_AUTO:validate}
    show-sql: ${JPA_SHOW_SQL:false}
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration

events:
  publishing:
    enabled: ${EVENT_PUBLISHING_ENABLED:true}

logging:
  level:
    com.company.techportfolio.portfolio: ${LOGGING_LEVEL:INFO}
    org.springframework.security: DEBUG
```

## Database Schema

### Technology Portfolios Table

```sql
CREATE TABLE technology_portfolios (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    owner_id BIGINT NOT NULL,
    organization_id BIGINT
);
```

### Technologies Table

```sql
CREATE TABLE technologies (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    category VARCHAR(100) NOT NULL,
    version VARCHAR(100),
    type VARCHAR(50) NOT NULL,
    maturity_level VARCHAR(50) NOT NULL,
    risk_level VARCHAR(50) NOT NULL,
    annual_cost DECIMAL(15,2),
    license_cost DECIMAL(15,2),
    maintenance_cost DECIMAL(15,2),
    vendor_name VARCHAR(200),
    vendor_contact VARCHAR(200),
    support_contract_expiry TIMESTAMP,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    portfolio_id BIGINT NOT NULL REFERENCES technology_portfolios(id)
);
```

### Portfolio Assessments Table

```sql
CREATE TABLE portfolio_assessments (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    assessment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    next_assessment_date TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    portfolio_id BIGINT NOT NULL REFERENCES technology_portfolios(id),
    assessor_id BIGINT NOT NULL
);
```

### Technology Assessments Table

```sql
CREATE TABLE technology_assessments (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    assessment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    next_assessment_date TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    technology_id BIGINT NOT NULL REFERENCES technologies(id),
    assessor_id BIGINT NOT NULL
);
```

### Technology Dependencies Table

```sql
CREATE TABLE technology_dependencies (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    description TEXT,
    strength VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    technology_id BIGINT NOT NULL REFERENCES technologies(id),
    dependent_technology_id BIGINT NOT NULL REFERENCES technologies(id)
);
```

## Running the Service

### Development Mode

```bash
# Build the project
./gradlew build

# Run the service
./gradlew bootRun
```

### Production Mode

```bash
# Build JAR
./gradlew build -x test

# Run JAR
java -jar build/libs/technology-portfolio-service-1.0.0.jar
```

### Docker

```bash
# Build Docker image
docker build -t technology-portfolio-service .

# Run container
docker run -p 8082:8082 \
  -e DB_HOST=host.docker.internal \
  -e DB_NAME=portfolio_db \
  -e DB_USERNAME=postgres \
  -e DB_PASSWORD=password \
  technology-portfolio-service
```

## API Endpoints

### Portfolio Management Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/portfolios` | Get all portfolios |
| GET | `/portfolios/{id}` | Get portfolio by ID |
| POST | `/portfolios` | Create new portfolio |
| PUT | `/portfolios/{id}` | Update portfolio |
| DELETE | `/portfolios/{id}` | Delete portfolio |
| GET | `/portfolios/{id}/technologies` | Get technologies in portfolio |
| GET | `/portfolios/{id}/assessments` | Get portfolio assessments |

### Technology Management Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/technologies` | Get all technologies |
| GET | `/technologies/{id}` | Get technology by ID |
| POST | `/technologies` | Create new technology |
| PUT | `/technologies/{id}` | Update technology |
| DELETE | `/technologies/{id}` | Delete technology |
| GET | `/technologies/{id}/assessments` | Get technology assessments |
| GET | `/technologies/{id}/dependencies` | Get technology dependencies |

### Assessment Management Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/assessments/portfolio/{portfolioId}` | Get portfolio assessments |
| GET | `/assessments/technology/{technologyId}` | Get technology assessments |
| POST | `/assessments/portfolio` | Create portfolio assessment |
| POST | `/assessments/technology` | Create technology assessment |
| PUT | `/assessments/{id}` | Update assessment |
| DELETE | `/assessments/{id}` | Delete assessment |

### Health Check

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/actuator/health` | Service health status |

## Usage Examples

### Create Portfolio

```bash
curl -X POST http://localhost:8082/portfolio/portfolios \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Enterprise Technology Portfolio",
    "description": "Main enterprise technology portfolio",
    "type": "ENTERPRISE",
    "ownerId": 1,
    "organizationId": 1
  }'
```

Response:
```json
{
  "id": 1,
  "name": "Enterprise Technology Portfolio",
  "description": "Main enterprise technology portfolio",
  "type": "ENTERPRISE",
  "status": "ACTIVE",
  "isActive": true,
  "createdAt": "2024-01-01T10:00:00",
  "ownerId": 1,
  "organizationId": 1,
  "technologies": [],
  "assessments": []
}
```

### Add Technology to Portfolio

```bash
curl -X POST http://localhost:8082/portfolio/technologies \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Spring Boot",
    "description": "Java framework for building web applications",
    "category": "Framework",
    "version": "3.4.0",
    "type": "FRAMEWORK",
    "maturityLevel": "MATURE",
    "riskLevel": "LOW",
    "annualCost": 50000.00,
    "vendorName": "VMware",
    "portfolioId": 1
  }'
```

### Create Portfolio Assessment

```bash
curl -X POST http://localhost:8082/portfolio/assessments/portfolio \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Q1 2024 Portfolio Review",
    "description": "Quarterly portfolio assessment",
    "type": "SECURITY",
    "status": "IN_PROGRESS",
    "portfolioId": 1,
    "assessorId": 1
  }'
```

### Get Portfolio with Technologies

```bash
curl -X GET http://localhost:8082/portfolio/portfolios/1
```

Response:
```json
{
  "id": 1,
  "name": "Enterprise Technology Portfolio",
  "description": "Main enterprise technology portfolio",
  "type": "ENTERPRISE",
  "status": "ACTIVE",
  "isActive": true,
  "createdAt": "2024-01-01T10:00:00",
  "ownerId": 1,
  "organizationId": 1,
  "technologies": [
    {
      "id": 1,
      "name": "Spring Boot",
      "description": "Java framework for building web applications",
      "category": "Framework",
      "version": "3.4.0",
      "type": "FRAMEWORK",
      "maturityLevel": "MATURE",
      "riskLevel": "LOW",
      "annualCost": 50000.00,
      "vendorName": "VMware"
    }
  ],
  "assessments": [
    {
      "id": 1,
      "title": "Q1 2024 Portfolio Review",
      "description": "Quarterly portfolio assessment",
      "type": "SECURITY",
      "status": "IN_PROGRESS",
      "assessmentDate": "2024-01-01T10:00:00"
    }
  ]
}
```

## Domain Events

The service publishes the following events:

### PortfolioCreatedEvent
```kotlin
data class PortfolioCreatedEvent(
    val portfolioId: Long,
    val name: String,
    val type: PortfolioType,
    val ownerId: Long,
    val organizationId: Long?,
    val timestamp: LocalDateTime = LocalDateTime.now()
)
```

### TechnologyAddedEvent
```kotlin
data class TechnologyAddedEvent(
    val technologyId: Long,
    val portfolioId: Long,
    val name: String,
    val type: TechnologyType,
    val maturityLevel: MaturityLevel,
    val riskLevel: RiskLevel,
    val timestamp: LocalDateTime = LocalDateTime.now()
)
```

### AssessmentCreatedEvent
```kotlin
data class AssessmentCreatedEvent(
    val assessmentId: Long,
    val title: String,
    val type: AssessmentType,
    val status: AssessmentStatus,
    val assessorId: Long,
    val timestamp: LocalDateTime = LocalDateTime.now()
)
```

## CQRS Implementation

The service implements Command Query Responsibility Segregation (CQRS) pattern:

### Commands

```kotlin
// Create Portfolio Command
data class CreatePortfolioCommand(
    val name: String,
    val description: String?,
    val type: PortfolioType,
    val ownerId: Long,
    val organizationId: Long?
)

// Add Technology Command
data class AddTechnologyCommand(
    val portfolioId: Long,
    val name: String,
    val description: String?,
    val category: String,
    val type: TechnologyType,
    val maturityLevel: MaturityLevel,
    val riskLevel: RiskLevel
)
```

### Queries

```kotlin
// Get Portfolio Query
data class GetPortfolioQuery(val portfolioId: Long)

// Get Technologies Query
data class GetTechnologiesQuery(
    val portfolioId: Long,
    val type: TechnologyType? = null,
    val maturityLevel: MaturityLevel? = null
)
```

### Command/Query Handlers

```kotlin
@Component
class CreatePortfolioCommandHandler(
    private val portfolioService: PortfolioService
) : CommandHandler<CreatePortfolioCommand, Long> {
    override fun handle(command: CreatePortfolioCommand): Long {
        return portfolioService.createPortfolio(command)
    }
}

@Component
class GetPortfolioQueryHandler(
    private val portfolioService: PortfolioService
) : QueryHandler<GetPortfolioQuery, TechnologyPortfolio> {
    override fun handle(query: GetPortfolioQuery): TechnologyPortfolio {
        return portfolioService.getPortfolio(query.portfolioId)
    }
}
```

## Testing

### Unit Tests

```bash
# Run unit tests
./gradlew test

# Run tests with coverage
./gradlew test jacocoTestReport
```

### Integration Tests

```bash
# Run integration tests
./gradlew integrationTest
```

### API Tests

```bash
# Run API tests
./gradlew test --tests "*ControllerTest"
```

## Monitoring and Logging

### Health Checks

The service exposes health check endpoints via Spring Boot Actuator:

- `/actuator/health` - Overall health status
- `/actuator/health/db` - Database health status
- `/actuator/info` - Application information
- `/actuator/metrics` - Application metrics

### Logging Configuration

```yaml
logging:
  level:
    com.company.techportfolio.portfolio: DEBUG
    org.springframework.security: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: logs/portfolio-service.log
```

## Performance Considerations

### Database Optimization

- Use appropriate indexes on frequently queried columns
- Implement pagination for large result sets
- Use lazy loading for related entities
- Consider read replicas for query operations

### Caching Strategy

```kotlin
@Cacheable("portfolios")
fun getPortfolio(id: Long): TechnologyPortfolio {
    return portfolioRepository.findById(id)
        .orElseThrow { PortfolioNotFoundException("Portfolio not found: $id") }
}

@CacheEvict("portfolios", key = "#portfolio.id")
fun updatePortfolio(portfolio: TechnologyPortfolio): TechnologyPortfolio {
    return portfolioRepository.save(portfolio)
}
```

### Event Publishing

- Use asynchronous event publishing for better performance
- Implement event sourcing for audit trails
- Use event versioning for backward compatibility

## Security Considerations

### Input Validation

- All input is validated using Bean Validation annotations
- SQL injection is prevented through JPA parameterized queries
- XSS protection is implemented through input sanitization

### Access Control

- Implement portfolio-level access control
- Validate user permissions for portfolio operations
- Audit all portfolio changes

### Data Protection

- Encrypt sensitive data at rest
- Implement data retention policies
- Regular security assessments

## Deployment

### Docker Compose

```yaml
version: '3.8'
services:
  technology-portfolio-service:
    build: .
    ports:
      - "8082:8082"
    environment:
      - DB_HOST=postgres
      - DB_NAME=portfolio_db
      - DB_USERNAME=postgres
      - DB_PASSWORD=password
    depends_on:
      - postgres

  postgres:
    image: postgres:14
    environment:
      - POSTGRES_DB=portfolio_db
      - POSTGRES_USER=postgres
      - POSTGRES_PASSWORD=password
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

volumes:
  postgres_data:
```

### Kubernetes

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: technology-portfolio-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: technology-portfolio-service
  template:
    metadata:
      labels:
        app: technology-portfolio-service
    spec:
      containers:
      - name: technology-portfolio-service
        image: technology-portfolio-service:1.0.0
        ports:
        - containerPort: 8082
        env:
        - name: DB_HOST
          value: postgres-service
        - name: DB_NAME
          value: portfolio_db
        envFrom:
        - secretRef:
            name: db-credentials
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8082
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8082
          initialDelaySeconds: 5
          periodSeconds: 5
```

## Troubleshooting

### Common Issues

1. **Database Connection Issues**:
   - Verify database credentials and connection string
   - Check if PostgreSQL is running and accessible
   - Ensure database exists and user has proper permissions

2. **Migration Issues**:
   - Check Flyway migration logs
   - Verify migration files are in correct location
   - Ensure database schema is compatible

3. **Performance Issues**:
   - Monitor database query performance
   - Check JPA query optimization
   - Review connection pool settings

4. **Event Publishing Issues**:
   - Verify event publishing is enabled
   - Check event listener configurations
   - Monitor event processing logs

### Debug Mode

Enable debug logging by adding to `application.yml`:

```yaml
logging:
  level:
    com.company.techportfolio.portfolio: DEBUG
    org.springframework.security: DEBUG
    org.hibernate.SQL: DEBUG
```

## Contributing

1. Follow the hexagonal architecture principles
2. Write unit tests for all new functionality
3. Update database migrations for schema changes
4. Follow Kotlin coding conventions
5. Ensure all tests pass before submitting PR
6. Update API documentation for any changes

## License

This project is licensed under the MIT License - see the LICENSE file for details. 