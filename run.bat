@echo off
REM ---------------------------------------------------------------
REM  ChainTrace launcher (Windows)
REM  Needs: Java 21 and Maven on PATH. Java: https://adoptium.net  Maven: https://maven.apache.org
REM ---------------------------------------------------------------
cd /d "%~dp0"

where java >nul 2>nul
if errorlevel 1 (
    echo [ChainTrace] Java was not found. Install Java 21 first: https://adoptium.net
    pause
    exit /b 1
)

where mvn >nul 2>nul
if errorlevel 1 (
    echo [ChainTrace] Maven was not found. Install Maven first: https://maven.apache.org
    pause
    exit /b 1
)

echo [ChainTrace] Starting... open http://localhost:8080 once you see "Started ChainTraceApplication".
call mvn spring-boot:run
pause
