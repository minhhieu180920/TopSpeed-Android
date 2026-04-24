# Hướng dẫn Build TopSpeed Audio Racing APK

## Yêu cầu hệ thống

1. **Java JDK 17** hoặc cao hơn
   - Download: https://adoptium.net/
   - Set JAVA_HOME: `set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17`

2. **Android SDK**
   - Download Android Studio: https://developer.android.com/studio
   - Hoặc command-line tools: https://developer.android.com/studio#command-line-tools-only

3. **NDK** (cho native C++ code)
   - Cài qua Android Studio SDK Manager
   - Hoặc download: https://developer.android.com/ndk/downloads

## Cách 1: Build bằng Android Studio (Khuyến nghị)

1. **Mở project**
   ```
   File > Open > Chọn thư mục TopSpeed-Android
   ```

2. **Sync Gradle**
   - Android Studio sẽ tự động sync
   - Hoặc: Tools > Gradle > Refresh Gradle Project

3. **Build APK**
   ```
   Build > Build Bundle(s) / APK(s) > Build APK(s)
   ```

4. **APK sẽ ở đâu?**
   ```
   app/build/outputs/apk/debug/app-debug.apk
   ```

## Cách 2: Build bằng Command Line

### Bước 1: Cài Gradle (nếu chưa có)

```bash
# Windows (dùng Chocolatey)
choco install gradle

# Hoặc download trực tiếp
# https://gradle.org/releases/
# Giải nén và thêm vào PATH
```

### Bước 2: Build

```bash
# Di chuyển vào thư mục project
cd TopSpeed-Android

# Build debug APK
gradle assembleDebug

# Hoặc nếu dùng wrapper
gradlew assembleDebug
```

### Bước 3: Cài đặt APK

```bash
# Qua ADB
adb install app/build/outputs/apk/debug/app-debug.apk

# Hoặc copy file APK vào điện thoại và cài thủ công
```

## Giải quyết vấn đề thường gặp

### Lỗi "JAVA_HOME not set"

```bash
# Windows
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.5

# Linux/Mac
export JAVA_HOME=/usr/lib/jvm/java-17
```

### Lỗi "Android SDK not found"

```bash
# Set ANDROID_HOME
# Windows
set ANDROID_HOME=C:\Users\YourName\AppData\Local\Android\Sdk

# Linux/Mac
export ANDROID_HOME=$HOME/Android/Sdk
```

### Lỗi NDK not found

```bash
# Cài NDK qua sdkmanager
sdkmanager "ndk;25.2.9519663"

# Hoặc set NDK_HOME
set NDK_HOME=C:\Users\YourName\AppData\Local\Android\Sdk\ndk\25.2.9519663
```

### Lỗi "Unsupported Java version"

Gradle 8.2 yêu cầu JDK 17+. Kiểm tra:

```bash
java -version
# Phải là 17 hoặc cao hơn
```

## Cấu trúc thư mục sau khi build thành công

```
TopSpeed-Android/
├── app/
│   ├── build/
│   │   ├── outputs/
│   │   │   ├── apk/
│   │   │   │   ├── debug/
│   │   │   │   │   └── app-debug.apk  ← FILE APK
│   │   │   │   └── release/
│   │   │   │       └── app-release.apk
│   │   └── intermediates/
│   └── src/
└── gradle/
```

## Tiếp theo

Sau khi build thành công:
1. Cài APK lên điện thoại Android
2. Bật điện thoại ở chế độ ngang (landscape)
3. Mở app và làm theo hướng dẫn bằng giọng nói

## Nếu muốn chỉnh sửa code

Các file quan trọng:
- `TopSpeedActivity.kt` - Activity chính, xử lý touch và tilt
- `GameLoop.kt` - Game loop và logic game
- `CarModel.kt` - Vật lý xe
- `TrackModel.kt` - Đường đua
- `AudioEngine.kt` - Quản lý âm thanh
- `TTSManager.kt` - Google TTS

## Lấy APK từ điện thoại đã cài

```bash
adb pull /data/app/com.topspeed.audio/base.apk ./TopSpeed-debug.apk
```
