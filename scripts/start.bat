@echo off
chcp 65001 >nul
cls

echo ==========================================
echo 🏦 LOS СИСТЕМ ЭХЛҮҮЛЖ БАЙНА
echo ==========================================
echo.

REM Төслийн үндсэн директор шалгах
if not exist "backend\pom.xml" (
    echo ❌ backend\pom.xml олдсонгүй!
    echo Төслийн үндсэн зам дээр скрипт ажиллуулна уу.
    pause
    exit /b 1
)

if not exist "frontend\package.json" (
    echo ❌ frontend\package.json олдсонгүй!
    echo Төслийн үндсэн зам дээр скрипт ажиллуулна уу.
    pause
    exit /b 1
)

echo ✅ Төслийн бүтэц баталгаажлаа
echo.

REM Java шалгах
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ⚠️  Java олдсонгүй
    echo Java 17+ суулгана уу
    echo.
)

REM Node.js шалгах
node --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ⚠️  Node.js олдсонгүй
    echo Node.js 18+ суулгана уу
    echo.
)

echo [1/3] Backend бэлдэж байна...
cd backend
call mvnw.cmd clean compile -q
if %errorlevel% neq 0 (
    echo ❌ Backend бэлдэх амжилтгүй!
    echo Дээрх алдааг шалгана уу.
    pause
    exit /b 1
)
echo ✅ Backend амжилттай бэлдэгдлээ
cd ..

echo.
echo [2/3] Frontend dependencies суулгаж байна...
cd frontend
if not exist "node_modules" (
    echo NPM packages суулгаж байна...
    call npm install --silent
    if %errorlevel% neq 0 (
        echo ❌ Frontend dependencies суулгах амжилтгүй!
        pause
        exit /b 1
    )
    echo ✅ Frontend dependencies амжилттай суулгагдлаа
) else (
    echo ✅ Frontend dependencies аль хэдийн суулгагдсан
)
cd ..

echo.
echo [3/3] Серvisүүдийг эхлүүлж байна...
echo.

REM Backend эхлүүлэх
echo Backend эхлүүлж байна (Spring Boot)...
start "LOS Backend" cmd /k "echo 🏦 LOS Backend эхэлж байна... && echo Backend URL: http://localhost:8080/los && echo Health Check: http://localhost:8080/los/api/v1/health && echo H2 Console: http://localhost:8080/los/h2-console && echo API Docs: http://localhost:8080/los/swagger-ui.html && echo. && cd backend && mvnw.cmd spring-boot:run"

REM Backend эхлэхийг хүлээх
timeout /t 5 /nobreak >nul

REM Frontend эхлүүлэх
echo Frontend эхлүүлж байна (React + Vite)...
start "LOS Frontend" cmd /k "echo ⚛️  LOS Frontend эхэлж байна... && echo Frontend URL: http://localhost:3001 && echo. && cd frontend && npm run dev"

echo.
echo ==========================================
echo 🎉 LOS СЕРVISҮҮД ЭХЭЛЖ БАЙНА...
echo ==========================================
echo.
echo 🌐 Хандах холбоосууд:
echo   Frontend:    http://localhost:3001
echo   Backend:     http://localhost:8080/los
echo   Health:      http://localhost:8080/los/api/v1/health  
echo   API Docs:    http://localhost:8080/los/swagger-ui.html
echo   Database:    http://localhost:8080/los/h2-console
echo.
echo 🗄️  H2 Database Console тохиргоо:
echo   JDBC URL:    jdbc:h2:mem:losdb
echo   Username:    sa
echo   Password:    (хоосон)
echo.
echo ⏳ Серvisүүд бүрэн эхлэхэд 30-60 секунд хүлээнэ үү...
echo.
echo ⛔ Серvisүүдийг зогсооход: .\scripts\stop.bat
echo.

REM 10 секундийн дараа browser нээх
echo 🌐 10 секундийн дараа browser нээх...
timeout /t 10 /nobreak >nul
start http://localhost:3001
start http://localhost:8080/los/api/v1/health

echo ✅ LOS Система амжилттай эхэллээ!
echo.
pause
