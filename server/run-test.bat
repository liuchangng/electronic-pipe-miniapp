@echo off
set JAVA_HOME=D:\Software\Java\jdk-26.0.2.1
set PATH=%JAVA_HOME%\bin;%PATH%
cd /d %~dp0
mvnw.cmd test