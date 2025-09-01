# 🔧 Fix Render Database Connection Issue

## 🚨 **Problem Identified**
The error shows that `DATABASE_URL` is set to `<your-db-url>` (placeholder text) instead of the actual database URL.

## ✅ **Solution: Set Correct Database URL**

### **Step 1: Get Your Database URL**
1. In Render dashboard, go to your **PostgreSQL database service**
2. Click on the **"Connect"** tab
3. Copy the **"Internal Database URL"** (it looks like this):
   ```
   postgresql://username:password@hostname:5432/database_name
   ```

### **Step 2: Update Backend Environment Variables**
1. Go to your **backend service** in Render
2. Click **"Environment"** tab
3. Find the `DATABASE_URL` variable
4. **Replace** `<your-db-url>` with the actual database URL you copied
5. Click **"Save Changes"**

### **Step 3: Redeploy**
1. Click **"Manual Deploy"** → **"Deploy Latest Commit"**
2. Wait for deployment to complete

## 📋 **Environment Variables Should Look Like:**

```
SPRING_PROFILES_ACTIVE=render
JWT_SECRET=your-super-secure-jwt-secret-change-this-in-production
DATABASE_URL=postgresql://username:password@hostname:5432/database_name
PORT=10000
```

## 🔍 **How to Find Database URL:**

### **Option A: From Database Service**
1. Go to PostgreSQL service in Render
2. "Connect" tab → Copy "Internal Database URL"

### **Option B: From Environment Variables**
1. In your database service
2. Look for auto-generated environment variables
3. Use the internal connection string

## ⚠️ **Important Notes:**
- Use **Internal Database URL** (not External)
- Don't include `<` or `>` brackets
- The URL should start with `postgresql://`
- Make sure it's the full connection string

## 🎯 **After Fixing:**
Your backend should:
- ✅ Connect to database successfully
- ✅ Create tables automatically
- ✅ Insert default users
- ✅ Start API server

## 🔐 **Test After Deployment:**
Visit your backend URL: `https://your-backend.onrender.com/api/health`
Should return: `{"status":"UP"}`

---

**The fix is simple: Replace the placeholder with the real database URL!** 🚀
