#!/bin/bash

# Technology Portfolio System - Local Development Runner
# This script uses Gradle to run all services locally for development

set -e

echo "🚀 Technology Portfolio System - Local Development Mode"
echo "======================================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

# Function to check if a port is in use
check_port() {
    local port=$1
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
        return 0  # Port is in use
    else
        return 1  # Port is free
    fi
}

# Function to wait for service to be ready
wait_for_service() {
    local url=$1
    local service_name=$2
    local max_attempts=30
    local attempt=1
    
    print_info "Waiting for $service_name to be ready..."
    
    while [ $attempt -le $max_attempts ]; do
        if curl -s "$url" >/dev/null 2>&1; then
            print_status "$service_name is ready!"
            return 0
        fi
        
        echo "   Attempt $attempt/$max_attempts - $service_name not ready yet..."
        sleep 5
        attempt=$((attempt + 1))
    done
    
    print_error "$service_name failed to start after $max_attempts attempts"
    return 1
}

# Function to check prerequisites
check_prerequisites() {
    echo ""
    print_info "Checking prerequisites..."
    
    # Check if Java is installed
    if ! command -v java &> /dev/null; then
        print_error "Java is not installed. Please install Java 21 or later."
        exit 1
    fi
    
    # Check Java version
    java_version=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
    if [ "$java_version" -lt 21 ]; then
        print_error "Java 21 or later is required. Found Java $java_version"
        exit 1
    fi
    
    print_status "Java $java_version found"
    
    # Check if Docker is installed (for infrastructure)
    if ! command -v docker &> /dev/null; then
        print_warning "Docker not found. You'll need to start PostgreSQL and Redis manually."
        DOCKER_AVAILABLE=false
    else
        print_status "Docker found"
        DOCKER_AVAILABLE=true
    fi
    
    # Check if Gradle wrapper exists
    if [ ! -f "./gradlew" ]; then
        print_error "Gradle wrapper not found. Please run this script from the project root."
        exit 1
    fi
    
    print_status "Gradle wrapper found"
}

# Function to check if infrastructure is running
check_infrastructure() {
    echo ""
    print_info "Checking infrastructure services..."
    
    local all_ready=true
    
    # Check PostgreSQL (auth)
    if check_port 5432; then
        print_status "PostgreSQL (auth) is running on port 5432"
    else
        print_warning "PostgreSQL (auth) not running on port 5432"
        all_ready=false
    fi
    
    # Check PostgreSQL (portfolio)
    if check_port 5433; then
        print_status "PostgreSQL (portfolio) is running on port 5433"
    else
        print_warning "PostgreSQL (portfolio) not running on port 5433"
        all_ready=false
    fi
    
    # Check Redis
    if check_port 6379; then
        print_status "Redis is running on port 6379"
    else
        print_warning "Redis not running on port 6379"
        all_ready=false
    fi
    
    # Check Eureka
    if check_port 8761; then
        print_status "Eureka is running on port 8761"
    else
        print_warning "Eureka not running on port 8761"
        all_ready=false
    fi
    
    if [ "$all_ready" = false ]; then
        if [ "$DOCKER_AVAILABLE" = true ]; then
            echo ""
            print_info "Starting infrastructure services with Docker..."
            ./gradlew startInfrastructure
            
            # Wait for services to be ready
            sleep 10
            wait_for_service "http://localhost:8761" "Eureka"
        else
            print_error "Infrastructure services are not running and Docker is not available."
            print_info "Please start the following services manually:"
            echo "   - PostgreSQL on port 5432 (database: authorization, user: auth_user, password: auth_password)"
            echo "   - PostgreSQL on port 5433 (database: portfolio, user: portfolio_user, password: portfolio_password)"
            echo "   - Redis on port 6379"
            echo "   - Eureka Server on port 8761"
            exit 1
        fi
    fi
}

# Function to build all services
build_services() {
    echo ""
    print_info "Building all services..."
    
    if ./gradlew clean build -x test; then
        print_status "All services built successfully"
    else
        print_error "Build failed"
        exit 1
    fi
}

# Function to start services
start_services() {
    echo ""
    print_info "Starting microservices..."
    
    # Create log directory
    mkdir -p logs
    
    # Start API Gateway
    print_info "Starting API Gateway (port 8081)..."
    ./gradlew runApiGateway > logs/api-gateway.log 2>&1 &
    API_GATEWAY_PID=$!
    echo $API_GATEWAY_PID > logs/api-gateway.pid
    
    # Wait a bit for API Gateway to start
    sleep 15
    
    # Start Authorization Service
    print_info "Starting Authorization Service (port 8082)..."
    ./gradlew runAuthorizationService > logs/authorization-service.log 2>&1 &
    AUTH_SERVICE_PID=$!
    echo $AUTH_SERVICE_PID > logs/authorization-service.pid
    
    # Wait a bit for Authorization Service to start
    sleep 15
    
    # Start Portfolio Service
    print_info "Starting Technology Portfolio Service (port 8083)..."
    ./gradlew runPortfolioService > logs/portfolio-service.log 2>&1 &
    PORTFOLIO_SERVICE_PID=$!
    echo $PORTFOLIO_SERVICE_PID > logs/portfolio-service.pid
    
    # Wait for services to be ready
    sleep 20
    
    # Check if services are responding
    if wait_for_service "http://localhost:8081/actuator/health" "API Gateway"; then
        print_status "API Gateway started successfully"
    else
        print_error "API Gateway failed to start"
        show_logs "api-gateway"
        cleanup_and_exit
    fi
    
    if wait_for_service "http://localhost:8082/actuator/health" "Authorization Service"; then
        print_status "Authorization Service started successfully"
    else
        print_error "Authorization Service failed to start"
        show_logs "authorization-service"
        cleanup_and_exit
    fi
    
    if wait_for_service "http://localhost:8083/actuator/health" "Portfolio Service"; then
        print_status "Portfolio Service started successfully"
    else
        print_error "Portfolio Service failed to start"
        show_logs "portfolio-service"
        cleanup_and_exit
    fi
}

# Function to show service information
show_service_info() {
    echo ""
    print_status "🎉 All services are running!"
    echo "=============================================="
    echo ""
    echo "📋 Service URLs:"
    echo "   🔐 Mock Login Page:      http://localhost:8081/mock-login"
    echo "   🚪 API Gateway:          http://localhost:8081"
    echo "   🔒 Authorization Service: http://localhost:8082"
    echo "   📁 Portfolio Service:     http://localhost:8083"
    echo "   🔍 Eureka Dashboard:      http://localhost:8761"
    echo ""
    echo "🔑 Test Users:"
    echo "   Username: user1, Password: password (Portfolio Manager)"
    echo "   Username: user2, Password: password (Viewer)"
    echo "   Username: admin, Password: secret   (Administrator)"
    echo ""
    echo "🧪 API Testing:"
    echo "   # Get test users"
    echo "   curl http://localhost:8081/api/auth/mock-users"
    echo ""
    echo "   # Login"
    echo "   curl -X POST http://localhost:8081/api/auth/mock-login \\"
    echo "     -H 'Content-Type: application/json' \\"
    echo "     -d '{\"username\": \"user1\", \"password\": \"password\"}'"
    echo ""
    echo "📝 Logs:"
    echo "   API Gateway:       tail -f logs/api-gateway.log"
    echo "   Authorization:     tail -f logs/authorization-service.log"
    echo "   Portfolio:         tail -f logs/portfolio-service.log"
    echo ""
    echo "🛑 To stop all services: ./stop-local.sh"
    echo ""
}

# Function to show logs
show_logs() {
    local service=$1
    echo ""
    print_info "Last 20 lines of $service logs:"
    echo "----------------------------------------"
    tail -20 logs/$service.log 2>/dev/null || echo "No logs found for $service"
    echo "----------------------------------------"
}

# Function to cleanup and exit
cleanup_and_exit() {
    echo ""
    print_info "Cleaning up..."
    
    # Kill service processes
    for pidfile in logs/*.pid; do
        if [ -f "$pidfile" ]; then
            pid=$(cat "$pidfile")
            if kill -0 "$pid" 2>/dev/null; then
                print_info "Stopping process $pid"
                kill "$pid" 2>/dev/null || true
            fi
            rm -f "$pidfile"
        fi
    done
    
    exit 1
}

# Function to handle script termination
cleanup_on_exit() {
    echo ""
    print_info "Script interrupted. Services are still running."
    print_info "Use ./stop-local.sh to stop all services."
    exit 0
}

# Trap signals for cleanup
trap cleanup_on_exit SIGINT SIGTERM

# Main execution
main() {
    check_prerequisites
    check_infrastructure
    build_services
    start_services
    show_service_info
    
    # Keep script running to show it's active
    print_info "Services are running. Press Ctrl+C to exit (services will continue running)."
    print_info "Use ./stop-local.sh to stop all services."
    
    # Wait indefinitely
    while true; do
        sleep 60
        # Optional: Check if services are still running
        if ! check_port 8081 || ! check_port 8082 || ! check_port 8083; then
            print_warning "One or more services may have stopped. Check logs for details."
        fi
    done
}

# Run main function
main "$@" 