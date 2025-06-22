#!/bin/bash

set -e

ENVIRONMENT=dev
COMPOSE_FILE=docker-compose.dev.yml
ENV_FILE=.env.dev

echo "🚀 Deploying Technology Portfolio System to DEV Environment"
echo "============================================================"

# Create directories
mkdir -p nginx ssl logs backups

# Generate SSL certificates (self-signed for dev)
if [ ! -f ssl/server.crt ]; then
    echo "📜 Generating SSL certificates..."
    openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
        -keyout ssl/server.key \
        -out ssl/server.crt \
        -subj "/C=US/ST=State/L=City/O=Company/CN=dev.techportfolio.company.com"
fi

# Pull latest images
echo "📥 Pulling latest Docker images..."
docker-compose -f $COMPOSE_FILE pull

# Stop existing services
echo "🛑 Stopping existing services..."
docker-compose -f $COMPOSE_FILE --env-file $ENV_FILE down

# Start services
echo "🚀 Starting services..."
docker-compose -f $COMPOSE_FILE --env-file $ENV_FILE up -d

# Wait for services to be ready
echo "⏳ Waiting for services to be ready..."
sleep 30

# Health checks
echo "🔍 Running health checks..."
./health-check.sh dev

echo "✅ DEV deployment completed successfully!"
echo "🌐 Application URL: http://dev.techportfolio.company.com"
echo "📊 Monitoring: http://dev.techportfolio.company.com/monitoring" 