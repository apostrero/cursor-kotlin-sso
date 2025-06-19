#!/bin/bash

ENVIRONMENT=${1:-dev}
BASE_URL="http://localhost"

if [ "$ENVIRONMENT" != "local" ]; then
    BASE_URL="http://$ENVIRONMENT.techportfolio.company.com"
fi

echo "🔍 Running health checks for $ENVIRONMENT environment..."

# Function to check service health
check_service() {
    local service=$1
    local port=$2
    local endpoint=$3
    local url="$BASE_URL:$port$endpoint"
    
    if [ "$ENVIRONMENT" != "local" ]; then
        url="$BASE_URL$endpoint"
    fi
    
    echo -n "Checking $service... "
    
    if curl -f -s "$url" > /dev/null; then
        echo "✅ OK"
        return 0
    else
        echo "❌ FAILED"
        return 1
    fi
}

# Check infrastructure services
check_service "Eureka" 8761 "/"
check_service "API Gateway" 8081 "/actuator/health"
check_service "Authorization Service" 8082 "/actuator/health"
check_service "Portfolio Service" 8083 "/actuator/health"

# Check database connectivity
echo -n "Checking database connectivity... "
if docker exec postgres-auth psql -U auth_user_dev -d authorization_dev -c "SELECT 1;" > /dev/null 2>&1; then
    echo "✅ OK"
else
    echo "❌ FAILED"
fi

# Check Redis connectivity
echo -n "Checking Redis connectivity... "
if docker exec redis redis-cli ping > /dev/null 2>&1; then
    echo "✅ OK"
else
    echo "❌ FAILED"
fi

echo "🎉 Health checks completed!" 