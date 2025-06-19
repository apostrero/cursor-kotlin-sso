# Gradle-Based Local Development Guide

This guide explains how to run the entire Technology Portfolio System locally using Gradle instead of Docker. This approach is ideal for development, debugging, and testing.

## 🚀 Quick Start

### Option 1: Simple Script (Recommended)
```bash
# Start all services
./run-local.sh

# Stop all services
./stop-local.sh

# Stop services and infrastructure
./stop-local.sh --infrastructure
```

### Option 2: Manual Gradle Commands
```bash
# Start infrastructure
./gradlew startInfrastructure

# Start individual services (in separate terminals)
./gradlew runApiGateway
./gradlew runAuthorizationService  
./gradlew runPortfolioService
```

## 📋 Prerequisites

### Required
- **Java 21+** - The project uses modern Java features
- **Gradle 8.5+** - Included via Gradle Wrapper (`./gradlew`)

### Optional (for infrastructure)
- **Docker** - For PostgreSQL, Redis, and Eureka (if not running separately)
- **PostgreSQL 15+** - If running database manually
- **Redis 7+** - If running cache manually

## 🏗️ System Architecture

The local development setup includes:

### Infrastructure Services
- **Eureka Server** (port 8761) - Service Discovery
- **PostgreSQL** (port 5432) - Authorization Database
- **PostgreSQL** (port 5433) - Portfolio Database  
- **Redis** (port 6379) - Session Cache

### Application Services
- **API Gateway** (port 8081) - Main entry point with mock authentication
- **Authorization Service** (port 8082) - User permissions and roles
- **Technology Portfolio Service** (port 8083) - Portfolio management

## 🔧 Detailed Setup

### 1. Infrastructure Setup

#### Option A: Using Docker (Recommended)
```bash
# Start all infrastructure services
./gradlew startInfrastructure

# Stop all infrastructure services
./gradlew stopInfrastructure
```

#### Option B: Manual Setup
If you prefer to run services manually:

**PostgreSQL (Authorization DB)**
```bash
# Create database
createdb authorization
psql authorization -c "CREATE USER auth_user WITH PASSWORD 'auth_password';"
psql authorization -c "GRANT ALL PRIVILEGES ON DATABASE authorization TO auth_user;"
```

**PostgreSQL (Portfolio DB)**
```bash
# Create database  
createdb portfolio
psql portfolio -c "CREATE USER portfolio_user WITH PASSWORD 'portfolio_password';"
psql portfolio -c "GRANT ALL PRIVILEGES ON DATABASE portfolio TO portfolio_user;"
```

**Redis**
```bash
# Start Redis
redis-server --port 6379
```

**Eureka Server**
```bash
# Run via Docker
docker run -d --name eureka-local -p 8761:8761 steeltoeoss/eureka-server
```

### 2. Application Services

#### Build All Services
```bash
./gradlew clean build -x test
```

#### Start Services Individually

**API Gateway**
```bash
./gradlew runApiGateway
```

**Authorization Service**
```bash
./gradlew runAuthorizationService
```

**Portfolio Service**
```bash
./gradlew runPortfolioService
```

## 🔑 Authentication & Testing

### Mock Authentication
The system includes mock authentication for development:

**Test Users:**
- `user1` / `password` - Portfolio Manager
- `user2` / `password` - Viewer  
- `admin` / `secret` - Administrator

### API Testing Examples

**Login**
```bash
curl -X POST http://localhost:8081/api/auth/mock-login \
  -H 'Content-Type: application/json' \
  -d '{"username": "user1", "password": "password"}'
```

**Get User Info**
```bash
curl http://localhost:8081/api/auth/mock-users
```

**Test Protected Endpoint**
```bash
# First login to get token
TOKEN=$(curl -s -X POST http://localhost:8081/api/auth/mock-login \
  -H 'Content-Type: application/json' \
  -d '{"username": "user1", "password": "password"}' | jq -r '.token')

# Use token for protected requests
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8081/api/portfolios
```

## 📊 Monitoring & Debugging

### Service URLs
- **Mock Login Page**: http://localhost:8081/mock-login
- **API Gateway**: http://localhost:8081
- **Authorization Service**: http://localhost:8082  
- **Portfolio Service**: http://localhost:8083
- **Eureka Dashboard**: http://localhost:8761

### Health Checks
```bash
# Check service health
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8083/actuator/health
```

### Log Files
When using `./run-local.sh`, logs are stored in:
- `logs/api-gateway.log`
- `logs/authorization-service.log`
- `logs/portfolio-service.log`

**View logs in real-time:**
```bash
tail -f logs/api-gateway.log
tail -f logs/authorization-service.log
tail -f logs/portfolio-service.log
```

## 🛠️ Development Workflow

### Hot Reload Development
For faster development cycles, you can use Spring Boot DevTools:

1. **Start infrastructure** (once):
   ```bash
   ./gradlew startInfrastructure
   ```

2. **Run individual services** in your IDE or with:
   ```bash
   # In separate terminals
   ./gradlew runApiGateway
   ./gradlew runAuthorizationService
   ./gradlew runPortfolioService
   ```

3. **Make changes** to your code - services will auto-restart

### Testing
```bash
# Run all tests
./gradlew test

# Run tests for specific service
./gradlew :api-gateway:test
./gradlew :authorization-service:test
./gradlew :technology-portfolio-service:test
```

### Database Migrations
```bash
# Run Flyway migrations manually
./gradlew :authorization-service:flywayMigrate
./gradlew :technology-portfolio-service:flywayMigrate
```

## 🔧 Configuration

### Environment Variables
The Gradle tasks set appropriate environment variables:

**API Gateway:**
- `SPRING_PROFILES_ACTIVE=mock-auth,local`
- `SERVER_PORT=8081`
- `JWT_SECRET=local-development-secret...`

**Authorization Service:**
- `SPRING_PROFILES_ACTIVE=local`
- `SERVER_PORT=8082`
- `SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/authorization`

**Portfolio Service:**
- `SPRING_PROFILES_ACTIVE=local`
- `SERVER_PORT=8083`
- `SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/portfolio`

### Custom Configuration
You can override settings by:

1. **Environment variables** (highest priority)
2. **application-local.yml** files in each service
3. **Command line arguments**: `./gradlew runApiGateway --args="--server.port=8090"`

## 🚨 Troubleshooting

### Common Issues

**Port Already in Use**
```bash
# Find process using port
lsof -i :8081

# Kill process
kill -9 <PID>
```

**Database Connection Issues**
```bash
# Check PostgreSQL is running
pg_isready -h localhost -p 5432
pg_isready -h localhost -p 5433

# Check database exists
psql -h localhost -p 5432 -U auth_user -d authorization -c "SELECT 1;"
```

**Service Won't Start**
```bash
# Check logs
tail -f logs/api-gateway.log

# Check Java version
java -version

# Check Gradle version
./gradlew --version
```

### Performance Tuning
```bash
# Increase JVM memory for services
export JAVA_OPTS="-Xmx1024m -Xms512m"
./gradlew runApiGateway
```

## 📝 Available Gradle Tasks

### Infrastructure Management
- `./gradlew startInfrastructure` - Start PostgreSQL, Redis, Eureka
- `./gradlew stopInfrastructure` - Stop infrastructure services

### Service Management  
- `./gradlew runApiGateway` - Run API Gateway
- `./gradlew runAuthorizationService` - Run Authorization Service
- `./gradlew runPortfolioService` - Run Portfolio Service

### Development
- `./gradlew clean build` - Clean and build all services
- `./gradlew test` - Run all tests
- `./gradlew bootRun` - Run service (from service directory)

### Utility
- `./gradlew tasks --group=application` - Show all application tasks
- `./gradlew dependencies` - Show dependency tree

## 🎯 Next Steps

1. **Start with the quick start** using `./run-local.sh`
2. **Access the mock login page** at http://localhost:8081/mock-login
3. **Test the APIs** using the provided curl examples
4. **Check service health** via actuator endpoints
5. **Monitor logs** for any issues

For production deployment, see the Docker Compose setup in the main README. 