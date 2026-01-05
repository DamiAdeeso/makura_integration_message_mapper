@echo off
REM Batch script to start the runtime service with correct Java version
REM Usage: start-service.bat

set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.9.10-hotspot

echo Starting Runtime Service...
echo JAVA_HOME: %JAVA_HOME%
echo.
echo Starting Spring Boot application...
call mvn spring-boot:run




