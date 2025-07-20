@echo off
echo ========================================
echo Kafka UI Tool - REST API Server
echo ========================================

REM Check if Java is available
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Java is not installed or not in PATH
    echo Please install Java 17+ and ensure it's in your PATH
    pause
    exit /b 1
)

REM Change to the Java project directory
cd /d "%~dp0kafka-ui-java"

if not exist "pom.xml" (
    echo ERROR: pom.xml not found. Make sure you're running this from the correct directory.
    pause
    exit /b 1
)

REM Check if JAR exists, if not build it
if not exist "target\kafka-ui-tool-2.0.0-jar-with-dependencies.jar" (
    echo JAR file not found. Building the project first
    call mvnw.cmd clean package
    if %errorlevel% neq 0 (
        echo ERROR: Build failed
        pause
        exit /b 1
    )
)

echo.
echo Starting REST API Server
echo Default port: 8080
echo.
echo Available endpoints:
echo   Health check: http://localhost:8080/api/health
echo   Info: http://localhost:8080/api/info
echo.
echo Press Ctrl+C to stop the server
echo.

REM Start the REST API server
java -cp target\kafka-ui-tool-2.0.0-jar-with-dependencies.jar com.kafkatool.RestApiMain %*

if %errorlevel% neq 0 (
    echo ERROR: Failed to start REST API server
    pause
    exit /b 1
)