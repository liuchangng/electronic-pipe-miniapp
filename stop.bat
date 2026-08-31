@echo off
rem ============================================================
rem Electronic Pipe MiniApp - Stop All Services
rem Kills processes by port, then WAITS until ports are freed
rem ============================================================
setlocal
chcp 65001 >nul 2>&1
cd /d "%~dp0"

set SERVER_PORT=8080
set ADMIN_PORT=3001

echo ========================================
echo   Electronic Pipe - Stop All
echo ========================================
echo.

rem ---------- Stop Backend API ----------
echo [STOP] Checking port %SERVER_PORT% (Backend API)...
for /f %%p in ('powershell -NoProfile -Command "Get-NetTCPConnection -LocalPort %SERVER_PORT% -State Listen -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess -Unique"') do (
  echo [KILL] Backend PID %%p (port %SERVER_PORT%)
  taskkill /PID %%p /T /F >nul 2>&1
)

rem ---------- Stop Admin Panel ----------
echo [STOP] Checking port %ADMIN_PORT% (Admin panel)...
for /f %%p in ('powershell -NoProfile -Command "Get-NetTCPConnection -LocalPort %ADMIN_PORT% -State Listen -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess -Unique"') do (
  echo [KILL] Admin PID %%p (port %ADMIN_PORT%)
  taskkill /PID %%p /T /F >nul 2>&1
)

rem ---------- Wait for ports to be fully released ----------
echo.
echo [WAIT] Waiting for ports to be released...

set /a WAIT_TRIES=0
:wait_ports_free
set /a WAIT_TRIES+=1
if %WAIT_TRIES% geq 30 goto ports_timeout

powershell -NoProfile -Command "if (Get-NetTCPConnection -LocalPort %SERVER_PORT% -State Listen -ErrorAction SilentlyContinue) { exit 1 } else { exit 0 }" >nul 2>&1
if %errorlevel% neq 0 (
  ping -n 2 127.0.0.1 >nul
  goto wait_ports_free
)

powershell -NoProfile -Command "if (Get-NetTCPConnection -LocalPort %ADMIN_PORT% -State Listen -ErrorAction SilentlyContinue) { exit 1 } else { exit 0 }" >nul 2>&1
if %errorlevel% neq 0 (
  ping -n 2 127.0.0.1 >nul
  goto wait_ports_free
)

echo [OK] All ports freed
echo.
echo [DONE] All services stopped
echo.
pause
endlocal
exit /b 0

:ports_timeout
echo [WARN] Ports still in use after 30s, some processes may not have stopped cleanly
echo        You can try running stop.bat again or manually check with: netstat -ano ^| findstr "%SERVER_PORT% %ADMIN_PORT%"
echo.
pause
endlocal
exit /b 1