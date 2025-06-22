# Technology Portfolio SSO

A comprehensive microservices-based technology portfolio management system with Single Sign-On (SSO) authentication, built with Kotlin, Spring Boot, and reactive programming.

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

## 📚 Documentation

**📖 [Complete Documentation](./docs/README.md)**

### Quick Links
- [🏗️ Architecture](./docs/architecture/) - System architecture and design
- [🛠️ Development](./docs/development/) - Development setup and guidelines
- [🚀 Deployment](./docs/deployment/) - Deployment instructions and guides
- [🔧 Services](./docs/services/) - Service-specific documentation
- [📈 Project History](./docs/project-history/) - Project evolution and milestones

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

## 🏗️ Architecture

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

1. Follow the [Implementation Guide](./docs/development/implementation-guide.md)
2. Use the [Gradle Local Development](./docs/development/gradle-local-development.md) workflow
3. Ensure all tests pass before submitting changes
4. Update relevant documentation

## 📞 Support

For questions or issues:
1. Check the [documentation](./docs/README.md)
2. Review [Project History](./docs/project-history/) for similar issues
3. Create an issue with detailed information

---

**Last Updated**: $(date)
**Version**: 1.0.0 