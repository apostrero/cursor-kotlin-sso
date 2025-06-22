# API Gateway Configuration Guide

This document explains the externalized configuration structure for the API Gateway service, including environment-specific profiles and configuration properties.

## Configuration Structure

The API Gateway uses Spring Boot's profile-based configuration system with the following structure:

```
src/main/resources/
├── application.yml                    # Base configuration (common settings)
├── application-local.yml             # Local development profile
├── application-dev.yml               # Development environment profile
├── application-test.yml              # Test environment profile
├── application-uat.yml               # UAT environment profile
├── application-prod.yml              # Production environment profile
├── application-docker.yml            # Docker environment profile
└── application-mock-auth.yml         # Mock authentication profile (legacy)
```

## Environment Profiles

### Local Development (`local`)
- **Purpose**: Individual developer setup with mock authentication
- **Authentication**: Mock authentication (no SAML)
- **Gateway**: Disabled for simplicity
- **Timeouts**: Short (3s connect, 5s read)
- **Logging**: DEBUG level
- **Usage**: `--spring.profiles.active=local`

### Development (`dev`)
- **Purpose**: Development environment with SAML authentication
- **Authentication**: SAML with SimpleSAMLphp
- **Gateway**: Enabled with externalized routes
- **Timeouts**: Standard (5s connect, 10s read)
- **Logging**: DEBUG level
- **Usage**: `--spring.profiles.active=dev`

### Test (`test`)
- **Purpose**: Unit and integration testing
- **Authentication**: Mock authentication
- **Gateway**: Disabled
- **Timeouts**: Very short (2s for tests)
- **Logging**: INFO level
- **Usage**: `--spring.profiles.active=test`

### UAT (`uat`)
- **Purpose**: User Acceptance Testing environment
- **Authentication**: SAML with UAT identity provider
- **Gateway**: Enabled with externalized routes
- **Timeouts**: Extended (10s for stability)
- **Logging**: INFO level
- **Usage**: `--spring.profiles.active=uat`

### Production (`prod`)
- **Purpose**: Production environment
- **Authentication**: SAML with production identity provider
- **Gateway**: Enabled with externalized routes and circuit breakers
- **Timeouts**: Long (10s connect, 30s read)
- **Logging**: WARN/ERROR level
- **Usage**: `--spring.profiles.active=prod`

### Docker (`docker`)
- **Purpose**: Containerized deployment
- **Authentication**: SAML with containerized identity provider
- **Gateway**: Enabled with externalized routes
- **Timeouts**: Standard (10s)
- **Logging**: INFO level
- **Usage**: `--spring.profiles.active=docker`

## Configuration Properties

### HTTP Client Configuration
```yaml
http:
  client:
    connect-timeout: 5  # Connection timeout in seconds
    read-timeout: 10    # Read timeout in seconds
```

### JWT Configuration
```yaml
jwt:
  secret: ${JWT_SECRET:default-secret-key}
  expiration: 3600      # Token expiration in seconds
```

### Service URLs
```yaml
services:
  authorization:
    url: http://authorization-service:8080
  audit:
    url: http://audit-service:8080
  user-management:
    url: http://user-management-service:8080
  timeout: 5s           # Service call timeout
```

### SAML Configuration
```yaml
saml:
  idp:
    metadata-url: ${SAML_IDP_METADATA_URL}
    entity-id: ${SAML_IDP_ENTITY_ID}
  sp:
    entity-id: ${SAML_SP_ENTITY_ID}
    base-url: ${SAML_SP_BASE_URL}
```

### Eureka Service Discovery
```yaml
eureka:
  client:
    service-url:
      defaultZone: http://eureka-server:8761/eureka/
    register-with-eureka: true
    fetch-registry: true
  instance:
    prefer-ip-address: true
    health-check-url-path: /actuator/health
```

### Mock Authentication
```yaml
mock:
  auth:
    enabled: true
    login-page: /mock-login
    success-url: /api/auth/mock-success
    users:
      - username: user1
        password: password
        roles: [ROLE_PORTFOLIO_MANAGER, READ_PORTFOLIO]
        email: user1@example.com
        firstName: User1
        lastName: MockUser
```

## Externalized Gateway Routes

The API Gateway routes are now externalized and configurable per environment. This provides:

- **Environment-specific routing**: Different routes for different environments
- **Dynamic route management**: Enable/disable routes without code changes
- **Configurable filters**: Add circuit breakers, retry logic, and headers per route
- **Route ordering**: Control route matching priority
- **Metadata support**: Add custom metadata for monitoring and management

### Route Configuration Structure
```yaml
gateway:
  routes:
    - id: technology-portfolio
      path: /api/portfolio/**
      uri: lb://technology-portfolio-service
      order: 1
      enabled: true
      filters:
        - type: rewrite-path
          args:
            regex: /api/portfolio/(?<segment>.*)
            replacement: /\${segment}
        - type: add-request-header
          args:
            name: X-Environment
            value: prod
        - type: circuit-breaker
          args:
            name: technology-portfolio-circuit-breaker
        - type: retry
          args:
            retries: 3
            statuses: 5XX,502,503,504
      metadata:
        service: technology-portfolio
        environment: production
        monitoring: enabled
```

### Supported Filter Types

#### rewrite-path
Rewrites request paths using regex patterns.
```yaml
- type: rewrite-path
  args:
    regex: /api/portfolio/(?<segment>.*)
    replacement: /\${segment}
```

#### add-request-header
Adds headers to incoming requests.
```yaml
- type: add-request-header
  args:
    name: X-Environment
    value: prod
```

#### add-response-header
Adds headers to outgoing responses.
```yaml
- type: add-response-header
  args:
    name: X-Response-Time
    value: ${System.currentTimeMillis()}
```

#### strip-prefix
Removes path prefixes from requests.
```yaml
- type: strip-prefix
  args:
    parts: 1
```

#### circuit-breaker
Applies circuit breaker pattern for fault tolerance.
```yaml
- type: circuit-breaker
  args:
    name: service-circuit-breaker
```

#### retry
Configures retry behavior for failed requests.
```yaml
- type: retry
  args:
    retries: 3
    statuses: 5XX,502,503,504
```

### Environment-Specific Route Examples

#### Development Routes
```yaml
gateway:
  routes:
    - id: technology-portfolio
      path: /api/portfolio/**
      uri: lb://technology-portfolio-service
      filters:
        - type: rewrite-path
          args:
            regex: /api/portfolio/(?<segment>.*)
            replacement: /\${segment}
        - type: add-request-header
          args:
            name: X-Environment
            value: dev
```

#### Production Routes
```yaml
gateway:
  routes:
    - id: technology-portfolio
      path: /api/portfolio/**
      uri: lb://technology-portfolio-service
      filters:
        - type: rewrite-path
          args:
            regex: /api/portfolio/(?<segment>.*)
            replacement: /\${segment}
        - type: add-request-header
          args:
            name: X-Environment
            value: prod
        - type: add-request-header
          args:
            name: X-Request-ID
            value: ${java.util.UUID.randomUUID()}
        - type: circuit-breaker
          args:
            name: technology-portfolio-circuit-breaker
        - type: retry
          args:
            retries: 3
            statuses: 5XX,502,503,504
```

#### Docker Routes
```yaml
gateway:
  routes:
    - id: technology-portfolio
      path: /api/portfolio/**
      uri: lb://technology-portfolio-service
      filters:
        - type: rewrite-path
          args:
            regex: /api/portfolio/(?<segment>.*)
            replacement: /\${segment}
        - type: add-request-header
          args:
            name: X-Environment
            value: docker
        - type: add-request-header
          args:
            name: X-Container-ID
            value: ${HOSTNAME:unknown}
        - type: circuit-breaker
          args:
            name: technology-portfolio-circuit-breaker
```

## Environment Variables

### Required for Production
- `JWT_SECRET`: Secret key for JWT signing
- `SAML_IDP_METADATA_URL`: SAML identity provider metadata URL
- `SAML_IDP_ENTITY_ID`: SAML identity provider entity ID
- `SAML_SP_ENTITY_ID`: SAML service provider entity ID
- `SAML_SP_BASE_URL`: SAML service provider base URL

### Optional (with defaults)
- `EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE`: Eureka server URL
- `REDIS_HOST`: Redis host (default: localhost)
- `REDIS_PORT`: Redis port (default: 6379)
- `HOSTNAME`: Container hostname (for Docker environments)

## Usage Examples

### Local Development
```bash
# Run with local profile
./gradlew bootRun --args='--spring.profiles.active=local'

# Or set environment variable
export SPRING_PROFILES_ACTIVE=local
./gradlew bootRun
```

### Docker Deployment
```bash
# Run with docker profile
docker run -e SPRING_PROFILES_ACTIVE=docker api-gateway:latest
```

### Production Deployment
```bash
# Set required environment variables
export JWT_SECRET="your-production-jwt-secret-key"
export SAML_IDP_METADATA_URL="https://idp.company.com/simplesaml/saml2/idp/metadata.php"
export SAML_IDP_ENTITY_ID="https://idp.company.com/simplesaml/saml2/idp/metadata.php"
export SAML_SP_ENTITY_ID="https://gateway.company.com"
export SAML_SP_BASE_URL="https://gateway.company.com"

# Run with production profile
java -jar api-gateway.jar --spring.profiles.active=prod
```

## Configuration Override Priority

Spring Boot configuration properties follow this priority order (highest to lowest):

1. Command line arguments
2. Environment variables
3. Profile-specific application properties
4. Base application properties
5. Default values in code

## Security Considerations

### Production Security
- Always use strong JWT secrets (at least 512 bits)
- Use HTTPS for all external communications
- Configure proper CORS settings
- Enable rate limiting
- Use secure SAML certificates
- Set appropriate logging levels
- Enable circuit breakers and retry logic

### Development Security
- Use mock authentication for local development
- Use development-specific JWT secrets
- Disable strict SSL for local testing
- Enable debug logging for troubleshooting

## Troubleshooting

### Common Issues

1. **Profile not found**: Ensure the profile name matches the file name (e.g., `application-local.yml` for `local` profile)

2. **Configuration not loaded**: Check that the profile is active and the configuration file exists

3. **Environment variables not recognized**: Ensure variable names match the expected format (e.g., `JWT_SECRET` not `jwt.secret`)

4. **Service connection timeouts**: Adjust timeout values in the appropriate profile configuration

5. **Route not working**: Check that the route is enabled and the gateway is enabled for the profile

6. **Filter not applied**: Verify filter type and arguments are correct

### Debug Configuration Loading
```bash
# Enable debug logging for configuration
export LOGGING_LEVEL_ORG_SPRINGFRAMEWORK_BOOT=DEBUG
./gradlew bootRun --args='--spring.profiles.active=local'
```

### Debug Route Configuration
```bash
# Enable debug logging for gateway
export LOGGING_LEVEL_ORG_SPRINGFRAMEWORK_CLOUD_GATEWAY=DEBUG
./gradlew bootRun --args='--spring.profiles.active=dev'
```

## Migration from Legacy Configuration

The old `application-mock-auth.yml` file and hardcoded `GatewayConfig` are still available for backward compatibility but are deprecated. New deployments should use the profile-based configuration system with externalized routes.

### Migration Steps
1. Identify the current configuration being used
2. Choose the appropriate profile (local, dev, test, uat, prod, docker)
3. Update startup commands to use the new profile
4. Test the configuration in the target environment
5. Remove references to the old configuration file
6. Update route configurations to use the new externalized format 