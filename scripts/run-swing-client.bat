@echo off
title PawLink Swing Client
cd /d "%~dp0.."

if exist "%JAVA_HOME%\bin\javaw.exe" (
    set "JAVAW_BIN=%JAVA_HOME%\bin\javaw.exe"
) else if exist "C:\Program Files\Java\jdk-25.0.5\bin\javaw.exe" (
    set "JAVAW_BIN=C:\Program Files\Java\jdk-25.0.5\bin\javaw.exe"
) else if exist "C:\Program Files\Java\latest\jdk-25\bin\javaw.exe" (
    set "JAVAW_BIN=C:\Program Files\Java\latest\jdk-25\bin\javaw.exe"
) else if exist "C:\Program Files\Java\jdk-25\bin\javaw.exe" (
    set "JAVAW_BIN=C:\Program Files\Java\jdk-25\bin\javaw.exe"
) else if exist "C:\Program Files\Java\jdk-26.0.2\bin\javaw.exe" (
    set "JAVAW_BIN=C:\Program Files\Java\jdk-26.0.2\bin\javaw.exe"
) else if exist "C:\Program Files\Java\jdk-21\bin\javaw.exe" (
    set "JAVAW_BIN=C:\Program Files\Java\jdk-21\bin\javaw.exe"
) else (
    set "JAVAW_BIN=javaw"
)

if not exist "swing-vet\target\swing-vet-1.0.0.jar" (
    echo Packaging swing-vet client...
    call mvn package -pl swing-vet -DskipTests
)

start "" "%JAVAW_BIN%" -jar "swing-vet\target\swing-vet-1.0.0.jar"
