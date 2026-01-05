@echo off
REM Batch script to start the runtime service using the JAR file
REM Usage: start-service-jar.bat

set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.9.10-hotspot

echo Starting Runtime Service from JAR...
echo JAVA_HOME: %JAVA_HOME%
echo.
"%JAVA_HOME%\bin\java.exe" -jar target\runtime-service-1.0.0-SNAPSHOT.jar




