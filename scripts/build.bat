@echo off
chcp 65001 >nul
cls

echo ==========================================
echo 🔨 LOS ТӨСЛИЙГ BUILD ХИЙЖ БАЙНА...
echo ==========================================
echo.

echo [1/2] Backend build хийж байна...
cd backend
call mvnw.cmd clean package -DskipTests
if %errorlevel% neq 0 (
    echo ❌ Backend build амжилтгүй!
    pause
    exit /b 1
)
echo ✅ Backend build амжилттай
cd ..

echo.
echo [2/2] Frontend build хийж байна...
cd frontend
call npm run build
if %errorlevel% neq 0 (
    echo ❌ Frontend build амжилтгүй!
    pause
    exit /b 1
)
echo ✅ Frontend build амжилттай
cd ..

echo.
echo ==========================================
echo ✅ BUILD АМЖИЛТТАЙ ДУУСЛАА!
echo ==========================================
echo.
echo 📦 Backend JAR: backend\target\loan-origination-service-1.0.0.jar
echo 📦 Frontend dist: frontend\dist\
echo.
pause
