# Technology Portfolio SSO System - Docker Setup

This document explains how to run the complete Technology Portfolio management system with SSO authentication using an **open source SimpleSAMLphp Identity Provider** via Docker Compose.

## 🎯 Overview

The system includes:
- **SimpleSAMLphp Identity Provider** (Open Source SAML IdP)
- **API Gateway** with SAML authentication
- **Authorization Service** for role-based access control
- **Technology Portfolio Service** for portfolio management
- **PostgreSQL databases** for data persistence
- **Redis** for caching and session management
- **Eureka** for service discovery
- **Prometheus & Grafana** for monitoring

## 🚀 Quick Start

### Prerequisites

- Docker and Docker Compose installed
- At least 4GB RAM available
- Ports 8080-8083, 3000, 5432-5433, 6379, 8761, 9090 available

### 1. Start the System

```bash
# Make the startup script executable
chmod +x docker-start.sh

# Start all services
./docker-start.sh
```

The script will:
1. 🏗️ Start infrastructure services (databases, Redis, Eureka, IdP)
2. 🔨 Build and start application services
3. 📊 Start monitoring services
4. ✅ Verify all services are healthy

### 2. Access the System

Once started, you can access:

| Service | URL | Description |
|---------|-----|-------------|
| 🔐 **SimpleSAMLphp IdP** | http://localhost:8080/simplesaml/ | Identity Provider |
| 🚪 **API Gateway** | http://localhost:8081 | Main application entry point |
| 🔒 **Authorization Service** | http://localhost:8082 | User authorization |
| 📁 **Portfolio Service** | http://localhost:8083 | Technology portfolio management |
| 🔍 **Eureka Discovery** | http://localhost:8761 | Service registry |
| 📈 **Prometheus** | http://localhost:9090 | Metrics collection |
| 📊 **Grafana** | http://localhost:3000 | Monitoring dashboards |

## 🔑 Test Users

The SimpleSAMLphp Identity Provider comes pre-configured with test users:

| Username | Password | Role | Permissions |
|----------|----------|------|-------------|
| `user1` | `password` | Portfolio Manager | Read/Write portfolios, View analytics |
| `user2` | `password` | Viewer | Read-only access |
| `admin` | `secret` | Administrator | Full system access |

## 🧪 Testing SAML SSO

### Basic SSO Flow Test

1. **Initiate Login**:
   ```
   Visit: http://localhost:8081/saml/login
   ```

2. **Identity Provider Redirect**:
   - You'll be automatically redirected to SimpleSAMLphp
   - URL: `http://localhost:8080/simplesaml/...`

3. **Authenticate**:
   - Enter username: `user1`
   - Enter password: `password`
   - Click "Login"

4. **Return to Application**:
   - You'll be redirected back to: `http://localhost:8081`
   - JWT token will be issued and stored
   - User session will be established

### API Testing with Authentication

```bash
# 1. Get SAML authentication (returns JWT token)
curl -X POST http://localhost:8081/api/auth/saml \
  -H "Content-Type: application/json" \
  -d '{"username": "user1"}'

# 2. Use JWT token for API calls
curl -X GET http://localhost:8081/api/portfolios \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# 3. Test authorization
curl -X POST http://localhost:8081/api/portfolios \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Portfolio",
    "description": "Created via API",
    "portfolioType": "APPLICATION"
  }'
```

## 🔧 Configuration

### Environment Variables

The system uses environment variables for configuration. Key variables in `docker-compose.yml`:

```yaml
# SimpleSAMLphp IdP Configuration
SIMPLESAMLPHP_SP_ENTITY_ID: http://localhost:8081
SIMPLESAMLPHP_SP_ASSERTION_CONSUMER_SERVICE: http://localhost:8081/saml/acs
SIMPLESAMLPHP_SP_SINGLE_LOGOUT_SERVICE: http://localhost:8081/saml/logout
SIMPLESAMLPHP_IDP_ADMIN_PASSWORD: admin123

# API Gateway SAML Configuration  
SAML_IDP_METADATA_URL: http://identity-provider:8080/simplesaml/saml2/idp/metadata.php
SAML_SP_ENTITY_ID: http://localhost:8081
SAML_SP_BASE_URL: http://localhost:8081

# Database Configuration
SPRING_DATASOURCE_URL: jdbc:postgresql://postgres-auth:5432/authorization
SPRING_DATASOURCE_USERNAME: auth_user
SPRING_DATASOURCE_PASSWORD: auth_password
```

### Custom User Configuration

To add custom users to SimpleSAMLphp, create a custom `authsources.php` file:

```php
<?php
$config = array(
    'admin' => array('core:AdminPassword'),
    'example-userpass' => array(
        'exampleauth:UserPass',
        'your-username:your-password' => array(
            'http://schemas.xmlsoap.org/ws/2005/05/identity/claims/emailaddress' => 'user@company.com',
            'http://schemas.xmlsoap.org/ws/2005/05/identity/claims/givenname' => 'First',
            'http://schemas.xmlsoap.org/ws/2005/05/identity/claims/surname' => 'Last',
            'http://schemas.xmlsoap.org/ws/2005/05/identity/claims/name' => 'user@company.com'
        ),
    ),
);
```

Then mount it in docker-compose.yml:
```yaml
identity-provider:
  # ... other config
  volumes:
    - ./custom-authsources.php:/var/www/simplesamlphp/config/authsources.php
```

## 📊 Monitoring

### Prometheus Metrics

Access Prometheus at http://localhost:9090 to query metrics:

```promql
# HTTP request rate
rate(http_requests_total[5m])

# Service health
up{job="api-gateway"}

# Database connections
postgres_connections_active
```

### Grafana Dashboards

Access Grafana at http://localhost:3000 (admin/admin123):

1. **Application Dashboard**: Service health, response times, error rates
2. **Infrastructure Dashboard**: Database, Redis, system metrics
3. **Security Dashboard**: Authentication events, authorization failures

## 🐛 Troubleshooting

### Common Issues

#### 1. Services Not Starting
```bash
# Check service logs
docker-compose logs -f [service-name]

# Check service health
docker-compose ps
```

#### 2. SAML Authentication Fails
```bash
# Check IdP logs
docker-compose logs -f identity-provider

# Check API Gateway logs  
docker-compose logs -f api-gateway

# Verify IdP metadata is accessible
curl http://localhost:8080/simplesaml/saml2/idp/metadata.php
```

#### 3. Database Connection Issues
```bash
# Check database logs
docker-compose logs -f postgres-auth
docker-compose logs -f postgres-portfolio

# Test database connectivity
docker-compose exec postgres-auth psql -U auth_user -d authorization -c "SELECT 1;"
```

#### 4. Service Discovery Issues
```bash
# Check Eureka dashboard
open http://localhost:8761

# Verify service registration
curl http://localhost:8761/eureka/apps
```

### Reset System

```bash
# Stop all services and remove volumes
docker-compose down -v

# Remove all containers and images
docker-compose down --rmi all --volumes --remove-orphans

# Restart fresh
./docker-start.sh
```

## 🔒 Security Considerations

### Development vs Production

This setup is configured for **development and testing**. For production:

1. **Change Default Passwords**:
   ```yaml
   SIMPLESAMLPHP_IDP_ADMIN_PASSWORD: your-secure-password
   POSTGRES_PASSWORD: your-secure-db-password
   ```

2. **Use Proper TLS Certificates**:
   - Replace self-signed certificates
   - Configure proper SSL/TLS termination

3. **Secure Network Configuration**:
   - Use internal networks for service communication
   - Expose only necessary ports

4. **Use External Databases**:
   - Replace containerized databases with managed services
   - Implement proper backup strategies

### SAML Security

- SimpleSAMLphp uses default test certificates
- For production, generate proper SAML signing certificates
- Configure proper SP metadata with your IdP

## 🚀 Alternative Identity Providers

While this setup uses SimpleSAMLphp, you can easily switch to other open source IdPs:

### Keycloak
```yaml
identity-provider:
  image: quay.io/keycloak/keycloak:latest
  environment:
    KEYCLOAK_ADMIN: admin
    KEYCLOAK_ADMIN_PASSWORD: admin123
  command: start-dev
```

### Authentik
```yaml
identity-provider:
  image: ghcr.io/goauthentik/server:latest
  # See Authentik documentation for configuration
```

## 📚 Additional Resources

- [SimpleSAMLphp Documentation](https://simplesamlphp.org/docs/stable/)
- [Spring Security SAML](https://docs.spring.io/spring-security/reference/servlet/saml2/index.html)
- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [Prometheus Configuration](https://prometheus.io/docs/prometheus/latest/configuration/configuration/)

## 🤝 Contributing

To contribute to this setup:

1. Fork the repository
2. Create a feature branch
3. Test your changes with `./docker-start.sh`
4. Submit a pull request

## 📝 License

This project is licensed under the MIT License - see the LICENSE file for details. 