# PowerShell script to start the runtime service with correct Java version
# Usage: .\start-service.ps1

$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.9.10-hotspot"

Write-Host "Starting Runtime Service..." -ForegroundColor Green
Write-Host "JAVA_HOME: $env:JAVA_HOME" -ForegroundColor Yellow
Write-Host "Java Version:" -ForegroundColor Yellow
& "$env:JAVA_HOME\bin\java.exe" -version

Write-Host "`nStarting Spring Boot application..." -ForegroundColor Green
mvn spring-boot:run




