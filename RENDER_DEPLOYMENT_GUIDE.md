# 🚀 Render Deployment Guide

## ✅ Fixed Issues
- ✅ Docker context issue resolved
- ✅ Database initialization file moved to correct location
- ✅ Render-specific Dockerfiles created
- ✅ Startup scripts configured for Render

## 🎯 **Deploy to Render (Step by Step)**

### **Step 1: Sign Up for Render**
1. Go to [render.com](https://render.com)
2. Click **"Get Started for Free"**
3. Sign up with your GitHub account
4. Connect your GitHub repository

### **Step 2: Create PostgreSQL Database**
1. In Render dashboard, click **"New"** → **"PostgreSQL"**
2. Configure:
   - **Name**: `hostel-ticketing-db`
   - **Database**: `hostel_ticketing`
   - **User**: `hostel_user`
   - **Region**: Choose closest to you
   - **Plan**: **Free** (1GB storage)
3. Click **"Create Database"**
4. **Important**: Copy the **Internal Database URL** (starts with `postgresql://`)

### **Step 3: Deploy Backend Service**
1. Click **"New"** → **"Web Service"**
2. Connect your GitHub repository: `hostel-ticketing-portal`
3. Configure:
   - **Name**: `hostel-backend`
   - **Root Directory**: `backend`
   - **Environment**: `Docker`
   - **Dockerfile Path**: `Dockerfile.render`
   - **Plan**: **Free**

4. **Environment Variables** (click "Advanced" → "Add Environment Variable"):
   ```
   SPRING_PROFILES_ACTIVE=render
   JWT_SECRET=your-super-secure-jwt-secret-change-this-in-production
   DATABASE_URL=<paste-your-database-internal-url-here>
   PORT=10000
   ```

5. Click **"Create Web Service"**

### **Step 4: Deploy Frontend Service**
1. Click **"New"** → **"Web Service"**
2. Same repository: `hostel-ticketing-portal`
3. Configure:
   - **Name**: `hostel-frontend`
   - **Root Directory**: `frontend`
   - **Environment**: `Docker`
   - **Dockerfile Path**: `Dockerfile.render`
   - **Plan**: **Free**

4. **Environment Variables**:
   ```
   REACT_APP_API_URL=https://hostel-backend.onrender.com/api
   BACKEND_URL=https://hostel-backend.onrender.com
   NODE_ENV=production
   PORT=10000
   ```
   
   **Note**: Replace `hostel-backend` with your actual backend service name

5. Click **"Create Web Service"**

### **Step 5: Update CORS Configuration**
1. Go to your backend service
2. Add environment variable:
   ```
   FRONTEND_URL=https://your-frontend-service.onrender.com
   ```
3. Click **"Save Changes"**

## ⏱️ **Deployment Timeline**
- Database creation: 2-3 minutes
- Backend deployment: 5-8 minutes
- Frontend deployment: 3-5 minutes
- **Total: ~15 minutes**

## 🔍 **Monitoring Deployment**
1. **Logs**: Click on each service → "Logs" tab
2. **Events**: Monitor build and deployment progress
3. **Health**: Check service status indicators

## 🎉 **After Successful Deployment**

### **Your URLs will be:**
- **Frontend**: `https://your-frontend-service.onrender.com`
- **Backend API**: `https://your-backend-service.onrender.com/api`
- **Database**: Internal connection (not public)

### **Test Login Credentials:**
- **Admin**: `admin@iimtrichy.ac.in` / `admin123`
- **Student**: `student@iimtrichy.ac.in` / `student123`
- **Staff**: `staff@iimtrichy.ac.in` / `staff123`

## 🔧 **Troubleshooting**

### **Common Issues:**

1. **Backend won't start**:
   - Check logs for database connection errors
   - Verify `DATABASE_URL` is set correctly
   - Ensure PostgreSQL service is running

2. **Frontend can't connect to backend**:
   - Verify `REACT_APP_API_URL` points to correct backend URL
   - Check CORS configuration
   - Ensure backend is running and healthy

3. **Database connection issues**:
   - Use **Internal Database URL** (not External)
   - Check if PostgreSQL service is active
   - Verify database credentials

### **Getting Help:**
- **Render Docs**: [render.com/docs](https://render.com/docs)
- **Render Community**: [community.render.com](https://community.render.com)
- **Service Logs**: Check individual service logs for errors

## 💰 **Render Free Tier Limits**
- **Web Services**: 750 hours/month (enough for your app)
- **PostgreSQL**: 1GB storage, 1 month retention
- **Bandwidth**: 100GB/month
- **Build Time**: 500 minutes/month

Your application should fit comfortably within these limits!

## 🎯 **Expected Results**
- ✅ **Live web application** with HTTPS
- ✅ **PostgreSQL database** with sample data
- ✅ **Automatic deployments** from GitHub
- ✅ **Professional URLs** (.onrender.com domains)
- ✅ **SSL certificates** automatically managed
- ✅ **Monitoring and logs** in Render dashboard

**Ready to deploy?** Follow the steps above and your app will be live! 🚀
