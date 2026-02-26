@echo off
chcp 65001 >nul
echo ============================================
echo     Screen Cast Pro
echo ============================================
echo.

set JAVA_HOME=%JAVA_HOME%
if "%JAVA_HOME%"=="" (
    echo Error: JAVA_HOME not set
    echo Please install Java 17 or higher
    pause
    exit /b 1
)

echo Using Java: %JAVA_HOME%
echo.

set LIB_DIR=%~dp0target\lib
set JAR_FILE=%~dp0target\pc-client-1.0.0.jar

if not exist "%JAR_FILE%" (
    echo Error: Main jar not found: %JAR_FILE%
    echo Please run: mvn clean package
    pause
    exit /b 1
)

if not exist "%LIB_DIR%" (
    echo Error: Lib directory not found: %LIB_DIR%
    echo Please run: mvn clean package
    pause
    exit /b 1
)

echo Starting...
echo.

java -jar "%JAR_FILE%"

if errorlevel 1 (
    echo.
    echo Program exited with error
    pause
)
