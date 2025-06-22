# Technology Portfolio Service API Documentation

## Overview

The Technology Portfolio Service provides a comprehensive REST API for managing technology portfolios, technologies, assessments, and dependencies. The API is built using Spring WebFlux and follows reactive programming principles with `Mono<T>` and `Flux<T>` types.

## Base URL

- **Development**: `http://localhost:8083/api/v1`
- **Test**: `https://test.techportfolio.company.com/api/v1`
- **Production**: `https://techportfolio.company.com/api/v1`

## Authentication

All API endpoints require JWT authentication. Include the JWT token in the Authorization header:

```
Authorization: Bearer <your-jwt-token>
```

### JWT Token Structure

```json
{
  "sub": "user123",
  "username": "john.doe",
  "email": "john.doe@company.com",
  "roles": ["USER", "PORTFOLIO_MANAGER"],
  "organizationId": 1,
  "iat": 1640995200,
  "exp": 1640998800
}
```

## Reactive Programming Concepts

### Mono<T>
- Represents a single result or empty result
- Used for create, read, update, delete operations
- Example: `Mono<PortfolioResponse>`

### Flux<T>
- Represents a stream of multiple results
- Used for collections, lists, and streaming operations
- Example: `Flux<PortfolioSummary>`

### Server-Sent Events (SSE)
- Real-time streaming of data
- Content-Type: `text/event-stream`
- Used for live updates and monitoring

## API Endpoints

### Portfolio Management

#### Create Portfolio
```http
POST /portfolios
Content-Type: application/json
Authorization: Bearer <jwt-token>

{
  "name": "Enterprise Java Applications",
  "description": "Portfolio for enterprise Java applications and frameworks",
  "type": "ENTERPRISE",
  "organizationId": 1
}
```

**Response (201 Created)**:
```json
{
  "id": 1,
  "name": "Enterprise Java Applications",
  "description": "Portfolio for enterprise Java applications and frameworks",
  "type": "ENTERPRISE",
  "organizationId": 1,
  "createdAt": "2024-01-15T10:30:00Z",
  "updatedAt": "2024-01-15T10:30:00Z",
  "technologies": []
}
```

#### Get All Portfolios (Reactive Pagination)
```http
GET /portfolios?page=0&size=20&sort=name,asc&type=ENTERPRISE
Authorization: Bearer <jwt-token>
```

**Response (200 OK)**:
```json
[
  {
    "id": 1,
    "name": "Enterprise Java Applications",
    "description": "Portfolio for enterprise Java applications",
    "type": "ENTERPRISE",
    "technologyCount": 5,
    "createdAt": "2024-01-15T10:30:00Z"
  },
  {
    "id": 2,
    "name": "Cloud Infrastructure",
    "description": "Portfolio for cloud infrastructure",
    "type": "CLOUD",
    "technologyCount": 3,
    "createdAt": "2024-01-15T11:00:00Z"
  }
]
```

#### Get Portfolio by ID
```http
GET /portfolios/1
Authorization: Bearer <jwt-token>
```

**Response (200 OK)**:
```json
{
  "id": 1,
  "name": "Enterprise Java Applications",
  "description": "Portfolio for enterprise Java applications and frameworks",
  "type": "ENTERPRISE",
  "organizationId": 1,
  "createdAt": "2024-01-15T10:30:00Z",
  "updatedAt": "2024-01-15T10:30:00Z",
  "technologies": [
    {
      "id": 1,
      "name": "Spring Boot",
      "version": "3.2.0",
      "category": "FRAMEWORK",
      "status": "ACTIVE",
      "maturityLevel": "MATURE",
      "description": "Spring Boot framework for Java applications"
    }
  ]
}
```

#### Update Portfolio
```http
PUT /portfolios/1
Content-Type: application/json
Authorization: Bearer <jwt-token>

{
  "name": "Updated Enterprise Java Applications",
  "description": "Updated description for enterprise Java applications",
  "type": "ENTERPRISE"
}
```

#### Delete Portfolio
```http
DELETE /portfolios/1
Authorization: Bearer <jwt-token>
```

**Response (204 No Content)**

### Technology Management

#### Add Technology to Portfolio
```http
POST /portfolios/1/technologies
Content-Type: application/json
Authorization: Bearer <jwt-token>

{
  "name": "Spring Boot",
  "version": "3.2.0",
  "category": "FRAMEWORK",
  "description": "Spring Boot framework for Java applications",
  "maturityLevel": "MATURE",
  "status": "ACTIVE"
}
```

**Response (201 Created)**:
```json
{
  "id": 1,
  "name": "Spring Boot",
  "version": "3.2.0",
  "category": "FRAMEWORK",
  "status": "ACTIVE",
  "maturityLevel": "MATURE",
  "description": "Spring Boot framework for Java applications",
  "portfolioId": 1,
  "createdAt": "2024-01-15T10:30:00Z",
  "updatedAt": "2024-01-15T10:30:00Z"
}
```

#### Get Portfolio Technologies
```http
GET /portfolios/1/technologies?page=0&size=20&sort=name,asc
Authorization: Bearer <jwt-token>
```

**Response (200 OK)**:
```json
[
  {
    "id": 1,
    "name": "Spring Boot",
    "version": "3.2.0",
    "category": "FRAMEWORK",
    "status": "ACTIVE",
    "maturityLevel": "MATURE",
    "description": "Spring Boot framework for Java applications"
  },
  {
    "id": 2,
    "name": "PostgreSQL",
    "version": "15.0",
    "category": "DATABASE",
    "status": "ACTIVE",
    "maturityLevel": "MATURE",
    "description": "PostgreSQL relational database"
  }
]
```

#### Update Technology
```http
PUT /technologies/1
Content-Type: application/json
Authorization: Bearer <jwt-token>

{
  "version": "3.3.0",
  "status": "ACTIVE",
  "description": "Updated Spring Boot framework description"
}
```

#### Remove Technology from Portfolio
```http
DELETE /portfolios/1/technologies/1
Authorization: Bearer <jwt-token>
```

**Response (204 No Content)**

### Streaming Endpoints

#### Stream Portfolios (Server-Sent Events)
```http
GET /portfolios/stream
Accept: text/event-stream
Authorization: Bearer <jwt-token>
```

**Response (200 OK)**:
```
data: {"id": 1, "name": "Enterprise Java", "type": "ENTERPRISE", "updatedAt": "2024-01-15T10:30:00Z"}

data: {"id": 2, "name": "Cloud Infrastructure", "type": "CLOUD", "updatedAt": "2024-01-15T10:31:00Z"}

data: {"id": 3, "name": "Mobile Applications", "type": "MOBILE", "updatedAt": "2024-01-15T10:32:00Z"}
```

#### Stream Technologies (Server-Sent Events)
```http
GET /technologies/stream
Accept: text/event-stream
Authorization: Bearer <jwt-token>
```

**Response (200 OK)**:
```
data: {"id": 1, "name": "Spring Boot", "version": "3.2.0", "category": "FRAMEWORK", "updatedAt": "2024-01-15T10:30:00Z"}

data: {"id": 2, "name": "PostgreSQL", "version": "15.0", "category": "DATABASE", "updatedAt": "2024-01-15T10:31:00Z"}
```

## Error Handling

### Error Response Format
```json
{
  "error": "Bad Request",
  "message": "Validation failed",
  "timestamp": "2024-01-15T10:30:00Z",
  "errors": [
    "Name is required",
    "Description cannot be empty"
  ]
}
```

### HTTP Status Codes

| Status Code | Description | Example |
|-------------|-------------|---------|
| 200 | OK | Successful GET request |
| 201 | Created | Successful POST request |
| 204 | No Content | Successful DELETE request |
| 400 | Bad Request | Invalid request data |
| 401 | Unauthorized | Missing or invalid authentication |
| 403 | Forbidden | Insufficient permissions |
| 404 | Not Found | Resource not found |
| 422 | Unprocessable Entity | Business rule violation |
| 500 | Internal Server Error | Server error |

## Pagination

### Query Parameters
- `page`: Page number (0-based, default: 0)
- `size`: Page size (default: 20, max: 100)
- `sort`: Sort field and direction (e.g., `name,asc`, `createdAt,desc`)

### Example
```http
GET /portfolios?page=0&size=10&sort=name,asc&type=ENTERPRISE
```

## Filtering

### Portfolio Filters
- `type`: Filter by portfolio type (ENTERPRISE, CLOUD, MOBILE, WEB, DATA)
- `organizationId`: Filter by organization ID
- `name`: Filter by portfolio name (partial match)

### Technology Filters
- `category`: Filter by technology category (FRAMEWORK, DATABASE, LANGUAGE, TOOL)
- `status`: Filter by technology status (ACTIVE, DEPRECATED, PLANNED)
- `maturityLevel`: Filter by maturity level (EXPERIMENTAL, ADOPTING, MATURE, DEPRECATED)

## Rate Limiting

API endpoints are rate-limited to ensure fair usage:

- **Standard endpoints**: 100 requests per minute
- **Streaming endpoints**: 10 connections per minute
- **Admin endpoints**: 50 requests per minute

Rate limit headers are included in responses:
```
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 95
X-RateLimit-Reset: 1640995260
```

## Client Examples

### JavaScript (Fetch API)
```javascript
// Create portfolio
const createPortfolio = async (portfolioData) => {
  const response = await fetch('/api/v1/portfolios', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify(portfolioData)
  });
  
  if (!response.ok) {
    throw new Error(`HTTP error! status: ${response.status}`);
  }
  
  return response.json();
};

// Stream portfolios
const streamPortfolios = () => {
  const eventSource = new EventSource('/api/v1/portfolios/stream', {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  
  eventSource.onmessage = (event) => {
    const portfolio = JSON.parse(event.data);
    console.log('Portfolio update:', portfolio);
  };
  
  eventSource.onerror = (error) => {
    console.error('SSE error:', error);
  };
  
  return eventSource;
};
```

### Python (requests)
```python
import requests
import json

# Create portfolio
def create_portfolio(portfolio_data, token):
    response = requests.post(
        'http://localhost:8083/api/v1/portfolios',
        headers={
            'Content-Type': 'application/json',
            'Authorization': f'Bearer {token}'
        },
        json=portfolio_data
    )
    response.raise_for_status()
    return response.json()

# Get portfolios with pagination
def get_portfolios(page=0, size=20, token=None):
    response = requests.get(
        'http://localhost:8083/api/v1/portfolios',
        headers={'Authorization': f'Bearer {token}'} if token else {},
        params={'page': page, 'size': size}
    )
    response.raise_for_status()
    return response.json()
```

### cURL Examples
```bash
# Create portfolio
curl -X POST http://localhost:8083/api/v1/portfolios \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d '{
    "name": "Enterprise Java Applications",
    "description": "Portfolio for enterprise Java applications",
    "type": "ENTERPRISE",
    "organizationId": 1
  }'

# Get portfolios with filtering
curl -X GET "http://localhost:8083/api/v1/portfolios?type=ENTERPRISE&page=0&size=10" \
  -H "Authorization: Bearer $JWT_TOKEN"

# Stream portfolios
curl -X GET http://localhost:8083/api/v1/portfolios/stream \
  -H "Accept: text/event-stream" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

## Monitoring and Health Checks

### Health Endpoints
- `GET /actuator/health` - Service health status
- `GET /actuator/health/liveness` - Liveness probe
- `GET /actuator/health/readiness` - Readiness probe

### Metrics Endpoints
- `GET /actuator/metrics` - Available metrics
- `GET /actuator/metrics/http.server.requests` - HTTP request metrics
- `GET /actuator/metrics/r2dbc.connections.active` - Database connection metrics
- `GET /actuator/prometheus` - Prometheus format metrics

### Example Health Response
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "SELECT 1"
      }
    },
    "r2dbc": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "SELECT 1"
      }
    },
    "reactive": {
      "status": "UP",
      "details": {
        "reactive": "WebFlux",
        "reactor": "Active",
        "backpressure": "Handled"
      }
    }
  }
}
```

## Best Practices

### Reactive Programming
1. **Use appropriate reactive types**: `Mono<T>` for single results, `Flux<T>` for collections
2. **Handle errors properly**: Use `onErrorMap`, `onErrorResume`, and `onErrorReturn`
3. **Avoid blocking operations**: Use reactive alternatives for I/O operations
4. **Implement backpressure handling**: Use `onBackpressureBuffer` or `onBackpressureDrop`

### API Design
1. **Consistent naming**: Use RESTful conventions for endpoint naming
2. **Proper HTTP methods**: Use GET, POST, PUT, DELETE appropriately
3. **Comprehensive error handling**: Return appropriate HTTP status codes
4. **Documentation**: Provide clear examples and descriptions

### Performance
1. **Use pagination**: Limit result sets to reasonable sizes
2. **Implement caching**: Cache frequently accessed data
3. **Optimize queries**: Use efficient database queries
4. **Monitor metrics**: Track performance and error rates

### Security
1. **Validate input**: Use Jakarta Bean Validation
2. **Implement authorization**: Check user permissions
3. **Rate limiting**: Prevent abuse with rate limiting
4. **Secure headers**: Use appropriate security headers

## Migration from Spring MVC

### Key Changes
1. **Return types**: Changed from `ResponseEntity<T>` to `Mono<T>` or `Flux<T>`
2. **Error handling**: Use reactive error operators instead of `@ExceptionHandler`
3. **Testing**: Use `WebTestClient` instead of `MockMvc`
4. **Dependencies**: Updated to WebFlux dependencies

### Example Migration
```kotlin
// Before (Spring MVC)
@GetMapping("/{id}")
fun getPortfolio(@PathVariable id: Long): ResponseEntity<PortfolioResponse> {
    val portfolio = portfolioService.getPortfolioById(id)
    return ResponseEntity.ok(portfolio)
}

// After (Spring WebFlux)
@GetMapping("/{id}")
fun getPortfolio(@PathVariable id: Long): Mono<PortfolioResponse> {
    return portfolioService.getPortfolioById(id)
        .onErrorMap { error ->
            RuntimeException("Failed to retrieve portfolio: ${error.message}")
        }
}
```

## Support

For API support and questions:
- **Email**: tech-portfolio@company.com
- **Documentation**: https://techportfolio.company.com/docs
- **GitHub**: https://github.com/company/tech-portfolio-service
- **Issues**: https://github.com/company/tech-portfolio-service/issues 