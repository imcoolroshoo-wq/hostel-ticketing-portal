#!/bin/bash

# Deploy Hostel Ticketing Portal to Railway
set -e

echo "🚀 Deploying Hostel Ticketing Portal to Railway"
echo "=============================================="

# Check if Railway CLI is installed
if ! command -v railway &> /dev/null; then
    echo "❌ Railway CLI is not installed."
    echo "Please install it from: https://docs.railway.app/develop/cli"
    echo "Run: npm install -g @railway/cli"
    exit 1
fi

# Check if user is logged in to Railway
if ! railway whoami &> /dev/null; then
    echo "❌ You are not logged in to Railway."
    echo "Please run: railway login"
    exit 1
fi

echo "✅ Railway CLI is ready"

# Create a new Railway project
echo "📦 Creating Railway project..."
railway login
railway init

# Deploy PostgreSQL database
echo "🗄️  Setting up PostgreSQL database..."
railway add --database postgresql

# Wait for database to be ready
echo "⏳ Waiting for database to be ready..."
sleep 10

# Deploy backend service
echo "🔧 Deploying backend service..."
cd backend
railway up --detach
cd ..

# Get backend URL
BACKEND_URL=$(railway status --json | jq -r '.deployments[0].url')
echo "✅ Backend deployed at: $BACKEND_URL"

# Deploy frontend service
echo "🎨 Deploying frontend service..."
cd frontend

# Set environment variable for frontend
export REACT_APP_API_URL="$BACKEND_URL/api"

railway up --detach
cd ..

# Get frontend URL
FRONTEND_URL=$(railway status --json | jq -r '.deployments[1].url')
echo "✅ Frontend deployed at: $FRONTEND_URL"

# Update backend CORS settings
echo "🔗 Updating CORS settings..."
railway variables set FRONTEND_URL="$FRONTEND_URL"

echo ""
echo "🎉 Deployment Complete!"
echo "======================="
echo "Frontend URL: $FRONTEND_URL"
echo "Backend URL:  $BACKEND_URL"
echo ""
echo "Default Login Credentials:"
echo "Admin:   admin@iimtrichy.ac.in / admin123"
echo "Student: student@iimtrichy.ac.in / student123"
echo "Staff:   staff@iimtrichy.ac.in / staff123"
echo ""
echo "📝 Next Steps:"
echo "1. Visit your frontend URL to test the application"
echo "2. Update environment variables in Railway dashboard if needed"
echo "3. Set up custom domain (optional)"
echo "4. Configure monitoring and alerts"
