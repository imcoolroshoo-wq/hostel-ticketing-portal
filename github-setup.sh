#!/bin/bash

# GitHub Setup Script
# Replace YOUR_USERNAME with your actual GitHub username

echo "🔧 Setting up GitHub repository..."

# Check if username is provided
if [ -z "$1" ]; then
    echo "❌ Please provide your GitHub username:"
    echo "Usage: ./github-setup.sh YOUR_USERNAME"
    echo "Example: ./github-setup.sh john-doe"
    exit 1
fi

USERNAME=$1
REPO_URL="https://github.com/$USERNAME/hostel-ticketing-portal.git"

echo "📡 Adding remote origin: $REPO_URL"
git remote add origin $REPO_URL

echo "🌿 Setting main branch..."
git branch -M main

echo "🚀 Pushing to GitHub..."
git push -u origin main

echo "✅ Done! Your repository is now on GitHub at:"
echo "   https://github.com/$USERNAME/hostel-ticketing-portal"
echo ""
echo "🎯 Next step: Deploy to Railway!"
echo "   1. Go to railway.app"
echo "   2. Click 'New Project' → 'Deploy from GitHub repo'"
echo "   3. Select your repository"
echo "   4. Follow the deployment guide"
