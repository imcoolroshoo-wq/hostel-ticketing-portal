# 🏠 Hostel Ticketing Portal

A comprehensive, role-based ticketing system for hostel management, built with modern technologies and designed for seamless user experience across different user roles.

## ✨ Features Overview

### 🎯 **Role-Based Access Control**
- **Students**: Create tickets, track progress, quick issue reporting
- **Staff**: Manage assigned tickets, update statuses, comprehensive ticket overview
- **Admins**: Full system control, user management, analytics, and reporting

### 🎫 **Advanced Ticket Management**
- **Smart Categories**: Maintenance, Housekeeping, Security, Facilities, Student Services, Emergency
- **Priority Levels**: Low, Medium, High, Urgent with automatic escalation
- **Status Tracking**: Open → In Progress → Pending → Resolved → Closed
- **Location-based**: Building and room-specific ticket management
- **Attachment Support**: File uploads for better issue documentation

### 📊 **Comprehensive Analytics & Reporting**
- **Real-time Dashboards**: Role-specific insights and metrics
- **Performance Analytics**: Resolution times, satisfaction rates, trend analysis
- **Export Capabilities**: PDF, Excel, CSV reports
- **Category Breakdown**: Detailed analysis by ticket types
- **Monthly Trends**: Historical data and performance tracking

### 🔔 **Smart Notification System**
- **Real-time Alerts**: Instant notifications for ticket updates
- **Priority-based**: Urgent notifications get immediate attention
- **Customizable Settings**: Email, push, and in-app notification preferences
- **Role-specific**: Tailored notifications based on user responsibilities

### 🎨 **Modern User Interface**
- **Responsive Design**: Works seamlessly on desktop, tablet, and mobile
- **Material Design**: Clean, intuitive interface with Material-UI components
- **Dark/Light Themes**: User preference support
- **Interactive Components**: Rich forms, dialogs, and data visualization

## 🏗️ Tech Stack

### Backend
- **Java 17** with Spring Boot 3.x
- **PostgreSQL** for robust data persistence
- **Redis** for caching and session management
- **Spring Security** for authentication and authorization
- **JPA/Hibernate** for ORM
- **Docker** for containerization

### Frontend
- **React 18** with TypeScript for type safety
- **Material-UI (MUI)** for modern, accessible UI components
- **React Router** for client-side routing
- **Axios** for HTTP client
- **Context API** for state management

### Infrastructure
- **Docker Compose** for multi-container orchestration
- **Nginx** for reverse proxy and load balancing
- **PostgreSQL** with optimized indexes
- **Redis** for performance optimization

## 🚀 Quick Start

### Prerequisites
- Docker and Docker Compose
- Java 17+ (for local development)
- Node.js 18+ (for local development)

### 🐳 Running with Docker (Recommended)

1. **Clone the repository:**
```bash
git clone <repository-url>
cd hostel-ticketing-portal
```

2. **Start all services:**
```bash
docker-compose up -d
```

3. **Access the application:**
- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080
- **Database**: localhost:5432 (postgres/postgres)
- **Redis**: localhost:6379

### 👥 Demo Accounts

The system includes pre-configured demo accounts for testing:

| Role | Email | Password | Features |
|------|-------|----------|----------|
| **Student** | student1@university.edu | student123 | Create tickets, track progress, quick actions |
| **Staff** | staff1@hostel.com | staff123 | Manage tickets, assign tasks, generate reports |
| **Admin** | admin@hostel.com | admin123 | Full system access, user management, analytics |

## 🎯 User Role Features

### 👨‍🎓 **Student Dashboard**
- **Personal Overview**: User info, room details, contact information
- **Quick Actions**: Pre-configured templates for common issues
- **My Tickets**: Personal ticket history and status tracking
- **Interactive Forms**: Easy ticket creation with location auto-fill
- **Progress Tracking**: Real-time updates on ticket resolution

### 🛠️ **Staff Dashboard**
- **Ticket Management**: Comprehensive view of all tickets
- **Assignment System**: Assign tickets to team members
- **Status Updates**: Bulk operations and quick status changes
- **Performance Metrics**: Individual and team performance tracking
- **Priority Management**: Focus on urgent and high-priority tickets

### 👑 **Admin Dashboard**
- **System Overview**: Complete system health and metrics
- **User Management**: Create, edit, activate/deactivate users
- **Advanced Analytics**: Detailed reports and trend analysis
- **System Settings**: Configuration and security management
- **Bulk Operations**: Mass ticket operations and data management

## 📊 Advanced Features

### 🔍 **Smart Reporting System**
- **Interactive Charts**: Visual representation of ticket data
- **Custom Date Ranges**: Flexible reporting periods
- **Category Analysis**: Breakdown by maintenance types
- **Performance Metrics**: Resolution times, satisfaction rates
- **Export Options**: Multiple format support (PDF, Excel, CSV)

### 🔔 **Notification Management**
- **Multi-channel Delivery**: Email, push, in-app notifications
- **Smart Filtering**: Role-based notification routing
- **Priority Handling**: Urgent notifications bypass normal queues
- **Customizable Settings**: User-controlled notification preferences
- **Batch Processing**: Efficient handling of multiple notifications

### 🔐 **Security & Permissions**
- **JWT Authentication**: Secure token-based authentication
- **Role-based Authorization**: Granular permission system
- **Data Encryption**: Sensitive data protection
- **Audit Logging**: Complete activity tracking
- **Session Management**: Secure session handling

## 🛠️ Development

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

### Database Setup
```bash
# The database is automatically initialized with Docker
# For manual setup, run the SQL scripts in backend/src/main/resources/db/
```

## 📚 API Documentation

### Authentication Endpoints
- `POST /api/users/authenticate` - User login
- `POST /api/users/logout` - User logout
- `GET /api/users/profile/{email}` - Get user profile

### Ticket Management
- `GET /api/tickets` - Get all tickets (with pagination)
- `POST /api/tickets` - Create new ticket
- `GET /api/tickets/{id}` - Get ticket details
- `PUT /api/tickets/{id}` - Update ticket
- `DELETE /api/tickets/{id}` - Delete ticket

### User Management (Admin only)
- `GET /api/users` - Get all users
- `POST /api/users` - Create new user
- `PUT /api/users/{id}` - Update user
- `PATCH /api/users/{id}/status` - Toggle user status

## 🎨 UI/UX Features

### Responsive Design
- **Mobile-first**: Optimized for mobile devices
- **Tablet Support**: Enhanced experience on tablets
- **Desktop**: Full-featured desktop interface

### Accessibility
- **WCAG Compliant**: Meets accessibility standards
- **Keyboard Navigation**: Full keyboard support
- **Screen Reader**: Compatible with assistive technologies
- **High Contrast**: Support for visual impairments

### Interactive Elements
- **Real-time Updates**: Live data refresh
- **Progressive Loading**: Smooth loading experiences
- **Error Handling**: Graceful error management
- **Offline Support**: Basic offline functionality

## 🔧 Configuration

### Environment Variables
```bash
# Backend
DATABASE_URL=jdbc:postgresql://localhost:5432/hostel_db
REDIS_URL=redis://localhost:6379
JWT_SECRET=your-secret-key

# Frontend
REACT_APP_API_URL=http://localhost:8080/api
```

### Docker Configuration
The system uses Docker Compose for easy deployment:
- **Frontend**: React development server
- **Backend**: Spring Boot application
- **Database**: PostgreSQL with persistent storage
- **Cache**: Redis for session management
- **Proxy**: Nginx for routing and load balancing

## 🚀 Deployment

### Production Deployment
1. **Build the application:**
```bash
docker-compose -f docker-compose.prod.yml build
```

2. **Deploy to production:**
```bash
docker-compose -f docker-compose.prod.yml up -d
```

### Environment Setup
- Configure environment variables for production
- Set up SSL certificates
- Configure backup strategies
- Set up monitoring and logging

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Material-UI team for the excellent component library
- Spring Boot community for the robust framework
- Docker team for containerization technology
- PostgreSQL team for the reliable database system

---

**Built with ❤️ for efficient hostel management**