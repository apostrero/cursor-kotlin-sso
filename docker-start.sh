#!/bin/bash

# Technology Portfolio SSO System - Docker Compose Startup Script
# This script starts the entire system including the open source SimpleSAMLphp Identity Provider

set -e

echo "🚀 Starting Technology Portfolio SSO System with SimpleSAMLphp Identity Provider"
echo "=================================================================="

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
        if $DOCKER_COMPOSE_CMD ps --format json | jq -r '.[] | select(.Name | contains("'$service_name'")) | .Health' | grep -q "healthy"; then
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
$DOCKER_COMPOSE_CMD down --remove-orphans

# Pull latest images
echo "📥 Pulling latest base images..."
$DOCKER_COMPOSE_CMD pull

# Start infrastructure services first
echo "🏗️  Starting infrastructure services..."
$DOCKER_COMPOSE_CMD up -d postgres-auth postgres-portfolio redis eureka-server identity-provider

# Wait for infrastructure to be ready
wait_for_service "postgres-auth"
wait_for_service "postgres-portfolio" 
wait_for_service "redis"
wait_for_service "eureka-server"
wait_for_service "identity-provider"

echo "✅ Infrastructure services are ready!"

# Build and start application services
echo "🔨 Building and starting application services..."
$DOCKER_COMPOSE_CMD up -d --build api-gateway authorization-service technology-portfolio-service

# Wait for application services
wait_for_service "api-gateway"
wait_for_service "authorization-service"
wait_for_service "technology-portfolio-service"

echo "✅ Application services are ready!"

# Start monitoring services
echo "📊 Starting monitoring services..."
$DOCKER_COMPOSE_CMD up -d prometheus grafana

echo ""
echo "🎉 System startup complete!"
echo "=================================================================="
echo ""
echo "📋 Service URLs:"
echo "   🔐 SimpleSAMLphp Identity Provider: http://localhost:8080/simplesaml/"
echo "   🚪 API Gateway:                     http://localhost:8081"
echo "   🔒 Authorization Service:           http://localhost:8082"
echo "   📁 Technology Portfolio Service:    http://localhost:8083"
echo "   🔍 Service Discovery (Eureka):      http://localhost:8761"
echo "   📈 Prometheus:                      http://localhost:9090"
echo "   📊 Grafana:                         http://localhost:3000"
echo ""
echo "🔑 Test Users (SimpleSAMLphp):"
echo "   Username: user1, Password: password (Portfolio Manager)"
echo "   Username: user2, Password: password (Viewer)"
echo "   Username: admin, Password: secret   (Administrator)"
echo ""
echo "🔧 Admin Interfaces:"
echo "   SimpleSAMLphp Admin: http://localhost:8080/simplesaml/module.php/core/frontpage_federation.php"
echo "   Grafana Admin:       admin/admin123"
echo ""
echo "🧪 To test SAML SSO:"
echo "   1. Visit: http://localhost:8081/saml/login"
echo "   2. You'll be redirected to SimpleSAMLphp"
echo "   3. Login with test credentials above"
echo "   4. You'll be redirected back to the application"
echo ""
echo "📝 To view logs:"
echo "   $DOCKER_COMPOSE_CMD logs -f [service-name]"
echo ""
echo "🛑 To stop all services:"
echo "   $DOCKER_COMPOSE_CMD down"
echo ""

# Show running containers
echo "🐳 Running containers:"
$DOCKER_COMPOSE_CMD ps 