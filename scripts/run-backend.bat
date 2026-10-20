@echo off
title PawLink Backend Server (Port 8080)
cd /d "%~dp0.."

if exist "%JAVA_HOME%\bin\java.exe" (
    set "PATH=%JAVA_HOME%\bin;%PATH%"
) else if exist "C:\Program Files\Java\jdk-25\bin\java.exe" (
    set "JAVA_HOME=C:\Program Files\Java\jdk-25"
    set "PATH=%JAVA_HOME%\bin;%PATH%"
) else if exist "C:\Program Files\Java\jdk-26.0.2\bin\java.exe" (
    set "JAVA_HOME=C:\Program Files\Java\jdk-26.0.2"
    set "PATH=%JAVA_HOME%\bin;%PATH%"
) else if exist "C:\Program Files\Java\jdk-21\bin\java.exe" (
    set "JAVA_HOME=C:\Program Files\Java\jdk-21"
    set "PATH=%JAVA_HOME%\bin;%PATH%"
)

REM Load environment variables from .env if present
if exist ".env" (
    echo [PawLink] Loading environment configuration from .env...
    for /f "usebackq eol=# tokens=1* delims==" %%A in (".env") do (
        if not "%%A"=="" (
            set "%%A=%%B"
        )
    )
)

echo Starting PawLink Backend on port 8080 with Java (JDK 25 LTS)...
mvn spring-boot:run -pl backend
pause
