@echo off
chcp 65001 >nul
cls

:: LOS Project Progress Tracker
echo ╔════════════════════════════════════════════╗
echo ║        LOS Төслийн Явцын Tracker           ║
echo ╚════════════════════════════════════════════╝
echo.

:: Файлын тооллого
echo Одоогийн файлын тоо:
echo ════════════════════════

:: Файлын тоог тооцоолох
if exist "backend\src\main\java" (
    for /f %%a in ('dir "backend\src\main\java\*.java" /s /b 2^>nul ^| find /c ".java"') do set java_count=%%a
) else (
    set java_count=0
    echo [⚠] Backend директор олдсонгүй
)

if exist "frontend\src" (
    for /f %%a in ('dir "frontend\src\*.tsx" /s /b 2^>nul ^| find /c ".tsx"') do set tsx_count=%%a
    for /f %%a in ('dir "frontend\src\*.ts" /s /b 2^>nul ^| find /c ".ts"') do set ts_count=%%a
) else (
    set tsx_count=0
    set ts_count=0
    echo [⚠] Frontend директор олдсонгүй
)

if exist "scripts" (
    for /f %%a in ('dir "scripts\*.bat" /s /b 2^>nul ^| find /c ".bat"') do set script_count=%%a
) else (
    set script_count=0
    echo [⚠] Scripts директор олдсонгүй
)

if exist "docker" (
    for /f %%a in ('dir "docker*" /b 2^>nul ^| find /c "docker"') do set docker_count=%%a
) else (
    set docker_count=0
    echo [⚠] Docker директор олдсонгүй
)

:: Нийт файлын тоо
set /a total_files=%java_count%+%tsx_count%+%ts_count%+%script_count%+%docker_count%

echo ├─ Java файл:          %java_count%
echo ├─ TypeScript файл:    %tsx_count%
echo ├─ React файл:         %ts_count%
echo ├─ Script файл:        %script_count%
echo ├─ Docker файл:        %docker_count%
echo └─ Нийт:              %total_files%
echo.

:: Төлөвлөгдсөн vs бодит
set target_files=140
set /a progress_percent=(%total_files%*100)/%target_files%

echo Төлөвлөгөөтэй харьцуулбал:
echo ═══════════════════════════
echo Зорилт:     %target_files% файл
echo Одоогийн:   %total_files% файл
echo Гүйцэтгэл:  %progress_percent%%%
echo.

:: Progress bar
echo Явцын харуулцуур:
set /a bars=%progress_percent%/5
set progress_bar=
for /l %%i in (1,1,%bars%) do set progress_bar=!progress_bar!█
for /l %%i in (%bars%,1,19) do set progress_bar=!progress_bar!░
echo [%progress_bar%] %progress_percent%%%
echo.

:: Долоо хоногийн явц
echo Долоо хоногийн явц:
echo ══════════════════

:: 1-р долоо хоног шалгах
if %java_count% geq 15 (
    echo [✓] 1-р долоо хоног: Суурь архитектур (Дууссан)
) else (
    echo [○] 1-р долоо хоног: Суурь архитектур - %java_count%/15 файл
)

:: 2-р долоо хоног шалгах
if %tsx_count% geq 30 (
    echo [✓] 2-р долоо хоног: Core Features (Дууссан)
) else (
    echo [○] 2-р долоо хоног: Core Features - %tsx_count%/30 файл
)

:: 3-р долоо хоног шалгах
if %java_count% geq 40 (
    echo [✓] 3-р долоо хоног: Advanced Features (Дууссан)
) else (
    echo [○] 3-р долоо хоног: Advanced Features - %java_count%/40 файл
)

:: 4-р долоо хоног шалгах
if %script_count% geq 5 (
    echo [✓] 4-р долоо хоног: Deployment ^& DevOps (Дууссан)
) else (
    echo [○] 4-р долоо хоног: Deployment ^& DevOps - %script_count%/5 файл
)

echo.

:: Дараагийн алхамууд
echo Дараагийн алхамууд:
echo ══════════════════

if %total_files% lss 20 (
    echo → Backend entity болон repository файлуудыг бүрдүүлэх
    echo → Database тохиргоог дуусгах
    echo → REST API суурийг бэлтгэх
) else if %total_files% lss 50 (
    echo → Frontend компонентуудыг хөгжүүлэх
    echo → Customer Management хэсгийг дуусгах
    echo → Loan Application форм бэлтгэх
) else if %total_files% lss 80 (
    echo → Workflow engine нэмэх
    echo → Risk assessment систем
    echo → Notification систем
) else (
    echo → Testing болон documentation
    echo → Docker configuration
    echo → CI/CD pipeline тохиргоо
)

echo.

:: Системийн шалгалт
echo Системийн шалгалт:
echo ═════════════════

:: Backend шалгах
if exist "backend\mvnw.cmd" (
    echo [✓] Backend: Maven wrapper бэлэн
) else (
    echo [✗] Backend: Maven wrapper дутуу
)

:: Frontend шалгах
if exist "frontend\package.json" (
    echo [✓] Frontend: package.json бэлэн
) else (
    echo [✗] Frontend: package.json дутуу
)

:: Docker шалгах
if exist "docker-compose.yml" (
    echo [✓] Docker: Configuration бэлэн
) else (
    echo [✗] Docker: Configuration дутуу
)

:: Database шалгах
if exist "backend\src\main\resources\application.yml" (
    echo [✓] Database: Тохиргоо бэлэн
) else (
    echo [✗] Database: Тохиргоо дутуу
)

echo.
echo ═════════════════════════════════════════════
echo Дахин шалгахын тулд: progress-tracker.bat
echo Цонхыг хаахын тулд ямар нэг товч дарна уу...
echo ═════════════════════════════════════════════
pause