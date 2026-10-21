@echo off
setlocal
cd /d "%~dp0"
echo Starting Spring Boot Application...
echo ===================================
.\gradlew.bat bootRun
pause
