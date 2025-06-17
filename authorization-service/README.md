# Authorization Service

The Authorization Service is responsible for managing user permissions, roles, and access control in the Technology Portfolio Management System. It follows hexagonal architecture principles and provides fine-grained authorization capabilities.

## Architecture Overview

The Authorization Service provides:

- **Role-Based Access Control (RBAC)**: Manage user roles and permissions
- **Permission Validation**: Validate user permissions for specific resources and actions
- **User Management**: Manage user accounts and their associations with organizations
- **Organization Management**: Handle organizational hierarchies and access control
- **Event-Driven Communication**: Publish authorization events for other services

## Technology Stack

- **Framework**: Spring Boot 3.4
- **Language**: Kotlin
- **Build Tool**: Gradle
- **Database**: PostgreSQL with Flyway migrations
- **ORM**: Spring Data JPA
- **Event Publishing**: Spring Events
- **API Documentation**: OpenAPI 3.0

## Project Structure

```
authorization-service/
├── src/main/kotlin/com/company/techportfolio/authorization/
│   ├── adapter/
│   │   ├── in/
│   │   │   └── web/
│   │   │       └── AuthorizationController.kt
│   │   └── out/
│   │       ├── persistence/
│   │       │   ├── entity/
│   │       │   │   ├── UserEntity.kt
│   │       │   │   ├── RoleEntity.kt
│   │       │   │   ├── PermissionEntity.kt
│   │       │   │   └── OrganizationEntity.kt
│   │       │   ├── repository/
│   │       │   │   ├── UserRepository.kt
│   │       │   │   ├── RoleRepository.kt
│   │       │   │   ├── PermissionRepository.kt
│   │       │   │   └── OrganizationRepository.kt
│   │       │   └── adapter/
│   │       │       ├── UserPersistenceAdapter.kt
│   │       │       ├── RolePersistenceAdapter.kt
│   │       │       └── PermissionPersistenceAdapter.kt
│   │       └── event/
│   │           └── AuthorizationEventPublisher.kt
│   ├── domain/
│   │   ├── model/
│   │   │   ├── AuthorizationRequest.kt
│   │   │   ├── AuthorizationResult.kt
│   │   │   └── UserPermission.kt
│   │   ├── port/
│   │   │   ├── UserRepository.kt
│   │   │   ├── RoleRepository.kt
│   │   │   ├── PermissionRepository.kt
│   │   │   └── EventPublisher.kt
│   │   └── service/
│   │       └── AuthorizationService.kt
│   └── AuthorizationServiceApplication.kt
├── src/main/resources/
│   ├── application.yml
│   └── db/migration/
│       ├── V1__Create_users_table.sql
│       ├── V2__Create_user_roles_table.sql
│       ├── V3__Create_roles_table.sql
│       ├── V4__Create_permissions_table.sql
│       └── V5__Create_role_permissions_table.sql
└── src/test/kotlin/
    └── com/company/techportfolio/authorization/
        └── domain/service/
            └── AuthorizationServiceTest.kt
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
SERVER_PORT=8081
SERVER_SERVLET_CONTEXT_PATH=/auth

# Database Configuration
DB_HOST=localhost
DB_PORT=5432
DB_NAME=authorization_db
DB_USERNAME=postgres
DB_PASSWORD=password

# JPA Configuration
JPA_HIBERNATE_DDL_AUTO=validate
JPA_SHOW_SQL=false

# Logging Configuration
LOGGING_LEVEL=INFO
```

### Configuration Files

#### application.yml

```yaml
server:
  port: ${SERVER_PORT:8081}
  servlet:
    context-path: ${SERVER_SERVLET_CONTEXT_PATH:/auth}

spring:
  application:
    name: authorization-service
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:authorization_db}
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

logging:
  level:
    com.company.techportfolio.authorization: ${LOGGING_LEVEL:INFO}
    org.springframework.security: DEBUG
```

## Database Schema

### Users Table

```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    is_active BOOLEAN DEFAULT true,
    is_enabled BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    last_login_at TIMESTAMP,
    organization_id BIGINT REFERENCES organizations(id)
);
```

### Roles Table

```sql
CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    description VARCHAR(500),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Permissions Table

```sql
CREATE TABLE permissions (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    description VARCHAR(500),
    resource VARCHAR(100) NOT NULL,
    action VARCHAR(50) NOT NULL,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### User Roles Table

```sql
CREATE TABLE user_roles (
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    role_id BIGINT REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);
```

### Role Permissions Table

```sql
CREATE TABLE role_permissions (
    role_id BIGINT REFERENCES roles(id) ON DELETE CASCADE,
    permission_id BIGINT REFERENCES permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
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
java -jar build/libs/authorization-service-1.0.0.jar
```

### Docker

```bash
# Build Docker image
docker build -t authorization-service .

# Run container
docker run -p 8081:8081 \
  -e DB_HOST=host.docker.internal \
  -e DB_NAME=authorization_db \
  -e DB_USERNAME=postgres \
  -e DB_PASSWORD=password \
  authorization-service
```

## API Endpoints

### Authorization Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/authorize` | Authorize user for specific resource and action |
| GET | `/users/{userId}/permissions` | Get user permissions |
| GET | `/users/{userId}/roles` | Get user roles |
| POST | `/users/{userId}/roles` | Assign roles to user |
| DELETE | `/users/{userId}/roles/{roleId}` | Remove role from user |

### User Management Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/users` | Get all users |
| GET | `/users/{id}` | Get user by ID |
| POST | `/users` | Create new user |
| PUT | `/users/{id}` | Update user |
| DELETE | `/users/{id}` | Delete user |

### Role Management Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/roles` | Get all roles |
| GET | `/roles/{id}` | Get role by ID |
| POST | `/roles` | Create new role |
| PUT | `/roles/{id}` | Update role |
| DELETE | `/roles/{id}` | Delete role |

### Permission Management Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/permissions` | Get all permissions |
| GET | `/permissions/{id}` | Get permission by ID |
| POST | `/permissions` | Create new permission |
| PUT | `/permissions/{id}` | Update permission |
| DELETE | `/permissions/{id}` | Delete permission |

### Health Check

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/actuator/health` | Service health status |

## Usage Examples

### Authorize User

```bash
curl -X POST http://localhost:8081/auth/authorize \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "resource": "portfolio",
    "action": "read",
    "resourceId": 123
  }'
```

Response:
```json
{
  "authorized": true,
  "userId": 1,
  "resource": "portfolio",
  "action": "read",
  "resourceId": 123,
  "permissions": ["portfolio:read", "portfolio:write"],
  "roles": ["PORTFOLIO_MANAGER"]
}
```

### Get User Permissions

```bash
curl -X GET http://localhost:8081/auth/users/1/permissions
```

Response:
```json
{
  "userId": 1,
  "permissions": [
    {
      "id": 1,
      "name": "Read Portfolio",
      "resource": "portfolio",
      "action": "read"
    },
    {
      "id": 2,
      "name": "Write Portfolio",
      "resource": "portfolio",
      "action": "write"
    }
  ]
}
```

### Create User

```bash
curl -X POST http://localhost:8081/auth/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john.doe",
    "email": "john.doe@company.com",
    "firstName": "John",
    "lastName": "Doe",
    "organizationId": 1
  }'
```

## Domain Events

The service publishes the following events:

### UserCreatedEvent
```kotlin
data class UserCreatedEvent(
    val userId: Long,
    val username: String,
    val email: String,
    val organizationId: Long?,
    val timestamp: LocalDateTime = LocalDateTime.now()
)
```

### UserRoleAssignedEvent
```kotlin
data class UserRoleAssignedEvent(
    val userId: Long,
    val roleId: Long,
    val roleName: String,
    val timestamp: LocalDateTime = LocalDateTime.now()
)
```

### PermissionGrantedEvent
```kotlin
data class PermissionGrantedEvent(
    val userId: Long,
    val permissionId: Long,
    val resource: String,
    val action: String,
    val timestamp: LocalDateTime = LocalDateTime.now()
)
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

### Database Tests

```bash
# Run database tests with test containers
./gradlew test --tests "*DatabaseTest"
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
    com.company.techportfolio.authorization: DEBUG
    org.springframework.security: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: logs/authorization-service.log
```

## Security Considerations

### Input Validation

- All input is validated using Bean Validation annotations
- SQL injection is prevented through JPA parameterized queries
- XSS protection is implemented through input sanitization

### Access Control

- Role-based access control (RBAC) is implemented
- Fine-grained permissions for resources and actions
- Organization-based access control for multi-tenant scenarios

### Data Protection

- Sensitive data is encrypted at rest
- Passwords are hashed using bcrypt
- Audit logging for all authorization decisions

## Deployment

### Docker Compose

```yaml
version: '3.8'
services:
  authorization-service:
    build: .
    ports:
      - "8081:8081"
    environment:
      - DB_HOST=postgres
      - DB_NAME=authorization_db
      - DB_USERNAME=postgres
      - DB_PASSWORD=password
    depends_on:
      - postgres

  postgres:
    image: postgres:14
    environment:
      - POSTGRES_DB=authorization_db
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
  name: authorization-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: authorization-service
  template:
    metadata:
      labels:
        app: authorization-service
    spec:
      containers:
      - name: authorization-service
        image: authorization-service:1.0.0
        ports:
        - containerPort: 8081
        env:
        - name: DB_HOST
          value: postgres-service
        - name: DB_NAME
          value: authorization_db
        envFrom:
        - secretRef:
            name: db-credentials
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8081
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8081
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

### Debug Mode

Enable debug logging by adding to `application.yml`:

```yaml
logging:
  level:
    com.company.techportfolio.authorization: DEBUG
    org.springframework.security: DEBUG
    org.hibernate.SQL: DEBUG
```

## Contributing

1. Follow the hexagonal architecture principles
2. Write unit tests for all new functionality
3. Update database migrations for schema changes
4. Follow Kotlin coding conventions
5. Ensure all tests pass before submitting PR

## License

This project is licensed under the MIT License - see the LICENSE file for details. 