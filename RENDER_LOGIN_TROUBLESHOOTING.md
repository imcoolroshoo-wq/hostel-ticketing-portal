# 🔧 Render Login Troubleshooting Guide

## 🔍 **Current Issue Analysis**

Your logs show:
```
GET /login HTTP/1.1" 200 621
GET /static/js/main.d145c3c0.js HTTP/1.1" 200 380703
```

**✅ Good:** Frontend loads successfully
**❌ Problem:** No login API calls in logs → Login attempts not reaching backend

## 🚀 **Step-by-Step Fix**

### **Step 1: Check Frontend Environment Variable**

1. **Go to Render Dashboard**
2. **Navigate to your Frontend service** (hostel-ticketing-frontend)
3. **Click on "Environment" tab**
4. **Check if this environment variable exists:**
   ```
   REACT_APP_API_URL = https://hostel-ticketing-portal.onrender.com/api
   ```

### **Step 2: Add Environment Variable (if missing)**

If the environment variable is missing:
1. **Click "Add Environment Variable"**
2. **Key:** `REACT_APP_API_URL`
3. **Value:** `https://hostel-ticketing-portal.onrender.com/api`
4. **Click "Save"**

### **Step 3: Redeploy Frontend**

After adding/updating the environment variable:
1. **Go to "Manual Deploy" tab**
2. **Click "Deploy Latest Commit"**
3. **Wait for deployment to complete**

### **Step 4: Test the Fix**

1. **Open browser developer tools** (F12)
2. **Go to Network tab**
3. **Visit:** `https://hostel-ticketing-frontend.onrender.com/login`
4. **Try to login with:** `admin@iimtrichy.ac.in` / `admin123`
5. **Check Network tab for API calls**

**Expected behavior:**
- You should see API calls to `hostel-ticketing-portal.onrender.com`
- **NOT** to `hostel-ticketing-frontend.onrender.com`

## 🔧 **Alternative Fix: Manual Deployment**

If environment variables don't work, you can manually deploy with hardcoded API URL:

### **1. Update Frontend Configuration**

The latest code already has a fallback to the correct backend URL:

```typescript
// In frontend/src/config/api.ts
const getApiBaseUrl = (): string => {
  if (process.env.NODE_ENV === 'production') {
    // Will use hardcoded backend URL as fallback
    return process.env.REACT_APP_API_URL || 'https://hostel-ticketing-portal.onrender.com/api';
  }
  return process.env.REACT_APP_API_URL || 'http://localhost:8080/api';
};
```

### **2. Force Redeploy**

1. **Go to your Frontend service on Render**
2. **Click "Manual Deploy"**
3. **Select "Deploy Latest Commit"**
4. **Wait for build and deployment to complete**

## 🔍 **Verification Steps**

### **1. Check API URL in Browser**

After deployment, open browser console and run:
```javascript
// Should show the backend URL
console.log(window.location.origin);
// Should be different from above
fetch('/api/health').then(r => console.log('API reachable')).catch(e => console.log('API not reachable:', e));
```

### **2. Check Network Calls**

In browser dev tools:
1. **Network tab**
2. **Try login**
3. **Look for POST requests to `/api/users/authenticate`**
4. **Verify they go to `hostel-ticketing-portal.onrender.com`**

### **3. Test Direct API Call**

Try this URL directly in browser:
```
https://hostel-ticketing-portal.onrender.com/api/health
```

Should return backend health status.

## 📊 **Expected vs Current Behavior**

### **❌ Current (Broken):**
```
Frontend: https://hostel-ticketing-frontend.onrender.com
API Calls: https://hostel-ticketing-frontend.onrender.com/api ← WRONG!
Result: 405 Method Not Allowed
```

### **✅ Expected (Fixed):**
```
Frontend: https://hostel-ticketing-frontend.onrender.com
API Calls: https://hostel-ticketing-portal.onrender.com/api ← CORRECT!
Result: Successful login
```

## 🚨 **If Still Not Working**

### **Check Backend Health**
```
https://hostel-ticketing-portal.onrender.com/api/health
```

### **Check Frontend Build**
Look for these files in your deployed frontend:
- `main.d145c3c0.js` should contain the correct API URL

### **Check Render Logs**
1. **Backend logs:** Should show incoming requests
2. **Frontend logs:** Should show successful build

## 🎯 **Quick Test Commands**

```bash
# Test backend directly
curl https://hostel-ticketing-portal.onrender.com/api/health

# Test frontend login API call
curl -X POST https://hostel-ticketing-portal.onrender.com/api/users/authenticate \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@iimtrichy.ac.in","password":"admin123"}'
```

## 📞 **Contact Steps**

If you need help:
1. **Share screenshots** of your Render environment variables
2. **Share browser console errors** during login attempt
3. **Share network tab** showing API calls

---

**The fix is almost certainly just setting the environment variable and redeploying the frontend!** 🎉
