@echo off
cd /d "%~dp0"
set "JAVA_HOME=D:\Software\Java\jdk-26.0.2.1"
set "PATH=D:\Software\maven-3.8.3\bin;%JAVA_HOME%\bin;%PATH%"
echo Starting Spring Boot Server...
echo JAVA_HOME: %JAVA_HOME%
echo.
mvn spring-boot:run
pause
