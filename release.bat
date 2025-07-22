@echo off
echo ========================================
echo Kafka UI Tool - Release Script
echo ========================================

REM Check if Java 17+ is available (required for jpackage)
java -version 2>&1 | findstr "java" >nul
if %errorlevel% neq 0 (
    echo ERROR: Java 17+ is required for jpackage
    echo Please install Java 17+ and ensure it's in your PATH
    pause
    exit /b 1
)

REM Check if jpackage is available
jpackage --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: jpackage is not available
    echo Please ensure you're using a full JDK  with jpackage support
    pause
    exit /b 1
)

REM Check if we're in the correct directory
if not exist "pom.xml" (
    echo ERROR: pom.xml not found. Make sure you're running this from the project root directory.
    pause
    exit /b 1
)

echo.
echo Step 1: Building all modules
call mvnw.cmd clean package -DskipTests
if %errorlevel% neq 0 (
    echo ERROR: Maven build failed
    pause
    exit /b 1
)

REM Check if JARs were created
if not exist "ui\target\kafka-ui-application-2.0.0-jar-with-dependencies.jar" (
    echo ERROR: UI JAR file not found after build
    pause
    exit /b 1
)

if not exist "service\target\kafka-ui-service-2.0.0-jar-with-dependencies.jar" (
    echo ERROR: Service JAR file not found after build
    pause
    exit /b 1
)

echo.
echo Step 2: Creating release directory
if exist "release" rmdir /s /q "release"
mkdir "release"

echo.
echo Step 3: Creating Windows executable with embedded JRE using jpackage
jpackage ^
    --input ui\target ^
    --name "KafkaUITool" ^
    --main-jar kafka-ui-application-2.0.0-jar-with-dependencies.jar ^
    --main-class com.kafkatool.ui.Main ^
    --type exe ^
    --dest "release" ^
    --app-version "2.0.0" ^
    --description "Cross-platform Kafka management tool" ^
    --vendor "KafkaUITool" ^
    --copyright "Copyright 2024" ^
    --win-console ^
    --win-dir-chooser ^
    --win-menu ^
    --win-shortcut

if %errorlevel% neq 0 (
    echo ERROR: jpackage failed to create executable
    pause
    exit /b 1
)

echo.
echo Step 4: Creating portable ZIP package
cd "release"
if exist "KafkaUITool-2.0.0.zip" del "KafkaUITool-2.0.0.zip"

REM Create a portable package with both JARs
mkdir "KafkaUITool-2.0.0-portable"
copy "..\ui\target\kafka-ui-application-2.0.0-jar-with-dependencies.jar" "KafkaUITool-2.0.0-portable\"
copy "..\service\target\kafka-ui-service-2.0.0-jar-with-dependencies.jar" "KafkaUITool-2.0.0-portable\"

echo @echo off > "KafkaUITool-2.0.0-portable\run-ui.bat"
echo echo Starting Kafka UI Tool GUI >> "KafkaUITool-2.0.0-portable\run-ui.bat"
echo java -jar kafka-ui-application-2.0.0-jar-with-dependencies.jar >> "KafkaUITool-2.0.0-portable\run-ui.bat"
echo pause >> "KafkaUITool-2.0.0-portable\run-ui.bat"

echo @echo off > "KafkaUITool-2.0.0-portable\run-service.bat"
echo echo Starting Kafka UI Tool Service >> "KafkaUITool-2.0.0-portable\run-service.bat"
echo java -jar kafka-ui-service-2.0.0-jar-with-dependencies.jar --api-server >> "KafkaUITool-2.0.0-portable\run-service.bat"
echo pause >> "KafkaUITool-2.0.0-portable\run-service.bat"

REM Create README for portable version
echo Kafka UI Tool - Portable Version > "KafkaUITool-2.0.0-portable\README.txt"
echo. >> "KafkaUITool-2.0.0-portable\README.txt"
echo Requirements: >> "KafkaUITool-2.0.0-portable\README.txt"
echo - Java 17+ installed and in PATH >> "KafkaUITool-2.0.0-portable\README.txt"
echo. >> "KafkaUITool-2.0.0-portable\README.txt"
echo To run GUI application: >> "KafkaUITool-2.0.0-portable\README.txt"
echo 1. Double-click run-ui.bat >> "KafkaUITool-2.0.0-portable\README.txt"
echo OR >> "KafkaUITool-2.0.0-portable\README.txt"
echo 2. Run: java -jar kafka-ui-application-2.0.0-jar-with-dependencies.jar >> "KafkaUITool-2.0.0-portable\README.txt"
echo. >> "KafkaUITool-2.0.0-portable\README.txt"
echo To run service mode: >> "KafkaUITool-2.0.0-portable\README.txt"
echo 1. Double-click run-service.bat >> "KafkaUITool-2.0.0-portable\README.txt"
echo OR >> "KafkaUITool-2.0.0-portable\README.txt"
echo 2. Run: java -jar kafka-ui-service-2.0.0-jar-with-dependencies.jar --api-server >> "KafkaUITool-2.0.0-portable\README.txt"

REM Use PowerShell to create ZIP (available on Windows 10+)
powershell -command "Compress-Archive -Path 'KafkaUITool-2.0.0-portable' -DestinationPath 'KafkaUITool-2.0.0-portable.zip'"

cd "%~dp0"

echo.
echo ========================================
echo RELEASE BUILD SUCCESSFUL!
echo ========================================
echo.
echo Created files in release directory:
echo - KafkaUITool-2.0.0.exe (Windows installer with embedded JRE)
echo - KafkaUITool-2.0.0-portable.zip (Portable JAR version)
echo.
echo The Windows installer includes an embedded JRE and does not require
echo Java to be installed on the target machine.
echo.
echo The portable version requires Java 17+ to be installed.
echo.
pause