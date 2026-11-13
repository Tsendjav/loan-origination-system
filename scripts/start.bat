@echo off
chcp 65001 >nul
rem ==========================================================================
rem  LOS — нэг командаар асаах (start.ps1-ийн wrapper)
rem    scripts\start.bat            Postgres + backend (Docker) + frontend
rem    scripts\start.bat -Stop      бүгдийг зогсоох (өгөгдөл хадгалагдана)
rem    scripts\start.bat -Local     backend-ийг локал mvn-ээр (hot reload)
rem ==========================================================================
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0..\start.ps1" %*
exit /b %errorlevel%
