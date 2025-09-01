#!/bin/bash

# Hostel Ticketing Portal Setup Script
# This script sets up the complete development environment

set -e

echo "🏠 Hostel Ticketing Portal Setup"
echo "================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if Docker is installed
check_docker() {
    print_status "Checking Docker installation..."
    if ! command -v docker &> /dev/null; then
        print_error "Docker is not installed. Please install Docker first."
        print_status "Visit: https://docs.docker.com/get-docker/"
        exit 1
    fi
    
    if ! docker info &> /dev/null; then
        print_error "Docker is not running. Please start Docker first."
        exit 1
    fi
    
    print_success "Docker is installed and running"
}

# Check if Docker Compose is installed
check_docker_compose() {
    print_status "Checking Docker Compose installation..."
    if ! command -v docker-compose &> /dev/null; then
        print_error "Docker Compose is not installed. Please install Docker Compose first."
        print_status "Visit: https://docs.docker.com/compose/install/"
        exit 1
    fi
    
    print_success "Docker Compose is installed"
}

# Check if Java is installed (for local development)
check_java() {
    print_status "Checking Java installation..."
    if ! command -v java &> /dev/null; then
        print_warning "Java is not installed. You can still use Docker for development."
        print_status "To install Java locally, visit: https://adoptium.net/"
    else
        JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)
        print_success "Java $JAVA_VERSION is installed"
    fi
}

# Check if Node.js is installed (for local development)
check_node() {
    print_status "Checking Node.js installation..."
    if ! command -v node &> /dev/null; then
        print_warning "Node.js is not installed. You can still use Docker for development."
        print_status "To install Node.js locally, visit: https://nodejs.org/"
    else
        NODE_VERSION=$(node --version)
        print_success "Node.js $NODE_VERSION is installed"
    fi
}

# Create SSL certificates for development
create_ssl_certs() {
    print_status "Creating SSL certificates for development..."
    
    if [ ! -d "nginx/ssl" ]; then
        mkdir -p nginx/ssl
    fi
    
    if [ ! -f "nginx/ssl/cert.pem" ] || [ ! -f "nginx/ssl/key.pem" ]; then
        print_status "Generating self-signed SSL certificates..."
        openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
            -keyout nginx/ssl/key.pem \
            -out nginx/ssl/cert.pem \
            -subj "/C=US/ST=State/L=City/O=Organization/CN=localhost"
        print_success "SSL certificates created"
    else
        print_success "SSL certificates already exist"
    fi
}

# Build and start services
start_services() {
    print_status "Building and starting services..."
    
    # Stop any existing containers
    docker-compose down --remove-orphans
    
    # Build and start services
    docker-compose up --build -d
    
    print_success "Services started successfully"
}

# Wait for services to be ready
wait_for_services() {
    print_status "Waiting for services to be ready..."
    
    # Wait for PostgreSQL
    print_status "Waiting for PostgreSQL..."
    until docker-compose exec -T postgres pg_isready -U hostel_user -d hostel_ticketing; do
        sleep 2
    done
    print_success "PostgreSQL is ready"
    
    # Wait for Redis
    print_status "Waiting for Redis..."
    until docker-compose exec -T redis redis-cli ping; do
        sleep 2
    done
    print_success "Redis is ready"
    
    # Wait for Backend
    print_status "Waiting for Backend..."
    until curl -s http://localhost:8080/api/actuator/health > /dev/null 2>&1; do
        sleep 5
    done
    print_success "Backend is ready"
    
    # Wait for Frontend
    print_status "Waiting for Frontend..."
    until curl -f http://localhost:3000 > /dev/null 2>&1; do
        sleep 5
    done
    print_success "Frontend is ready"
}

# Show service status
show_status() {
    print_status "Service Status:"
    echo ""
    docker-compose ps
    echo ""
    
    print_status "Service URLs:"
    echo "  Frontend:     http://localhost:3000"
    echo "  Backend API:  http://localhost:8080/api"
    echo "  Swagger UI:   http://localhost:8080/swagger-ui.html"
    echo "  PostgreSQL:   localhost:5432"
    echo "  Redis:        localhost:6379"
    echo ""
    
    print_status "Default Credentials:"
    echo "  Admin:        admin@hostel.com / admin123"
    echo "  Staff:        staff1@hostel.com / staff123"
    echo "  Student:      student1@university.edu / student123"
    echo ""
}

# Show logs
show_logs() {
    print_status "Showing service logs (Ctrl+C to exit)..."
    docker-compose logs -f
}

# Stop services
stop_services() {
    print_status "Stopping services..."
    docker-compose down
    print_success "Services stopped"
}

# Clean up everything
cleanup() {
    print_status "Cleaning up everything..."
    docker-compose down -v --remove-orphans
    docker system prune -f
    print_success "Cleanup completed"
}

# Main menu
show_menu() {
    echo ""
    echo "What would you like to do?"
    echo "1. Setup and start all services"
    echo "2. Start services only"
    echo "3. Stop services"
    echo "4. Show service status"
    echo "5. Show logs"
    echo "6. Cleanup everything"
    echo "7. Exit"
    echo ""
    read -p "Enter your choice (1-7): " choice
    
    case $choice in
        1)
            check_docker
            check_docker_compose
            check_java
            check_node
            create_ssl_certs
            start_services
            wait_for_services
            show_status
            ;;
        2)
            start_services
            wait_for_services
            show_status
            ;;
        3)
            stop_services
            ;;
        4)
            show_status
            ;;
        5)
            show_logs
            ;;
        6)
            cleanup
            ;;
        7)
            print_status "Goodbye!"
            exit 0
            ;;
        *)
            print_error "Invalid choice. Please try again."
            show_menu
            ;;
    esac
}

# Check if script is run with arguments
if [ $# -eq 0 ]; then
    show_menu
else
    case $1 in
        "start")
            check_docker
            check_docker_compose
            create_ssl_certs
            start_services
            wait_for_services
            show_status
            ;;
        "stop")
            stop_services
            ;;
        "status")
            show_status
            ;;
        "logs")
            show_logs
            ;;
        "cleanup")
            cleanup
            ;;
        "setup")
            check_docker
            check_docker_compose
            check_java
            check_node
            create_ssl_certs
            start_services
            wait_for_services
            show_status
            ;;
        *)
            print_error "Usage: $0 [start|stop|status|logs|cleanup|setup]"
            print_error "Or run without arguments for interactive menu"
            exit 1
            ;;
    esac
fi 