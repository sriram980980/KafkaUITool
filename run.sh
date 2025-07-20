#!/bin/bash

# Kafka UI Tool Launcher Script
# This script simplifies running the Kafka UI Tool Java application

# Set script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$SCRIPT_DIR/kafka-ui-java"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if Java is installed
check_java() {
    if ! command -v java &> /dev/null; then
        print_error "Java is not installed or not in PATH"
        print_info "Please install Java 17 or higher and try again"
        exit 1
    fi
    
    # Check Java version
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | awk -F '"' '{print $2}' | cut -d'.' -f1)
    if [ "$JAVA_VERSION" -lt 17 ]; then
        print_error "Java 17 or higher is required (found Java $JAVA_VERSION)"
        exit 1
    fi
    
    print_info "Java $JAVA_VERSION detected"
}

# Check if Maven is installed
check_maven() {
    if ! command -v mvn &> /dev/null; then
        print_error "Maven is not installed or not in PATH"
        print_info "Please install Maven 3.6+ and try again"
        exit 1
    fi
    
    print_info "Maven detected"
}

# Build the project
build_project() {
    print_info "Building Kafka UI Tool..."
    cd "$PROJECT_DIR" || exit 1
    
    if ! mvn clean package -DskipTests; then
        print_error "Build failed"
        exit 1
    fi
    
    print_info "Build completed successfully"
}

# Run the application
run_application() {
    print_info "Starting Kafka UI Tool..."
    cd "$PROJECT_DIR" || exit 1
    
    # Try to run with JavaFX Maven plugin first
    if command -v mvn &> /dev/null; then
        mvn javafx:run
    else
        # Fallback to direct Java execution
        JAR_FILE="target/kafka-ui-tool-2.0.0-jar-with-dependencies.jar"
        if [ -f "$JAR_FILE" ]; then
            java -jar "$JAR_FILE"
        else
            print_error "JAR file not found. Please run './run.sh build' first"
            exit 1
        fi
    fi
}

# Clean build artifacts
clean_project() {
    print_info "Cleaning build artifacts..."
    cd "$PROJECT_DIR" || exit 1
    mvn clean
    print_info "Clean completed"
}

# Show help
show_help() {
    echo "Kafka UI Tool Launcher"
    echo ""
    echo "Usage: $0 [COMMAND]"
    echo ""
    echo "Commands:"
    echo "  run     - Build and run the application (default)"
    echo "  build   - Build the project only"
    echo "  clean   - Clean build artifacts"
    echo "  test    - Run tests"
    echo "  help    - Show this help message"
    echo ""
    echo "Requirements:"
    echo "  - Java 17 or higher"
    echo "  - Maven 3.6 or higher"
}

# Run tests
run_tests() {
    print_info "Running tests..."
    cd "$PROJECT_DIR" || exit 1
    mvn test
}

# Main script logic
main() {
    check_java
    
    case "${1:-run}" in
        "run")
            check_maven
            build_project
            run_application
            ;;
        "build")
            check_maven
            build_project
            ;;
        "clean")
            check_maven
            clean_project
            ;;
        "test")
            check_maven
            run_tests
            ;;
        "help"|"-h"|"--help")
            show_help
            ;;
        *)
            print_error "Unknown command: $1"
            show_help
            exit 1
            ;;
    esac
}

# Run main function with all arguments
main "$@"