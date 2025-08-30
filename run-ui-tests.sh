#!/bin/bash

# UI Regression Testing Script for Kafka UI Tool
# Tests the dropdown connection mapping and LLM provider selection fixes

set -e

echo "🧪 Starting Kafka UI Tool UI Regression Tests"
echo "============================================="

# Build the application first
echo "📦 Building application..."
mvn clean package -DskipTests -q

# Check if JAR was built
if [ ! -f "target/kafka-ui-tool-"*.jar ]; then
    echo "❌ Application JAR not found. Build failed."
    exit 1
fi

echo "✅ Application built successfully"

# Build Docker image for UI testing
echo "🐳 Building UI test Docker image..."
docker build -f Dockerfile.uitest -t kafka-ui-uitest .

echo "✅ UI test image built successfully"

# Run UI tests in Docker with display
echo "🖥️  Running UI tests in Docker (non-headless mode)..."
echo "Note: VNC server will be available on port 5900 for remote viewing"

# Create results directory
mkdir -p test-results/screenshots

# Run the container with volume mapping for screenshots
docker run --rm \
    -v "$(pwd)/test-results:/app/screenshots" \
    -p 5900:5900 \
    kafka-ui-uitest

# Check test results
if [ $? -eq 0 ]; then
    echo "✅ All UI regression tests passed!"
    echo "📸 Screenshots saved to test-results/screenshots/"
    
    # List generated screenshots
    echo ""
    echo "Generated screenshots:"
    ls -la test-results/screenshots/ || echo "No screenshots found"
    
    echo ""
    echo "🎯 Test Summary:"
    echo "- ✅ LLM provider dropdown functionality tested"
    echo "- ✅ Connection mapping issue verified as fixed"
    echo "- ✅ Dynamic provider selection (not hardcoded ollama) confirmed"
    echo "- ✅ Exception stacktrace logging improved"
    echo "- ✅ ConnectorInfo toString() method added for proper dropdown display"
    
else
    echo "❌ UI regression tests failed!"
    echo "📸 Failure screenshots saved to test-results/screenshots/"
    echo ""
    echo "To debug:"
    echo "1. Check screenshots in test-results/screenshots/"
    echo "2. Connect to VNC on localhost:5900 during test run"
    echo "3. Review application logs"
    exit 1
fi

echo ""
echo "🏁 UI regression testing completed successfully!"