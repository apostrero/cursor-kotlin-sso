# PlantUML Sequence Diagrams

This directory contains PlantUML sequence diagrams that illustrate the authentication and authorization flows in the Technology Portfolio SSO system.

## 📊 Available Diagrams

### 1. **Non-Authenticated User Flow** (`non-authenticated-user-flow.puml`)
- **Purpose**: Shows the complete flow when a user without authentication tries to access a protected resource
- **Key Steps**:
  - Initial request to protected resource
  - 401 Unauthorized response
  - Login process with mock authentication
  - JWT token generation
  - Successful access to the original resource

### 2. **Authenticated User Flow** (`authenticated-user-flow.puml`)
- **Purpose**: Shows the flow when an already authenticated user accesses protected resources
- **Key Steps**:
  - JWT token validation
  - Authorization checks
  - Service-to-service communication
  - Data retrieval and response
  - Error handling scenarios

### 3. **Authentication Flow Overview** (`authentication-flow-overview.puml`)
- **Purpose**: Comprehensive overview showing all authentication scenarios
- **Key Flows**:
  - Non-authenticated user flow
  - Authenticated user flow
  - SAML authentication flow (production)
  - Error handling and security features

### 4. **Mock IdP - Non-Authenticated User Flow** (`mock-idp-non-authenticated-user-flow.puml`)
- **Purpose**: Detailed flow for non-authenticated users using mock IdP
- **Key Steps**:
  - Mock login page display
  - Available test users listing
  - Mock credential validation
  - JWT token generation with mock user claims
  - Mock configuration details

### 5. **Mock IdP - Authenticated User Flow** (`mock-idp-authenticated-user-flow.puml`)
- **Purpose**: Detailed flow for authenticated users using mock IdP
- **Key Steps**:
  - Mock JWT token validation
  - Mock user role-based authorization
  - Service communication with mock user context
  - Portfolio creation with mock user
  - Different mock user role scenarios

### 6. **Mock IdP - Complete Authentication Flow Overview** (`mock-idp-overview.puml`)
- **Purpose**: Comprehensive overview of all mock IdP authentication scenarios
- **Key Flows**:
  - Non-authenticated user with mock IdP
  - Authenticated user with mock IdP
  - Different mock user roles and permissions
  - Resource creation with mock users
  - Mock IdP configuration and benefits

### 7. **SAML - Non-Authenticated User Flow** (`saml-non-authenticated-user-flow.puml`)
- **Purpose**: Detailed flow for non-authenticated users using SAML with SimpleSAMLphp IdP
- **Key Steps**:
  - SAML AuthnRequest generation
  - SimpleSAMLphp IdP interaction
  - SAML Response processing
  - User attribute mapping
  - JWT token generation with SAML claims
  - SAML configuration details

### 8. **SAML - Authenticated User Flow** (`saml-authenticated-user-flow.puml`)
- **Purpose**: Detailed flow for authenticated users using SAML with SimpleSAMLphp IdP
- **Key Steps**:
  - SAML JWT token validation
  - SAML user role-based authorization
  - Service communication with SAML user context
  - Portfolio creation with SAML user
  - SAML session management
  - Different SAML user role scenarios

### 9. **SAML - Complete Authentication Flow Overview** (`saml-overview.puml`)
- **Purpose**: Comprehensive overview of all SAML authentication scenarios with SimpleSAMLphp IdP
- **Key Flows**:
  - Non-authenticated user with SAML
  - Authenticated user with SAML
  - Different SAML user roles and permissions
  - Resource creation with SAML users
  - SAML configuration and production benefits

## 🛠️ How to Use These Diagrams

### Viewing the Diagrams

#### Option 1: Online PlantUML Editor
1. Copy the content of any `.puml` file
2. Go to [PlantUML Online Editor](http://www.plantuml.com/plantuml/uml/)
3. Paste the content and view the generated diagram

#### Option 2: VS Code Extension
1. Install the "PlantUML" extension in VS Code
2. Open any `.puml` file
3. Use `Ctrl+Shift+P` and run "PlantUML: Preview Current Diagram"

#### Option 3: Local PlantUML Installation
```bash
# Install PlantUML (requires Java)
# macOS
brew install plantuml

# Generate PNG from PUML file
plantuml saml-non-authenticated-user-flow.puml

# Generate SVG
plantuml -tsvg saml-non-authenticated-user-flow.puml
```

#### Option 4: IntelliJ IDEA Plugin
1. Install "PlantUML integration" plugin
2. Open `.puml` files directly in the IDE
3. Diagrams will render automatically

### Generating Images

To generate PNG/SVG images for documentation:

```bash
# Generate all diagrams
for file in *.puml; do
    plantuml "$file"
done

# Generate specific format
plantuml -tpng *.puml
plantuml -tsvg *.puml

# Generate only mock IdP diagrams
plantuml mock-idp-*.puml

# Generate only SAML diagrams
plantuml saml-*.puml
```

## 🏗️ Architecture Components

The diagrams show the following components:

### Services
- **API Gateway (8080)**: Central entry point, handles authentication and routing
- **Authorization Service (8082)**: Manages user authentication and authorization
- **Technology Portfolio Service (8083)**: Core business logic for portfolio management
- **SimpleSAMLphp IdP (8081)**: Identity Provider for SAML authentication

### Databases
- **PostgreSQL Auth DB**: Stores user credentials, roles, and permissions
- **PostgreSQL Portfolio DB**: Stores portfolio and technology data

### External Components
- **Browser**: Client-side application
- **User**: End user interacting with the system

## 🔐 Authentication Modes

### Mock Authentication (Development)
- Simple username/password authentication
- No external dependencies
- Suitable for development and testing
- Configured via `application-mock-auth.yml`
- **Test Users**:
  - `user1`/`password` (Portfolio Manager)
  - `user2`/`password` (Viewer)
  - `admin`/`secret` (Administrator)

### SAML Authentication (Production)
- Enterprise-grade SAML 2.0 SSO
- Uses SimpleSAMLphp as Identity Provider
- Production-ready implementation
- Configured via `docker-compose.yml`
- **SAML Users**:
  - `user1`/`password` (portfolio-managers group)
  - `user2`/`password` (viewers group)
  - `admin`/`secret` (admins group)

## 🔄 Flow Patterns

### Common Patterns in All Flows

1. **Request Interception**: API Gateway intercepts all requests
2. **Token Validation**: JWT tokens are validated at multiple points
3. **Authorization Check**: User permissions are verified before access
4. **Service Communication**: Services communicate with JWT tokens
5. **Error Handling**: Comprehensive error scenarios are handled
6. **Security**: Multiple security layers protect the system

### Mock IdP Specific Patterns

1. **Mock User Configuration**: Users defined in `application-mock-auth.yml`
2. **Mock Credential Validation**: Simple username/password matching
3. **Mock JWT Generation**: Tokens with mock user claims
4. **Mock Role-Based Access**: Different permissions per mock user
5. **Mock Error Scenarios**: Specific error handling for mock authentication

### SAML Specific Patterns

1. **SAML AuthnRequest**: SP-initiated authentication requests
2. **SAML Response Processing**: XML signature validation and attribute extraction
3. **SAML User Mapping**: SAML attributes to application roles
4. **SAML Session Management**: Session lifecycle and token refresh
5. **SAML Error Handling**: XML validation and IdP communication errors

### Key Security Features

- **JWT Token Validation**: Signature verification and expiration checks
- **Role-Based Access Control (RBAC)**: User roles determine access levels
- **Permission-Based Authorization**: Fine-grained permission checks
- **Token Expiration Handling**: Automatic token refresh and renewal
- **Secure Communication**: HTTPS and secure cookie handling
- **Mock User Validation**: Configuration-based user authentication
- **SAML Security**: XML signature validation, encryption, and metadata verification

## 📝 Customizing Diagrams

### Adding New Flows

1. Create a new `.puml` file following the naming convention
2. Use the existing components and styling
3. Follow the established patterns for consistency
4. Update this README with the new diagram description

### Modifying Existing Diagrams

1. Keep the same component names and ports for consistency
2. Maintain the visual styling and emoji usage
3. Update the title and description as needed
4. Test the diagram generation after changes

### Styling Guidelines

- Use emojis for component identification
- Include port numbers in component names
- Use clear, descriptive step names
- Add notes for important details
- Group related steps with `==` separators

## 🔗 Related Documentation

- [SSO Architecture](../architecture/sso-architecture.md) - Detailed SSO implementation
- [Hexagonal Architecture](../architecture/hexagonal-architecture.md) - System architecture
- [Implementation Guide](../development/implementation-guide.md) - Development setup
- [Docker Setup](../deployment/docker-setup.md) - Deployment instructions

## 🐛 Troubleshooting

### Common Issues

1. **Diagram not rendering**: Check PlantUML syntax and ensure Java is installed
2. **Missing components**: Verify all participant declarations are present
3. **Styling issues**: Check theme and formatting consistency
4. **File not found**: Ensure `.puml` files are in the correct directory

### Getting Help

- Check [PlantUML Documentation](https://plantuml.com/)
- Review existing diagrams for patterns
- Test syntax in the online editor first
- Consult the architecture documentation for component details

---

**Last Updated**: $(date)
**PlantUML Version**: 1.2023.0+ 