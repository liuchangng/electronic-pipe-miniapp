@echo off
rem ============================================================
rem Electronic Pipe MiniApp - One-click launcher
rem Starts: Backend API(8080) + Admin(3001) + Build WeChat MiniApp
rem Features: Health check / Force restart stale services / Tab labels
rem ============================================================
setlocal
chcp 65001 >nul 2>&1
cd /d "%~dp0"

rem ---------- Environment ----------
set "JAVA_HOME=D:\Software\Java\jdk-26.0.2.1"
set "PATH=D:\Software\maven-3.8.3\bin;%JAVA_HOME%\bin;%PATH%"

set SERVER_PORT=8080
set ADMIN_PORT=3001
set SERVER_URL=http://127.0.0.1:%SERVER_PORT%
set ADMIN_URL=http://127.0.0.1:%ADMIN_PORT%

echo ========================================
echo   Electronic Pipe - Start All
echo ========================================
echo.

rem ---------- [1/3] Start Backend API ----------
call :port_busy %SERVER_PORT%
if %errorlevel% equ 0 (
  rem Port is in use - check if service is actually healthy
  curl.exe -s -m 3 -o nul %SERVER_URL%/api/health >nul 2>&1
  if %errorlevel% equ 0 (
    echo [SKIP] Backend already running and healthy on port %SERVER_PORT%
  ) else (
    echo [WARN] Port %SERVER_PORT% is occupied but service is not healthy
    echo [KILL] Force killing stale process on port %SERVER_PORT%...
    for /f %%p in ('powershell -NoProfile -Command "Get-NetTCPConnection -LocalPort %SERVER_PORT% -State Listen -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess -Unique"') do (
      taskkill /PID %%p /T /F >nul 2>&1
    )
    call :wait_port_free %SERVER_PORT% 15
    echo [START] Backend API  %SERVER_URL%
    start "Pipe-Server" cmd /k "cd /d %~dp0server && mvn spring-boot:run"
  )
) else (
  echo [START] Backend API  %SERVER_URL%
  start "Pipe-Server" cmd /k "cd /d %~dp0server && mvn spring-boot:run"
)

rem ---------- [2/3] Start Admin ----------
call :port_busy %ADMIN_PORT%
if %errorlevel% equ 0 (
  rem Port is in use - check if service is actually healthy
  curl.exe -s -m 3 -o nul %ADMIN_URL% >nul 2>&1
  if %errorlevel% equ 0 (
    echo [SKIP] Admin already running and healthy on port %ADMIN_PORT%
  ) else (
    echo [WARN] Port %ADMIN_PORT% is occupied but service is not healthy
    echo [KILL] Force killing stale process on port %ADMIN_PORT%...
    for /f %%p in ('powershell -NoProfile -Command "Get-NetTCPConnection -LocalPort %ADMIN_PORT% -State Listen -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess -Unique"') do (
      taskkill /PID %%p /T /F >nul 2>&1
    )
    call :wait_port_free %ADMIN_PORT% 15
    echo [START] Admin panel %ADMIN_URL%
    start "Pipe-Admin" cmd /k "cd /d %~dp0admin && npm run dev"
  )
) else (
  echo [START] Admin panel %ADMIN_URL%
  start "Pipe-Admin" cmd /k "cd /d %~dp0admin && npm run dev"
)

rem ---------- Wait for Backend health check ----------
echo.
echo [WAIT] Waiting for backend...
set /a TRIES=0

:wait_server
curl.exe -s -m 2 -o nul %SERVER_URL%/api/health >nul 2>&1
if %errorlevel% neq 0 (
  set /a TRIES+=1
  if %TRIES% geq 40 goto server_timeout
  ping -n 3 127.0.0.1 >nul
  goto wait_server
)
echo [OK] Backend is healthy

rem ---------- Wait for Admin health check ----------
echo [WAIT] Waiting for admin...
set /a TRIES=0

:wait_admin
curl.exe -s -m 2 -o nul %ADMIN_URL% >nul 2>&1
if %errorlevel% neq 0 (
  set /a TRIES+=1
  if %TRIES% geq 30 goto admin_timeout
  ping -n 3 127.0.0.1 >nul
  goto wait_admin
)
echo [OK] Admin is up

rem ---------- [3/3] Build WeChat MiniApp ----------
echo.
echo [BUILD] Building WeChat MiniApp...
cd /d "%~dp0client"
call npm run build:mp-weixin
if %errorlevel% neq 0 (
  echo [WARN] MiniApp build failed, please check manually
) else (
  echo [OK] MiniApp built: dist\build\mp-weixin
)

echo.
echo ========================================
echo   All services started!
echo ========================================
echo   Backend API:  %SERVER_URL%
echo   Admin panel:  %ADMIN_URL%
echo   MiniApp:      client\dist\build\mp-weixin
echo ========================================
echo.
echo Closing this window won't affect running services
echo Run stop.bat to shut them down
pause
endlocal
exit /b 0

rem ---------- Timeout handlers ----------
:server_timeout
echo [WARN] Backend startup timed out (~2min), may still be starting
echo        Check manually: %SERVER_URL%/api/health
goto continue_after_timeout

:admin_timeout
echo [WARN] Admin startup timed out, may still be starting
echo        Check manually: %ADMIN_URL%

:continue_after_timeout
cd /d "%~dp0client"
echo.
echo [BUILD] Building WeChat MiniApp...
call npm run build:mp-weixin
echo.
echo ========================================
echo   Services may still be starting
echo ========================================
pause
endlocal
exit /b 1

rem ---------- Subroutine: check if port is busy ----------
:port_busy
rem %1 = port number. Returns 0 if listening, 1 if free
powershell -NoProfile -Command "if (Get-NetTCPConnection -LocalPort %~1 -State Listen -ErrorAction SilentlyContinue) { exit 0 } else { exit 1 }"
exit /b %errorlevel%

rem ---------- Subroutine: wait for port to be free ----------
:wait_port_free
rem %1 = port number, %2 = max retries (default 15)
set /a WP_TRIES=0
set /a WP_MAX=%~2
if %WP_MAX% leq 0 set /a WP_MAX=15
:wpf_loop
set /a WP_TRIES+=1
if %WP_TRIES% geq %WP_MAX% goto wpf_done
powershell -NoProfile -Command "if (Get-NetTCPConnection -LocalPort %~1 -State Listen -ErrorAction SilentlyContinue) { exit 1 } else { exit 0 }" >nul 2>&1
if %errorlevel% equ 0 goto wpf_done
ping -n 2 127.0.0.1 >nul
goto wpf_loop
:wpf_done
exit /b 0