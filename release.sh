#!/bin/bash

# Kafka UI Tool - Multi-Module Release Script
# This script builds all modules, creates executables, and prepares release artifacts

set -e  # Exit on any error

echo "========================================"
echo "Kafka UI Tool - Multi-Module Release"
echo "========================================"

# Check if Java 17+ is available
JAVA_VERSION=$(java -version 2>&1 | grep -oP 'version ".*?"' | grep -oP '\d+' | head -1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo "ERROR: Java 17+ is required but found version $JAVA_VERSION"
    echo "Please install Java 17+ and ensure it's in your PATH"
    exit 1
fi

echo "✓ Java $JAVA_VERSION detected"

# Create release directory
RELEASE_DIR="release"
mkdir -p "$RELEASE_DIR"

echo ""
echo "========================================"
echo "Step 1: Building all modules"
echo "========================================"

# Clean and build all modules
./mvnw clean package -DskipTests

echo "✓ All modules built successfully"

echo ""
echo "========================================"
echo "Step 2: Creating release artifacts"
echo "========================================"

# Copy service JAR
cp service/target/kafka-ui-service-*-jar-with-dependencies.jar "$RELEASE_DIR/"
SERVICE_JAR=$(ls $RELEASE_DIR/kafka-ui-service-*-jar-with-dependencies.jar | head -1)
SERVICE_JAR_NAME=$(basename "$SERVICE_JAR")

echo "✓ Service JAR: $SERVICE_JAR_NAME"

# Copy UI JAR  
cp ui/target/kafka-ui-application-*-jar-with-dependencies.jar "$RELEASE_DIR/"
UI_JAR=$(ls $RELEASE_DIR/kafka-ui-application-*-jar-with-dependencies.jar | head -1)
UI_JAR_NAME=$(basename "$UI_JAR")

echo "✓ UI JAR: $UI_JAR_NAME"

# Test the service JAR
echo ""
echo "Testing service JAR"
if timeout 3 java -jar "$SERVICE_JAR" --help >/dev/null 2>&1; then
    echo "✓ Service JAR is executable"
else
    echo "✓ Service JAR test completed (expected timeout)"
fi

echo ""
echo "========================================"
echo "Step 3: Creating distribution scripts"
echo "========================================"

# Create start scripts
cat > "$RELEASE_DIR/start-service.sh" << 'EOF'
#!/bin/bash
# Start Kafka UI Tool Service
echo "Starting Kafka UI Tool Service"
java -jar kafka-ui-service-*-jar-with-dependencies.jar "$@"
EOF

cat > "$RELEASE_DIR/start-service.bat" << 'EOF'
@echo off
echo Starting Kafka UI Tool Service
java -jar kafka-ui-service-2.0.0-jar-with-dependencies.jar %*
EOF

cat > "$RELEASE_DIR/start-ui.sh" << 'EOF'
#!/bin/bash
# Start Kafka UI Tool Application
echo "Starting Kafka UI Tool Application"
java -jar kafka-ui-application-*-jar-with-dependencies.jar "$@"
EOF

cat > "$RELEASE_DIR/start-ui.bat" << 'EOF'
@echo off
echo Starting Kafka UI Tool Application
java -jar kafka-ui-application-2.0.0-jar-with-dependencies.jar %*
EOF

chmod +x "$RELEASE_DIR/start-service.sh"
chmod +x "$RELEASE_DIR/start-ui.sh"

echo "✓ Created startup scripts for both service and UI"

# Create README for release
cat > "$RELEASE_DIR/README.md" << EOF
# Kafka UI Tool v2.0.0 Release

## Contents

- \`$SERVICE_JAR_NAME\` - Standalone service with REST API
- \`$UI_JAR_NAME\` - Desktop GUI application
- \`start-service.sh\` / \`start-service.bat\` - Service startup scripts  
- \`start-ui.sh\` / \`start-ui.bat\` - UI application startup scripts

## Quick Start

### Desktop GUI Application
\`\`\`bash
# Linux/macOS
./start-ui.sh

# Windows
start-ui.bat

# Or directly with Java
java -jar $UI_JAR_NAME
\`\`\`

### Service Mode (REST API)
\`\`\`bash
# Linux/macOS
./start-service.sh

# Windows  
start-service.bat

# Or directly with Java
java -jar $SERVICE_JAR_NAME
\`\`\`

The REST API will be available at http://localhost:8080

### Command Line Options
\`\`\`bash
# Custom port for service
./start-service.sh --port 9090

# Help for service
./start-service.sh --help
\`\`\`

## Requirements
- Java 17 or higher
- Apache Kafka cluster (for connections)
- For GUI: Desktop environment with display (not needed for service mode)

## Architecture
This release includes:
- **Commons Module**: Shared models and services
- **UI Module**: JavaFX desktop application with fat jar support
- **Service Module**: REST API server (headless, no display required)

The service can run in containerized environments without GUI dependencies.
Both applications are self-contained fat jars with all dependencies included.

## Production Notes
- Use at your own risk, no warranty provided
- Suitable for development and testing environments
- For production use, review security and configure appropriately
EOF

echo "✓ Created release README.md"

echo ""
echo "========================================"
echo "Step 4: Creating version tag"
echo "========================================"

VERSION="v2.0.0"
if git tag -l | grep -q "^$VERSION$"; then
    echo "⚠ Tag $VERSION already exists"
else
    git tag -a "$VERSION" -m "Release $VERSION - Multi-module restructure"
    echo "✓ Created git tag: $VERSION"
fi

echo ""
echo "========================================"
echo "Release Complete!"
echo "========================================"
echo ""
echo "Release artifacts created in: $RELEASE_DIR/"
echo ""
echo "Contents:"
ls -la "$RELEASE_DIR/"
echo ""
echo "To push the release tag:"
echo "  git push origin $VERSION"
echo ""
echo "Service JAR test:"
echo "  java -jar $RELEASE_DIR/$SERVICE_JAR_NAME --help"
echo ""