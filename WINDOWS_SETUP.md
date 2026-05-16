# Flash-Cart Windows Setup Guide

## Prerequisites Check

### 1. Verify Java Installation
```powershell
java -version
```

**If not installed:**
- Download [Java 17 LTS](https://www.oracle.com/java/technologies/downloads/)
- Choose "Windows x64 Installer"
- Run installer (default paths work fine)
- **Important**: Restart your terminal/computer after installing

### 2. Verify PostgreSQL Installation
```powershell
psql --version
```

**If not installed:**
- Download [PostgreSQL 15](https://www.postgresql.org/download/windows/)
- Run installer
- Remember the password you set for `postgres` user
- Default port: 5432 (keep it)

### 3. Create Database
```powershell
createdb flash_cart_db
# Or with password:
createdb -U postgres flash_cart_db
# (it will prompt for password)
```

---

## Maven Setup Options

### Option A: Use Maven Wrapper (RECOMMENDED - Already Configured)
Maven Wrapper is already set up in the project. No installation needed!

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

First run will auto-download Maven (~5-10 seconds).

### Option B: Install Maven Globally (For Any Project)

#### Step 1: Download Maven
1. Go to [maven.apache.org/download.cgi](https://maven.apache.org/download.cgi)
2. Download `apache-maven-3.8.7-bin.zip` (not src)
3. Extract to: `C:\Program Files\Apache\maven`

#### Step 2: Set Environment Variable
1. Right-click Start Menu → Search **"Environment"**
2. Click **"Edit the system environment variables"**
3. Click **"Environment Variables..."**
4. Under "System variables", click **"New..."**
5. Variable name: `M2_HOME`
6. Variable value: `C:\Program Files\Apache\maven`
7. Click OK → OK → OK

#### Step 3: Add Maven to PATH
1. In Environment Variables, find "Path" and click **"Edit..."**
2. Click **"New"**
3. Add: `C:\Program Files\Apache\maven\bin`
4. Click OK → OK → OK

#### Step 4: Verify Installation
**Close terminal and open a new one**, then:
```powershell
mvn --version
```

Should display Maven version (e.g., "Apache Maven 3.8.7")

#### Step 5: Run Backend
```powershell
cd Flash-Cart/backend
mvn spring-boot:run
```

---

## Running the Backend

### Method 1: Maven Wrapper (First Time Easiest)
```powershell
cd Flash-Cart/backend
.\mvnw.cmd spring-boot:run
```
**Wait for "Started FlashCartApplication"** (about 15-30 seconds first time)

### Method 2: Maven (After Global Installation)
```powershell
cd Flash-Cart/backend
mvn spring-boot:run
```

### Method 3: Docker (If Installed)
```powershell
# Start PostgreSQL
docker run --name flashcart-db `
  -e POSTGRES_PASSWORD=password `
  -e POSTGRES_DB=flash_cart_db `
  -p 5432:5432 `
  -d postgres:15

# Then run backend normally
cd Flash-Cart/backend
mvn spring-boot:run
```

---

## Running the Frontend

```powershell
cd Flash-Cart/frontend

# Python 3 (if installed)
python -m http.server 8000 --directory public

# OR with Node.js (if installed)
npx http-server public -p 8000

# OR use any other HTTP server
```

Open: **http://localhost:8000**

---

## Verify Everything Works

### 1. Check Backend
```powershell
curl http://localhost:8080/api/users/exists/test@example.com
```
Should return: `{"exists":false}`

### 2. Check Frontend
Open http://localhost:8000 in browser
Should see Flash-Cart dashboard with "Create Account" modal

### 3. Check Database Connection
```powershell
psql -U postgres flash_cart_db
# At prompt, type: \dt
# Should list database tables: users, groups, expenses, expense_splits
# Type: \q to exit
```

---

## Common Issues & Fixes

### ❌ "The term 'mvn' is not recognized"
```
✅ FIX: Use Maven Wrapper instead
cd backend
.\mvnw.cmd spring-boot:run
```

```
✅ FIX 2: Add Maven to PATH (see "Install Maven Globally" above)
```

### ❌ "java: The term 'java' is not recognized"
```
✅ FIX: Install Java 17+ from:
https://www.oracle.com/java/technologies/downloads/

Then RESTART your terminal
```

### ❌ Database connection refused
```
✅ FIX: Ensure PostgreSQL is running
# Windows: Check Services (search "Services" in Start Menu)
# PostgreSQL should be listed and running

✅ FIX 2: Create the database if missing
createdb flash_cart_db
```

### ❌ "error: gradle not found"
```
✅ This is a Spring Boot project, uses Maven NOT Gradle
Use: mvn spring-boot:run (not gradle build)
```

### ❌ Port 8080 already in use
```
✅ FIX: Change port in application.properties
# Edit: backend/src/main/resources/application.properties
server.port=8081
```

### ❌ Port 8000 already in use (frontend)
```
✅ FIX: Change HTTP server port
python -m http.server 9000 --directory public
# Then open: http://localhost:9000
```

---

## Step-by-Step First Run

### Terminal 1: Backend
```powershell
# Navigate to project
cd C:\Users\pinna\OneDrive\Desktop\Flash-Cart

# Go to backend
cd backend

# Run with Maven Wrapper
.\mvnw.cmd spring-boot:run

# 🔄 Wait for: "Started FlashCartApplication in X seconds"
# ✅ Backend ready at http://localhost:8080
```

### Terminal 2: Frontend
```powershell
# Navigate to frontend
cd C:\Users\pinna\OneDrive\Desktop\Flash-Cart\frontend

# Start HTTP server
python -m http.server 8000 --directory public

# ✅ Frontend ready at http://localhost:8000
```

### Browser
1. Open http://localhost:8000
2. Click "Create Account"
3. Enter:
   - Username: testuser
   - Email: test@example.com
   - Password: password123
4. ✅ Account created!

---

## Quick Reference

| What | Command | Port |
|------|---------|------|
| Backend | `.\mvnw.cmd spring-boot:run` | 8080 |
| Frontend | `python -m http.server 8000` | 8000 |
| Database | (Auto-started by Spring Boot) | 5432 |

---

## Next Steps

1. ✅ Run backend & frontend
2. ✅ Create account on frontend
3. ✅ Create group and add expense
4. ✅ See dashboard metrics
5. Follow [DEPLOYMENT.md](DEPLOYMENT.md) to deploy to cloud

---

**Need more help?** See [README.md](README.md) for full documentation
