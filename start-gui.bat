@echo off
echo ========================================
echo Kafka UI Tool - GUI Launcher
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

echo Select GUI launch method:
echo 1. Maven JavaFX Plugin (Recommended)
echo 2. Fat JAR with JavaFX arguments
echo 3. Fat JAR (simple - may show warnings)
echo.
set /p choice="Enter your choice (1-3): "

if "%choice%"=="1" (
    echo.
    echo Starting GUI using Maven JavaFX Plugin 
    echo This is the recommended method for GUI launch.
    echo.
    call mvnw.cmd javafx:run
) else if "%choice%"=="2" (
    echo.
    echo Building JAR if needed 
    if not exist "target\kafka-ui-tool-2.0.0-jar-with-dependencies.jar" (
        call mvnw.cmd clean package -DskipTests
        if %errorlevel% neq 0 (
            echo ERROR: Build failed
            pause
            exit /b 1
        )
    )
    echo.
    echo Starting GUI with proper JavaFX arguments 
    java --add-opens javafx.graphics/com.sun.javafx.application=ALL-UNNAMED --add-exports javafx.graphics/com.sun.glass.ui=ALL-UNNAMED --enable-native-access=ALL-UNNAMED -jar target\kafka-ui-tool-2.0.0-jar-with-dependencies.jar
) else if "%choice%"=="3" (
    echo.
    echo Building JAR if needed 
    if not exist "target\kafka-ui-tool-2.0.0-jar-with-dependencies.jar" (
        call mvnw.cmd clean package -DskipTests
        if %errorlevel% neq 0 (
            echo ERROR: Build failed
            pause
            exit /b 1
        )
    )
    echo.
    echo Starting GUI (simple method - may show warnings) 
    java -jar target\kafka-ui-tool-2.0.0-jar-with-dependencies.jar
) else (
    echo Invalid choice. Please run the script again.
)

echo.
pause
