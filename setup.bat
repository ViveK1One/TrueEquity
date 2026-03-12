@echo off
setlocal EnableDelayedExpansion
title TrueEquity Setup

echo ============================================================
echo  TrueEquity - Automatic Setup and Launch (Windows)
echo ============================================================
echo.

:: ── 1. Check Java ────────────────────────────────────────────
echo [1/4] Checking Java...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo  Java not found. Downloading and installing Java 17 via winget...
    winget install --id Microsoft.OpenJDK.17 --accept-source-agreements --accept-package-agreements
    if %errorlevel% neq 0 (
        echo.
        echo  winget failed. Please install Java 17 manually from:
        echo  https://adoptium.net/temurin/releases/?version=17
        echo  Then re-run this script.
        pause
        exit /b 1
    )
    echo  Java installed. Refreshing environment...
    :: Refresh PATH so java is found in this session
    for /f "tokens=*" %%i in ('where java 2^>nul') do set JAVA_PATH=%%i
    if "!JAVA_PATH!"=="" (
        echo  Please restart your terminal and run setup.bat again after Java is installed.
        pause
        exit /b 1
    )
) else (
    for /f "tokens=3" %%v in ('java -version 2^>^&1 ^| findstr /i "version"') do (
        set JAVA_VER=%%v
    )
    echo  Java found: !JAVA_VER!
)

:: ── 2. Check Maven ───────────────────────────────────────────
echo.
echo [2/4] Checking Maven...
mvn -v >nul 2>&1
if %errorlevel% neq 0 (
    echo  Maven not found. Downloading Maven 3.9...
    set MAVEN_VERSION=3.9.6
    set MAVEN_URL=https://downloads.apache.org/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip
    set MAVEN_DIR=%USERPROFILE%\apache-maven-3.9.6

    if not exist "%MAVEN_DIR%" (
        echo  Downloading from !MAVEN_URL!
        powershell -Command "Invoke-WebRequest -Uri '!MAVEN_URL!' -OutFile '%TEMP%\maven.zip'"
        if %errorlevel% neq 0 (
            echo  Download failed. Please install Maven manually from https://maven.apache.org/download.cgi
            pause
            exit /b 1
        )
        powershell -Command "Expand-Archive -Path '%TEMP%\maven.zip' -DestinationPath '%USERPROFILE%' -Force"
        del "%TEMP%\maven.zip"
    )

    set PATH=%MAVEN_DIR%\bin;%PATH%
    mvn -v >nul 2>&1
    if %errorlevel% neq 0 (
        echo  Maven setup failed. Please add %MAVEN_DIR%\bin to your PATH and re-run.
        pause
        exit /b 1
    )
    echo  Maven installed at %MAVEN_DIR%
) else (
    for /f "tokens=3" %%v in ('mvn -v 2^>^&1 ^| findstr /i "Apache Maven"') do (
        echo  Maven found: %%v
        goto :maven_ok
    )
    :maven_ok
)

:: ── 3. Check Node.js ─────────────────────────────────────────
echo.
echo [3/4] Checking Node.js...
node -v >nul 2>&1
if %errorlevel% neq 0 (
    echo  Node.js not found. Downloading and installing Node.js 20 LTS via winget...
    winget install --id OpenJS.NodeJS.LTS --accept-source-agreements --accept-package-agreements
    if %errorlevel% neq 0 (
        echo.
        echo  winget failed. Please install Node.js 20 LTS manually from:
        echo  https://nodejs.org/en/download
        echo  Then re-run this script.
        pause
        exit /b 1
    )
    echo  Node.js installed. Please restart this terminal and run setup.bat again.
    pause
    exit /b 0
) else (
    for /f %%v in ('node -v') do echo  Node.js found: %%v
)

:: ── 4. Create frontend\.env.local if missing ─────────────────
echo.
echo [4/4] Checking frontend environment file...
if not exist "frontend\.env.local" (
    echo  Creating frontend\.env.local with Supabase credentials...
    (
        echo DATABASE_HOST=db.laohdpupmjhfsupcomzv.supabase.co
        echo DATABASE_PORT=5432
        echo DATABASE_NAME=postgres
        echo DATABASE_USER=postgres
        echo DATABASE_PASSWORD=-6C,*wamDmFQ-DY
        echo JAVA_BACKEND_URL=http://localhost:8080
        echo NEXT_PUBLIC_APP_URL=http://localhost:3000
    ) > "frontend\.env.local"
    echo  Created frontend\.env.local
) else (
    echo  frontend\.env.local already exists, skipping.
)

:: ── 5. Install frontend dependencies ─────────────────────────
echo.
echo [5/5] Installing frontend dependencies (npm install)...
cd frontend
call npm install
if %errorlevel% neq 0 (
    echo  npm install failed. Check your Node.js installation.
    cd ..
    pause
    exit /b 1
)
cd ..
echo  Frontend dependencies installed.

:: ── 6. Build backend ─────────────────────────────────────────
echo.
echo [6/6] Building backend (mvn clean install -DskipTests)...
call mvn clean install -DskipTests
if %errorlevel% neq 0 (
    echo  Maven build failed. Check Java and Maven installation.
    pause
    exit /b 1
)
echo  Backend built successfully.

:: ── 7. Launch both services ───────────────────────────────────
echo.
echo ============================================================
echo  Starting TrueEquity...
echo  - Backend  : http://localhost:8080
echo  - Frontend : http://localhost:3000
echo.
echo  Two terminal windows will open.
echo  Wait for the backend to print "Application started" before
echo  opening the browser.
echo ============================================================
echo.

:: Kill any existing process on port 8080 before starting
for /f "tokens=5" %%p in ('netstat -ano 2^>nul ^| findstr ":8080 "') do (
    taskkill /PID %%p /F >nul 2>&1
)

:: Start backend in a new window
start "TrueEquity Backend" cmd /k "mvn spring-boot:run"

:: Give backend a few seconds to start binding the port
timeout /t 5 /nobreak >nul

:: Start frontend in a new window
start "TrueEquity Frontend" cmd /k "cd frontend && npm run dev"

echo.
echo  Both services are starting in separate windows.
echo  Open http://localhost:3000 in your browser once both are ready.
echo.
pause
