@echo off
title Faculty-Course Assignment System (FCAS)
echo ==============================================================================
echo    Starting Faculty-Course Assignment System (Modern Flat UI + SQLite)
echo ==============================================================================
javac -cp "lib/flatlaf.jar;lib/sqlite-jdbc.jar;src" -d bin src/com/fcas/*.java
if %errorlevel% neq 0 (
    echo Compilation failed!
    pause
    exit /b %errorlevel%
)
java -cp "bin;lib/flatlaf.jar;lib/sqlite-jdbc.jar" com.fcas.FCASAppGUI
pause
