# 🔍 Health Check Commands for Render Deployment

## 📡 **Static Outbound IPs**
Your Render service uses these static IPs for outbound connections:
- `13.228.225.19`
- `18.142.128.26`
- `54.254.162.138`

## 🏥 **Health Check cURL Commands**

### **1. Basic Health Check**
```bash
curl -X GET "https://your-backend-service.onrender.com/api/health" \
  -H "Accept: application/json" \
  -v
```

### **2. Health Check with Timeout**
```bash
curl -X GET "https://your-backend-service.onrender.com/api/health" \
  -H "Accept: application/json" \
  --connect-timeout 30 \
  --max-time 60 \
  -v
```

### **3. Detailed Health Check (Spring Actuator)**
```bash
curl -X GET "https://your-backend-service.onrender.com/api/actuator/health" \
  -H "Accept: application/json" \
  -v
```

### **4. Database Connection Test**
```bash
curl -X GET "https://your-backend-service.onrender.com/api/actuator/health/db" \
  -H "Accept: application/json" \
  -v
```

### **5. Test Authentication Endpoint**
```bash
curl -X POST "https://your-backend-service.onrender.com/api/users/authenticate" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{
    "email": "admin@iimtrichy.ac.in",
    "password": "admin123"
  }' \
  -v
```

### **6. Test User Listing (Admin)**
```bash
# First get auth token from login, then:
curl -X GET "https://your-backend-service.onrender.com/api/admin/users" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Accept: application/json" \
  -v
```

### **7. Test Tickets Endpoint**
```bash
curl -X GET "https://your-backend-service.onrender.com/api/tickets" \
  -H "Accept: application/json" \
  -v
```

## 🎯 **Expected Responses**

### **Health Check Success:**
```json
{
  "status": "UP"
}
```

### **Detailed Health Success:**
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP"
    }
  }
}
```

### **Authentication Success:**
```json
{
  "authenticated": true,
  "user": {
    "id": "1",
    "username": "admin",
    "email": "admin@iimtrichy.ac.in",
    "role": "ADMIN",
    "firstName": "System",
    "lastName": "Administrator"
  },
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

## 🔧 **Troubleshooting Commands**

### **Check Response Headers:**
```bash
curl -I "https://your-backend-service.onrender.com/api/health"
```

### **Check with Different User-Agent:**
```bash
curl -X GET "https://your-backend-service.onrender.com/api/health" \
  -H "User-Agent: HealthCheck/1.0" \
  -H "Accept: application/json"
```

### **Test from Different IP (if needed):**
```bash
curl -X GET "https://your-backend-service.onrender.com/api/health" \
  --interface 0.0.0.0 \
  -v
```

## 📊 **Monitoring Script**
```bash
#!/bin/bash
# health-monitor.sh

BACKEND_URL="https://your-backend-service.onrender.com"
TIMESTAMP=$(date '+%Y-%m-%d %H:%M:%S')

echo "[$TIMESTAMP] Checking backend health..."

# Health check
HEALTH_RESPONSE=$(curl -s -w "%{http_code}" -o /tmp/health.json "$BACKEND_URL/api/health")
HEALTH_CODE="${HEALTH_RESPONSE: -3}"

if [ "$HEALTH_CODE" = "200" ]; then
    echo "✅ Health check passed (HTTP $HEALTH_CODE)"
    cat /tmp/health.json
else
    echo "❌ Health check failed (HTTP $HEALTH_CODE)"
fi

# Database check
DB_RESPONSE=$(curl -s -w "%{http_code}" -o /tmp/db.json "$BACKEND_URL/api/actuator/health/db")
DB_CODE="${DB_RESPONSE: -3}"

if [ "$DB_CODE" = "200" ]; then
    echo "✅ Database check passed (HTTP $DB_CODE)"
else
    echo "❌ Database check failed (HTTP $DB_CODE)"
fi

echo "----------------------------------------"
```

## 🚀 **Quick Test Script**
```bash
# Replace with your actual backend URL
BACKEND_URL="https://your-backend-service.onrender.com"

echo "Testing backend deployment..."
echo "1. Health check:"
curl -s "$BACKEND_URL/api/health" | jq .

echo -e "\n2. Authentication test:"
curl -s -X POST "$BACKEND_URL/api/users/authenticate" \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@iimtrichy.ac.in","password":"admin123"}' | jq .

echo -e "\n3. Tickets endpoint:"
curl -s "$BACKEND_URL/api/tickets" | jq .
```

---

**Replace `your-backend-service.onrender.com` with your actual Render backend URL!** 🎯
