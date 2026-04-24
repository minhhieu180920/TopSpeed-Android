#!/bin/bash

# Script deploy TopSpeed Android lên GitHub
# Chạy script này để khởi tạo và push code lên GitHub

echo "=========================================="
echo "TopSpeed Android - Deploy to GitHub"
echo "=========================================="

# Di chuyển vào thư mục project
cd "$(dirname "$0")"

# Kiểm tra Git đã cài chưa
if ! command -v git &> /dev/null; then
    echo "Error: Git chưa được cài đặt"
    echo "Tải Git tại: https://git-scm.com/download"
    exit 1
fi

# Kiểm tra đã có .git chưa
if [ -d ".git" ]; then
    echo "Đã có Git repository"
else
    echo "Khởi tạo Git repository..."
    git init
    git branch -M main
fi

# Kiểm tra remote
if git remote -v | grep -q "origin"; then
    echo "Remote 'origin' đã tồn tại"
else
    echo ""
    echo "Chưa có remote origin"
    echo "Vui lòng tạo repository trên GitHub trước:"
    echo "1. Vào https://github.com/new"
    echo "2. Tạo repository mới (VD: topspeed-android)"
    echo "3. Copy URL repository"
    echo ""
    read -p "Nhập GitHub repository URL: " repo_url

    if [ -z "$repo_url" ]; then
        echo "URL không hợp lệ"
        exit 1
    fi

    git remote add origin "$repo_url"
fi

# Tạo .gitignore
if [ ! -f ".gitignore" ]; then
    echo "Tạo .gitignore..."
    cat > .gitignore << 'EOF'
# Gradle
.gradle/
build/
!gradle/wrapper/gradle-wrapper.jar

# Android Studio
*.iml
.idea/
local.properties

# Build outputs
*.apk
*.aab
*.ap_
*.dex

# Generated files
gen/
out/

# Local config
local.properties
*.keystore
*.jks

# Misc
.DS_Store
Thumbs.db
*.log
EOF
fi

# Add all files
echo "Thêm files..."
git add .

# Kiểm tra có thay đổi không
if git diff --cached --quiet; then
    echo "Không có thay đổi để commit"
else
    # Commit
    echo ""
    read -p "Nhập commit message (Enter để dùng mặc định): " commit_msg
    if [ -z "$commit_msg" ]; then
        commit_msg="Initial commit - TopSpeed Audio Racing Android"
    fi

    git commit -m "$commit_msg"

    # Push
    echo ""
    echo "Đang push lên GitHub..."
    echo "Nếu lần đầu, bạn cần xác thực GitHub"

    git push -u origin main

    echo ""
    echo "=========================================="
    echo "Deploy thành công!"
    echo "=========================================="
    echo ""
    echo "GitHub Actions sẽ tự động build APK sau khi push"
    echo "Kiểm tra progress tại:"
    echo "  https://github.com/<username>/<repo>/actions"
    echo ""
fi
