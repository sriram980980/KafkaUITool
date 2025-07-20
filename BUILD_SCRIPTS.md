# Build and Release Scripts

This repository includes automated build scripts for Windows to streamline the development and release process.

## Scripts Overview

### 1. build.bat - Development Build Script

**Purpose**: Quick development builds for testing and development.

**What it does**:
- Cleans previous builds
- Compiles the Java source code
- Runs unit tests
- Creates JAR files including the fat JAR with all dependencies

**Output**:
- `target/kafka-ui-tool-2.0.0.jar` - Basic JAR
- `target/kafka-ui-tool-2.0.0-jar-with-dependencies.jar` - Fat JAR with all dependencies

**Usage**:
```cmd
build.bat
```

**Requirements**:
- Java 17+ installed and in PATH
- Internet connection (for Maven dependencies on first run)

### 2. release.bat - Production Release Script

**Purpose**: Creates production-ready distributions with Windows executables.

**What it does**:
- Builds the JAR with dependencies (using build pipeline)
- Creates a Windows executable (.exe) with embedded JRE using jpackage
- Creates a portable ZIP package for systems with Java already installed
- Sets up proper Windows installer with shortcuts and menu entries

**Output** (in `release/` directory):
- `KafkaUITool-2.0.0.exe` - Windows installer with embedded JRE
- `KafkaUITool-2.0.0-portable.zip` - Portable version (requires Java 17+)

**Usage**:
```cmd
release.bat
```

**Requirements**:
- Java 17+ JDK (full JDK, not just JRE) with jpackage support
- Internet connection (for Maven dependencies)
- Windows 10+ (for PowerShell ZIP creation)

### 3. release.ps1 - PowerShell Release Script

**Purpose**: Same as release.bat but with enhanced PowerShell features.

**Features**:
- Colored output for better readability
- Enhanced error handling and validation
- Cleaner progress reporting

**Usage**:
```powershell
.\release.ps1
```

### 4. test-build-scripts.bat - Build Environment Test

**Purpose**: Validates that the build environment is properly configured.

**What it tests**:
- Java 17+ availability
- jpackage availability (for release scripts)
- Maven wrapper functionality
- Compilation process
- JAR packaging
- JAR content validation

**Usage**:
```cmd
test-build-scripts.bat
```

## Benefits of the Windows Executable

The Windows executable created by `release.bat` includes:

1. **Embedded JRE**: No need for users to install Java
2. **Native Windows Installer**: Professional installation experience
3. **Start Menu Integration**: Automatic shortcut creation
4. **Desktop Shortcut**: Optional desktop shortcut during installation
5. **Windows Registry Integration**: Proper Windows application registration
6. **Uninstall Support**: Standard Windows Add/Remove Programs integration

## Usage Examples

### First Time Setup
```cmd
# Test your environment first
test-build-scripts.bat

# If tests pass, proceed with development
build.bat
```

### For Developers
```cmd
# Quick build for testing
build.bat

# Run the application
cd kafka-ui-java
java -jar target\kafka-ui-tool-2.0.0-jar-with-dependencies.jar
```

### For Release Managers
```cmd
# Create full release packages
release.bat

# Or use PowerShell version for better output
release.ps1

# Distribute the files from the release\ directory
# - KafkaUITool-2.0.0.exe for end users (no Java required)
# - KafkaUITool-2.0.0-portable.zip for power users with Java
```

## Troubleshooting

### Common Issues

1. **"Java is not installed or not in PATH"**
   - Install Java 17+ from [Eclipse Temurin](https://adoptium.net/)
   - Ensure `java` command works in Command Prompt

2. **"jpackage is not available"**
   - Ensure you have a full JDK installation (not just JRE)
   - Some JDK distributions may not include jpackage

3. **"Maven build failed"**
   - Check internet connection for dependency downloads
   - Verify you're running from the correct directory

4. **Build succeeds but executable doesn't work**
   - Check Windows Defender or antivirus software
   - Try running as administrator

### Platform Notes

- These scripts are designed for Windows
- For Linux/macOS, use the Maven commands directly:
  ```bash
  cd kafka-ui-java
  ./mvnw clean package
  ```

## Integration with CI/CD

These scripts can be integrated into automated build pipelines:

```cmd
# In CI/CD pipeline
call build.bat
if %errorlevel% neq 0 exit /b 1

call release.bat
if %errorlevel% neq 0 exit /b 1

# Upload artifacts from release\ directory
```