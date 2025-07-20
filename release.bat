@echo off
echo ========================================
echo Kafka UI Tool - Release Script
echo ========================================

REM Check if Java 17+ is available (required for jpackage)
java -version 2>&1 | findstr "17\|18\|19\|20\|21" >nul
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
    echo Please ensure you're using a full JDK (not JRE) with jpackage support
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
echo Step 1: Building JAR with dependencies...
call mvnw.cmd clean package
if %errorlevel% neq 0 (
    echo ERROR: Maven build failed
    pause
    exit /b 1
)

REM Check if JAR was created
if not exist "target\kafka-ui-tool-2.0.0-jar-with-dependencies.jar" (
    echo ERROR: JAR file not found after build
    pause
    exit /b 1
)

echo.
echo Step 2: Creating release directory...
if exist "..\release" rmdir /s /q "..\release"
mkdir "..\release"

echo.
echo Step 3: Creating Windows executable with embedded JRE using jpackage...
jpackage ^
    --input target ^
    --name "KafkaUITool" ^
    --main-jar kafka-ui-tool-2.0.0-jar-with-dependencies.jar ^
    --main-class com.kafkatool.KafkaUIApplication ^
    --type exe ^
    --dest "..\release" ^
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
echo Step 4: Creating portable ZIP package...
cd "..\release"
if exist "KafkaUITool-2.0.0.zip" del "KafkaUITool-2.0.0.zip"

REM Create a portable package with the JAR
mkdir "KafkaUITool-2.0.0-portable"
copy "..\kafka-ui-java\target\kafka-ui-tool-2.0.0-jar-with-dependencies.jar" "KafkaUITool-2.0.0-portable\"
echo @echo off > "KafkaUITool-2.0.0-portable\run.bat"
echo echo Starting Kafka UI Tool... >> "KafkaUITool-2.0.0-portable\run.bat"
echo java -jar kafka-ui-tool-2.0.0-jar-with-dependencies.jar >> "KafkaUITool-2.0.0-portable\run.bat"
echo pause >> "KafkaUITool-2.0.0-portable\run.bat"

REM Create README for portable version
echo Kafka UI Tool - Portable Version > "KafkaUITool-2.0.0-portable\README.txt"
echo. >> "KafkaUITool-2.0.0-portable\README.txt"
echo Requirements: >> "KafkaUITool-2.0.0-portable\README.txt"
echo - Java 17+ installed and in PATH >> "KafkaUITool-2.0.0-portable\README.txt"
echo. >> "KafkaUITool-2.0.0-portable\README.txt"
echo To run: >> "KafkaUITool-2.0.0-portable\README.txt"
echo 1. Double-click run.bat >> "KafkaUITool-2.0.0-portable\README.txt"
echo OR >> "KafkaUITool-2.0.0-portable\README.txt"
echo 2. Run: java -jar kafka-ui-tool-2.0.0-jar-with-dependencies.jar >> "KafkaUITool-2.0.0-portable\README.txt"

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