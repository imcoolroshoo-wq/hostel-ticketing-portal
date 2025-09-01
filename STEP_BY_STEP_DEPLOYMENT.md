# Step-by-Step Railway Deployment Guide

## Prerequisites ✅
- Railway CLI installed ✅
- GitHub repository with your code
- Railway account (you have a token, so this is ready)

## Step 1: Complete Railway Authentication

Since you have a Railway token, you need to complete the authentication:

1. **Option A: Browser Login**
   ```bash
   railway login
   ```
   This will open a browser window for authentication.

2. **Option B: Use Railway Web Interface** (Recommended)
   - Go to [railway.app](https://railway.app)
   - Sign in with your account
   - Use the web interface for deployment

## Step 2: Deploy via Railway Web Interface

### 2.1 Create New Project
1. Go to [railway.app](https://railway.app)
2. Click "New Project"
3. Select "Deploy from GitHub repo"
4. Connect your GitHub account if not already connected
5. Select your `hostel-ticketing-portal` repository

### 2.2 Deploy Database First
1. In your new project, click "New Service"
2. Select "Database" → "PostgreSQL"
3. Railway will create a PostgreSQL database
4. Note: The `DATABASE_URL` will be automatically available to other services

### 2.3 Deploy Backend Service
1. Click "New Service" → "GitHub Repo"
2. Select your repository again
3. Configure the service:
   - **Service Name**: `backend`
   - **Root Directory**: `backend`
   - **Build Command**: Leave empty (Docker will handle it)
   - **Start Command**: Leave empty (Docker will handle it)

4. Go to "Settings" → "Environment"
5. Add these environment variables:
   ```
   SPRING_PROFILES_ACTIVE=railway
   JWT_SECRET=your-super-secure-jwt-secret-change-this-in-production
   PORT=8080
   ```

6. Go to "Settings" → "Deploy"
7. Set **Dockerfile Path**: `Dockerfile.railway`

8. Click "Deploy"

### 2.4 Get Backend URL
1. Once backend is deployed, go to the backend service
2. Go to "Settings" → "Networking"
3. Click "Generate Domain" if not already generated
4. Copy the backend URL (e.g., `https://backend-production-xxxx.railway.app`)

### 2.5 Deploy Frontend Service
1. Click "New Service" → "GitHub Repo"
2. Select your repository again
3. Configure the service:
   - **Service Name**: `frontend`
   - **Root Directory**: `frontend`

4. Go to "Settings" → "Environment"
5. Add these environment variables (replace with your actual backend URL):
   ```
   REACT_APP_API_URL=https://your-backend-url.railway.app/api
   BACKEND_URL=https://your-backend-url.railway.app
   NODE_ENV=production
   ```

6. Go to "Settings" → "Deploy"
7. Set **Dockerfile Path**: `Dockerfile.railway`

8. Click "Deploy"

### 2.6 Update Backend CORS
1. Go back to your backend service
2. Go to "Settings" → "Environment"
3. Add the frontend URL:
   ```
   FRONTEND_URL=https://your-frontend-url.railway.app
   ```
4. Redeploy the backend service

## Step 3: Verify Deployment

### 3.1 Check Services
1. Ensure all 3 services are running:
   - ✅ PostgreSQL (Database)
   - ✅ Backend (API)
   - ✅ Frontend (Web App)

### 3.2 Test the Application
1. Visit your frontend URL
2. Try logging in with default credentials:
   - **Admin**: `admin@iimtrichy.ac.in` / `admin123`
   - **Student**: `student@iimtrichy.ac.in` / `student123`
   - **Staff**: `staff@iimtrichy.ac.in` / `staff123`

## Step 4: Optional Configurations

### 4.1 Custom Domains (Optional)
1. Go to each service → "Settings" → "Networking"
2. Add custom domain if you have one

### 4.2 Email Configuration (Optional)
Add to backend environment variables:
```
MAIL_USERNAME=your-gmail@gmail.com
MAIL_PASSWORD=your-app-password
```

### 4.3 Monitoring
1. Check logs in each service's "Deployments" tab
2. Monitor resource usage in "Metrics" tab

## Alternative: CLI Deployment (If Authentication Works)

If you can get Railway CLI authentication working:

```bash
# Authenticate
railway login

# Create project
railway init

# Deploy database
railway add postgresql

# Deploy backend
cd backend
railway up --detach

# Deploy frontend  
cd ../frontend
railway up --detach
```

## Troubleshooting

### Common Issues:

1. **Backend won't start**:
   - Check logs in Railway dashboard
   - Verify `DATABASE_URL` is available
   - Check environment variables

2. **Frontend can't connect to backend**:
   - Verify `REACT_APP_API_URL` is correct
   - Check CORS configuration
   - Ensure backend is running

3. **Database connection issues**:
   - Verify PostgreSQL service is running
   - Check if `DATABASE_URL` is properly set

### Getting Help:
- Check service logs in Railway dashboard
- Railway Discord: [discord.gg/railway](https://discord.gg/railway)
- Railway Docs: [docs.railway.app](https://docs.railway.app)

## Expected Results

After successful deployment:
- **Frontend**: Accessible web interface
- **Backend**: API endpoints working
- **Database**: PostgreSQL with sample data
- **Authentication**: Login system working
- **Features**: Ticket creation, management, etc.

## Cost Information

Railway free tier:
- $5/month credit
- 500 hours of usage
- 1GB RAM per service
- 1GB storage

Your application should fit comfortably within these limits for development/testing.
