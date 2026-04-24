@echo off
chcp 65001 >nul
echo ==========================================
echo TopSpeed Android - Deploy to GitHub
echo ==========================================

REM Di chuyển vào thư mục script
cd /d "%~dp0"

REM Kiểm tra Git
where git >nul 2>&1
if %errorlevel% neq 0 (
    echo Error: Git chua duoc cai dat
    echo Tai Git tai: https://git-scm.com/download
    pause
    exit /b 1
)

REM Kiểm tra đã có .git chưa
if exist ".git" (
    echo Da co Git repository
) else (
    echo Khoi tao Git repository...
    git init
    git branch -M main
)

REM Kiểm tra remote
git remote -v | findstr "origin" >nul
if %errorlevel% equ 0 (
    echo Remote 'origin' da ton tai
) else (
    echo.
    echo Chua co remote origin
    echo Vui long tao repository tren GitHub truoc:
    echo 1. Vao https://github.com/new
    echo 2. Tao repository moi (VD: topspeed-android)
    echo 3. Copy URL repository
    echo.
    set /p repo_url="Nhap GitHub repository URL: "

    if "%repo_url%"=="" (
        echo URL khong hop le
        pause
        exit /b 1
    )

    git remote add origin %repo_url%
)

REM Tạo .gitignore
if not exist ".gitignore" (
    echo Tao .gitignore...
    (
        echo # Gradle
        echo .gradle/
        echo build/
        echo !gradle/wrapper/gradle-wrapper.jar
        echo.
        echo # Android Studio
        echo *.iml
        echo .idea/
        echo local.properties
        echo.
        echo # Build outputs
        echo *.apk
        echo *.aab
        echo.
        echo # Generated files
        echo gen/
        echo out/
        echo.
        echo # Local config
        echo local.properties
        echo *.keystore
        echo.
        echo # Misc
        echo .DS_Store
    ) > .gitignore
)

REM Add all files
echo Dang them files...
git add .

REM Kiểm tra có thay đổi không
git diff --cached --quiet
if %errorlevel% equ 0 (
    echo Khong co thay doi de commit
) else (
    echo.
    set /p commit_msg="Nhap commit message (Enter de dung mac dinh): "

    if "%commit_msg%"=="" (
        set commit_msg=Update - TopSpeed Audio Racing Android
    )

    git commit -m "%commit_msg%"

    REM Push
    echo.
    echo Dang push len GitHub...
    echo Neu lan dau, ban can xac thuc GitHub

    git push -u origin main

    echo.
    echo ==========================================
    echo Deploy thanh cong!
    echo ==========================================
    echo.
    echo GitHub Actions se tu dong build APK sau khi push
    echo Kiem tra progress tai:
    echo   https://github.com/
)

echo.
pause
