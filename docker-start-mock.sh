#!/bin/bash

# Technology Portfolio SSO System - Mock Authentication Mode
# This script starts the system with mock authentication (no external IdP required)

set -e

echo "🚀 Starting Technology Portfolio System with Mock Authentication"
echo "=============================================================="

# Create necessary directories
echo "📁 Creating necessary directories..."
mkdir -p monitoring/grafana/dashboards
mkdir -p monitoring/grafana/datasources
mkdir -p database

# Check if docker-compose is available
if ! command -v docker-compose &> /dev/null && ! command -v docker compose &> /dev/null; then
    echo "❌ Docker Compose is not installed. Please install Docker Compose first."
    exit 1
fi

# Use docker compose or docker-compose based on availability
DOCKER_COMPOSE_CMD="docker compose"
if ! command -v docker compose &> /dev/null; then
    DOCKER_COMPOSE_CMD="docker-compose"
fi

echo "🐳 Using: $DOCKER_COMPOSE_CMD"

# Function to wait for service health
wait_for_service() {
    local service_name=$1
    local max_attempts=30
    local attempt=1
    
    echo "⏳ Waiting for $service_name to be healthy..."
    
    while [ $attempt -le $max_attempts ]; do
        if $DOCKER_COMPOSE_CMD -f docker-compose-mock.yml ps --format json | jq -r '.[] | select(.Name | contains("'$service_name'")) | .Health' | grep -q "healthy"; then
            echo "✅ $service_name is healthy"
            return 0
        fi
        
        echo "   Attempt $attempt/$max_attempts - $service_name not ready yet..."
        sleep 10
        attempt=$((attempt + 1))
    done
    
    echo "❌ $service_name failed to become healthy after $max_attempts attempts"
    return 1
}

# Stop any existing containers
echo "🛑 Stopping existing containers..."
$DOCKER_COMPOSE_CMD -f docker-compose-mock.yml down --remove-orphans

# Pull latest images
echo "📥 Pulling latest base images..."
$DOCKER_COMPOSE_CMD -f docker-compose-mock.yml pull

# Start infrastructure services first
echo "🏗️  Starting infrastructure services..."
$DOCKER_COMPOSE_CMD -f docker-compose-mock.yml up -d postgres-auth postgres-portfolio redis eureka-server

# Wait for infrastructure to be ready
wait_for_service "postgres-auth"
wait_for_service "postgres-portfolio" 
wait_for_service "redis"
wait_for_service "eureka-server"

echo "✅ Infrastructure services are ready!"

# Build and start application services
echo "🔨 Building and starting application services..."
$DOCKER_COMPOSE_CMD -f docker-compose-mock.yml up -d --build api-gateway authorization-service technology-portfolio-service

# Wait for application services
wait_for_service "api-gateway"
wait_for_service "authorization-service"
wait_for_service "technology-portfolio-service"

echo "✅ Application services are ready!"

# Start monitoring services (optional)
echo "📊 Starting monitoring services..."
$DOCKER_COMPOSE_CMD -f docker-compose-mock.yml up -d prometheus grafana

echo ""
echo "🎉 Mock Authentication System startup complete!"
echo "=============================================================="
echo ""
echo "📋 Service URLs:"
echo "   🔐 Mock Login Page:                 http://localhost:8081/mock-login"
echo "   🚪 API Gateway:                     http://localhost:8081"
echo "   🔒 Authorization Service:           http://localhost:8082"
echo "   📁 Technology Portfolio Service:    http://localhost:8083"
echo "   🔍 Service Discovery (Eureka):      http://localhost:8761"
echo "   📈 Prometheus:                      http://localhost:9090"
echo "   📊 Grafana:                         http://localhost:3000"
echo ""
echo "🔑 Test Users (Mock Authentication):"
echo "   Username: user1, Password: password (Portfolio Manager)"
echo "   Username: user2, Password: password (Viewer)"
echo "   Username: admin, Password: secret   (Administrator)"
echo ""
echo "🧪 Testing Mock Authentication:"
echo ""
echo "   🌐 Web Interface:"
echo "     1. Visit: http://localhost:8081/mock-login"
echo "     2. Click on any test user to auto-fill credentials"
echo "     3. Click 'Sign In'"
echo "     4. You'll get a JWT token response"
echo ""
echo "   📡 API Interface:"
echo "     # Get available test users"
echo "     curl http://localhost:8081/api/auth/mock-users"
echo ""
echo "     # Login programmatically"
echo "     curl -X POST http://localhost:8081/api/auth/mock-login \\"
echo "       -H 'Content-Type: application/json' \\"
echo "       -d '{\"username\": \"user1\", \"password\": \"password\"}'"
echo ""
echo "     # Use JWT token for API calls"
echo "     curl -X GET http://localhost:8081/api/portfolios \\"
echo "       -H 'Authorization: Bearer YOUR_JWT_TOKEN'"
echo ""
echo "🔧 Admin Interfaces:"
echo "   Grafana Admin: admin/admin123"
echo ""
echo "📝 To view logs:"
echo "   $DOCKER_COMPOSE_CMD -f docker-compose-mock.yml logs -f [service-name]"
echo ""
echo "🛑 To stop all services:"
echo "   $DOCKER_COMPOSE_CMD -f docker-compose-mock.yml down"
echo ""
echo "⚡ Benefits of Mock Mode:"
echo "   ✅ Faster startup (no external IdP container)"
echo "   ✅ No SAML complexity"
echo "   ✅ Perfect for development and testing"
echo "   ✅ Same JWT tokens and authorization as SAML mode"
echo ""

# Show running containers
echo "🐳 Running containers:"
$DOCKER_COMPOSE_CMD -f docker-compose-mock.yml ps 