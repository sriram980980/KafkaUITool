@echo off
echo ========================================
echo Kafka UI Tool v2.0 - Decoupled Launch Demo
echo ========================================
echo.

REM Change to the Java project directory
cd /d "%~dp0kafka-ui-java"

if not exist "target\kafka-ui-tool-2.0.0-jar-with-dependencies.jar" (
    echo ERROR: JAR file not found. Please build the project first.
    echo Run: mvnw.cmd clean package
    pause
    exit /b 1
)

echo Select launch mode:
echo 1. GUI Mode (requires JavaFX)
echo 2. API Server Mode (headless, no JavaFX required)
echo 3. Help/Info
echo.
set /p choice="Enter your choice (1-3): "

if "%choice%"=="1" (
    echo.
    echo Starting GUI mode...
    echo Note: This requires JavaFX to be available
    echo.
    java -jar target\kafka-ui-tool-2.0.0-jar-with-dependencies.jar
) else if "%choice%"=="2" (
    echo.
    echo Starting API Server mode...
    echo This mode runs without JavaFX requirements
    echo.
    java -jar target\kafka-ui-tool-2.0.0-jar-with-dependencies.jar --api-server --port=8080
) else if "%choice%"=="3" (
    echo.
    echo Showing help information...
    echo.
    java -jar target\kafka-ui-tool-2.0.0-jar-with-dependencies.jar --help
) else (
    echo Invalid choice. Please run the script again.
)

echo.
pause
