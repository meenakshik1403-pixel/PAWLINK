@echo off
title PawLink Swing Client (Debug Console)
cd /d "%~dp0.."

if exist "%JAVA_HOME%\bin\java.exe" (
    set "JAVA_BIN=%JAVA_HOME%\bin\java.exe"
) else if exist "C:\Program Files\Java\jdk-25.0.5\bin\java.exe" (
    set "JAVA_BIN=C:\Program Files\Java\jdk-25.0.5\bin\java.exe"
) else if exist "C:\Program Files\Java\latest\jdk-25\bin\java.exe" (
    set "JAVA_BIN=C:\Program Files\Java\latest\jdk-25\bin\java.exe"
) else if exist "C:\Program Files\Java\jdk-25\bin\java.exe" (
    set "JAVA_BIN=C:\Program Files\Java\jdk-25\bin\java.exe"
) else if exist "C:\Program Files\Java\jdk-26.0.2\bin\java.exe" (
    set "JAVA_BIN=C:\Program Files\Java\jdk-26.0.2\bin\java.exe"
) else if exist "C:\Program Files\Java\jdk-21\bin\java.exe" (
    set "JAVA_BIN=C:\Program Files\Java\jdk-21\bin\java.exe"
) else (
    set "JAVA_BIN=java"
)

if not exist "swing-client\target\swing-client-1.0.0.jar" (
    echo Packaging swing-client...
    call mvn package -pl swing-client -DskipTests
)

echo ========================================================
echo Starting PawLink Swing Desktop Client (JDK 25 LTS)...
echo Connecting to Backend at: http://localhost:8080
echo ========================================================
"%JAVA_BIN%" -jar "swing-client\target\swing-client-1.0.0.jar"
pause
