@echo off
title PawLink Backend Server (Port 8080)
cd /d "%~dp0"
set "JAVA_HOME=C:\Program Files\Java\jdk-21"
mvn spring-boot:run -pl backend
pause
