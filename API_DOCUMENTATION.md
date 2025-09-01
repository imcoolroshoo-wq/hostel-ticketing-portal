# Hostel Ticketing Portal API Documentation

## Overview

The Hostel Ticketing Portal provides a comprehensive REST API for managing hostel-related issues and their resolution. The API is built using Spring Boot and follows RESTful principles.

## Base URL

- **Development**: `http://localhost:8080/api`
- **Production**: `https://your-domain.com/api`

## Authentication

The API uses JWT (JSON Web Token) authentication. Include the token in the Authorization header:

```
Authorization: Bearer <your-jwt-token>
```

## API Endpoints

### Authentication

#### POST /auth/login
Authenticate a user and receive a JWT token.

**Request Body:**
```json
{
  "username": "student1",
  "password": "student123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": "uuid",
    "username": "student1",
    "email": "student1@university.edu",
    "firstName": "Alice",
    "lastName": "Johnson",
    "role": "STUDENT",
    "studentId": "STU001",
    "roomNumber": "101",
    "building": "Block A"
  }
}
```

#### POST /auth/refresh
Refresh an expired JWT token.

**Request Body:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

#### POST /auth/logout
Logout a user (invalidate token).

**Headers:**
```
Authorization: Bearer <your-jwt-token>
```

### Users

#### GET /users/profile
Get current user's profile.

**Headers:**
```
Authorization: Bearer <your-jwt-token>
```

**Response:**
```json
{
  "id": "uuid",
  "username": "student1",
  "email": "student1@university.edu",
  "firstName": "Alice",
  "lastName": "Johnson",
  "role": "STUDENT",
  "studentId": "STU001",
  "roomNumber": "101",
  "building": "Block A",
  "phone": "+1234567893",
  "isActive": true,
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

#### PUT /users/profile
Update current user's profile.

**Headers:**
```
Authorization: Bearer <your-jwt-token>
```

**Request Body:**
```json
{
  "firstName": "Alice",
  "lastName": "Johnson",
  "phone": "+1234567893"
}
```

#### GET /users (Admin/Staff only)
Get all users with pagination and filtering.

**Headers:**
```
Authorization: Bearer <your-jwt-token>
```

**Query Parameters:**
- `page`: Page number (default: 0)
- `size`: Page size (default: 20)
- `role`: Filter by role (STUDENT, STAFF, ADMIN)
- `building`: Filter by building
- `isActive`: Filter by active status

**Response:**
```json
{
  "content": [
    {
      "id": "uuid",
      "username": "student1",
      "email": "student1@university.edu",
      "firstName": "Alice",
      "lastName": "Johnson",
      "role": "STUDENT",
      "studentId": "STU001",
      "roomNumber": "101",
      "building": "Block A",
      "isActive": true
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 20,
  "number": 0
}
```

### Tickets

#### POST /tickets
Create a new ticket.

**Headers:**
```
Authorization: Bearer <your-jwt-token>
```

**Request Body:**
```json
{
  "title": "Leaky Faucet in Bathroom",
  "description": "The faucet in the bathroom is leaking continuously, causing water wastage and potential damage to the floor.",
  "category": "MAINTENANCE",
  "priority": "MEDIUM",
  "roomNumber": "101",
  "building": "Block A",
  "locationDetails": "Bathroom - Right side sink"
}
```

**Response:**
```json
{
  "id": "uuid",
  "ticketNumber": "TKT-2024-015-000001",
  "title": "Leaky Faucet in Bathroom",
  "description": "The faucet in the bathroom is leaking continuously, causing water wastage and potential damage to the floor.",
  "category": "MAINTENANCE",
  "priority": "MEDIUM",
  "status": "OPEN",
  "roomNumber": "101",
  "building": "Block A",
  "locationDetails": "Bathroom - Right side sink",
  "createdBy": {
    "id": "uuid",
    "username": "student1",
    "firstName": "Alice",
    "lastName": "Johnson"
  },
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

#### GET /tickets
Get tickets with pagination and filtering.

**Headers:**
```
Authorization: Bearer <your-jwt-token>
```

**Query Parameters:**
- `page`: Page number (default: 0)
- `size`: Page size (default: 20)
- `status`: Filter by status (OPEN, IN_PROGRESS, PENDING, RESOLVED, CLOSED, CANCELLED)
- `priority`: Filter by priority (LOW, MEDIUM, HIGH, URGENT)
- `category`: Filter by category (MAINTENANCE, HOUSEKEEPING, SECURITY, FACILITIES, STUDENT_SERVICES, EMERGENCY)
- `building`: Filter by building
- `roomNumber`: Filter by room number
- `assignedTo`: Filter by assigned staff
- `createdBy`: Filter by ticket creator
- `createdAfter`: Filter by creation date (ISO format)
- `createdBefore`: Filter by creation date (ISO format)

**Response:**
```json
{
  "content": [
    {
      "id": "uuid",
      "ticketNumber": "TKT-2024-015-000001",
      "title": "Leaky Faucet in Bathroom",
      "category": "MAINTENANCE",
      "priority": "MEDIUM",
      "status": "OPEN",
      "roomNumber": "101",
      "building": "Block A",
      "createdAt": "2024-01-15T10:30:00",
      "createdBy": {
        "id": "uuid",
        "firstName": "Alice",
        "lastName": "Johnson"
      },
      "assignedTo": {
        "id": "uuid",
        "firstName": "John",
        "lastName": "Maintenance"
      }
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 20,
  "number": 0
}
```

#### GET /tickets/{id}
Get a specific ticket by ID.

**Headers:**
```
Authorization: Bearer <your-jwt-token>
```

**Response:**
```json
{
  "id": "uuid",
  "ticketNumber": "TKT-2024-015-000001",
  "title": "Leaky Faucet in Bathroom",
  "description": "The faucet in the bathroom is leaking continuously, causing water wastage and potential damage to the floor.",
  "category": "MAINTENANCE",
  "priority": "MEDIUM",
  "status": "OPEN",
  "roomNumber": "101",
  "building": "Block A",
  "locationDetails": "Bathroom - Right side sink",
  "estimatedResolutionTime": "2024-01-16T10:30:00",
  "createdBy": {
    "id": "uuid",
    "username": "student1",
    "firstName": "Alice",
    "lastName": "Johnson"
  },
  "assignedTo": {
    "id": "uuid",
    "username": "staff1",
    "firstName": "John",
    "lastName": "Maintenance"
  },
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00",
  "comments": [
    {
      "id": "uuid",
      "comment": "I will check this issue tomorrow morning.",
      "user": {
        "id": "uuid",
        "firstName": "John",
        "lastName": "Maintenance"
      },
      "createdAt": "2024-01-15T11:00:00",
      "isInternal": false
    }
  ],
  "attachments": [],
  "history": [
    {
      "fieldName": "status",
      "oldValue": "OPEN",
      "newValue": "IN_PROGRESS",
      "changedBy": {
        "id": "uuid",
        "firstName": "John",
        "lastName": "Maintenance"
      },
      "changedAt": "2024-01-15T11:00:00"
    }
  ]
}
```

#### PUT /tickets/{id}
Update a ticket (Staff/Admin only).

**Headers:**
```
Authorization: Bearer <your-jwt-token>
```

**Request Body:**
```json
{
  "status": "IN_PROGRESS",
  "priority": "HIGH",
  "assignedTo": "uuid",
  "estimatedResolutionTime": "2024-01-16T10:30:00"
}
```

#### DELETE /tickets/{id}
Cancel a ticket (Creator or Admin only).

**Headers:**
```
Authorization: Bearer <your-jwt-token>
```

### Comments

#### POST /tickets/{id}/comments
Add a comment to a ticket.

**Headers:**
```
Authorization: Bearer <your-jwt-token>
```

**Request Body:**
```json
{
  "comment": "I will check this issue tomorrow morning.",
  "isInternal": false
}
```

**Response:**
```json
{
  "id": "uuid",
  "comment": "I will check this issue tomorrow morning.",
  "user": {
    "id": "uuid",
    "firstName": "John",
    "lastName": "Maintenance"
  },
  "createdAt": "2024-01-15T11:00:00",
  "isInternal": false
}
```

#### GET /tickets/{id}/comments
Get comments for a ticket.

**Headers:**
```
Authorization: Bearer <your-jwt-token>
```

**Query Parameters:**
- `page`: Page number (default: 0)
- `size`: Page size (default: 20)
- `isInternal`: Filter by internal comments (true/false)

### Attachments

#### POST /tickets/{id}/attachments
Upload an attachment to a ticket.

**Headers:**
```
Authorization: Bearer <your-jwt-token>
Content-Type: multipart/form-data
```

**Form Data:**
- `file`: The file to upload (max 10MB)

**Response:**
```json
{
  "id": "uuid",
  "filename": "faucet_leak.jpg",
  "originalFilename": "faucet_leak.jpg",
  "fileSize": 1024000,
  "mimeType": "image/jpeg",
  "uploadedBy": {
    "id": "uuid",
    "firstName": "Alice",
    "lastName": "Johnson"
  },
  "createdAt": "2024-01-15T10:30:00"
}
```

#### GET /tickets/{id}/attachments
Get attachments for a ticket.

**Headers:**
```
Authorization: Bearer <your-jwt-token>
```

#### GET /attachments/{id}/download
Download an attachment.

**Headers:**
```
Authorization: Bearer <your-jwt-token>
```

### Notifications

#### GET /notifications
Get user's notifications.

**Headers:**
```
Authorization: Bearer <your-jwt-token>
```

**Query Parameters:**
- `page`: Page number (default: 0)
- `size`: Page size (default: 20)
- `isRead`: Filter by read status (true/false)
- `type`: Filter by type (EMAIL, SMS, IN_APP)

#### PUT /notifications/{id}/read
Mark a notification as read.

**Headers:**
```
Authorization: Bearer <your-jwt-token>
```

#### PUT /notifications/read-all
Mark all notifications as read.

**Headers:**
```
Authorization: Bearer <your-jwt-token>
```

### Reports (Admin/Staff only)

#### GET /reports/ticket-summary
Get ticket summary statistics.

**Headers:**
```
Authorization: Bearer <your-jwt-token>
```

**Query Parameters:**
- `startDate`: Start date for statistics (ISO format)
- `endDate`: End date for statistics (ISO format)
- `building`: Filter by building

**Response:**
```json
{
  "totalTickets": 150,
  "openTickets": 25,
  "inProgressTickets": 15,
  "resolvedTickets": 100,
  "closedTickets": 10,
  "averageResolutionTime": 48.5,
  "ticketsByCategory": {
    "MAINTENANCE": 60,
    "HOUSEKEEPING": 30,
    "SECURITY": 20,
    "FACILITIES": 25,
    "STUDENT_SERVICES": 10,
    "EMERGENCY": 5
  },
  "ticketsByPriority": {
    "LOW": 40,
    "MEDIUM": 80,
    "HIGH": 25,
    "URGENT": 5
  },
  "ticketsByBuilding": {
    "Block A": 50,
    "Block B": 45,
    "Block C": 55
  }
}
```

#### GET /reports/response-times
Get average response times by category and priority.

**Headers:**
```
Authorization: Bearer <your-jwt-token>
```

#### GET /reports/staff-performance
Get staff performance metrics.

**Headers:**
```
Authorization: Bearer <your-jwt-token>
```

### Maintenance Schedule (Admin/Staff only)

#### GET /maintenance/schedule
Get maintenance schedule.

**Headers:**
```
Authorization: Bearer <your-jwt-token>
```

**Query Parameters:**
- `building`: Filter by building
- `startDate`: Start date (ISO format)
- `endDate`: End date (ISO format)
- `status`: Filter by status

#### POST /maintenance/schedule
Create a maintenance schedule entry.

**Headers:**
```
Authorization: Bearer <your-jwt-token>
```

**Request Body:**
```json
{
  "building": "Block A",
  "maintenanceType": "HVAC Maintenance",
  "scheduledDate": "2024-01-20",
  "estimatedDurationHours": 8,
  "description": "Annual HVAC system maintenance and filter replacement",
  "assignedStaff": "uuid"
}
```

## Error Responses

All error responses follow this format:

```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/tickets",
  "details": [
    {
      "field": "title",
      "message": "Title is required"
    }
  ]
}
```

### Common HTTP Status Codes

- `200 OK`: Request successful
- `201 Created`: Resource created successfully
- `400 Bad Request`: Invalid request data
- `401 Unauthorized`: Authentication required
- `403 Forbidden`: Insufficient permissions
- `404 Not Found`: Resource not found
- `409 Conflict`: Resource conflict
- `422 Unprocessable Entity`: Validation error
- `500 Internal Server Error`: Server error

## Rate Limiting

The API implements rate limiting to prevent abuse:

- **General API**: 20 requests per 10 minutes
- **Login**: 5 requests per 10 minutes
- **File uploads**: 10 requests per 10 minutes

## File Upload Limits

- **Maximum file size**: 10MB
- **Allowed file types**: Images, documents, videos, audio
- **Maximum files per ticket**: 10

## WebSocket Support

The API supports WebSocket connections for real-time updates:

- **Endpoint**: `/ws`
- **Protocol**: STOMP over WebSocket
- **Topics**:
  - `/topic/tickets` - Ticket updates
  - `/topic/notifications` - New notifications
  - `/user/queue/updates` - User-specific updates

## Pagination

All list endpoints support pagination:

- **Page size**: 1-100 (default: 20)
- **Page number**: 0-based indexing
- **Response includes**: content, totalElements, totalPages, size, number

## Filtering and Sorting

Most endpoints support filtering and sorting:

- **Filtering**: By various fields using query parameters
- **Sorting**: By any field using `sort` parameter
- **Direction**: ASC or DESC (default: ASC)

## CORS Configuration

The API supports CORS for cross-origin requests:

- **Allowed origins**: Configurable
- **Allowed methods**: GET, POST, PUT, DELETE, OPTIONS
- **Allowed headers**: Standard headers + Authorization
- **Credentials**: Supported

## Security Features

- **JWT Authentication**: Stateless authentication
- **Role-based Access Control**: Different permissions for different user roles
- **Input Validation**: Comprehensive request validation
- **SQL Injection Protection**: Using JPA/Hibernate
- **XSS Protection**: Input sanitization
- **CSRF Protection**: Token-based protection
- **Rate Limiting**: Prevents abuse
- **Audit Logging**: Tracks all changes 