@echo off
title PawLink Swing Client
cd /d "%~dp0"

if exist "C:\Program Files\Java\jdk-21\bin\javaw.exe" (
    start "" "C:\Program Files\Java\jdk-21\bin\javaw.exe" -jar "swing-vet\target\swing-vet-1.0.0.jar"
) else (
    start "" javaw -jar "swing-vet\target\swing-vet-1.0.0.jar"
)
