@echo off
REM Flash-Cart - Backend Setup and Run Script
REM This script automatically configures Maven and runs the backend

setlocal enabledelayedexpansion

echo.
echo ========================================
echo  Flash-Cart Backend Startup Script
echo ========================================
echo.

REM Check Java
java -version >nul 2>&1
if errorlevel 1 (
    echo ❌ Java is not installed or not in PATH
    echo.
    echo Download Java 17+ from:
    echo https://www.oracle.com/java/technologies/downloads/
    echo.
    echo After installing, restart this script.
    pause
    exit /b 1
)
echo ✅ Java found

REM Check PostgreSQL
psql --version >nul 2>&1
if errorlevel 1 (
    echo ⚠️  PostgreSQL not found, but continuing...
    echo Make sure PostgreSQL is running on port 5432
) else (
    echo ✅ PostgreSQL found
)

REM Try to find Maven
set MAVEN_HOME=
set MAVEN_CMD=

REM Priority 1: Check common installation paths
if exist "C:\Program Files\apache-maven-3.9.15\bin\mvn.cmd" (
    set MAVEN_HOME=C:\Program Files\apache-maven-3.9.15
    set MAVEN_CMD=C:\Program Files\apache-maven-3.9.15\bin\mvn.cmd
    goto :maven_found
)

if exist "C:\Program Files\Apache\maven\bin\mvn.cmd" (
    set MAVEN_HOME=C:\Program Files\Apache\maven
    set MAVEN_CMD=C:\Program Files\Apache\maven\bin\mvn.cmd
    goto :maven_found
)

if exist "C:\Maven\bin\mvn.cmd" (
    set MAVEN_HOME=C:\Maven
    set MAVEN_CMD=C:\Maven\bin\mvn.cmd
    goto :maven_found
)

if exist "C:\Program Files\Maven\bin\mvn.cmd" (
    set MAVEN_HOME=C:\Program Files\Maven
    set MAVEN_CMD=C:\Program Files\Maven\bin\mvn.cmd
    goto :maven_found
)

REM Priority 2: Check if mvn exists in PATH
for /f "delims=" %%A in ('where mvn.cmd 2^>nul') do (
    set MAVEN_CMD=%%A
    goto :maven_found
)

REM Priority 3: Check MAVEN_HOME environment variable
if defined MAVEN_HOME (
    if exist "!MAVEN_HOME!\bin\mvn.cmd" (
        set MAVEN_CMD=!MAVEN_HOME!\bin\mvn.cmd
        goto :maven_found
    )
)

echo.
echo ❌ Maven not found!
echo.
echo Please install Maven 3.8+ from:
echo https://maven.apache.org/download.cgi
echo.
echo Installation steps:
echo 1. Download apache-maven-3.8.7-bin.zip
echo 2. Extract to C:\Program Files\Apache\maven
echo 3. Restart your terminal
echo 4. Run this script again
echo.
pause
exit /b 1

:maven_found
echo ✅ Maven found at: !MAVEN_CMD!
echo.

REM Check database
echo Checking database...
psql -U postgres -d flash_cart_db -c "SELECT 1;" >nul 2>&1
if errorlevel 1 (
    echo Creating database flash_cart_db...
    createdb -U postgres flash_cart_db >nul 2>&1
    if errorlevel 1 (
        echo ⚠️  Could not create database
        echo Make sure PostgreSQL is running
    ) else (
        echo ✅ Database created
    )
) else (
    echo ✅ Database exists
)

echo.
echo ========================================
echo  Starting Backend...
echo ========================================
echo.
echo Backend will run on: http://localhost:8080
echo Press Ctrl+C to stop
echo.

REM Run Maven
"!MAVEN_CMD!" clean install spring-boot:run -DskipTests

pause
