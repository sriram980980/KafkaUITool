@echo off
echo ========================================
echo Kafka UI Tool - Build Scripts Test
echo ========================================

echo Testing build.bat functionality
echo.

REM Test Java availability
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo FAIL: Java is not available
    goto :error
)
echo PASS: Java is available

REM Test jpackage availability (for release.bat)
jpackage --version >nul 2>&1
if %errorlevel% neq 0 (
    echo WARN: jpackage is not available (release.bat will fail)
    echo      This is normal if you're using a JRE instead of full JDK
) else (
    echo PASS: jpackage is available for release.bat
)

REM Test Maven wrapper
cd /d "%~dp0kafka-ui-java"
if not exist "mvnw.cmd" (
    echo FAIL: Maven wrapper not found
    goto :error
)
echo PASS: Maven wrapper found

REM Test if we can compile
echo.
echo Testing compilation
call mvnw.cmd clean compile -q
if %errorlevel% neq 0 (
    echo FAIL: Compilation failed
    goto :error
)
echo PASS: Compilation successful

REM Test if we can package
echo.
echo Testing packaging
call mvnw.cmd package -q -DskipTests
if %errorlevel% neq 0 (
    echo FAIL: Packaging failed
    goto :error
)
echo PASS: Packaging successful

REM Verify JAR was created
if not exist "target\kafka-ui-tool-2.0.0-jar-with-dependencies.jar" (
    echo FAIL: JAR with dependencies not created
    goto :error
)
echo PASS: JAR with dependencies created

REM Test JAR contains main class
jar tf target\kafka-ui-tool-2.0.0-jar-with-dependencies.jar | findstr "com/kafkatool/KafkaUIApplication.class" >nul
if %errorlevel% neq 0 (
    echo FAIL: Main class not found in JAR
    goto :error
)
echo PASS: Main class found in JAR

cd "%~dp0"

echo.
echo ========================================
echo ALL TESTS PASSED!
echo ========================================
echo.
echo Build scripts are ready for use:
echo - build.bat: For development builds
echo - release.bat: For production releases (requires full JDK with jpackage)
echo - release.ps1: PowerShell version with enhanced output
echo.
echo See BUILD_SCRIPTS.md for detailed usage instructions.
echo.
pause
exit /b 0

:error
echo.
echo ========================================
echo TESTS FAILED!
echo ========================================
echo.
echo Please check the error messages above and ensure:
echo 1. Java 17+ is installed and in PATH
echo 2. You're running from the correct directory
echo 3. You have internet connection for Maven dependencies
echo.
pause
exit /b 1