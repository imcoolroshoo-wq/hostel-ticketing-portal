#!/bin/bash

# Render startup script for backend
set -e

echo "Starting Render deployment..."

# Wait for database to be ready
echo "Waiting for database connection..."
if [ -n "$DATABASE_URL" ]; then
    # Extract connection details from DATABASE_URL
    DB_HOST=$(echo $DATABASE_URL | sed 's/.*@\([^:]*\).*/\1/')
    DB_PORT=$(echo $DATABASE_URL | sed 's/.*:\([0-9]*\)\/.*/\1/')
    DB_USER=$(echo $DATABASE_URL | sed 's/.*\/\/\([^:]*\):.*/\1/')
    
    # Wait for database to be ready
    until pg_isready -h $DB_HOST -p $DB_PORT -U $DB_USER; do
        echo "Database is unavailable - sleeping"
        sleep 2
    done
    
    echo "Database is ready!"
    
    # Run database initialization if needed
    if [ -f "/app/railway-init.sql" ]; then
        echo "Running database initialization..."
        psql $DATABASE_URL -f /app/railway-init.sql || echo "Database initialization completed (may have been run before)"
    fi
else
    echo "DATABASE_URL not set, skipping database initialization"
fi

# Start the Spring Boot application
echo "Starting Spring Boot application..."
exec java $JAVA_OPTS -Dspring.profiles.active=render -jar app.jar
