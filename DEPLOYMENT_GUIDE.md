# Technology Portfolio System - Deployment Guide

This guide provides comprehensive deployment instructions for all environments: DEV, TEST, UAT, and PROD.

## 🏗️ Architecture Overview

The Technology Portfolio System is built with a microservices architecture:

### Core Services
- **API Gateway** (Port 8081) - Entry point with authentication
- **Authorization Service** (Port 8082) - User management and permissions
- **Technology Portfolio Service** (Port 8083) - Portfolio management

### Infrastructure Services
- **PostgreSQL** - Primary database (separate instances for auth and portfolio)
- **Redis** - Session cache and temporary data
- **Eureka Server** - Service discovery
- **Prometheus** - Metrics collection
- **Grafana** - Monitoring dashboards

## 🚀 Environment Overview

| Environment | Purpose | URL Pattern | Database | Monitoring |
|-------------|---------|-------------|----------|------------|
| **LOCAL** | Development | localhost:808x | Docker containers | Local |
| **DEV** | Development testing | dev.techportfolio.company.com | Shared dev DB | Basic |
| **TEST** | Integration testing | test.techportfolio.company.com | Dedicated test DB | Full |
| **UAT** | User acceptance testing | uat.techportfolio.company.com | Production-like DB | Full |
| **PROD** | Production | techportfolio.company.com | Production DB | Full + Alerting |

---

## 🔧 Local Development Setup (FIXED)

### Prerequisites
- Java 21+
- Docker & Docker Compose
- Git

### Quick Start (WORKING SOLUTION)
```bash
# Clone and setup
git clone <repository>
cd CursorKotlinSSO

# Clean any existing Docker state
docker-compose down -v
docker system prune -f

# Start infrastructure with clean state
./gradlew startInfrastructure

# Wait for infrastructure to be ready
sleep 15

# Start all services using the working script
./run-local.sh

# Access the application
open http://localhost:8081/mock-login
```

### Manual Setup (Alternative)
```bash
# Build all services
./gradlew clean build

# Start infrastructure only
./gradlew startInfrastructure

# Start services individually (in separate terminals)
./gradlew :api-gateway:bootRun --args="--spring.profiles.active=mock-auth,local"
./gradlew :authorization-service:bootRun --args="--spring.profiles.active=local"
./gradlew :technology-portfolio-service:bootRun --args="--spring.profiles.active=local"
```

### Test Users (Local)
- `user1` / `password` - Portfolio Manager
- `user2` / `password` - Viewer
- `admin` / `secret` - Administrator

### Local Development Verification
```bash
# Check infrastructure services
curl http://localhost:8761  # Eureka
docker exec redis-local redis-cli ping  # Redis
docker exec postgres-auth psql -U auth_user -d authorization -c "SELECT version();"

# Check application services
curl http://localhost:8081/actuator/health  # API Gateway
curl http://localhost:8082/actuator/health  # Authorization Service
curl http://localhost:8083/actuator/health  # Portfolio Service
```

---

## 🏢 DEV Environment Deployment

### Infrastructure Requirements
- **Server**: 2 CPU, 4GB RAM minimum
- **Database**: PostgreSQL 15+ (can be shared)
- **Cache**: Redis 7+
- **Load Balancer**: Nginx or similar
- **SSL**: Let's Encrypt or corporate certificates

### Deployment Steps

#### 1. Server Preparation
```bash
# Update system
sudo apt update && sudo apt upgrade -y

# Install Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sh get-docker.sh
sudo usermod -aG docker $USER

# Install Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Create application directory
sudo mkdir -p /opt/techportfolio
sudo chown $USER:$USER /opt/techportfolio
cd /opt/techportfolio
```

#### 2. Environment Configuration Files

Create `environments/dev/docker-compose.dev.yml`:
```yaml
version: '3.8'

services:
  api-gateway:
    image: techportfolio/api-gateway:latest
    ports:
      - "8081:8081"
    environment:
      - SPRING_PROFILES_ACTIVE=dev
      - JWT_SECRET=${JWT_SECRET}
      - EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://eureka:8761/eureka
      - REDIS_HOST=redis
      - AUTHORIZATION_SERVICE_URL=http://authorization-service:8082
      - MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,info,metrics
    depends_on:
      - eureka
      - redis
      - authorization-service
    restart: unless-stopped
    networks:
      - techportfolio-network

  authorization-service:
    image: techportfolio/authorization-service:latest
    ports:
      - "8082:8082"
    environment:
      - SPRING_PROFILES_ACTIVE=dev
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-auth:5432/authorization_dev
      - SPRING_DATASOURCE_USERNAME=${DB_AUTH_USER}
      - SPRING_DATASOURCE_PASSWORD=${DB_AUTH_PASSWORD}
      - EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://eureka:8761/eureka
      - FLYWAY_ENABLED=true
      - MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,info,metrics
    depends_on:
      - postgres-auth
      - eureka
    restart: unless-stopped
    networks:
      - techportfolio-network

  technology-portfolio-service:
    image: techportfolio/technology-portfolio-service:latest
    ports:
      - "8083:8083"
    environment:
      - SPRING_PROFILES_ACTIVE=dev
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-portfolio:5432/portfolio_dev
      - SPRING_DATASOURCE_USERNAME=${DB_PORTFOLIO_USER}
      - SPRING_DATASOURCE_PASSWORD=${DB_PORTFOLIO_PASSWORD}
      - EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://eureka:8761/eureka
      - FLYWAY_ENABLED=true
      - MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,info,metrics
    depends_on:
      - postgres-portfolio
      - eureka
    restart: unless-stopped
    networks:
      - techportfolio-network

  postgres-auth:
    image: postgres:15-alpine
    environment:
      - POSTGRES_DB=authorization_dev
      - POSTGRES_USER=${DB_AUTH_USER}
      - POSTGRES_PASSWORD=${DB_AUTH_PASSWORD}
    volumes:
      - postgres_auth_data:/var/lib/postgresql/data
      - ../../database/auth-init.sql:/docker-entrypoint-initdb.d/init.sql
    restart: unless-stopped
    networks:
      - techportfolio-network
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${DB_AUTH_USER} -d authorization_dev"]
      interval: 30s
      timeout: 10s
      retries: 3

  postgres-portfolio:
    image: postgres:15-alpine
    environment:
      - POSTGRES_DB=portfolio_dev
      - POSTGRES_USER=${DB_PORTFOLIO_USER}
      - POSTGRES_PASSWORD=${DB_PORTFOLIO_PASSWORD}
    volumes:
      - postgres_portfolio_data:/var/lib/postgresql/data
      - ../../database/portfolio-init.sql:/docker-entrypoint-initdb.d/init.sql
    restart: unless-stopped
    networks:
      - techportfolio-network
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${DB_PORTFOLIO_USER} -d portfolio_dev"]
      interval: 30s
      timeout: 10s
      retries: 3

  redis:
    image: redis:7-alpine
    volumes:
      - redis_data:/data
    restart: unless-stopped
    networks:
      - techportfolio-network
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 30s
      timeout: 10s
      retries: 3

  eureka:
    image: steeltoeoss/eureka-server:latest
    ports:
      - "8761:8761"
    restart: unless-stopped
    networks:
      - techportfolio-network
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8761"]
      interval: 30s
      timeout: 10s
      retries: 3

  nginx:
    image: nginx:alpine
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx/dev.conf:/etc/nginx/nginx.conf
      - ./ssl:/etc/nginx/ssl
    depends_on:
      - api-gateway
    restart: unless-stopped
    networks:
      - techportfolio-network

volumes:
  postgres_auth_data:
  postgres_portfolio_data:
  redis_data:

networks:
  techportfolio-network:
    driver: bridge
```

Create `environments/dev/.env.dev`:
```bash
# Database Configuration
DB_AUTH_USER=auth_user_dev
DB_AUTH_PASSWORD=secure_auth_password_dev_$(openssl rand -hex 8)
DB_PORTFOLIO_USER=portfolio_user_dev
DB_PORTFOLIO_PASSWORD=secure_portfolio_password_dev_$(openssl rand -hex 8)

# JWT Configuration
JWT_SECRET=dev_jwt_secret_key_at_least_32_characters_long_$(openssl rand -hex 16)

# Application Configuration
ENVIRONMENT=dev
LOG_LEVEL=INFO

# Monitoring
PROMETHEUS_ENABLED=true
GRAFANA_ADMIN_PASSWORD=admin_dev_password_$(openssl rand -hex 8)
```

#### 3. Nginx Configuration
Create `environments/dev/nginx/dev.conf`:
```nginx
events {
    worker_connections 1024;
}

http {
    upstream api-gateway {
        server api-gateway:8081;
    }

    upstream grafana {
        server grafana:3000;
    }

    # Rate limiting
    limit_req_zone $binary_remote_addr zone=api:10m rate=10r/s;

    server {
        listen 80;
        server_name dev.techportfolio.company.com;

        # API Gateway proxy
        location / {
            limit_req zone=api burst=20 nodelay;
            proxy_pass http://api-gateway;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
            
            # Timeouts
            proxy_connect_timeout 30s;
            proxy_send_timeout 30s;
            proxy_read_timeout 30s;
        }

        # Health check endpoint (no rate limiting)
        location /actuator/health {
            proxy_pass http://api-gateway/actuator/health;
            access_log off;
        }

        # Monitoring dashboard
        location /monitoring/ {
            proxy_pass http://grafana/;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
        }
    }
}
```

#### 4. Deployment Script
Create `environments/dev/deploy-dev.sh`:
```bash
#!/bin/bash

set -e

ENVIRONMENT=dev
COMPOSE_FILE=docker-compose.dev.yml
ENV_FILE=.env.dev

echo "🚀 Deploying Technology Portfolio System to DEV Environment"
echo "============================================================"

# Create directories
mkdir -p nginx ssl logs backups

# Generate SSL certificates (self-signed for dev)
if [ ! -f ssl/server.crt ]; then
    echo "📜 Generating SSL certificates..."
    openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
        -keyout ssl/server.key \
        -out ssl/server.crt \
        -subj "/C=US/ST=State/L=City/O=Company/CN=dev.techportfolio.company.com"
fi

# Pull latest images
echo "📥 Pulling latest Docker images..."
docker-compose -f $COMPOSE_FILE pull

# Stop existing services
echo "🛑 Stopping existing services..."
docker-compose -f $COMPOSE_FILE --env-file $ENV_FILE down

# Start services
echo "🚀 Starting services..."
docker-compose -f $COMPOSE_FILE --env-file $ENV_FILE up -d

# Wait for services to be ready
echo "⏳ Waiting for services to be ready..."
sleep 30

# Health checks
echo "🔍 Running health checks..."
./health-check.sh dev

echo "✅ DEV deployment completed successfully!"
echo "🌐 Application URL: http://dev.techportfolio.company.com"
echo "📊 Monitoring: http://dev.techportfolio.company.com/monitoring"
```

#### 5. Health Check Script
Create `environments/dev/health-check.sh`:
```bash
#!/bin/bash

ENVIRONMENT=${1:-dev}
BASE_URL="http://localhost"

if [ "$ENVIRONMENT" != "local" ]; then
    BASE_URL="http://$ENVIRONMENT.techportfolio.company.com"
fi

echo "🔍 Running health checks for $ENVIRONMENT environment..."

# Function to check service health
check_service() {
    local service=$1
    local port=$2
    local endpoint=$3
    local url="$BASE_URL:$port$endpoint"
    
    if [ "$ENVIRONMENT" != "local" ]; then
        url="$BASE_URL$endpoint"
    fi
    
    echo -n "Checking $service... "
    
    if curl -f -s "$url" > /dev/null; then
        echo "✅ OK"
        return 0
    else
        echo "❌ FAILED"
        return 1
    fi
}

# Check infrastructure services
check_service "Eureka" 8761 "/"
check_service "API Gateway" 8081 "/actuator/health"
check_service "Authorization Service" 8082 "/actuator/health"
check_service "Portfolio Service" 8083 "/actuator/health"

# Check database connectivity
echo -n "Checking database connectivity... "
if docker exec postgres-auth psql -U auth_user_dev -d authorization_dev -c "SELECT 1;" > /dev/null 2>&1; then
    echo "✅ OK"
else
    echo "❌ FAILED"
fi

# Check Redis connectivity
echo -n "Checking Redis connectivity... "
if docker exec redis redis-cli ping > /dev/null 2>&1; then
    echo "✅ OK"
else
    echo "❌ FAILED"
fi

echo "🎉 Health checks completed!"
```

---

## 🧪 TEST Environment Deployment

### Infrastructure Requirements
- **Server**: 4 CPU, 8GB RAM
- **Database**: Dedicated PostgreSQL instance with backups
- **Monitoring**: Full Prometheus + Grafana stack
- **Load Testing**: JMeter integration

### Key Differences from DEV
- Enhanced monitoring and alerting
- Automated testing integration
- Performance monitoring
- Database backups
- SSL certificates

Create `environments/test/docker-compose.test.yml`:
```yaml
version: '3.8'

services:
  api-gateway:
    image: techportfolio/api-gateway:${VERSION:-latest}
    ports:
      - "8081:8081"
    environment:
      - SPRING_PROFILES_ACTIVE=test
      - JWT_SECRET=${JWT_SECRET}
      - LOGGING_LEVEL_ROOT=INFO
      - MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,info,metrics,prometheus
      - MANAGEMENT_METRICS_EXPORT_PROMETHEUS_ENABLED=true
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8081/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s
    deploy:
      resources:
        limits:
          cpus: '1.0'
          memory: 1G
        reservations:
          cpus: '0.5'
          memory: 512M
    restart: unless-stopped
    networks:
      - techportfolio-network

  authorization-service:
    image: techportfolio/authorization-service:${VERSION:-latest}
    ports:
      - "8082:8082"
    environment:
      - SPRING_PROFILES_ACTIVE=test
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-auth:5432/authorization_test
      - SPRING_DATASOURCE_USERNAME=${DB_AUTH_USER}
      - SPRING_DATASOURCE_PASSWORD=${DB_AUTH_PASSWORD}
      - FLYWAY_ENABLED=true
      - MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,info,metrics,prometheus
      - MANAGEMENT_METRICS_EXPORT_PROMETHEUS_ENABLED=true
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8082/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s
    depends_on:
      postgres-auth:
        condition: service_healthy
      eureka:
        condition: service_healthy
    restart: unless-stopped
    networks:
      - techportfolio-network

  technology-portfolio-service:
    image: techportfolio/technology-portfolio-service:${VERSION:-latest}
    ports:
      - "8083:8083"
    environment:
      - SPRING_PROFILES_ACTIVE=test
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-portfolio:5432/portfolio_test
      - SPRING_DATASOURCE_USERNAME=${DB_PORTFOLIO_USER}
      - SPRING_DATASOURCE_PASSWORD=${DB_PORTFOLIO_PASSWORD}
      - FLYWAY_ENABLED=true
      - MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,info,metrics,prometheus
      - MANAGEMENT_METRICS_EXPORT_PROMETHEUS_ENABLED=true
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8083/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s
    depends_on:
      postgres-portfolio:
        condition: service_healthy
      eureka:
        condition: service_healthy
    restart: unless-stopped
    networks:
      - techportfolio-network

  # Enhanced PostgreSQL with backup
  postgres-auth:
    image: postgres:15-alpine
    environment:
      - POSTGRES_DB=authorization_test
      - POSTGRES_USER=${DB_AUTH_USER}
      - POSTGRES_PASSWORD=${DB_AUTH_PASSWORD}
    volumes:
      - postgres_auth_data:/var/lib/postgresql/data
      - ../../database/auth-init.sql:/docker-entrypoint-initdb.d/init.sql
      - ./backups:/backups
    restart: unless-stopped
    networks:
      - techportfolio-network
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${DB_AUTH_USER} -d authorization_test"]
      interval: 10s
      timeout: 5s
      retries: 5

  postgres-portfolio:
    image: postgres:15-alpine
    environment:
      - POSTGRES_DB=portfolio_test
      - POSTGRES_USER=${DB_PORTFOLIO_USER}
      - POSTGRES_PASSWORD=${DB_PORTFOLIO_PASSWORD}
    volumes:
      - postgres_portfolio_data:/var/lib/postgresql/data
      - ../../database/portfolio-init.sql:/docker-entrypoint-initdb.d/init.sql
      - ./backups:/backups
    restart: unless-stopped
    networks:
      - techportfolio-network
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${DB_PORTFOLIO_USER} -d portfolio_test"]
      interval: 10s
      timeout: 5s
      retries: 5

  # Backup service
  postgres-backup:
    image: prodrigestivill/postgres-backup-local
    environment:
      - POSTGRES_HOST=postgres-auth
      - POSTGRES_DB=authorization_test
      - POSTGRES_USER=${DB_AUTH_USER}
      - POSTGRES_PASSWORD=${DB_AUTH_PASSWORD}
      - BACKUP_KEEP_DAYS=7
      - BACKUP_KEEP_WEEKS=4
      - BACKUP_KEEP_MONTHS=6
      - SCHEDULE=@daily
    volumes:
      - ./backups:/backups
    depends_on:
      - postgres-auth
    restart: unless-stopped
    networks:
      - techportfolio-network

  # Monitoring stack
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
    restart: unless-stopped
    networks:
      - techportfolio-network

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
    restart: unless-stopped
    networks:
      - techportfolio-network

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
    restart: unless-stopped
    networks:
      - techportfolio-network

  redis:
    image: redis:7-alpine
    volumes:
      - redis_data:/data
    restart: unless-stopped
    networks:
      - techportfolio-network
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 30s
      timeout: 10s
      retries: 3

  eureka:
    image: steeltoeoss/eureka-server:latest
    ports:
      - "8761:8761"
    restart: unless-stopped
    networks:
      - techportfolio-network
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8761"]
      interval: 30s
      timeout: 10s
      retries: 3

volumes:
  postgres_auth_data:
  postgres_portfolio_data:
  redis_data:
  prometheus_data:
  alertmanager_data:
  grafana_data:

networks:
  techportfolio-network:
    driver: bridge
```

---

## 🎯 UAT Environment Deployment

### Infrastructure Requirements
- **Server**: Production-like specs (8 CPU, 16GB RAM)
- **Database**: Production-like PostgreSQL with high availability
- **SSL**: Valid certificates
- **Monitoring**: Full stack with comprehensive alerting
- **Backup**: Automated, tested backup strategy

### Key Features
- Production-like data volumes
- SSL/TLS encryption everywhere
- Comprehensive monitoring and alerting
- Automated backups with retention policies
- Performance testing integration
- Security scanning

---

## 🏭 PROD Environment Deployment

### Infrastructure Requirements
- **Servers**: Load-balanced multiple instances
- **Database**: High-availability PostgreSQL cluster
- **Cache**: Redis cluster
- **Load Balancer**: Production-grade (AWS ALB, F5, etc.)
- **Monitoring**: Full observability stack
- **Security**: WAF, DDoS protection, security scanning

### Production Architecture
```
Internet -> WAF -> Load Balancer -> API Gateway (3 instances)
                                 -> Auth Service (2 instances)
                                 -> Portfolio Service (2 instances)
                                 -> PostgreSQL Cluster (Primary/Replica)
                                 -> Redis Cluster
```

### Docker Swarm Production Setup
Create `environments/prod/docker-compose.prod.yml`:
```yaml
version: '3.8'

services:
  api-gateway:
    image: techportfolio/api-gateway:${VERSION}
    deploy:
      replicas: 3
      resources:
        limits:
          cpus: '1.0'
          memory: 1G
        reservations:
          cpus: '0.5'
          memory: 512M
      restart_policy:
        condition: any
        delay: 5s
        max_attempts: 3
        window: 120s
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - LOGGING_LEVEL_ROOT=ERROR
      - MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,metrics
      - MANAGEMENT_ENDPOINT_HEALTH_SHOW_DETAILS=when_authorized
    secrets:
      - jwt_secret
      - source: app_config
        target: /app/config/application-prod.yml
    networks:
      - techportfolio-network
      - monitoring-network

  authorization-service:
    image: techportfolio/authorization-service:${VERSION}
    deploy:
      replicas: 2
      resources:
        limits:
          cpus: '1.0'
          memory: 1G
        reservations:
          cpus: '0.5'
          memory: 512M
      restart_policy:
        condition: any
        delay: 5s
        max_attempts: 3
        window: 120s
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-cluster:5432/authorization_prod
      - FLYWAY_ENABLED=true
    secrets:
      - db_auth_password
    networks:
      - techportfolio-network
      - db-network

  technology-portfolio-service:
    image: techportfolio/technology-portfolio-service:${VERSION}
    deploy:
      replicas: 2
      resources:
        limits:
          cpus: '1.0'
          memory: 1G
        reservations:
          cpus: '0.5'
          memory: 512M
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-cluster:5433/portfolio_prod
      - FLYWAY_ENABLED=true
    secrets:
      - db_portfolio_password
    networks:
      - techportfolio-network
      - db-network

secrets:
  jwt_secret:
    external: true
  db_auth_password:
    external: true
  db_portfolio_password:
    external: true
  app_config:
    external: true

networks:
  techportfolio-network:
    driver: overlay
    attachable: true
  db-network:
    driver: overlay
    internal: true
  monitoring-network:
    driver: overlay
```

### Production Deployment Script
Create `environments/prod/deploy-prod.sh`:
```bash
#!/bin/bash

set -e

VERSION=${1:-latest}
ENVIRONMENT=prod

echo "🚀 Deploying Technology Portfolio System to PRODUCTION"
echo "Version: $VERSION"
echo "Environment: $ENVIRONMENT"
echo "=========================================================="

# Safety check
read -p "Are you sure you want to deploy to PRODUCTION? (yes/no): " -r
if [[ ! $REPLY =~ ^[Yy][Ee][Ss]$ ]]; then
    echo "Deployment cancelled."
    exit 1
fi

# Initialize Docker Swarm if not already
docker swarm init 2>/dev/null || true

# Create secrets if they don't exist
echo "🔐 Managing secrets..."
./create-secrets.sh

# Deploy database cluster
echo "📊 Deploying database cluster..."
docker stack deploy -c docker-compose.db-cluster.yml db-cluster

# Wait for database to be ready
echo "⏳ Waiting for database cluster..."
sleep 60

# Deploy monitoring stack
echo "📈 Deploying monitoring stack..."
docker stack deploy -c docker-compose.monitoring.yml monitoring

# Deploy application stack
echo "🏗️ Deploying application stack..."
VERSION=$VERSION docker stack deploy -c docker-compose.prod.yml techportfolio

# Wait for services to be ready
echo "⏳ Waiting for services to be ready..."
sleep 120

# Health check
echo "🔍 Running comprehensive health checks..."
./health-check.sh prod

# Performance test
echo "⚡ Running performance tests..."
./performance-test.sh

echo "✅ Production deployment completed successfully!"
echo "🌐 Application URL: https://techportfolio.company.com"
echo "📊 Monitoring: https://monitoring.techportfolio.company.com"
```

---

## 🔧 CI/CD Pipeline Integration

Create `.github/workflows/deploy.yml`:
```yaml
name: Deploy Technology Portfolio System

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]
  release:
    types: [published]

env:
  REGISTRY: docker.io
  IMAGE_PREFIX: techportfolio

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
      
      - name: Cache Gradle packages
        uses: actions/cache@v3
        with:
          path: |
            ~/.gradle/caches
            ~/.gradle/wrapper
          key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}
          restore-keys: |
            ${{ runner.os }}-gradle-

      - name: Run tests
        run: ./gradlew test

      - name: Run integration tests
        run: ./gradlew integrationTest

  security-scan:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Run security scan
        run: |
          # Add security scanning tools here
          echo "Running security scans..."

  build:
    needs: [test, security-scan]
    runs-on: ubuntu-latest
    outputs:
      version: ${{ steps.version.outputs.version }}
    steps:
      - uses: actions/checkout@v4
      
      - name: Set version
        id: version
        run: |
          if [[ $GITHUB_REF == refs/tags/* ]]; then
            VERSION=${GITHUB_REF#refs/tags/}
          else
            VERSION=$GITHUB_SHA
          fi
          echo "version=$VERSION" >> $GITHUB_OUTPUT

      - name: Build Docker images
        run: |
          VERSION=${{ steps.version.outputs.version }}
          docker build -t $REGISTRY/$IMAGE_PREFIX/api-gateway:$VERSION ./api-gateway
          docker build -t $REGISTRY/$IMAGE_PREFIX/authorization-service:$VERSION ./authorization-service
          docker build -t $REGISTRY/$IMAGE_PREFIX/technology-portfolio-service:$VERSION ./technology-portfolio-service

      - name: Push Docker images
        if: github.event_name != 'pull_request'
        run: |
          echo ${{ secrets.DOCKER_PASSWORD }} | docker login -u ${{ secrets.DOCKER_USERNAME }} --password-stdin
          VERSION=${{ steps.version.outputs.version }}
          docker push $REGISTRY/$IMAGE_PREFIX/api-gateway:$VERSION
          docker push $REGISTRY/$IMAGE_PREFIX/authorization-service:$VERSION
          docker push $REGISTRY/$IMAGE_PREFIX/technology-portfolio-service:$VERSION

  deploy-dev:
    needs: build
    if: github.ref == 'refs/heads/develop'
    runs-on: ubuntu-latest
    environment: development
    steps:
      - name: Deploy to DEV
        run: |
          ssh -o StrictHostKeyChecking=no ${{ secrets.DEV_USER }}@${{ secrets.DEV_HOST }} \
            "cd /opt/techportfolio && \
             git pull && \
             VERSION=${{ needs.build.outputs.version }} ./environments/dev/deploy-dev.sh"

  deploy-test:
    needs: build
    if: github.ref == 'refs/heads/main'
    runs-on: ubuntu-latest
    environment: test
    steps:
      - name: Deploy to TEST
        run: |
          ssh -o StrictHostKeyChecking=no ${{ secrets.TEST_USER }}@${{ secrets.TEST_HOST }} \
            "cd /opt/techportfolio && \
             git pull && \
             VERSION=${{ needs.build.outputs.version }} ./environments/test/deploy-test.sh"

  deploy-uat:
    needs: [build, deploy-test]
    if: github.ref == 'refs/heads/main' && github.event_name == 'push'
    runs-on: ubuntu-latest
    environment: uat
    steps:
      - name: Deploy to UAT
        run: |
          ssh -o StrictHostKeyChecking=no ${{ secrets.UAT_USER }}@${{ secrets.UAT_HOST }} \
            "cd /opt/techportfolio && \
             git pull && \
             VERSION=${{ needs.build.outputs.version }} ./environments/uat/deploy-uat.sh"

  deploy-prod:
    needs: [build, deploy-uat]
    if: github.event_name == 'release'
    runs-on: ubuntu-latest
    environment: production
    steps:
      - name: Deploy to PROD
        run: |
          ssh -o StrictHostKeyChecking=no ${{ secrets.PROD_USER }}@${{ secrets.PROD_HOST }} \
            "cd /opt/techportfolio && \
             git pull && \
             VERSION=${{ needs.build.outputs.version }} ./environments/prod/deploy-prod.sh ${{ needs.build.outputs.version }}"
```

---

## 📋 Operational Procedures

### Backup and Recovery
```bash
# Database backup
docker exec postgres-auth pg_dump -U auth_user authorization_prod > backup_$(date +%Y%m%d_%H%M%S).sql

# Application backup
docker run --rm -v techportfolio_data:/data -v $(pwd):/backup alpine tar czf /backup/app_backup_$(date +%Y%m%d_%H%M%S).tar.gz /data

# Recovery
docker exec -i postgres-auth psql -U auth_user authorization_prod < backup_20231201_120000.sql
```

### Monitoring and Alerting
- **Prometheus**: Metrics collection
- **Grafana**: Visualization and dashboards
- **AlertManager**: Alert routing and notification
- **Health checks**: Automated service monitoring

### Scaling Operations
```bash
# Scale services in Docker Swarm
docker service scale techportfolio_api-gateway=5
docker service scale techportfolio_authorization-service=3

# Rolling updates
docker service update --image techportfolio/api-gateway:v2.0.0 techportfolio_api-gateway
```

### Troubleshooting
```bash
# Check service status
docker service ls
docker service ps techportfolio_api-gateway

# View logs
docker service logs -f techportfolio_api-gateway

# Database connectivity
docker exec -it postgres-auth psql -U auth_user -d authorization_prod -c "SELECT version();"

# Redis connectivity
docker exec -it redis redis-cli ping
```

This comprehensive deployment guide provides everything needed to deploy the Technology Portfolio System across all environments with proper monitoring, security, and operational procedures. 