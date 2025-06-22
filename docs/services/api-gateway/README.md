# API Gateway Service

The API Gateway is the entry point for all client requests in the Technology Portfolio Management System. It handles SAML authentication, request routing, and provides a unified API interface for all microservices.

## Architecture Overview

The API Gateway follows hexagonal architecture principles and provides:

- **SAML Authentication**: Integration with Ping Federated Identity Provider
- **Request Routing**: Routes requests to appropriate microservices
- **JWT Token Management**: Issues and validates JWT tokens for authenticated users
- **CORS Configuration**: Handles cross-origin requests
- **Request/Response Transformation**: Transforms requests and responses as needed

## Technology Stack

- **Framework**: Spring Boot 3.4
- **Language**: Kotlin
- **Build Tool**: Gradle
- **Authentication**: Spring Security SAML
- **API Documentation**: OpenAPI 3.0

## Project Structure

```
api-gateway/
├── src/main/kotlin/com/company/techportfolio/gateway/
│   ├── adapter/
│   │   └── in/
│   │       └── web/
│   │           └── AuthenticationController.kt
│   ├── config/
│   │   ├── ApplicationConfig.kt
│   │   ├── GatewayConfig.kt
│   │   └── SamlConfig.kt
│   ├── domain/
│   │   ├── model/
│   │   │   ├── AuthenticationResult.kt
│   │   │   └── TokenValidationResult.kt
│   │   ├── port/
│   │   │   ├── AuditPort.kt
│   │   │   ├── AuthenticationPort.kt
│   │   │   └── AuthorizationPort.kt
│   │   └── service/
│   │       └── AuthenticationService.kt
│   └── ApiGatewayApplication.kt
└── src/main/resources/
    └── application.yml
```

## Setup and Configuration

### Prerequisites

- Java 21 or higher
- Gradle 8.0 or higher
- Ping Federated Identity Provider configured

### Environment Variables

Create a `.env` file in the project root:

```bash
# Server Configuration
SERVER_PORT=8080
SERVER_SERVLET_CONTEXT_PATH=/api

# SAML Configuration
SAML_ENTITY_ID=https://your-domain.com/api/saml/metadata
SAML_SSO_URL=https://your-pingfederate-server.com/idp/SSO.saml2
SAML_SLO_URL=https://your-pingfederate-server.com/idp/SLO.saml2
SAML_X509_CERTIFICATE=your-x509-certificate-content

# JWT Configuration
JWT_SECRET=your-jwt-secret-key
JWT_EXPIRATION=3600

# Microservice URLs
AUTHORIZATION_SERVICE_URL=http://localhost:8081
TECHNOLOGY_PORTFOLIO_SERVICE_URL=http://localhost:8082
```

### Configuration Files

#### application.yml

```yaml
server:
  port: ${SERVER_PORT:8080}
  servlet:
    context-path: ${SERVER_SERVLET_CONTEXT_PATH:/api}

spring:
  application:
    name: api-gateway
  security:
    saml2:
      relyingparty:
        registration:
          pingfederate:
            signing:
              credentials:
                - certificate-location: classpath:saml/certificate.crt
                  private-key-location: classpath:saml/private.key
            identityprovider:
              entity-id: ${SAML_ENTITY_ID}
              sso-url: ${SAML_SSO_URL}
              slo-url: ${SAML_SLO_URL}
              verification:
                credentials:
                  - certificate-location: classpath:saml/idp-certificate.crt

jwt:
  secret: ${JWT_SECRET}
  expiration: ${JWT_EXPIRATION:3600}

microservices:
  authorization:
    url: ${AUTHORIZATION_SERVICE_URL:http://localhost:8081}
  technology-portfolio:
    url: ${TECHNOLOGY_PORTFOLIO_SERVICE_URL:http://localhost:8082}
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
java -jar build/libs/api-gateway-1.0.0.jar
```

## API Endpoints

### Authentication Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/saml/login` | Initiate SAML login |
| POST | `/saml/sso` | SAML Single Sign-On callback |
| GET | `/saml/logout` | Initiate SAML logout |
| POST | `/saml/slo` | SAML Single Logout callback |
| GET | `/auth/validate` | Validate JWT token |
| POST | `/auth/refresh` | Refresh JWT token |

### Health Check

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/actuator/health` | Service health status |

## Authentication Flow

1. **Unauthenticated User**:
   - User accesses protected resource
   - Gateway redirects to `/saml/login`
   - User is redirected to Ping Federate
   - After successful authentication, user is redirected back to `/saml/sso`
   - Gateway validates SAML response and issues JWT token
   - User is redirected to original resource with JWT token

2. **Authenticated User**:
   - User includes JWT token in Authorization header
   - Gateway validates token and forwards request to appropriate microservice
   - Microservice processes request and returns response

## Security Configuration

### CORS Configuration

```kotlin
@Configuration
class CorsConfig {
    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowedOrigins = listOf("http://localhost:3000", "https://your-frontend-domain.com")
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
        configuration.allowedHeaders = listOf("*")
        configuration.allowCredentials = true
        
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
}
```

### JWT Token Structure

```json
{
  "sub": "user123",
  "username": "john.doe",
  "email": "john.doe@company.com",
  "roles": ["USER", "PORTFOLIO_MANAGER"],
  "organizationId": 1,
  "iat": 1640995200,
  "exp": 1640998800
}
```

## Monitoring and Logging

### Health Checks

The service exposes health check endpoints via Spring Boot Actuator:

- `/actuator/health` - Overall health status
- `/actuator/info` - Application information
- `/actuator/metrics` - Application metrics

### Logging Configuration

```yaml
logging:
  level:
    com.company.techportfolio.gateway: DEBUG
    org.springframework.security: DEBUG
    org.springframework.security.saml2: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
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

### Manual Testing

1. **Test SAML Login**:
   ```bash
   curl -X GET http://localhost:8080/api/saml/login
   ```

2. **Test Token Validation**:
   ```bash
   curl -X GET http://localhost:8080/api/auth/validate \
     -H "Authorization: Bearer YOUR_JWT_TOKEN"
   ```

## Troubleshooting

### Common Issues

1. **SAML Configuration Errors**:
   - Verify certificate paths in `application.yml`
   - Check entity ID matches Ping Federate configuration
   - Ensure SSO/SLO URLs are correct

2. **CORS Issues**:
   - Verify allowed origins in CORS configuration
   - Check if frontend is sending correct headers

3. **JWT Token Issues**:
   - Verify JWT secret is properly configured
   - Check token expiration settings
   - Ensure token format is correct

### Debug Mode

Enable debug logging by adding to `application.yml`:

```yaml
logging:
  level:
    com.company.techportfolio.gateway: DEBUG
    org.springframework.security: DEBUG
```

## Deployment

### Docker

```dockerfile
FROM openjdk:21-jdk-slim
COPY build/libs/api-gateway-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### Kubernetes

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: api-gateway
spec:
  replicas: 3
  selector:
    matchLabels:
      app: api-gateway
  template:
    metadata:
      labels:
        app: api-gateway
    spec:
      containers:
      - name: api-gateway
        image: api-gateway:1.0.0
        ports:
        - containerPort: 8080
        env:
        - name: SERVER_PORT
          value: "8080"
        - name: SAML_ENTITY_ID
          valueFrom:
            secretKeyRef:
              name: saml-config
              key: entity-id
```

## Contributing

1. Follow the hexagonal architecture principles
2. Write unit tests for all new functionality
3. Update documentation for any API changes
4. Follow Kotlin coding conventions
5. Ensure all tests pass before submitting PR

## License

This project is licensed under the MIT License - see the LICENSE file for details. 