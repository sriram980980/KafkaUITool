# Kafka UI Tool - Release Script (PowerShell version)
# This script provides the same functionality as release.bat but using PowerShell

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Kafka UI Tool - Release Script (PowerShell)" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# Check if Java 17+ is available
try {
    $javaVersion = java -version 2>&1 | Select-String "17\.|18\.|19\.|20\.|21\."
    if (-not $javaVersion) {
        throw "Java 17+ not found"
    }
    Write-Host "✓ Java 17+ detected" -ForegroundColor Green
} catch {
    Write-Host "✗ ERROR: Java 17+ is required for jpackage" -ForegroundColor Red
    Write-Host "Please install Java 17+ and ensure it's in your PATH" -ForegroundColor Yellow
    Read-Host "Press Enter to exit"
    exit 1
}

# Check if jpackage is available
try {
    $null = jpackage --version 2>&1
    Write-Host "✓ jpackage is available" -ForegroundColor Green
} catch {
    Write-Host "✗ ERROR: jpackage is not available" -ForegroundColor Red
    Write-Host "Please ensure you're using a full JDK with jpackage support" -ForegroundColor Yellow
    Read-Host "Press Enter to exit"
    exit 1
}

# Change to the Java project directory
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$javaProjectDir = Join-Path $scriptDir "kafka-ui-java"

if (-not (Test-Path (Join-Path $javaProjectDir "pom.xml"))) {
    Write-Host "✗ ERROR: pom.xml not found in kafka-ui-java directory" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}

Set-Location $javaProjectDir

Write-Host ""
Write-Host "Step 1: Building JAR with dependencies" -ForegroundColor Yellow
& .\mvnw.cmd clean package
if ($LASTEXITCODE -ne 0) {
    Write-Host "✗ ERROR: Maven build failed" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}

$jarFile = "target\kafka-ui-tool-2.0.0-jar-with-dependencies.jar"
if (-not (Test-Path $jarFile)) {
    Write-Host "✗ ERROR: JAR file not found after build" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}

Write-Host "✓ JAR build successful" -ForegroundColor Green

Write-Host ""
Write-Host "Step 2: Creating release directory" -ForegroundColor Yellow
$releaseDir = Join-Path $scriptDir "release"
if (Test-Path $releaseDir) {
    Remove-Item $releaseDir -Recurse -Force
}
New-Item -ItemType Directory -Path $releaseDir | Out-Null
Write-Host "✓ Release directory created" -ForegroundColor Green

Write-Host ""
Write-Host "Step 3: Creating Windows executable with embedded JRE" -ForegroundColor Yellow

# Create the jpackage command
$jpackageArgs = @(
    "--input", "target"
    "--name", "KafkaUITool"
    "--main-jar", "kafka-ui-tool-2.0.0-jar-with-dependencies.jar"
    "--main-class", "com.kafkatool.KafkaUIApplication"
    "--type", "exe"
    "--dest", $releaseDir
    "--app-version", "2.0.0"
    "--description", "Cross-platform Kafka management tool"
    "--vendor", "KafkaUITool"
    "--copyright", "Copyright 2024"
    "--win-console"
    "--win-dir-chooser"
    "--win-menu"
    "--win-shortcut"
)

& jpackage @jpackageArgs
if ($LASTEXITCODE -ne 0) {
    Write-Host "✗ ERROR: jpackage failed to create executable" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}

Write-Host "✓ Windows executable created successfully" -ForegroundColor Green

Write-Host ""
Write-Host "Step 4: Creating portable ZIP package" -ForegroundColor Yellow
Set-Location $releaseDir

$portableDir = "KafkaUITool-2.0.0-portable"
New-Item -ItemType Directory -Path $portableDir | Out-Null

# Copy JAR file
Copy-Item (Join-Path $javaProjectDir $jarFile) $portableDir

# Create run.bat for portable version
$runBatContent = @"
@echo off
echo Starting Kafka UI Tool
java -jar kafka-ui-tool-2.0.0-jar-with-dependencies.jar
pause
"@
$runBatContent | Out-File -FilePath (Join-Path $portableDir "run.bat") -Encoding ASCII

# Create README for portable version
$readmeContent = @"
Kafka UI Tool - Portable Version

Requirements:
- Java 17+ installed and in PATH

To run:
1. Double-click run.bat
OR
2. Run: java -jar kafka-ui-tool-2.0.0-jar-with-dependencies.jar
"@
$readmeContent | Out-File -FilePath (Join-Path $portableDir "README.txt") -Encoding UTF8

# Create ZIP file
$zipPath = "$portableDir.zip"
if (Test-Path $zipPath) {
    Remove-Item $zipPath
}
Compress-Archive -Path $portableDir -DestinationPath $zipPath

Write-Host "✓ Portable ZIP package created" -ForegroundColor Green

Set-Location $scriptDir

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "RELEASE BUILD SUCCESSFUL!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Created files in release directory:" -ForegroundColor White
Write-Host "- KafkaUITool-2.0.0.exe (Windows installer with embedded JRE)" -ForegroundColor Yellow
Write-Host "- KafkaUITool-2.0.0-portable.zip (Portable JAR version)" -ForegroundColor Yellow
Write-Host ""
Write-Host "The Windows installer includes an embedded JRE and does not require" -ForegroundColor White
Write-Host "Java to be installed on the target machine." -ForegroundColor White
Write-Host ""
Write-Host "The portable version requires Java 17+ to be installed." -ForegroundColor White
Write-Host ""

Read-Host "Press Enter to exit"