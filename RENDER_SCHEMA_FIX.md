# 🔧 Render Database Schema Fix

## 🎯 **Issue Fixed**

**Error:** `ERROR: column "password" of relation "users" does not exist`

**Root Cause:** Database schema mismatch between entity definition and data initialization.

## ✅ **What Was Fixed**

### **1. Column Name Mismatch**
- **Entity Definition:** `password_hash` (User.java line 47)
- **Data SQL:** `password` (data.sql)
- **Fix:** Updated data.sql to use `password_hash`

### **2. DDL Strategy**
- **Before:** `ddl-auto: create-drop` (drops tables on shutdown)
- **After:** `ddl-auto: update` (preserves data, updates schema)

## 🔧 **Files Updated**

### **1. data.sql**
```sql
-- Fixed all INSERT statements
INSERT INTO users (username, email, password_hash, ...)
-- Changed from: password
-- Changed to:   password_hash
```

### **2. application-render.yml**
```yaml
jpa:
  hibernate:
    ddl-auto: update  # Changed from create-drop
```

## ✅ **Expected Results**

After this fix:
- ✅ **Database schema created correctly**
- ✅ **Data initialization succeeds**
- ✅ **Application starts successfully**
- ✅ **Users can login with default credentials**

## 🔍 **Test After Deploy**

### **Health Check:**
```bash
curl https://hostel-backend.onrender.com/api/health
```

### **Login Test:**
Default credentials:
- **Admin:** `admin@iimtrichy.ac.in` / `admin123`
- **Student:** `student@iimtrichy.ac.in` / `student123`
- **Staff:** `staff@iimtrichy.ac.in` / `staff123`

**This fix resolves the database schema mismatch completely!** 🚀
