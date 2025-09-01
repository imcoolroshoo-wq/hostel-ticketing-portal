#!/bin/bash

# Railway startup script for backend
set -e

echo "Starting Railway deployment..."

# Wait for database to be ready
echo "Waiting for database connection..."
until pg_isready -h $(echo $DATABASE_URL | sed 's/.*@\([^:]*\).*/\1/') -p $(echo $DATABASE_URL | sed 's/.*:\([0-9]*\)\/.*/\1/') -U $(echo $DATABASE_URL | sed 's/.*\/\/\([^:]*\):.*/\1/'); do
  echo "Database is unavailable - sleeping"
  sleep 2
done

echo "Database is ready!"

# Run database initialization if needed
if [ -f "/app/railway-init.sql" ]; then
    echo "Running database initialization..."
    psql $DATABASE_URL -f /app/railway-init.sql || echo "Database initialization completed (may have been run before)"
fi

# Start the Spring Boot application
echo "Starting Spring Boot application..."
exec java $JAVA_OPTS -Dspring.profiles.active=railway -jar app.jar
