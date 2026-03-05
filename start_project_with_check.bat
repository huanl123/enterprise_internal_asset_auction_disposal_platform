@echo off
setlocal enabledelayedexpansion

echo ============================================
echo Starting Enterprise Asset Auction Platform
echo ============================================

REM Default port settings
set BACKEND_PORT=8084
set FRONTEND_PORT=5173

REM Read backend port from config
if exist "backend\src\main\resources\application.yml" (
    for /f "tokens=2 delims=: " %%p in ('findstr "server.port" "backend\src\main\resources\application.yml"') do (
        set BACKEND_PORT=%%p
    )
)

echo Backend Port: %BACKEND_PORT%
echo Frontend Port: %FRONTEND_PORT%
echo.

REM Clean up ports
echo Cleaning up ports...
call :cleanup_port %BACKEND_PORT%
call :cleanup_port %FRONTEND_PORT%

REM Start backend
echo Starting Backend Service...
start "Backend Service" cmd /k "cd /d "%cd%\backend" && call mvn spring-boot:run"

REM Wait for backend to be ready
echo Waiting for backend to be ready...
call :wait_for_backend %BACKEND_PORT%

if !BACKEND_READY!==1 (
    echo Backend is ready!
) else (
    echo [WARNING] Backend may not be ready. Starting frontend anyway...
)

REM Start frontend
echo Starting Frontend Service...
start "Frontend Service" cmd /k "cd /d "%cd%\frontend" && call npm run dev"

echo.
echo ============================================
echo All services started!
echo Backend: http://localhost:%BACKEND_PORT%
echo Frontend: http://localhost:%FRONTEND_PORT%
echo ============================================
echo.
pause

endlocal
goto :eof

:cleanup_port
set PORT=%1
echo Checking port %PORT%...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :%PORT%') do (
    echo Terminating process occupying port %PORT% (PID: %%a)
    taskkill /pid %%a /f 2>nul
)
ping 127.0.0.1 -n 2 >nul
goto :eof

:wait_for_backend
set BACKEND_PORT=%1
set BACKEND_READY=0
set MAX_WAIT=30
set WAIT_COUNT=0

echo Waiting for backend service on port %BACKEND_PORT%...

:check_loop
if %WAIT_COUNT% geq %MAX_WAIT% (
    echo Timeout waiting for backend
    goto :end_wait
)

set /a WAIT_COUNT+=1
echo Checking backend status... (%WAIT_COUNT%/%MAX_WAIT%)

REM Try to connect to backend health endpoint
powershell -Command "try { $request = [System.Net.WebRequest]::Create('http://localhost:%BACKEND_PORT%/api/department/list'); $request.Timeout = 5000; $response = $request.GetResponse(); if ($response.StatusCode -eq 200) { exit 0 } else { exit 1 } } catch { exit 1 }" 2>nul

if errorlevel 1 (
    ping 127.0.0.1 -n 3 >nul
    goto :check_loop
) else (
    set BACKEND_READY=1
    goto :end_wait
)

:end_wait
goto :eof