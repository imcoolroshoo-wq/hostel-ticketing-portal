# Quick Railway Deployment Guide

## Option 1: Automated Deployment (Recommended)

1. **Install Railway CLI**:
   ```bash
   npm install -g @railway/cli
   ```

2. **Run the deployment script**:
   ```bash
   ./deploy-to-railway.sh
   ```

## Option 2: Manual Deployment via Railway Dashboard

### Step 1: Create Railway Account
1. Go to [railway.app](https://railway.app)
2. Sign up with GitHub
3. Create a new project

### Step 2: Deploy Database
1. Click "New" → "Database" → "PostgreSQL"
2. Railway will provision a PostgreSQL database
3. Note the `DATABASE_URL` in environment variables

### Step 3: Deploy Backend
1. Click "New" → "GitHub Repo"
2. Select your repository
3. Configure service:
   - **Name**: `backend`
   - **Root Directory**: `backend`
   - **Build Command**: `docker build -f Dockerfile.railway -t backend .`
   - **Start Command**: `/app/railway-start.sh`

4. Set environment variables:
   ```
   SPRING_PROFILES_ACTIVE=railway
   JWT_SECRET=your-super-secure-jwt-secret-here
   MAIL_USERNAME=your-email@gmail.com (optional)
   MAIL_PASSWORD=your-app-password (optional)
   ```

### Step 4: Deploy Frontend
1. Click "New" → "GitHub Repo" (same repo)
2. Configure service:
   - **Name**: `frontend`
   - **Root Directory**: `frontend`
   - **Build Command**: `docker build -f Dockerfile.railway -t frontend .`

3. Set environment variables:
   ```
   REACT_APP_API_URL=https://your-backend-url.railway.app/api
   BACKEND_URL=https://your-backend-url.railway.app
   ```

### Step 5: Update CORS
1. Go to backend service
2. Add environment variable:
   ```
   FRONTEND_URL=https://your-frontend-url.railway.app
   ```

## Testing Your Deployment

1. Visit your frontend URL
2. Login with default credentials:
   - **Admin**: `admin@iimtrichy.ac.in` / `admin123`
   - **Student**: `student@iimtrichy.ac.in` / `student123`
   - **Staff**: `staff@iimtrichy.ac.in` / `staff123`

## Troubleshooting

### Backend Issues
- Check logs in Railway dashboard
- Verify `DATABASE_URL` is set
- Ensure all environment variables are configured

### Frontend Issues
- Verify `REACT_APP_API_URL` points to backend
- Check browser console for errors
- Ensure CORS is configured correctly

### Database Issues
- Check if PostgreSQL service is running
- Verify database connection in backend logs
- Run database initialization manually if needed

## Cost Management

Railway free tier includes:
- $5/month credit
- 500 hours of usage
- 1GB RAM per service

To optimize:
1. Use smaller container sizes
2. Set up auto-scaling
3. Monitor usage in dashboard

## Security Notes

1. Change default JWT secret
2. Use strong passwords for email configuration
3. Keep environment variables secure
4. Enable Railway's security features

## Support

- Railway Docs: [docs.railway.app](https://docs.railway.app)
- Railway Discord: [discord.gg/railway](https://discord.gg/railway)
- Project Issues: Create GitHub issues
