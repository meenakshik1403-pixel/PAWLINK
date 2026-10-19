@echo off
title PawLink Swing Client (Debug Console)
cd /d "%~dp0"
set "JAVA_HOME=C:\Program Files\Java\jdk-21"
echo ========================================================
echo Starting PawLink Swing Desktop Client...
echo Connecting to Backend at: http://localhost:8080
echo ========================================================
"%JAVA_HOME%\bin\java.exe" -jar "swing-vet\target\swing-vet-1.0.0.jar"
pause
