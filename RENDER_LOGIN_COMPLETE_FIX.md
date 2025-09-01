# Render Frontend Login Issue - Complete Fix Guide

## 🔍 **Problem Analysis**

The frontend is loading correctly on `https://hostel-ticketing-frontend.onrender.com/login`, but login API calls are failing. This typically happens when:

1. Frontend is trying to call APIs on the frontend URL instead of backend URL
2. Environment variable `REACT_APP_API_URL` is not properly set on Render
3. CORS issues between frontend and backend domains

## ✅ **Step-by-Step Fix**

### **Step 1: Verify Environment Variable on Render**

1. Go to your Render Dashboard → Frontend Service
2. Go to **Environment** tab
3. **Ensure this environment variable exists:**
   ```
   REACT_APP_API_URL = https://hostel-ticketing-portal.onrender.com/api
   ```

### **Step 2: Trigger Frontend Rebuild**

Even if you set the environment variable, Render might be using a cached build:

1. Go to **Deploys** tab
2. Click **"Deploy Latest Commit"** or **"Clear build cache & deploy"**
3. Wait for rebuild to complete

### **Step 3: Verify Backend is Accessible**

Test the backend directly:
```bash
curl https://hostel-ticketing-portal.onrender.com/api/health
```

Expected response:
```json
{"service":"Hostel Ticketing Portal","version":"1.0.0","status":"UP","timestamp":"..."}
```

### **Step 4: Test Login API Directly**

```bash
curl -X POST https://hostel-ticketing-portal.onrender.com/api/users/authenticate \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail":"admin","password":"admin123"}'
```

Expected response:
```json
{"user":{"id":"...","username":"admin",...},"token":"..."}
```

### **Step 5: Debug Frontend Console**

1. Open browser developer tools (F12)
2. Go to **Network** tab
3. Try to login
4. Check if API calls are going to the correct URL:
   - ✅ **Correct:** `https://hostel-ticketing-portal.onrender.com/api/users/authenticate`
   - ❌ **Wrong:** `https://hostel-ticketing-frontend.onrender.com/api/users/authenticate`

### **Step 6: If Still Not Working - Force Rebuild**

If the environment variable isn't taking effect:

1. **Option A: Change any frontend file**
   - Make a small change to any file in `frontend/src/`
   - Commit and push to trigger new build

2. **Option B: Clear Render cache**
   - In Render Dashboard → Settings → Danger Zone
   - Click "Clear build cache"
   - Then deploy again

## 🔧 **Additional Debugging**

### **Check Current API URL in Browser Console**

Add this to your browser console on the frontend:
```javascript
console.log('NODE_ENV:', process.env.NODE_ENV);
console.log('REACT_APP_API_URL:', process.env.REACT_APP_API_URL);
```

### **Temporary Debug Build**

You can temporarily add console logging to see what URL is being used:

1. Edit `frontend/src/config/api.ts`:
   ```typescript
   const getApiBaseUrl = (): string => {
     console.log('NODE_ENV:', process.env.NODE_ENV);
     console.log('REACT_APP_API_URL from env:', process.env.REACT_APP_API_URL);
     
     if (process.env.NODE_ENV === 'production') {
       const apiUrl = process.env.REACT_APP_API_URL || 'https://hostel-ticketing-portal.onrender.com/api';
       console.log('Using production API URL:', apiUrl);
       return apiUrl;
     }
     
     const devUrl = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';
     console.log('Using development API URL:', devUrl);
     return devUrl;
   };
   ```

2. Commit, push, and check browser console after deployment

## 🚀 **Expected Result**

After fixing:
- ✅ Login with `admin` / `admin123` should work
- ✅ Login with `student` / `student123` should work  
- ✅ Login with `staff` / `staff123` should work
- ✅ No more 405 Method Not Allowed errors
- ✅ API calls go to backend URL, not frontend URL

## 📋 **Quick Checklist**

- [ ] Environment variable `REACT_APP_API_URL` set on Render
- [ ] Frontend service rebuilt after setting environment variable
- [ ] Backend health endpoint returns 200 OK
- [ ] Login API endpoint returns user data (not 405 error)
- [ ] Browser network tab shows API calls going to backend URL
- [ ] No CORS errors in browser console

## 🆘 **If Still Not Working**

1. **Check Render build logs** for any environment variable issues
2. **Verify both services are running** (backend and frontend)
3. **Test with a simple REST client** like Postman to isolate frontend vs backend issues
4. **Check browser console** for any error messages
5. **Verify CORS settings** in backend are allowing the frontend domain

The most common cause is that the environment variable isn't properly set or the frontend wasn't rebuilt after setting it.
