# 🔧 Render Frontend Environment Variable Fix

## ❌ **Problem**: Login 405 Method Not Allowed Error

The frontend is getting 405 errors when trying to authenticate because it's calling itself instead of the backend API.

### **Error in Logs:**
```
POST /api/users/authenticate HTTP/1.1" 405 559
```

## ✅ **Root Cause**

The React frontend is falling back to `window.location.origin + '/api'` in production, which makes it call:
- `https://hostel-ticketing-frontend.onrender.com/api/users/authenticate`

But it should be calling the backend:
- `https://hostel-ticketing-portal.onrender.com/api/users/authenticate`

## 🚀 **Fix: Set Environment Variable**

### **In Render Frontend Service:**

1. **Go to your Frontend service** on Render dashboard
2. **Navigate to Environment tab**
3. **Add the following environment variable:**

```
Key: REACT_APP_API_URL
Value: https://hostel-ticketing-portal.onrender.com/api
```

4. **Save and Redeploy** the frontend service

### **Expected Result:**
- ✅ Login requests will go to the backend API
- ✅ All API calls will work correctly
- ✅ No more 405 errors

## 🔄 **Backend Table Recreation**

The backend is already configured to drop and recreate tables on every startup:

```yaml
# In application-render.yml
jpa:
  hibernate:
    ddl-auto: create-drop  # ✅ Drops and recreates tables
sql:
  init:
    mode: always          # ✅ Runs data.sql on every startup
```

## 📋 **Complete Fix Checklist**

- [ ] Set `REACT_APP_API_URL=https://hostel-ticketing-portal.onrender.com/api` in frontend environment
- [ ] Redeploy frontend service
- [ ] Test login functionality
- [ ] Backend automatically recreates tables on restart

## 🎯 **Verification Steps**

1. **Check environment variable is set:**
   - In Render dashboard → Frontend service → Environment
   - Verify `REACT_APP_API_URL` is set correctly

2. **Test API calls:**
   - Open browser dev tools → Network tab
   - Attempt login
   - Verify API calls go to `hostel-ticketing-portal.onrender.com`

3. **Check backend logs:**
   - Verify table creation logs on backend startup
   - Should see schema drop/create messages

## 🔧 **Alternative: If Environment Variable Doesn't Work**

If setting the environment variable doesn't work, update the frontend code to hardcode the backend URL in production:

```typescript
// In frontend/src/config/api.ts
const getApiBaseUrl = (): string => {
  if (process.env.NODE_ENV === 'production') {
    return 'https://hostel-ticketing-portal.onrender.com/api';
  }
  return process.env.REACT_APP_API_URL || 'http://localhost:8080/api';
};
```

---

**This fix will resolve the login issues and ensure proper API communication between frontend and backend services.**
