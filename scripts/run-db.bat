@echo off
chcp 65001 >nul
setlocal EnableDelayedExpansion

rem Скриптийг аль ч хавтсенаас ажиллуулсан ч төслийн root руу шилжинэ
pushd "%~dp0.."

echo ==========================================
echo  LOS - PostgreSQL (Docker Desktop) + Backend
echo ==========================================
echo.

docker info >nul 2>&1
if errorlevel 1 (
  echo [X] Docker daemon ажиллахгүй байна. Docker Desktop-оо асаана уу.
  popd
  pause
  exit /b 1
)

echo [1/3] postgres контейнер асааж байна...
docker compose up -d postgres
if errorlevel 1 (
  echo [X] postgres асаахад алдаа гарлаа
  popd
  pause
  exit /b 1
)

echo [2/3] PostgreSQL бэлэн болохыг хүлээж байна...
set READY=0
for /l %%i in (1,1,40) do (
  if "!READY!"=="0" (
    docker compose exec -T postgres pg_isready -U los_user -d los_db >nul 2>&1
    if not errorlevel 1 (
      set READY=1
      echo     OK - localhost:5432 / los_db
    ) else (
      timeout /t 2 /nobreak >nul
    )
  )
)

if "!READY!"=="0" (
  echo [X] PostgreSQL 80 секундэд бэлэн болсонгүй. docker compose logs postgres
  popd
  pause
  exit /b 1
)

if /i "%~1"=="--db-only" (
  echo.
  echo [OK] Зөвхөн DB асаалаа. Backend-ийг өөрөө асаах:
  echo      cd backend ^&^& set SPRING_PROFILES_ACTIVE=docker ^&^& mvn spring-boot:run
  popd
  exit /b 0
)

echo.
echo [3/3] Backend-ийг docker профайлаар ажиллуулж байна...
echo     DDL: update - өгөгдөл хадгалагдана
echo     Хаяг: http://localhost:8080/los
echo     Зогсоох: Ctrl+C
echo.

pushd backend
set SPRING_PROFILES_ACTIVE=docker
call mvn spring-boot:run
popd

popd
endlocal
