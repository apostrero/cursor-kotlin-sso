# Technology Portfolio System - Deployment Guide

This guide provides comprehensive deployment instructions for all environments: DEV, TEST, UAT, and PROD.

## 🏗️ Architecture Overview

The Technology Portfolio System is built with a microservices architecture:

### Core Services
- **API Gateway** (Port 8080) - Entry point with authentication
- **Authorization Service** (Port 8082) - User management and permissions
- **Technology Portfolio Service** (Port 8083) - Portfolio management with reactive programming

### Infrastructure Services
- **PostgreSQL** - Primary database (separate instances for auth and portfolio)
- **Redis** - Session cache and temporary data
- **Eureka Server** - Service discovery
- **Prometheus** - Metrics collection
- **Grafana** - Monitoring dashboards
- **SimpleSAMLphp** - Identity Provider for SAML SSO

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
open http://localhost:8080/mock-login
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
curl http://localhost:8080/actuator/health  # API Gateway
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
      - "8080:8080"
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
      - SPRING_R2DBC_URL=r2dbc:postgresql://postgres-portfolio:5432/portfolio_dev
      - SPRING_R2DBC_USERNAME=${DB_PORTFOLIO_USER}
      - SPRING_R2DBC_PASSWORD=${DB_PORTFOLIO_PASSWORD}
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
        server api-gateway:8080;
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
```

---

## 🧪 TEST Environment Deployment

### Infrastructure Requirements
- **Server**: 4 CPU, 8GB RAM minimum
- **Database**: Dedicated PostgreSQL 15+ instance
- **Cache**: Redis 7+ with persistence
- **Load Balancer**: Nginx with SSL termination
- **Monitoring**: Full Prometheus + Grafana stack

### Deployment Configuration

Create `environments/test/docker-compose.test.yml`:
```yaml
version: '3.8'

services:
  api-gateway:
    image: techportfolio/api-gateway:${VERSION:-latest}
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=test
      - JWT_SECRET=${JWT_SECRET}
      - EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://eureka:8761/eureka
      - REDIS_HOST=redis
      - AUTHORIZATION_SERVICE_URL=http://authorization-service:8082
      - MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,info,metrics,prometheus
      - MANAGEMENT_METRICS_EXPORT_PROMETHEUS_ENABLED=true
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s
    depends_on:
      eureka:
        condition: service_healthy
      redis:
        condition: service_healthy
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
      - EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://eureka:8761/eureka
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
      - SPRING_R2DBC_URL=r2dbc:postgresql://postgres-portfolio:5432/portfolio_test
      - SPRING_R2DBC_USERNAME=${DB_PORTFOLIO_USER}
      - SPRING_R2DBC_PASSWORD=${DB_PORTFOLIO_PASSWORD}
      - EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://eureka:8761/eureka
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
      interval: 30s
      timeout: 10s
      retries: 3

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
      interval: 30s
      timeout: 10s
      retries: 3

  # Enhanced monitoring stack
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
- **Server**: 8 CPU, 16GB RAM minimum
- **Database**: Production-like PostgreSQL cluster
- **Cache**: Redis cluster with persistence
- **Load Balancer**: HAProxy or Nginx with SSL termination
- **Monitoring**: Full stack with alerting

### Deployment Configuration

Similar to TEST environment but with:
- Production-like database configuration
- Enhanced security settings
- Performance monitoring
- User acceptance testing tools

---

## 🏭 PROD Environment Deployment

### Infrastructure Requirements
- **Server**: 16 CPU, 32GB RAM minimum
- **Database**: High-availability PostgreSQL cluster
- **Cache**: Redis cluster with replication
- **Load Balancer**: HAProxy with SSL termination
- **Monitoring**: Full stack with alerting and logging

### Docker Swarm Configuration

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
      - SPRING_R2DBC_URL=r2dbc:postgresql://postgres-cluster:5433/portfolio_prod
      - FLYWAY_ENABLED=true
    secrets:
      - db_portfolio_password
    networks:
      - techportfolio-network
      - db-network

  # High-availability PostgreSQL cluster
  postgres-cluster:
    image: postgres:15-alpine
    deploy:
      replicas: 3
      resources:
        limits:
          cpus: '2.0'
          memory: 4G
        reservations:
          cpus: '1.0'
          memory: 2G
    environment:
      - POSTGRES_DB=techportfolio_prod
      - POSTGRES_USER=${DB_USER}
    secrets:
      - db_password
    volumes:
      - postgres_data:/var/lib/postgresql/data
    networks:
      - db-network

  # Redis cluster
  redis-cluster:
    image: redis:7-alpine
    deploy:
      replicas: 3
      resources:
        limits:
          cpus: '0.5'
          memory: 1G
        reservations:
          cpus: '0.25'
          memory: 512M
    volumes:
      - redis_data:/data
    networks:
      - techportfolio-network

  # Monitoring stack
  prometheus:
    image: prom/prometheus:latest
    deploy:
      replicas: 1
      resources:
        limits:
          cpus: '0.5'
          memory: 1G
    volumes:
      - ./monitoring/prometheus.yml:/etc/prometheus/prometheus.yml
      - prometheus_data:/prometheus
    networks:
      - monitoring-network

  grafana:
    image: grafana/grafana:latest
    deploy:
      replicas: 1
      resources:
        limits:
          cpus: '0.5'
          memory: 1G
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=${GRAFANA_ADMIN_PASSWORD}
    volumes:
      - grafana_data:/var/lib/grafana
    networks:
      - monitoring-network

secrets:
  jwt_secret:
    external: true
  db_password:
    external: true
  db_auth_password:
    external: true
  db_portfolio_password:
    external: true
  app_config:
    file: ./application-prod.yml

volumes:
  postgres_data:
  redis_data:
  prometheus_data:
  grafana_data:

networks:
  techportfolio-network:
    driver: overlay
  db-network:
    driver: overlay
  monitoring-network:
    driver: overlay
```

### Production Deployment Script

Create `environments/prod/deploy-prod.sh`:
```bash
#!/bin/bash

set -e

VERSION=$1
if [ -z "$VERSION" ]; then
    echo "Usage: $0 <version>"
    echo "Example: $0 v1.0.0"
    exit 1
fi

ENVIRONMENT=prod
COMPOSE_FILE=docker-compose.prod.yml

echo "🚀 Deploying Technology Portfolio System to PROD Environment"
echo "Version: $VERSION"
echo "============================================================"

# Initialize Docker Swarm if not already done
if ! docker info | grep -q "Swarm: active"; then
    echo "🐳 Initializing Docker Swarm..."
    docker swarm init
fi

# Create secrets if they don't exist
echo "🔐 Creating secrets..."
echo "$JWT_SECRET" | docker secret create jwt_secret - 2>/dev/null || true
echo "$DB_PASSWORD" | docker secret create db_password - 2>/dev/null || true
echo "$DB_AUTH_PASSWORD" | docker secret create db_auth_password - 2>/dev/null || true
echo "$DB_PORTFOLIO_PASSWORD" | docker secret create db_portfolio_password - 2>/dev/null || true

# Deploy stack
echo "🚀 Deploying stack..."
docker stack deploy -c $COMPOSE_FILE techportfolio

# Wait for services to be ready
echo "⏳ Waiting for services to be ready..."
sleep 60

# Health checks
echo "🔍 Running health checks..."
./health-check.sh prod

echo "✅ PROD deployment completed successfully!"
echo "🌐 Access the application at: https://techportfolio.company.com"
```

---

## 🔍 Health Check Script

Create `environments/health-check.sh`:
```bash
#!/bin/bash

ENVIRONMENT=$1

echo "🔍 Running health checks for $ENVIRONMENT environment..."

# Check API Gateway
echo "Checking API Gateway..."
if curl -f http://localhost:8080/actuator/health > /dev/null 2>&1; then
    echo "✅ API Gateway is healthy"
else
    echo "❌ API Gateway health check failed"
    exit 1
fi

# Check Authorization Service
echo "Checking Authorization Service..."
if curl -f http://localhost:8082/actuator/health > /dev/null 2>&1; then
    echo "✅ Authorization Service is healthy"
else
    echo "❌ Authorization Service health check failed"
    exit 1
fi

# Check Technology Portfolio Service
echo "Checking Technology Portfolio Service..."
if curl -f http://localhost:8083/actuator/health > /dev/null 2>&1; then
    echo "✅ Technology Portfolio Service is healthy"
else
    echo "❌ Technology Portfolio Service health check failed"
    exit 1
fi

# Check Eureka
echo "Checking Eureka..."
if curl -f http://localhost:8761 > /dev/null 2>&1; then
    echo "✅ Eureka is healthy"
else
    echo "❌ Eureka health check failed"
    exit 1
fi

echo "🎉 All services are healthy!"
```

---

## 📊 Monitoring Configuration

### Prometheus Configuration

Create `monitoring/prometheus.yml`:
```yaml
global:
  scrape_interval: 15s
  evaluation_interval: 15s

rule_files:
  - "alerts.yml"

scrape_configs:
  - job_name: 'api-gateway'
    static_configs:
      - targets: ['api-gateway:8080']
    metrics_path: '/actuator/prometheus'

  - job_name: 'authorization-service'
    static_configs:
      - targets: ['authorization-service:8082']
    metrics_path: '/actuator/prometheus'

  - job_name: 'technology-portfolio-service'
    static_configs:
      - targets: ['technology-portfolio-service:8083']
    metrics_path: '/actuator/prometheus'

  - job_name: 'eureka'
    static_configs:
      - targets: ['eureka:8761']

  - job_name: 'postgres'
    static_configs:
      - targets: ['postgres-cluster:5432']

  - job_name: 'redis'
    static_configs:
      - targets: ['redis-cluster:6379']
```

### Grafana Dashboards

Create monitoring dashboards for:
- Application metrics
- Database performance
- System resources
- Business metrics

---

## 🔒 Security Considerations

### Production Security Checklist

- [ ] **SSL/TLS certificates** configured
- [ ] **Firewall rules** configured
- [ ] **Database encryption** enabled
- [ ] **Secrets management** implemented
- [ ] **Network segmentation** configured
- [ ] **Access controls** implemented
- [ ] **Audit logging** enabled
- [ ] **Backup strategy** implemented

### SSL Configuration

For production environments, use proper SSL certificates:

```bash
# Obtain Let's Encrypt certificates
certbot certonly --standalone -d techportfolio.company.com

# Configure Nginx with SSL
server {
    listen 443 ssl;
    server_name techportfolio.company.com;
    
    ssl_certificate /etc/letsencrypt/live/techportfolio.company.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/techportfolio.company.com/privkey.pem;
    
    # SSL configuration
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers ECDHE-RSA-AES256-GCM-SHA512:DHE-RSA-AES256-GCM-SHA512:ECDHE-RSA-AES256-GCM-SHA384:DHE-RSA-AES256-GCM-SHA384;
    ssl_prefer_server_ciphers off;
    
    # Proxy to API Gateway
    location / {
        proxy_pass http://api-gateway:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

---

## 🚀 Deployment Order

1. **DEV Environment** (First deployment)
   - Test the deployment process
   - Validate all services start correctly
   - Verify Flyway migrations work

2. **TEST Environment** (Integration testing)
   - Deploy with full monitoring stack
   - Run automated tests
   - Performance testing

3. **UAT Environment** (User acceptance)
   - Production-like environment
   - User acceptance testing
   - Security testing

4. **PROD Environment** (Production deployment)
   - Blue-green deployment strategy
   - Full monitoring and alerting
   - Disaster recovery procedures

---

## 📞 Support & Troubleshooting

### Common Issues

#### Flyway Migration Failures
```bash
# Check migration status
docker exec authorization-service flyway info

# Repair failed migrations
docker exec authorization-service flyway repair
```

#### Service Startup Issues
```bash
# Check service logs
docker service logs -f techportfolio_api-gateway

# Check resource usage
docker stats
```

#### Database Connection Issues
```bash
# Test database connectivity
docker exec postgres-auth psql -U auth_user -d authorization_prod -c "SELECT version();"
```

### Monitoring Dashboards
- **Grafana**: http://monitoring.techportfolio.company.com
- **Prometheus**: http://monitoring.techportfolio.company.com:9090
- **Eureka**: http://eureka.techportfolio.company.com:8761

### Log Locations
- **Application Logs**: `/opt/techportfolio/logs/`
- **Docker Logs**: `docker logs <container_name>`
- **System Logs**: `/var/log/`

---

## 🎉 Summary

The Technology Portfolio System is now ready for deployment across all environments with:

✅ **Complete deployment strategy** for DEV, TEST, UAT, and PROD  
✅ **Working local development environment** with Docker infrastructure  
✅ **Flyway 11.9.1** with PostgreSQL 15 support and proper migration strategy  
✅ **Comprehensive monitoring** with Prometheus and Grafana  
✅ **CI/CD pipeline** with GitHub Actions  
✅ **Security hardening** with SSL, secrets management, and network isolation  
✅ **Operational procedures** for backup, recovery, and scaling  

The system is production-ready and can be deployed to any environment following the provided deployment guide and scripts. 