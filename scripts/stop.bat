@echo off
chcp 65001 >nul
cls

echo ==========================================
echo ⛔ LOS СЕРVISҮҮДИЙГ ЗОГСООЖ БАЙНА...
echo ==========================================
echo.

echo Java процессүүдийг зогсоож байна (Backend)...
taskkill /f /im java.exe >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ Java процессүүд зогсогдлоо
) else (
    echo ℹ️  Java процесс олдсонгүй
)

echo Node.js процессүүдийг зогсоож байна (Frontend)...
taskkill /f /im node.exe >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ Node.js процессүүд зогсогдлоо
) else (
    echo ℹ️  Node.js процесс олдсонгүй
)

echo Command window хаах...
taskkill /f /fi "WindowTitle eq LOS Backend*" >nul 2>&1
taskkill /f /fi "WindowTitle eq LOS Frontend*" >nul 2>&1

echo.
echo ==========================================
echo ✅ БҮГД СЕРVISҮҮД ЗОГСОГДЛОО!
echo ==========================================
echo.
echo 🔄 Дахин эхлүүлэх: .\scripts\start.bat
echo.
pause
