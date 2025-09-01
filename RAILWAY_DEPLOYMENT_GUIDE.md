# Railway Deployment Guide

This guide will help you deploy the Hostel Ticketing Portal to Railway, a free cloud platform.

## Prerequisites

1. **GitHub Account**: Your code should be in a GitHub repository
2. **Railway Account**: Sign up at [railway.app](https://railway.app) (free tier available)
3. **Gmail Account**: For email notifications (optional)

## Step 1: Prepare Your Repository

1. Push your code to GitHub:
```bash
git add .
git commit -m "Prepare for Railway deployment"
git push origin main
```

## Step 2: Deploy Backend to Railway

### 2.1 Create Backend Service

1. Go to [railway.app](https://railway.app) and sign in
2. Click "New Project" → "Deploy from GitHub repo"
3. Select your repository
4. Railway will detect your project structure

### 2.2 Configure Backend Service

1. **Service Settings**:
   - Name: `hostel-backend`
   - Root Directory: `backend`
   - Dockerfile Path: `Dockerfile.railway`

2. **Environment Variables** (in Railway dashboard):
   ```
   SPRING_PROFILES_ACTIVE=railway
   JWT_SECRET=your-super-secure-jwt-secret-key-here-change-this
   MAIL_USERNAME=your-email@gmail.com (optional)
   MAIL_PASSWORD=your-app-password (optional)
   ```

3. **Database Setup**:
   - In Railway dashboard, click "New" → "Database" → "PostgreSQL"
   - Railway will automatically set `DATABASE_URL`

### 2.3 Deploy Backend

1. Railway will automatically build and deploy
2. Note the backend URL (e.g., `https://hostel-backend-production.railway.app`)

## Step 3: Deploy Frontend to Railway

### 3.1 Create Frontend Service

1. In the same Railway project, click "New Service"
2. Select "GitHub Repo" → Choose your repository again
3. Configure:
   - Name: `hostel-frontend`
   - Root Directory: `frontend`
   - Dockerfile Path: `Dockerfile.railway`

### 3.2 Configure Frontend Service

1. **Environment Variables**:
   ```
   REACT_APP_API_URL=https://your-backend-url.railway.app/api
   BACKEND_URL=https://your-backend-url.railway.app
   ```

2. **Build Settings**:
   - Build Command: `npm run build`
   - Start Command: `/docker-entrypoint.sh`

### 3.3 Deploy Frontend

1. Railway will build and deploy the frontend
2. Note the frontend URL (e.g., `https://hostel-frontend-production.railway.app`)

## Step 4: Update CORS Configuration

Update your backend's CORS configuration to allow your frontend domain:

1. Go to backend service environment variables
2. Add/Update:
   ```
   FRONTEND_URL=https://your-frontend-url.railway.app
   ```

## Step 5: Initialize Database

1. **Option A: Automatic (Recommended)**
   - The application will automatically create tables on first run

2. **Option B: Manual**
   - Connect to your Railway PostgreSQL database
   - Run the SQL from `backend/src/main/resources/db/init.sql`

## Step 6: Test Your Deployment

1. Visit your frontend URL
2. Try logging in with default credentials:
   - Admin: `admin@iimtrichy.ac.in` / `admin123`
   - Student: `student@iimtrichy.ac.in` / `student123`
   - Staff: `staff@iimtrichy.ac.in` / `staff123`

## Troubleshooting

### Common Issues

1. **Backend not starting**:
   - Check logs in Railway dashboard
   - Verify environment variables
   - Ensure `DATABASE_URL` is set

2. **Frontend can't connect to backend**:
   - Verify `REACT_APP_API_URL` points to correct backend URL
   - Check CORS configuration

3. **Database connection issues**:
   - Verify PostgreSQL service is running
   - Check database URL format

### Monitoring

- **Logs**: Available in Railway dashboard for each service
- **Metrics**: CPU, memory, and network usage
- **Health Checks**: Configured in Dockerfiles

## Cost Optimization

Railway free tier includes:
- $5/month credit
- 500 hours of usage
- 1GB RAM per service
- 1GB storage

To optimize costs:
1. Use smaller container sizes
2. Set up auto-scaling
3. Monitor usage in Railway dashboard

## Security Considerations

1. **Environment Variables**: Never commit secrets to Git
2. **JWT Secret**: Use a strong, unique secret
3. **Database**: Railway provides secure connections
4. **HTTPS**: Automatically provided by Railway

## Backup Strategy

1. **Database**: Use Railway's backup features
2. **Code**: Keep in Git repository
3. **Environment Variables**: Document in secure location

## Updates and Maintenance

1. **Code Updates**: Push to GitHub → Railway auto-deploys
2. **Database Migrations**: Handled by Hibernate
3. **Monitoring**: Use Railway dashboard and logs

## Support

- Railway Documentation: [docs.railway.app](https://docs.railway.app)
- Railway Discord: [discord.gg/railway](https://discord.gg/railway)
- Project Issues: Create GitHub issues in your repository
