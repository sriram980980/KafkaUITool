@echo off
echo ========================================
echo Kafka UI Tool - Build Script
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

echo.
echo Step 1: Cleaning previous builds...
call mvnw.cmd clean
if %errorlevel% neq 0 (
    echo ERROR: Maven clean failed
    pause
    exit /b 1
)

echo.
echo Step 2: Compiling sources...
call mvnw.cmd compile
if %errorlevel% neq 0 (
    echo ERROR: Maven compile failed
    pause
    exit /b 1
)

echo.
echo Step 3: Running tests...
call mvnw.cmd test
if %errorlevel% neq 0 (
    echo ERROR: Tests failed
    pause
    exit /b 1
)

echo.
echo Step 4: Packaging JAR with dependencies...
call mvnw.cmd package
if %errorlevel% neq 0 (
    echo ERROR: Maven package failed
    pause
    exit /b 1
)

echo.
echo ========================================
echo BUILD SUCCESSFUL!
echo ========================================
echo.
echo JAR files created in target directory:
echo - kafka-ui-tool-2.0.0.jar
echo - kafka-ui-tool-2.0.0-jar-with-dependencies.jar
echo.
echo To run the application:
echo java --module-path path\to\javafx\lib --add-modules javafx.controls,javafx.fxml -jar target\kafka-ui-tool-2.0.0-jar-with-dependencies.jar
echo.
pause