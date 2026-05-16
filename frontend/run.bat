@echo off
REM Simple script to start frontend
echo.
echo ========================================
echo  Flash-Cart Frontend Startup
echo ========================================
echo.

cd /d "%~dp0"

REM Check if Python is available
python --version >nul 2>&1
if errorlevel 1 (
    echo ❌ Python not found
    echo Try: npx http-server public -p 8000
    pause
    exit /b 1
)

echo ✅ Starting frontend server on port 8000
echo.
echo Frontend will run on: http://localhost:8000
echo Press Ctrl+C to stop
echo.

python -m http.server 8000 --directory public

pause
