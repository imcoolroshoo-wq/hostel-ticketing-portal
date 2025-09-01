#!/bin/bash

echo "Starting All-in-One Hostel Ticketing Portal..."

# Initialize PostgreSQL data directory if it doesn't exist
if [ ! -d "/var/lib/postgresql/14/main" ]; then
    echo "Initializing PostgreSQL..."
    mkdir -p /var/lib/postgresql/14/main
    chown postgres:postgres /var/lib/postgresql/14/main
    sudo -u postgres /usr/lib/postgresql/14/bin/initdb -D /var/lib/postgresql/14/main
fi

# Start PostgreSQL
echo "Starting PostgreSQL..."
sudo -u postgres /usr/lib/postgresql/14/bin/pg_ctl -D /var/lib/postgresql/14/main -l /var/log/postgresql.log start

# Wait for PostgreSQL to be ready
echo "Waiting for PostgreSQL to be ready..."
until sudo -u postgres psql -c '\q'; do
    echo "PostgreSQL is unavailable - sleeping"
    sleep 1
done

# Create database and user if they don't exist
echo "Setting up database..."
sudo -u postgres psql -c "CREATE USER hostel_user WITH PASSWORD 'hostel_password';" 2>/dev/null || true
sudo -u postgres psql -c "CREATE DATABASE hostel_ticketing OWNER hostel_user;" 2>/dev/null || true
sudo -u postgres psql -c "GRANT ALL PRIVILEGES ON DATABASE hostel_ticketing TO hostel_user;" 2>/dev/null || true

# Wait a bit more for database to be fully ready
sleep 3

# Start backend
echo "Starting Spring Boot backend..."
cd /app/backend
java -jar target/ticketing-portal-1.0.0.jar --spring.profiles.active=allinone &
BACKEND_PID=$!

# Wait for backend to start
echo "Waiting for backend to be ready..."
until curl -f http://localhost:8080/api/health; do
    echo "Backend is unavailable - sleeping"
    sleep 2
done

# Start Nginx
echo "Starting Nginx..."
nginx -g "daemon off;" &
NGINX_PID=$!

echo "All services started successfully!"
echo "Frontend: http://localhost"
echo "Backend API: http://localhost/api"
echo "Direct Backend: http://localhost:8080/api"

# Keep the container running
wait $BACKEND_PID $NGINX_PID
