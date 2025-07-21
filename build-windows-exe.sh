#!/bin/bash

# Build Windows Executable for Kafka UI Tool
# This script creates a Windows .exe using jpackage

set -e

echo "========================================"
echo "Building Windows Executable for UI"
echo "========================================"

# Check if Java 17+ is available
JAVA_VERSION=$(java -version 2>&1 | grep -oP 'version ".*?"' | grep -oP '\d+' | head -1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo "ERROR: Java 17+ is required but found version $JAVA_VERSION"
    echo "Please install Java 17+ and ensure it's in your PATH"
    exit 1
fi

# Check if jpackage is available
if ! command -v jpackage &> /dev/null; then
    echo "ERROR: jpackage is not available. Make sure you have JDK 17+ (not just JRE)"
    exit 1
fi

echo "✓ Java $JAVA_VERSION with jpackage detected"

# Create temp directory for Windows executable build
WIN_BUILD_DIR="/tmp/windows-build"
mkdir -p "$WIN_BUILD_DIR"

echo ""
echo "========================================"
echo "Step 1: Building all modules"
echo "========================================"

# Build all modules (commons must be built first for ui dependency)
./mvnw clean package -DskipTests

echo "✓ All modules built successfully"

echo ""
echo "========================================"
echo "Step 2: Creating cross-platform executable"
echo "========================================"

# Note: We're on Linux, so we'll create a Linux executable for demonstration
# For Windows, this would need to be run on a Windows machine with JDK 17+
PLATFORM=$(uname -s | tr '[:upper:]' '[:lower:]')
echo "Detected platform: $PLATFORM"

if [ "$PLATFORM" = "linux" ]; then
    echo "Creating Linux executable (for demonstration)..."
    # Create Linux executable using jpackage
    jpackage \
        --input ui/target \
        --main-jar kafka-ui-application-2.0.0-jar-with-dependencies.jar \
        --main-class com.kafkatool.ui.Main \
        --name "KafkaUITool" \
        --app-version "2.0.0" \
        --description "Kafka UI Tool - Cross-platform Kafka management application" \
        --vendor "Kafka UI Tool Project" \
        --dest "$WIN_BUILD_DIR" \
        --type deb
        
    echo "✓ Linux package created (substitute for Windows executable)"
    echo ""
    echo "NOTE: To create Windows executable, run this on Windows:"
    echo "jpackage --input ui/target --main-jar kafka-ui-application-2.0.0-jar-with-dependencies.jar --main-class com.kafkatool.ui.Main --name KafkaUITool --app-version 2.0.0 --dest . --type exe --win-console --win-dir-chooser --win-shortcut --win-menu"
else
    # Create Windows executable using jpackage (if on Windows)
    jpackage \
        --input ui/target \
        --main-jar kafka-ui-application-2.0.0-jar-with-dependencies.jar \
        --main-class com.kafkatool.ui.Main \
        --name "KafkaUITool" \
        --app-version "2.0.0" \
        --description "Kafka UI Tool - Cross-platform Kafka management application" \
        --vendor "Kafka UI Tool Project" \
        --copyright "Mozilla Public License 2.0" \
        --dest "$WIN_BUILD_DIR" \
        --type exe \
        --win-console \
        --win-dir-chooser \
        --win-shortcut \
        --win-menu

    echo "✓ Windows executable created"
fi

echo ""
echo "========================================"
echo "Step 3: Testing executable package"
echo "========================================"

# Test the executable (this will fail in headless but we can check it exists)
if [ -f "$WIN_BUILD_DIR/kafkauitool_2.0.0-1_amd64.deb" ] || [ -f "$WIN_BUILD_DIR/KafkaUITool-2.0.0.exe" ]; then
    echo "✓ Executable package found:"
    ls -la "$WIN_BUILD_DIR/"
else
    echo "✗ Executable package not found"
    ls -la "$WIN_BUILD_DIR/"
fi

echo ""
echo "========================================"
echo "Cross-Platform Build Complete!"
echo "========================================"
echo ""
echo "Build artifacts created at: $WIN_BUILD_DIR/"
echo ""
echo "To copy to release directory:"
echo "  cp $WIN_BUILD_DIR/* release/ 2>/dev/null || true"
echo ""
echo "For Windows executable (.exe), run this script on Windows with JDK 17+:"
echo "  jpackage --input ui/target --main-jar kafka-ui-application-2.0.0-jar-with-dependencies.jar \\"
echo "    --main-class com.kafkatool.ui.Main --name KafkaUITool --app-version 2.0.0 \\"
echo "    --dest . --type exe --win-console --win-dir-chooser --win-shortcut --win-menu"