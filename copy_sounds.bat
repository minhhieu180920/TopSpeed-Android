@echo off
echo ========================================
echo TopSpeed Audio - Copy Sounds Script
echo ========================================
echo.

set SRC=..\..\TopSpeed-h\TopSpeed-h\installer\tspeed\Sounds
set DEST=app\src\main\res\raw

echo Source: %SRC%
echo Destination: %DEST%
echo.

if not exist "%SRC%" (
    echo ERROR: Source folder not found!
    echo Please make sure TopSpeed-h is in the same directory
    pause
    exit /b 1
)

if not exist "%DEST%" (
    mkdir "%DEST%"
)

echo Copying sounds...
xcopy /E /I /Y "%SRC%\en" "%DEST%\en" 2>nul

echo.
echo ========================================
echo Copy complete!
echo.
echo Files in destination:
dir /b "%DEST%\en" 2>nul || echo   (empty)
echo ========================================
echo.
echo Next steps:
echo 1. Build project: gradlew assembleDebug
echo 2. Install APK: adb install app\build\outputs\apk\debug\app-debug.apk
echo.
pause
