# Technology Portfolio Service - Deployment Guide

## Overview

This guide provides comprehensive deployment instructions for the reactive Technology Portfolio Service, including environment setup, monitoring configuration, and operational procedures.

## Architecture Overview

### Reactive Technology Stack
- **Framework**: Spring Boot 3.4 with WebFlux
- **Database**: PostgreSQL 15 with R2DBC
- **Security**: JWT with OAuth2 Resource Server
- **Monitoring**: Prometheus + Grafana
- **Container**: Docker with multi-stage builds
- **Orchestration**: Docker Compose / Kubernetes

### Service Components
```
Technology Portfolio Service
├── Web Layer (WebFlux Controllers)
├── Service Layer (Reactive Services)
├── Repository Layer (R2DBC Adapters)
├── Security Layer (Reactive Security)
├── Event Layer (Reactive Event Publishing)
└── Monitoring Layer (Micrometer + Actuator)
```

## Prerequisites

### System Requirements
- **CPU**: 2+ cores (4+ recommended for production)
- **Memory**: 4GB+ RAM (8GB+ recommended for production)
- **Storage**: 20GB+ available space
- **Network**: Stable internet connection for dependencies

### Software Requirements
- **Java**: OpenJDK 21 or higher
- **Docker**: 20.10+ with Docker Compose
- **PostgreSQL**: 15+ (for local development)
- **Gradle**: 8.0+ (for building)

### Environment Variables
```bash
# Database Configuration
DB_HOST=localhost
DB_PORT=5433
DB_NAME=portfolio
DB_USERNAME=portfolio_user
DB_PASSWORD=portfolio_password

# JWT Configuration
JWT_ISSUER_URI=http://api-gateway:8080
JWT_JWK_SET_URI=http://api-gateway:8080/.well-known/jwks.json
JWT_SECRET=your-256-bit-secret-key-here

# Service Configuration
SERVER_PORT=8083
SPRING_PROFILES_ACTIVE=prod
MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,info,metrics,prometheus

# Monitoring Configuration
PROMETHEUS_ENABLED=true
GRAFANA_ENABLED=true
```

## Environment Configurations

### Development Environment

#### Docker Compose Configuration
```yaml
version: '3.8'

services:
  technology-portfolio-service:
    build:
      context: .
      dockerfile: Dockerfile.dev
    ports:
      - "8083:8083"
    environment:
      - SPRING_PROFILES_ACTIVE=dev
      - DB_HOST=postgres-portfolio
      - DB_PORT=5433
      - DB_NAME=portfolio
      - DB_USERNAME=portfolio_user
      - DB_PASSWORD=portfolio_password
      - JWT_ISSUER_URI=http://api-gateway:8080
      - JWT_JWK_SET_URI=http://api-gateway:8080/.well-known/jwks.json
      - JWT_SECRET=dev-secret-key
      - MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=*
      - LOGGING_LEVEL_ROOT=DEBUG
    volumes:
      - ./logs:/app/logs
      - ./config:/app/config
    depends_on:
      - postgres-portfolio
      - api-gateway
    networks:
      - techportfolio-network
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8083/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s

  postgres-portfolio:
    image: postgres:15-alpine
    environment:
      - POSTGRES_DB=portfolio
      - POSTGRES_USER=portfolio_user
      - POSTGRES_PASSWORD=portfolio_password
    ports:
      - "5433:5432"
    volumes:
      - postgres_portfolio_data:/var/lib/postgresql/data
      - ./database/portfolio-init.sql:/docker-entrypoint-initdb.d/init.sql
    networks:
      - techportfolio-network
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U portfolio_user -d portfolio"]
      interval: 30s
      timeout: 10s
      retries: 3

  prometheus:
    image: prom/prometheus:latest
    ports:
      - "9090:9090"
    volumes:
      - ./monitoring/prometheus.yml:/etc/prometheus/prometheus.yml
      - prometheus_data:/prometheus
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'
      - '--storage.tsdb.path=/prometheus'
      - '--web.console.libraries=/etc/prometheus/console_libraries'
      - '--web.console.templates=/etc/prometheus/consoles'
      - '--storage.tsdb.retention.time=15d'
      - '--web.enable-lifecycle'
    networks:
      - techportfolio-network

  grafana:
    image: grafana/grafana:latest
    ports:
      - "3000:3000"
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=admin123
      - GF_USERS_ALLOW_SIGN_UP=false
      - GF_INSTALL_PLUGINS=grafana-piechart-panel
    volumes:
      - grafana_data:/var/lib/grafana
      - ./monitoring/grafana/dashboards:/etc/grafana/provisioning/dashboards
      - ./monitoring/grafana/datasources:/etc/grafana/provisioning/datasources
    networks:
      - techportfolio-network

volumes:
  postgres_portfolio_data:
  prometheus_data:
  grafana_data:

networks:
  techportfolio-network:
    driver: bridge
```

#### Development Dockerfile
```dockerfile
FROM openjdk:21-jdk-slim

WORKDIR /app

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Copy application JAR
COPY build/libs/technology-portfolio-service-*.jar app.jar

# Create directories
RUN mkdir -p /app/logs /app/config

# Set environment variables
ENV JAVA_OPTS="-Xms512m -Xmx1g -XX:+UseG1GC -XX:+UseContainerSupport"

# Expose port
EXPOSE 8083

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8083/actuator/health || exit 1

# Run application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

### Test Environment

#### Test Configuration
```yaml
# application-test.yml
spring:
  profiles:
    active: test
  
  r2dbc:
    url: r2dbc:h2:mem:///testdb
    username: sa
    password: 
    pool:
      max-size: 5
      initial-size: 1
  
  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
  metrics:
    export:
      prometheus:
        enabled: true

logging:
  level:
    com.company.techportfolio.portfolio: INFO
    org.springframework.security: INFO
    org.springframework.r2dbc: INFO
    reactor.netty: INFO

# Test-specific configuration
test:
  performance:
    enabled: true
    metrics:
      enabled: true
      export: true
  load:
    enabled: true
    concurrent-users: 100
    requests-per-user: 10
  integration:
    enabled: true
    cleanup: true
    data-setup: true
```

#### Test Docker Compose
```yaml
version: '3.8'

services:
  technology-portfolio-service-test:
    build:
      context: .
      dockerfile: Dockerfile.test
    environment:
      - SPRING_PROFILES_ACTIVE=test
      - MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,info,metrics,prometheus
      - LOGGING_LEVEL_ROOT=INFO
    ports:
      - "8083:8083"
    networks:
      - techportfolio-network
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8083/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s

  prometheus-test:
    image: prom/prometheus:latest
    ports:
      - "9090:9090"
    volumes:
      - ./monitoring/prometheus-test.yml:/etc/prometheus/prometheus.yml
      - prometheus_test_data:/prometheus
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'
      - '--storage.tsdb.path=/prometheus'
      - '--storage.tsdb.retention.time=7d'
    networks:
      - techportfolio-network

  grafana-test:
    image: grafana/grafana:latest
    ports:
      - "3000:3000"
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=admin123
      - GF_USERS_ALLOW_SIGN_UP=false
    volumes:
      - grafana_test_data:/var/lib/grafana
      - ./monitoring/grafana/dashboards:/etc/grafana/provisioning/dashboards
      - ./monitoring/grafana/datasources:/etc/grafana/provisioning/datasources
    networks:
      - techportfolio-network

volumes:
  prometheus_test_data:
  grafana_test_data:

networks:
  techportfolio-network:
    driver: bridge
```

### Production Environment

#### Production Configuration
```yaml
# application-prod.yml
spring:
  profiles:
    active: prod
  
  r2dbc:
    url: r2dbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    pool:
      initial-size: 10
      max-size: 50
      max-idle-time: 60m
      validation-query: SELECT 1
      validation-depth: remote
  
  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration
    validate-on-migrate: true
  
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: ${JWT_ISSUER_URI}
          jwk-set-uri: ${JWT_JWK_SET_URI}

server:
  port: ${SERVER_PORT:8083}
  compression:
    enabled: true
    mime-types: text/html,text/xml,text/plain,text/css,text/javascript,application/javascript,application/json
    min-response-size: 1024

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
      base-path: /actuator
      cors:
        allowed-origins: ${CORS_ALLOWED_ORIGINS:https://monitoring.company.com}
        allowed-methods: GET,POST,PUT,DELETE,OPTIONS
        allowed-headers: "*"
        allow-credentials: true
        max-age: 3600
  endpoint:
    health:
      show-details: never
      show-components: always
      probes:
        enabled: true
      group:
        readiness:
          include: readinessState,db,r2dbc
        liveness:
          include: livenessState
    metrics:
      enabled: true
    prometheus:
      enabled: true
    info:
      enabled: true
  metrics:
    export:
      prometheus:
        enabled: true
        descriptions: true
        step: 15s
    tags:
      application: ${spring.application.name}
      environment: ${SPRING_PROFILES_ACTIVE}
      version: ${APP_VERSION:1.0.0}
    distribution:
      percentiles-histogram:
        http.server.requests: true
        r2dbc.connections.active: true
        reactor.netty.connectionpool: true
      percentiles:
        http.server.requests: 0.5,0.95,0.99
        r2dbc.connections.active: 0.5,0.95,0.99
        reactor.netty.connectionpool: 0.5,0.95,0.99
    enable:
      jvm: true
      process: true
      system: true
      reactor: true
      r2dbc: true

logging:
  level:
    root: ${LOGGING_LEVEL_ROOT:WARN}
    com.company.techportfolio.portfolio: ${LOGGING_LEVEL_PORTFOLIO:INFO}
    org.springframework.security: ${LOGGING_LEVEL_SECURITY:WARN}
    org.springframework.r2dbc: ${LOGGING_LEVEL_R2DBC:WARN}
    reactor.netty: ${LOGGING_LEVEL_REACTOR:WARN}
    io.r2dbc.postgresql: ${LOGGING_LEVEL_R2DBC_POSTGRESQL:WARN}
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: ${LOG_FILE_PATH:/var/log/technology-portfolio-service.log}
    max-size: ${LOG_FILE_MAX_SIZE:100MB}
    max-history: ${LOG_FILE_MAX_HISTORY:30}

# Performance Configuration
performance:
  monitoring:
    enabled: true
    metrics:
      enabled: true
      export: true
    profiling:
      enabled: true
      sampling-rate: 0.01
  
  caching:
    enabled: true
    ttl: 5m
    max-size: 1000
  
  connection-pool:
    r2dbc:
      max-size: 50
      initial-size: 10
      max-idle-time: 60m
      validation-query: SELECT 1
      validation-depth: remote

# Security Configuration
security:
  jwt:
    issuer: ${JWT_ISSUER:http://api-gateway.company.com}
    audience: ${JWT_AUDIENCE:technology-portfolio-service}
    expiration: ${JWT_EXPIRATION:3600}
    refresh-expiration: ${JWT_REFRESH_EXPIRATION:86400}
  
  cors:
    allowed-origins: ${CORS_ALLOWED_ORIGINS:https://frontend.company.com}
    allowed-methods: ${CORS_ALLOWED_METHODS:GET,POST,PUT,DELETE,OPTIONS}
    allowed-headers: ${CORS_ALLOWED_HEADERS:Authorization,Content-Type,X-Requested-With}
    allow-credentials: ${CORS_ALLOW_CREDENTIALS:true}
    max-age: ${CORS_MAX_AGE:3600}
  
  rate-limiting:
    enabled: true
    requests-per-minute: ${RATE_LIMITING_REQUESTS_PER_MINUTE:1000}
    burst-size: ${RATE_LIMITING_BURST_SIZE:100}

# Business Configuration
business:
  portfolio:
    max-technologies-per-portfolio: ${MAX_TECHNOLOGIES_PER_PORTFOLIO:100}
    max-portfolios-per-user: ${MAX_PORTFOLIOS_PER_USER:50}
    max-portfolios-per-organization: ${MAX_PORTFOLIOS_PER_ORGANIZATION:1000}
  
  technology:
    max-dependencies-per-technology: ${MAX_DEPENDENCIES_PER_TECHNOLOGY:20}
    max-versions-per-technology: ${MAX_VERSIONS_PER_TECHNOLOGY:10}
  
  assessment:
    max-assessments-per-portfolio: ${MAX_ASSESSMENTS_PER_PORTFOLIO:10}
    assessment-validity-days: ${ASSESSMENT_VALIDITY_DAYS:365}

# Feature Flags
features:
  streaming:
    enabled: true
    sse:
      enabled: true
      timeout: 30s
      buffer-size: 1000
  
  real-time:
    enabled: true
    websocket:
      enabled: false
      path: /ws
  
  analytics:
    enabled: true
    metrics:
      enabled: true
      export: true
  
  audit:
    enabled: true
    events:
      enabled: true
      retention: 90d
```

#### Production Dockerfile
```dockerfile
# Multi-stage build for production
FROM openjdk:21-jdk-slim AS builder

WORKDIR /app

# Copy build files
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle gradle
COPY gradlew ./

# Download dependencies
RUN ./gradlew dependencies --no-daemon

# Copy source code
COPY src src

# Build application
RUN ./gradlew build -x test --no-daemon

# Production stage
FROM openjdk:21-jre-slim

# Create non-root user
RUN groupadd -r appuser && useradd -r -g appuser appuser

WORKDIR /app

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Copy application JAR from builder stage
COPY --from=builder /app/build/libs/technology-portfolio-service-*.jar app.jar

# Create directories
RUN mkdir -p /app/logs /app/config /var/log && \
    chown -R appuser:appuser /app /var/log

# Switch to non-root user
USER appuser

# Set environment variables
ENV JAVA_OPTS="-Xms1g -Xmx2g -XX:+UseG1GC -XX:+UseContainerSupport -XX:MaxGCPauseMillis=200 -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap"

# Expose port
EXPOSE 8083

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8083/actuator/health || exit 1

# Run application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

#### Production Docker Compose
```yaml
version: '3.8'

services:
  technology-portfolio-service:
    image: techportfolio/technology-portfolio-service:${VERSION:-latest}
    ports:
      - "8083:8083"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DB_HOST=${DB_HOST}
      - DB_PORT=${DB_PORT}
      - DB_NAME=${DB_NAME}
      - DB_USERNAME=${DB_USERNAME}
      - DB_PASSWORD=${DB_PASSWORD}
      - JWT_ISSUER_URI=${JWT_ISSUER_URI}
      - JWT_JWK_SET_URI=${JWT_JWK_SET_URI}
      - SERVER_PORT=8083
      - MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,info,metrics,prometheus
      - LOGGING_LEVEL_ROOT=${LOGGING_LEVEL_ROOT:-WARN}
      - LOG_FILE_PATH=/var/log/technology-portfolio-service.log
      - LOG_FILE_MAX_SIZE=${LOG_FILE_MAX_SIZE:-100MB}
      - LOG_FILE_MAX_HISTORY=${LOG_FILE_MAX_HISTORY:-30}
      - PERFORMANCE_MONITORING_ENABLED=true
      - PERFORMANCE_METRICS_ENABLED=true
      - PERFORMANCE_PROFILING_ENABLED=true
      - PERFORMANCE_PROFILING_SAMPLING_RATE=0.01
      - CACHING_ENABLED=true
      - CACHING_TTL=5m
      - CACHING_MAX_SIZE=1000
      - R2DBC_POOL_MAX_SIZE=50
      - R2DBC_POOL_INITIAL_SIZE=10
      - R2DBC_POOL_MAX_IDLE_TIME=60m
      - SECURITY_RATE_LIMITING_ENABLED=true
      - SECURITY_RATE_LIMITING_REQUESTS_PER_MINUTE=1000
      - SECURITY_RATE_LIMITING_BURST_SIZE=100
      - CORS_ALLOWED_ORIGINS=${CORS_ALLOWED_ORIGINS}
      - CORS_ALLOWED_METHODS=${CORS_ALLOWED_METHODS:-GET,POST,PUT,DELETE,OPTIONS}
      - CORS_ALLOWED_HEADERS=${CORS_ALLOWED_HEADERS:-Authorization,Content-Type,X-Requested-With}
      - CORS_ALLOW_CREDENTIALS=${CORS_ALLOW_CREDENTIALS:-true}
      - CORS_MAX_AGE=${CORS_MAX_AGE:-3600}
      - MAX_TECHNOLOGIES_PER_PORTFOLIO=${MAX_TECHNOLOGIES_PER_PORTFOLIO:-100}
      - MAX_PORTFOLIOS_PER_USER=${MAX_PORTFOLIOS_PER_USER:-50}
      - MAX_PORTFOLIOS_PER_ORGANIZATION=${MAX_PORTFOLIOS_PER_ORGANIZATION:-1000}
      - MAX_DEPENDENCIES_PER_TECHNOLOGY=${MAX_DEPENDENCIES_PER_TECHNOLOGY:-20}
      - MAX_VERSIONS_PER_TECHNOLOGY=${MAX_VERSIONS_PER_TECHNOLOGY:-10}
      - MAX_ASSESSMENTS_PER_PORTFOLIO=${MAX_ASSESSMENTS_PER_PORTFOLIO:-10}
      - ASSESSMENT_VALIDITY_DAYS=${ASSESSMENT_VALIDITY_DAYS:-365}
      - STREAMING_ENABLED=true
      - SSE_ENABLED=true
      - SSE_TIMEOUT=30s
      - SSE_BUFFER_SIZE=1000
      - REALTIME_ENABLED=true
      - WEBSOCKET_ENABLED=false
      - WEBSOCKET_PATH=/ws
      - ANALYTICS_ENABLED=true
      - ANALYTICS_METRICS_ENABLED=true
      - ANALYTICS_METRICS_EXPORT=true
      - AUDIT_ENABLED=true
      - AUDIT_EVENTS_ENABLED=true
      - AUDIT_EVENTS_RETENTION=90d
    volumes:
      - /var/log:/var/log
      - /app/config:/app/config
    depends_on:
      - postgres-portfolio
      - api-gateway
    networks:
      - techportfolio-network
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8083/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s
    deploy:
      resources:
        limits:
          cpus: '2.0'
          memory: 2G
        reservations:
          cpus: '1.0'
          memory: 1G
      restart_policy:
        condition: on-failure
        delay: 5s
        max_attempts: 3
        window: 120s
    restart: unless-stopped

  postgres-portfolio:
    image: postgres:15-alpine
    environment:
      - POSTGRES_DB=${DB_NAME}
      - POSTGRES_USER=${DB_USERNAME}
      - POSTGRES_PASSWORD=${DB_PASSWORD}
    volumes:
      - postgres_portfolio_data:/var/lib/postgresql/data
      - ./database/portfolio-init.sql:/docker-entrypoint-initdb.d/init.sql
      - ./database/backups:/backups
    networks:
      - techportfolio-network
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${DB_USERNAME} -d ${DB_NAME}"]
      interval: 30s
      timeout: 10s
      retries: 3
    deploy:
      resources:
        limits:
          cpus: '1.0'
          memory: 1G
        reservations:
          cpus: '0.5'
          memory: 512M
    restart: unless-stopped

  prometheus:
    image: prom/prometheus:latest
    ports:
      - "9090:9090"
    volumes:
      - ./monitoring/prometheus.yml:/etc/prometheus/prometheus.yml
      - ./monitoring/alerts.yml:/etc/prometheus/alerts.yml
      - prometheus_data:/prometheus
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'
      - '--storage.tsdb.path=/prometheus'
      - '--web.console.libraries=/etc/prometheus/console_libraries'
      - '--web.console.templates=/etc/prometheus/consoles'
      - '--storage.tsdb.retention.time=30d'
      - '--web.enable-lifecycle'
      - '--alertmanager.url=http://alertmanager:9093'
    networks:
      - techportfolio-network
    deploy:
      resources:
        limits:
          cpus: '0.5'
          memory: 512M
        reservations:
          cpus: '0.25'
          memory: 256M
    restart: unless-stopped

  alertmanager:
    image: prom/alertmanager:latest
    ports:
      - "9093:9093"
    volumes:
      - ./monitoring/alertmanager.yml:/etc/alertmanager/alertmanager.yml
      - alertmanager_data:/alertmanager
    command:
      - '--config.file=/etc/alertmanager/alertmanager.yml'
      - '--storage.path=/alertmanager'
    networks:
      - techportfolio-network
    deploy:
      resources:
        limits:
          cpus: '0.25'
          memory: 256M
        reservations:
          cpus: '0.1'
          memory: 128M
    restart: unless-stopped

  grafana:
    image: grafana/grafana:latest
    ports:
      - "3000:3000"
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=${GRAFANA_ADMIN_PASSWORD}
      - GF_USERS_ALLOW_SIGN_UP=false
      - GF_INSTALL_PLUGINS=grafana-piechart-panel
    volumes:
      - grafana_data:/var/lib/grafana
      - ./monitoring/grafana/dashboards:/etc/grafana/provisioning/dashboards
      - ./monitoring/grafana/datasources:/etc/grafana/provisioning/datasources
    networks:
      - techportfolio-network
    deploy:
      resources:
        limits:
          cpus: '0.5'
          memory: 512M
        reservations:
          cpus: '0.25'
          memory: 256M
    restart: unless-stopped

volumes:
  postgres_portfolio_data:
  prometheus_data:
  alertmanager_data:
  grafana_data:

networks:
  techportfolio-network:
    driver: bridge
```

## Deployment Scripts

### Development Deployment
```bash
#!/bin/bash
# deploy-dev.sh

set -e

echo "🚀 Deploying Technology Portfolio Service to Development Environment"
echo "=================================================================="

# Build the application
echo "📦 Building application..."
./gradlew build -x test

# Build Docker image
echo "🐳 Building Docker image..."
docker build -f Dockerfile.dev -t techportfolio/technology-portfolio-service:dev .

# Start services
echo "🏗️ Starting services..."
docker-compose -f docker-compose.dev.yml up -d

# Wait for services to be ready
echo "⏳ Waiting for services to be ready..."
sleep 30

# Health checks
echo "🏥 Running health checks..."
curl -f http://localhost:8083/actuator/health || exit 1
curl -f http://localhost:9090/-/healthy || exit 1
curl -f http://localhost:3000/api/health || exit 1

echo "✅ Development deployment completed successfully!"
echo ""
echo "📋 Service URLs:"
echo "   Technology Portfolio Service: http://localhost:8083"
echo "   Prometheus:                  http://localhost:9090"
echo "   Grafana:                     http://localhost:3000"
echo ""
echo "🔑 Grafana Credentials:"
echo "   Username: admin"
echo "   Password: admin123"
```

### Production Deployment
```bash
#!/bin/bash
# deploy-prod.sh

set -e

VERSION=${1:-latest}
ENVIRONMENT=${2:-prod}

echo "🚀 Deploying Technology Portfolio Service to Production Environment"
echo "=================================================================="
echo "Version: $VERSION"
echo "Environment: $ENVIRONMENT"

# Validate environment variables
echo "🔍 Validating environment variables..."
required_vars=("DB_HOST" "DB_PORT" "DB_NAME" "DB_USERNAME" "DB_PASSWORD" "JWT_ISSUER_URI" "JWT_JWK_SET_URI")
for var in "${required_vars[@]}"; do
    if [ -z "${!var}" ]; then
        echo "❌ Error: $var environment variable is required"
        exit 1
    fi
done

# Create backup
echo "💾 Creating database backup..."
docker exec postgres-portfolio pg_dump -U $DB_USERNAME $DB_NAME > backup_$(date +%Y%m%d_%H%M%S).sql

# Pull latest image
echo "📥 Pulling latest Docker image..."
docker pull techportfolio/technology-portfolio-service:$VERSION

# Update services
echo "🔄 Updating services..."
docker-compose -f docker-compose.prod.yml up -d --no-deps technology-portfolio-service

# Wait for service to be ready
echo "⏳ Waiting for service to be ready..."
sleep 60

# Health checks
echo "🏥 Running health checks..."
for i in {1..10}; do
    if curl -f http://localhost:8083/actuator/health > /dev/null 2>&1; then
        echo "✅ Service is healthy"
        break
    else
        echo "⏳ Waiting for service to be healthy... (attempt $i/10)"
        sleep 10
    fi
done

if [ $i -eq 10 ]; then
    echo "❌ Service failed to become healthy"
    exit 1
fi

# Run smoke tests
echo "🧪 Running smoke tests..."
curl -f http://localhost:8083/actuator/info || exit 1
curl -f http://localhost:8083/actuator/metrics || exit 1

echo "✅ Production deployment completed successfully!"
echo ""
echo "📋 Service URLs:"
echo "   Technology Portfolio Service: http://localhost:8083"
echo "   Prometheus:                  http://localhost:9090"
echo "   Grafana:                     http://localhost:3000"
echo ""
echo "📊 Monitoring:"
echo "   Health:                      http://localhost:8083/actuator/health"
echo "   Metrics:                     http://localhost:8083/actuator/metrics"
echo "   Prometheus:                  http://localhost:8083/actuator/prometheus"
```

## Monitoring and Alerting

### Prometheus Configuration
```yaml
# prometheus.yml
global:
  scrape_interval: 15s
  evaluation_interval: 15s

rule_files:
  - "alerts.yml"

scrape_configs:
  - job_name: 'technology-portfolio-service'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['technology-portfolio-service:8083']
    scrape_interval: 30s
    scrape_timeout: 10s

  - job_name: 'postgres-portfolio'
    static_configs:
      - targets: ['postgres-portfolio:5432']
    scrape_interval: 60s

alerting:
  alertmanagers:
    - static_configs:
        - targets:
          - alertmanager:9093
```

### Alert Rules
```yaml
# alerts.yml
groups:
  - name: technology-portfolio-service
    rules:
      - alert: ServiceDown
        expr: up{job="technology-portfolio-service"} == 0
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "Technology Portfolio Service is down"
          description: "Service has been down for more than 1 minute"

      - alert: HighErrorRate
        expr: rate(http_server_requests_seconds_count{job="technology-portfolio-service",status=~"4..|5.."}[5m]) > 0.1
        for: 2m
        labels:
          severity: warning
        annotations:
          summary: "High error rate detected"
          description: "Error rate is {{ $value }} errors per second"

      - alert: HighResponseTime
        expr: histogram_quantile(0.95, rate(http_server_requests_seconds_bucket{job="technology-portfolio-service"}[5m])) > 2
        for: 2m
        labels:
          severity: warning
        annotations:
          summary: "High response time detected"
          description: "95th percentile response time is {{ $value }} seconds"

      - alert: HighMemoryUsage
        expr: (jvm_memory_used_bytes{job="technology-portfolio-service",area="heap"} / jvm_memory_max_bytes{job="technology-portfolio-service",area="heap"}) > 0.8
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High memory usage detected"
          description: "Memory usage is {{ $value | humanizePercentage }}"

      - alert: DatabaseConnectionPoolExhausted
        expr: r2dbc_connections_active{job="technology-portfolio-service"} / r2dbc_connections_max{job="technology-portfolio-service"} > 0.8
        for: 2m
        labels:
          severity: warning
        annotations:
          summary: "Database connection pool nearly exhausted"
          description: "Connection pool usage is {{ $value | humanizePercentage }}"

      - alert: HighCPUUsage
        expr: process_cpu_usage{job="technology-portfolio-service"} > 0.8
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High CPU usage detected"
          description: "CPU usage is {{ $value | humanizePercentage }}"
```

### AlertManager Configuration
```yaml
# alertmanager.yml
global:
  resolve_timeout: 5m
  slack_api_url: 'https://hooks.slack.com/services/YOUR_SLACK_WEBHOOK'

route:
  group_by: ['alertname']
  group_wait: 10s
  group_interval: 10s
  repeat_interval: 1h
  receiver: 'slack-notifications'

receivers:
  - name: 'slack-notifications'
    slack_configs:
      - channel: '#techportfolio-alerts'
        title: '{{ template "slack.title" . }}'
        text: '{{ template "slack.text" . }}'
        send_resolved: true

templates:
  - '/etc/alertmanager/template/*.tmpl'

inhibit_rules:
  - source_match:
      severity: 'critical'
    target_match:
      severity: 'warning'
    equal: ['alertname', 'dev', 'instance']
```

## Operational Procedures

### Health Checks
```bash
#!/bin/bash
# health-check.sh

ENVIRONMENT=${1:-dev}

echo "🏥 Running health checks for $ENVIRONMENT environment"
echo "=================================================="

# Service health
echo "🔍 Checking service health..."
curl -f http://localhost:8083/actuator/health || {
    echo "❌ Service health check failed"
    exit 1
}

# Database health
echo "🗄️ Checking database health..."
curl -f http://localhost:8083/actuator/health/db || {
    echo "❌ Database health check failed"
    exit 1
}

# R2DBC health
echo "🔌 Checking R2DBC health..."
curl -f http://localhost:8083/actuator/health/r2dbc || {
    echo "❌ R2DBC health check failed"
    exit 1
}

# Prometheus health
echo "📊 Checking Prometheus health..."
curl -f http://localhost:9090/-/healthy || {
    echo "❌ Prometheus health check failed"
    exit 1
}

# Grafana health
echo "📈 Checking Grafana health..."
curl -f http://localhost:3000/api/health || {
    echo "❌ Grafana health check failed"
    exit 1
}

echo "✅ All health checks passed!"
```

### Backup and Recovery
```bash
#!/bin/bash
# backup.sh

BACKUP_DIR="/backups"
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="portfolio_backup_$DATE.sql"

echo "💾 Creating database backup..."
echo "Backup file: $BACKUP_FILE"

# Create backup
docker exec postgres-portfolio pg_dump -U $DB_USERNAME $DB_NAME > "$BACKUP_DIR/$BACKUP_FILE"

# Compress backup
gzip "$BACKUP_DIR/$BACKUP_FILE"

echo "✅ Backup completed: $BACKUP_FILE.gz"

# Clean old backups (keep last 30 days)
find $BACKUP_DIR -name "portfolio_backup_*.sql.gz" -mtime +30 -delete

echo "🧹 Cleaned up backups older than 30 days"
```

```bash
#!/bin/bash
# restore.sh

BACKUP_FILE=$1

if [ -z "$BACKUP_FILE" ]; then
    echo "❌ Error: Backup file not specified"
    echo "Usage: $0 <backup_file>"
    exit 1
fi

echo "🔄 Restoring database from backup: $BACKUP_FILE"

# Stop the application
echo "⏹️ Stopping application..."
docker-compose stop technology-portfolio-service

# Restore database
echo "🗄️ Restoring database..."
gunzip -c "$BACKUP_FILE" | docker exec -i postgres-portfolio psql -U $DB_USERNAME $DB_NAME

# Start the application
echo "▶️ Starting application..."
docker-compose start technology-portfolio-service

# Wait for service to be ready
echo "⏳ Waiting for service to be ready..."
sleep 30

# Health check
echo "🏥 Running health check..."
curl -f http://localhost:8083/actuator/health || {
    echo "❌ Service health check failed after restore"
    exit 1
}

echo "✅ Database restore completed successfully!"
```

### Log Management
```bash
#!/bin/bash
# log-management.sh

LOG_DIR="/var/log"
SERVICE_NAME="technology-portfolio-service"

echo "📝 Managing logs for $SERVICE_NAME"
echo "=================================="

# Rotate logs
echo "🔄 Rotating logs..."
logrotate -f /etc/logrotate.d/technology-portfolio-service

# Compress old logs
echo "🗜️ Compressing old logs..."
find $LOG_DIR -name "$SERVICE_NAME*.log.*" -mtime +1 -exec gzip {} \;

# Clean up old compressed logs (keep last 30 days)
echo "🧹 Cleaning up old compressed logs..."
find $LOG_DIR -name "$SERVICE_NAME*.log.*.gz" -mtime +30 -delete

# Check log disk usage
echo "💾 Checking log disk usage..."
du -sh $LOG_DIR/*$SERVICE_NAME*

echo "✅ Log management completed!"
```

## Troubleshooting

### Common Issues

#### 1. Service Won't Start
```bash
# Check logs
docker logs technology-portfolio-service

# Check environment variables
docker exec technology-portfolio-service env | grep -E "(DB_|JWT_|SPRING_)"

# Check database connectivity
docker exec postgres-portfolio pg_isready -U $DB_USERNAME -d $DB_NAME
```

#### 2. High Memory Usage
```bash
# Check JVM memory usage
curl http://localhost:8083/actuator/metrics/jvm.memory.used

# Check heap dump
curl http://localhost:8083/actuator/heapdump -o heapdump.hprof

# Analyze with jhat or MAT
jhat heapdump.hprof
```

#### 3. Database Connection Issues
```bash
# Check connection pool status
curl http://localhost:8083/actuator/metrics/r2dbc.connections.active

# Check database health
curl http://localhost:8083/actuator/health/db

# Test database connectivity
docker exec postgres-portfolio psql -U $DB_USERNAME -d $DB_NAME -c "SELECT version();"
```

#### 4. Performance Issues
```bash
# Check response times
curl http://localhost:8083/actuator/metrics/http.server.requests

# Check error rates
curl http://localhost:8083/actuator/metrics/http.server.requests | grep -E "(4..|5..)"

# Check CPU usage
curl http://localhost:8083/actuator/metrics/process.cpu.usage
```

### Performance Tuning

#### JVM Options
```bash
# Production JVM options
JAVA_OPTS="-Xms1g -Xmx2g -XX:+UseG1GC -XX:+UseContainerSupport -XX:MaxGCPauseMillis=200 -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap"
```

#### Database Tuning
```sql
-- PostgreSQL tuning for R2DBC
ALTER SYSTEM SET max_connections = 200;
ALTER SYSTEM SET shared_buffers = '256MB';
ALTER SYSTEM SET effective_cache_size = '1GB';
ALTER SYSTEM SET maintenance_work_mem = '64MB';
ALTER SYSTEM SET checkpoint_completion_target = 0.9;
ALTER SYSTEM SET wal_buffers = '16MB';
ALTER SYSTEM SET default_statistics_target = 100;
```

#### Connection Pool Tuning
```yaml
spring:
  r2dbc:
    pool:
      initial-size: 10
      max-size: 50
      max-idle-time: 60m
      validation-query: SELECT 1
      validation-depth: remote
```

## Security Considerations

### Network Security
- Use internal Docker networks
- Restrict external access to monitoring ports
- Implement proper firewall rules
- Use SSL/TLS for external communication

### Application Security
- Use non-root user in containers
- Implement proper JWT validation
- Enable CORS with specific origins
- Use rate limiting
- Implement proper input validation

### Database Security
- Use strong passwords
- Restrict database access
- Enable SSL connections
- Regular security updates
- Backup encryption

## Monitoring Dashboards

### Grafana Dashboard Setup
1. Import the provided dashboard JSON
2. Configure Prometheus data source
3. Set up alerting rules
4. Configure notification channels

### Key Metrics to Monitor
- HTTP request rate and response times
- Error rates and types
- Database connection pool usage
- JVM memory and GC metrics
- CPU and system resource usage
- Business metrics (portfolio operations)

### Alerting Thresholds
- Service down: Immediate
- Error rate > 1%: 2 minutes
- Response time > 2s: 2 minutes
- Memory usage > 80%: 5 minutes
- CPU usage > 80%: 5 minutes
- Connection pool > 80%: 2 minutes

## Conclusion

This deployment guide provides comprehensive instructions for deploying the reactive Technology Portfolio Service across different environments. The guide covers:

- Environment-specific configurations
- Docker containerization
- Monitoring and alerting setup
- Operational procedures
- Troubleshooting guidelines
- Security considerations

Following this guide ensures a robust, scalable, and maintainable deployment of the reactive service with proper monitoring and operational procedures in place. 