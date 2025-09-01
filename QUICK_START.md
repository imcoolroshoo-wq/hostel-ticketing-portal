# 🚀 Quick Start Guide - Hostel Ticketing Portal

Get your hostel ticketing portal up and running in minutes!

## Prerequisites

- **Docker** (required)
- **Docker Compose** (required)
- **Java 17+** (optional, for local development)
- **Node.js 18+** (optional, for local development)

## 🐳 Quick Start with Docker (Recommended)

### 1. Clone the Repository
```bash
git clone <your-repo-url>
cd hostel-ticketing-portal
```

### 2. Run the Setup Script
```bash
./setup.sh setup
```

This single command will:
- ✅ Check your system requirements
- ✅ Create SSL certificates
- ✅ Build and start all services
- ✅ Wait for services to be ready
- ✅ Show you the access URLs

### 3. Access Your Portal

Once setup is complete, you can access:

- **🌐 Frontend**: http://localhost:3000
- **🔧 Backend API**: http://localhost:8080/api
- **📚 API Documentation**: http://localhost:8080/swagger-ui.html
- **🗄️ Database**: localhost:5432
- **⚡ Cache**: localhost:6379

### 4. Login with Default Credentials

| Role | Username | Password | Access Level |
|------|----------|----------|--------------|
| **Admin** | `admin@hostel.com` | `admin123` | Full system access |
| **Staff** | `staff1@hostel.com` | `staff123` | Ticket management |
| **Student** | `student1@university.edu` | `student123` | Create/view tickets |

## 🛠️ Manual Setup

If you prefer manual setup or want to customize:

### 1. Create SSL Certificates
```bash
mkdir -p nginx/ssl
openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
  -keyout nginx/ssl/key.pem \
  -out nginx/ssl/cert.pem \
  -subj "/C=US/ST=State/L=City/O=Organization/CN=localhost"
```

### 2. Start Services
```bash
docker-compose up --build -d
```

### 3. Wait for Services
```bash
# Wait for database
until docker-compose exec -T postgres pg_isready -U hostel_user -d hostel_ticketing; do sleep 2; done

# Wait for backend
until curl -f http://localhost:8080/api/actuator/health; do sleep 5; done

# Wait for frontend
until curl -f http://localhost:3000; do sleep 5; done
```

## 🔧 Development Mode

### Backend Development
```bash
cd backend
./mvnw spring-boot:run
```

### Frontend Development
```bash
cd frontend
npm install
npm start
```

## 📊 What You Get

### ✅ Complete Ticketing System
- **Ticket Creation**: Students can report issues
- **Ticket Management**: Staff can assign and track tickets
- **Real-time Updates**: WebSocket notifications
- **File Attachments**: Support for images and documents
- **Role-based Access**: Different permissions for different users

### ✅ Comprehensive Categories
- 🔧 **Maintenance**: Plumbing, electrical, HVAC
- 🧹 **Housekeeping**: Cleaning, pest control
- 🔒 **Security**: Access control, safety issues
- 🏢 **Facilities**: Common areas, equipment
- 👨‍🎓 **Student Services**: Room changes, requests
- 🚨 **Emergency**: Urgent issues

### ✅ Advanced Features
- **Priority Management**: Low, Medium, High, Urgent
- **Status Tracking**: Open → In Progress → Resolved → Closed
- **Escalation System**: Automatic escalation for overdue tickets
- **Audit Trail**: Complete history of all changes
- **Reporting**: Analytics and performance metrics
- **Maintenance Scheduling**: Planned maintenance activities

## 🚨 Troubleshooting

### Common Issues

#### Port Already in Use
```bash
# Check what's using the port
lsof -i :8080
lsof -i :3000

# Stop conflicting services or change ports in docker-compose.yml
```

#### Database Connection Issues
```bash
# Check database status
docker-compose exec postgres pg_isready -U hostel_user -d hostel_ticketing

# View database logs
docker-compose logs postgres
```

#### Frontend Not Loading
```bash
# Check frontend logs
docker-compose logs frontend

# Rebuild frontend
docker-compose up --build frontend
```

### Reset Everything
```bash
# Stop and remove all containers, volumes, and images
./setup.sh cleanup

# Start fresh
./setup.sh setup
```

## 📚 Next Steps

### 1. Explore the System
- Login as different user types
- Create sample tickets
- Test the workflow

### 2. Customize Configuration
- Edit `backend/src/main/resources/application.yml`
- Modify `docker-compose.yml` for your environment
- Update `nginx/nginx.conf` for production

### 3. Add Your Data
- Import your user database
- Configure your buildings and rooms
- Set up email/SMS notifications

### 4. Deploy to Production
- Update environment variables
- Configure SSL certificates
- Set up monitoring and backups

## 🆘 Need Help?

### Quick Commands
```bash
./setup.sh status    # Check service status
./setup.sh logs      # View all logs
./setup.sh stop      # Stop services
./setup.sh start     # Start services
```

### Documentation
- **API Docs**: [API_DOCUMENTATION.md](API_DOCUMENTATION.md)
- **README**: [README.md](README.md)
- **Database Schema**: Check the init.sql file

### Support
- Check the logs: `./setup.sh logs`
- Verify service status: `./setup.sh status`
- Review configuration files
- Check Docker container health

## 🎉 You're All Set!

Your hostel ticketing portal is now running with:
- ✅ **Backend API** with Spring Boot
- ✅ **Frontend UI** with React
- ✅ **Database** with PostgreSQL
- ✅ **Cache** with Redis
- ✅ **Reverse Proxy** with Nginx
- ✅ **SSL** encryption
- ✅ **Sample data** to get started

Start creating tickets and managing your hostel issues efficiently! 🏠✨ 