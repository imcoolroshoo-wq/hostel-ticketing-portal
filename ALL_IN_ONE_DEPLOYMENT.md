# 🚀 All-in-One Container Deployment

## What This Does
- ✅ **Single Container** with Frontend + Backend + Database
- ✅ **Zero configuration** needed
- ✅ **Works anywhere** that supports Docker
- ✅ **Complete isolation** - everything runs together
- ✅ **Simple deployment** - one container, one command

## 🎯 Quick Deployment Options

### **Option 1: Railway (Recommended)**
1. Go to [railway.app](https://railway.app)
2. **New Project** → **Deploy from GitHub repo**
3. Select your `hostel-ticketing-portal` repository
4. **Root Directory**: Leave empty (uses root)
5. **Dockerfile**: `Dockerfile.all-in-one`
6. Click **Deploy**

**That's it!** Railway will build and deploy everything in one container.

### **Option 2: Render**
1. Go to [render.com](https://render.com)
2. **New** → **Web Service**
3. Connect your GitHub repository
4. **Environment**: Docker
5. **Dockerfile Path**: `Dockerfile.all-in-one`
6. **Plan**: Free
7. Click **Create Web Service**

### **Option 3: DigitalOcean App Platform**
1. Go to [digitalocean.com/products/app-platform](https://digitalocean.com/products/app-platform)
2. **Create App** → **GitHub**
3. Select your repository
4. **Dockerfile**: `Dockerfile.all-in-one`
5. Deploy

### **Option 4: Fly.io**
```bash
# Install Fly CLI
curl -L https://fly.io/install.sh | sh

# Deploy
fly launch --dockerfile Dockerfile.all-in-one
fly deploy
```

## 🔧 Local Testing

### **Build and Run Locally:**
```bash
# Build the container
docker build -f Dockerfile.all-in-one -t hostel-app .

# Run the container
docker run -p 80:80 -p 8080:8080 hostel-app
```

### **Access Your App:**
- **Frontend**: http://localhost
- **Backend API**: http://localhost/api
- **Health Check**: http://localhost/api/health

## 🏆 **What's Included**

### **Components:**
- ✅ **PostgreSQL Database** (port 5432)
- ✅ **Spring Boot Backend** (port 8080)
- ✅ **React Frontend** (served via Nginx on port 80)
- ✅ **Nginx Reverse Proxy** (handles routing)

### **Default Users:**
- **Admin**: `admin@iimtrichy.ac.in` / `admin123`
- **Student**: `student@iimtrichy.ac.in` / `student123`
- **Staff**: `staff@iimtrichy.ac.in` / `staff123`

## 📋 **Container Architecture**

```
┌─── Port 80 (Nginx) ───┐
│  Frontend (React)     │
│  API Proxy (/api/*)   │ ──→ Port 8080 (Spring Boot)
└───────────────────────┘         │
                                  │
                                  ▼
                         Port 5432 (PostgreSQL)
```

## 🎯 **Deployment Timeline**

- **Build time**: 5-10 minutes (first deployment)
- **Container size**: ~2GB
- **Memory usage**: ~1GB RAM
- **Startup time**: 2-3 minutes

## 🔍 **Health Checks**

The container includes built-in health checks:
- **Database**: PostgreSQL connection test
- **Backend**: Spring Boot actuator health
- **Frontend**: Nginx status

## 💰 **Cost Estimates**

### **Railway**: 
- Free tier: $5 credit/month
- Your app: ~$8-12/month

### **Render**:
- Free tier: 750 hours/month
- Perfect for development/testing

### **DigitalOcean**:
- $5/month for basic tier
- Includes 512MB RAM

## 🚀 **Ready to Deploy?**

### **I recommend Railway** because:
1. Easiest deployment (click and deploy)
2. Automatic HTTPS
3. Good free tier
4. No configuration needed

**Go to [railway.app](https://railway.app) and deploy your all-in-one container now!**

### **After Deployment:**
1. Wait 5-10 minutes for build
2. Get your Railway URL
3. Test at `https://your-app.railway.app`
4. Login with admin credentials

**Your complete hostel management system will be live in one container!** 🎉
