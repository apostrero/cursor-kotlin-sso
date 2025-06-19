#!/bin/bash

# Technology Portfolio System - Stop Local Services
# This script stops all locally running services and optionally infrastructure

echo "🛑 Stopping Technology Portfolio System"
echo "======================================="

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

# Function to stop services by PID files
stop_services() {
    print_info "Stopping microservices..."
    
    local services_stopped=0
    
    # Stop services based on PID files
    for pidfile in logs/*.pid; do
        if [ -f "$pidfile" ]; then
            service_name=$(basename "$pidfile" .pid)
            pid=$(cat "$pidfile")
            
            if kill -0 "$pid" 2>/dev/null; then
                print_info "Stopping $service_name (PID: $pid)..."
                if kill "$pid" 2>/dev/null; then
                    # Wait for process to stop
                    local attempts=0
                    while kill -0 "$pid" 2>/dev/null && [ $attempts -lt 30 ]; do
                        sleep 1
                        attempts=$((attempts + 1))
                    done
                    
                    if kill -0 "$pid" 2>/dev/null; then
                        print_warning "Force killing $service_name..."
                        kill -9 "$pid" 2>/dev/null || true
                    fi
                    
                    print_status "$service_name stopped"
                    services_stopped=$((services_stopped + 1))
                else
                    print_error "Failed to stop $service_name"
                fi
            else
                print_info "$service_name was not running"
            fi
            
            rm -f "$pidfile"
        fi
    done
    
    # Also try to stop by port (backup method)
    local ports=(8081 8082 8083)
    local service_names=("API Gateway" "Authorization Service" "Portfolio Service")
    
    for i in "${!ports[@]}"; do
        port=${ports[$i]}
        service_name=${service_names[$i]}
        
        pid=$(lsof -ti :$port 2>/dev/null || true)
        if [ -n "$pid" ]; then
            print_info "Found $service_name still running on port $port (PID: $pid)"
            if kill "$pid" 2>/dev/null; then
                print_status "Stopped $service_name"
                services_stopped=$((services_stopped + 1))
            else
                print_warning "Could not stop $service_name"
            fi
        fi
    done
    
    if [ $services_stopped -gt 0 ]; then
        print_status "Stopped $services_stopped service(s)"
    else
        print_info "No services were running"
    fi
}

# Function to stop infrastructure
stop_infrastructure() {
    print_info "Stopping infrastructure services..."
    
    if command -v docker &> /dev/null; then
        ./gradlew stopInfrastructure
        print_status "Infrastructure services stopped"
    else
        print_warning "Docker not available. Infrastructure services need to be stopped manually."
    fi
}

# Function to cleanup logs and temporary files
cleanup_files() {
    print_info "Cleaning up temporary files..."
    
    # Remove PID files
    rm -f logs/*.pid 2>/dev/null || true
    
    # Optionally clean up log files (commented out to preserve logs)
    # rm -f logs/*.log 2>/dev/null || true
    
    print_status "Cleanup completed"
}

# Function to show final status
show_final_status() {
    echo ""
    print_status "🎉 All services stopped!"
    echo ""
    print_info "📝 Log files are preserved in the logs/ directory"
    print_info "🔄 To start services again: ./run-local.sh"
    echo ""
}

# Main execution
main() {
    local stop_infrastructure_flag=false
    
    # Parse command line arguments
    while [[ $# -gt 0 ]]; do
        case $1 in
            --infrastructure|-i)
                stop_infrastructure_flag=true
                shift
                ;;
            --help|-h)
                echo "Usage: $0 [OPTIONS]"
                echo ""
                echo "Options:"
                echo "  -i, --infrastructure    Also stop infrastructure services (PostgreSQL, Redis, Eureka)"
                echo "  -h, --help             Show this help message"
                echo ""
                echo "Examples:"
                echo "  $0                     Stop only microservices"
                echo "  $0 --infrastructure    Stop microservices and infrastructure"
                exit 0
                ;;
            *)
                print_error "Unknown option: $1"
                echo "Use --help for usage information"
                exit 1
                ;;
        esac
    done
    
    stop_services
    
    if [ "$stop_infrastructure_flag" = true ]; then
        stop_infrastructure
    else
        print_info "Infrastructure services left running (use -i flag to stop them too)"
    fi
    
    cleanup_files
    show_final_status
}

# Run main function
main "$@" 