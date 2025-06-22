# API Gateway Configuration Externalization - Migration Summary

This document summarizes the changes made to externalize all hardcoded configurations in the API Gateway service and implement environment-specific profiles, including the new externalized route configuration system.

## Overview

The API Gateway configuration has been completely externalized to support multiple environments (local, dev, test, uat, prod, docker) with profile-based configuration management. All hardcoded values have been moved to configuration files and environment variables, including the gateway routes which are now fully configurable per environment.

## Changes Made

### 1. Configuration Files Created

#### Base Configuration
- **`application.yml`**: Base configuration with common settings
  - Removed hardcoded values
  - Added HTTP client configuration properties
  - Kept only common settings shared across all environments

#### Environment-Specific Profiles
- **`application-local.yml`**: Local development with mock authentication
- **`application-dev.yml`**: Development environment with SAML authentication
- **`application-test.yml`**: Test environment with mock authentication
- **`application-uat.yml`**: UAT environment with SAML authentication
- **`application-prod.yml`**: Production environment with SAML authentication
- **`application-docker.yml`**: Docker environment with SAML authentication

#### Updated Test Configuration
- **`application-test.yml`** (test resources): Updated to align with new structure

### 2. Code Changes

#### ApplicationConfig.kt
- **Externalized HTTP client timeouts**:
  - Added `@Value` annotations for `http.client.connect-timeout` and `http.client.read-timeout`
  - Removed hardcoded `Duration.ofSeconds(5)` and `Duration.ofSeconds(10)`
  - Made timeouts configurable per environment

#### New Route Configuration Classes
- **`RouteConfiguration.kt`**: Configuration properties for externalized routes
  - `RouteConfiguration` class with `@ConfigurationProperties`
  - `RouteDefinition` data class for individual routes
  - `RouteFilter` data class for route filters

- **`ExternalizedGatewayConfig.kt`**: New gateway configuration using externalized routes
  - Reads route definitions from YAML configuration
  - Supports multiple filter types (rewrite-path, add-request-header, circuit-breaker, retry, etc.)
  - Dynamic route building with metadata support
  - Environment-specific route configurations

#### Deprecated GatewayConfig.kt
- **Marked as deprecated**: Original hardcoded route configuration
- **Added deprecation annotations**: Directs users to new externalized configuration
- **Maintained for backward compatibility**: Still functional but not recommended

#### Configuration Properties Added
- **HTTP Client Configuration**:
  ```yaml
  http:
    client:
      connect-timeout: 5  # seconds
      read-timeout: 10    # seconds
  ```

- **Service URLs** (already externalized):
  ```yaml
  services:
    authorization:
      url: http://authorization-service:8080
    audit:
      url: http://audit-service:8080
    user-management:
      url: http://user-management-service:8080
    timeout: 5s
  ```

- **Externalized Gateway Routes**:
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
        metadata:
          service: technology-portfolio
          environment: production
  ```

### 3. Environment-Specific Configurations

#### Local Development (`local`)
- Mock authentication enabled
- Gateway disabled for simplicity
- Short timeouts (3s connect, 5s read)
- DEBUG logging level
- Local service URLs

#### Development (`dev`)
- SAML authentication with SimpleSAMLphp
- Gateway enabled with externalized routes
- Standard timeouts (5s connect, 10s read)
- DEBUG logging level
- Local service URLs
- Basic route configuration with environment headers

#### Test (`test`)
- Mock authentication
- Gateway disabled
- Very short timeouts (2s for tests)
- INFO logging level
- Test-specific service URLs
- Minimal route configuration (disabled)

#### UAT (`uat`)
- SAML authentication with UAT identity provider
- Gateway enabled with externalized routes
- Extended timeouts (10s for stability)
- INFO logging level
- UAT service URLs
- Production-like security settings

#### Production (`prod`)
- SAML authentication with production identity provider
- Gateway enabled with externalized routes and circuit breakers
- Long timeouts (10s connect, 30s read)
- WARN/ERROR logging level
- Production service URLs
- Strict security settings
- Rate limiting enabled
- Advanced route configuration with:
  - Circuit breakers for fault tolerance
  - Retry logic for resilience
  - Request ID tracking
  - Environment-specific headers

#### Docker (`docker`)
- SAML authentication with containerized identity provider
- Gateway enabled with externalized routes
- Standard timeouts (10s)
- INFO logging level
- Container service URLs
- Redis configuration
- Container-specific route configuration with:
  - Container ID headers
  - Circuit breakers
  - Retry logic

### 4. Route Externalization Features

#### Supported Filter Types
- **rewrite-path**: Path rewriting with regex patterns
- **add-request-header**: Add headers to incoming requests
- **add-response-header**: Add headers to outgoing responses
- **strip-prefix**: Remove path prefixes
- **circuit-breaker**: Apply circuit breaker pattern
- **retry**: Configure retry behavior

#### Route Management Features
- **Dynamic enabling/disabling**: Routes can be enabled/disabled per environment
- **Route ordering**: Control route matching priority
- **Metadata support**: Add custom metadata for monitoring
- **Environment-specific configuration**: Different routes for different environments

#### Environment-Specific Route Examples

**Development Routes**:
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

**Production Routes**:
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

### 5. Environment Variables

#### Required for Production
- `JWT_SECRET`: Secret key for JWT signing
- `SAML_IDP_METADATA_URL`: SAML identity provider metadata URL
- `SAML_IDP_ENTITY_ID`: SAML identity provider entity ID
- `SAML_SP_ENTITY_ID`: SAML service provider entity ID
- `SAML_SP_BASE_URL`: SAML service provider base URL

#### Optional (with defaults)
- `EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE`: Eureka server URL
- `REDIS_HOST`: Redis host
- `REDIS_PORT`: Redis port
- `HOSTNAME`: Container hostname (for Docker environments)

### 6. Docker Configuration Updates

#### docker-compose.yml
- Updated to use `docker` profile
- Added comprehensive environment variable configuration
- Improved service health checks
- Added monitoring services (Prometheus, Grafana)

### 7. Documentation

#### Configuration Guide
- **`CONFIGURATION.md`**: Comprehensive configuration guide
  - Environment profiles explanation
  - Configuration properties reference
  - Externalized route configuration documentation
  - Supported filter types and examples
  - Usage examples
  - Security considerations
  - Troubleshooting guide

#### Migration Summary
- **`CONFIGURATION_MIGRATION_SUMMARY.md`**: This document

## Benefits Achieved

### 1. Environment Isolation
- Each environment has its own configuration
- No risk of production settings in development
- Easy environment-specific customization
- Environment-specific route configurations

### 2. Security Improvements
- JWT secrets externalized to environment variables
- SAML configuration externalized
- Production-specific security settings
- Circuit breakers and retry logic for resilience

### 3. Operational Flexibility
- Easy profile switching
- Configurable timeouts per environment
- Environment-specific logging levels
- Dynamic route management without code changes

### 4. Maintainability
- Clear separation of concerns
- Easy to add new environments
- Centralized configuration management
- Route configuration externalized from code

### 5. Deployment Flexibility
- Support for containerized deployments
- Environment variable overrides
- Profile-based configuration loading
- Environment-specific routing

### 6. Advanced Features
- Circuit breaker pattern for fault tolerance
- Retry logic for resilience
- Request tracking with unique IDs
- Environment-specific headers
- Route metadata for monitoring

## Migration Steps for Existing Deployments

### 1. Identify Current Configuration
- Determine which configuration is currently being used
- Map existing settings to new profile structure
- Identify current route configurations

### 2. Choose Appropriate Profile
- **Local development**: Use `local` profile
- **Development environment**: Use `dev` profile
- **Testing**: Use `test` profile
- **UAT**: Use `uat` profile
- **Production**: Use `prod` profile
- **Docker**: Use `docker` profile

### 3. Update Startup Commands
```bash
# Old way (deprecated)
java -jar api-gateway.jar

# New way
java -jar api-gateway.jar --spring.profiles.active=prod
```

### 4. Set Environment Variables
```bash
# Production example
export JWT_SECRET="your-production-jwt-secret-key"
export SAML_IDP_METADATA_URL="https://idp.company.com/simplesaml/saml2/idp/metadata.php"
export SAML_IDP_ENTITY_ID="https://idp.company.com/simplesaml/saml2/idp/metadata.php"
export SAML_SP_ENTITY_ID="https://gateway.company.com"
export SAML_SP_BASE_URL="https://gateway.company.com"
```

### 5. Test Configuration
- Verify profile loading
- Test authentication flows
- Validate service communication
- Check logging levels
- Test route configurations
- Verify filter functionality

## Backward Compatibility

The old `application-mock-auth.yml` file and hardcoded `GatewayConfig` are still available for backward compatibility but are deprecated. New deployments should use the profile-based configuration system with externalized routes.

## Next Steps

### 1. Update Other Services
- Apply similar externalization to other microservices
- Create environment-specific profiles for each service
- Update docker-compose configurations

### 2. Configuration Management
- Consider using Spring Cloud Config for centralized configuration
- Implement configuration encryption for sensitive values
- Add configuration validation

### 3. Monitoring and Alerting
- Add configuration change monitoring
- Implement configuration drift detection
- Set up alerts for configuration issues
- Monitor route performance and circuit breaker states

### 4. Documentation
- Update deployment guides
- Create environment setup scripts
- Document configuration troubleshooting procedures
- Create route configuration templates

### 5. Advanced Route Features
- Implement rate limiting filters
- Add authentication/authorization filters
- Create custom filter factories
- Add route metrics and monitoring 