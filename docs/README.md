# Technology Portfolio SSO - Documentation

Welcome to the Technology Portfolio SSO project documentation. This comprehensive guide provides everything you need to understand, develop, deploy, and maintain the system.

## 📚 Documentation Structure

### 🏗️ [Architecture](./architecture/)
- [Hexagonal Architecture](./architecture/hexagonal-architecture.md) - System architecture overview
- [Shared Module Structure](./architecture/shared-module-structure.md) - Shared components organization
- [SSO Architecture](./architecture/sso-architecture.md) - Single Sign-On implementation

### 🛠️ [Development](./development/)
- [Implementation Guide](./development/implementation-guide.md) - Development setup and guidelines
- [Gradle Local Development](./development/gradle-local-development.md) - Local development workflow
- [WebFlux Migration Plan](./development/webflux-migration-plan.md) - Reactive programming migration
- [Flux Usage Guide](./development/flux-usage-guide.md) - Reactive programming patterns

### 🚀 [Deployment](./deployment/)
- [Deployment Guide](./deployment/deployment-guide.md) - Complete deployment instructions
- [Deployment Summary](./deployment/deployment-summary.md) - Quick deployment reference
- [Docker Setup](./deployment/docker-setup.md) - Containerized deployment
- [Environments](./deployment/environments/) - Environment-specific configurations

### 🔧 [Services](./services/)
- [API Gateway](./services/api-gateway/) - Gateway service documentation
- [Authorization Service](./services/authorization-service/) - Authorization service documentation
- [Technology Portfolio Service](./services/technology-portfolio-service/) - Portfolio service documentation
- [Shared Module](./services/shared/) - Shared components documentation

### 📈 [Project History](./project-history/)
- [Phase 3-8 Completion Summaries](./project-history/) - Project evolution documentation

## 🚀 Quick Start

### Prerequisites
- Java 21
- Kotlin 1.9.22
- Docker & Docker Compose
- Gradle 8.5+

### Local Development
```bash
# Clone the repository
git clone <repository-url>
cd CursorKotlinSSO

# Start local environment
./run-local.sh

# Or use Docker
./docker-start.sh
```

### Service Ports
- **API Gateway**: http://localhost:8080
- **Authorization Service**: http://localhost:8082
- **Technology Portfolio Service**: http://localhost:8083
- **SimpleSAMLphp IdP**: http://localhost:8081

## 🔐 Authentication

The system supports dual authentication modes:

### SAML Authentication
- Uses SimpleSAMLphp as Identity Provider
- Production-ready SSO implementation
- Configured via `docker-compose.yml`

### Mock Authentication
- Development-friendly mock authentication
- No external dependencies
- Configured via `docker-compose-mock.yml`

## 🏗️ Architecture Overview

This is a microservices-based system with:

- **Hexagonal Architecture** for clean separation of concerns
- **Reactive Programming** using Spring WebFlux
- **Event-Driven Communication** between services
- **CQRS Pattern** for command and query separation
- **Shared Module** for cross-service components

## 📊 Monitoring

- **Prometheus**: Metrics collection
- **Grafana**: Visualization dashboards
- **Health Checks**: Service health monitoring

## 🤝 Contributing

1. Follow the [Implementation Guide](./development/implementation-guide.md)
2. Use the [Gradle Local Development](./development/gradle-local-development.md) workflow
3. Ensure all tests pass before submitting changes
4. Update relevant documentation

## 📞 Support

For questions or issues:
1. Check the relevant documentation section
2. Review [Project History](./project-history/) for similar issues
3. Create an issue with detailed information

## 📝 Documentation Maintenance

This documentation is maintained alongside the codebase. When making changes:

1. Update relevant documentation files
2. Ensure links remain valid
3. Add new sections as needed
4. Keep the structure organized

---

**Last Updated**: $(date)
**Version**: 1.0.0 