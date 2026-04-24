# TopSpeed Audio Racing - Android Version

Game đua xe audio cho người khiếm thị, port từ Top Speed 3 (GPL v3) của Playing in the Dark.

## Tính năng

- 🎮 Game đua xe audio cho người khiếm thị
- 🔊 Âm thanh 3D spatial
- 🎙️ Google TTS đọc menu và thông tin race
- 📱 Điều khiển bằng cử chỉ và nghiêng điện thoại
- 🌐 Multiplayer online
- ♿ Hỗ trợ TalkBack

## Điều khiển

### Menu chính (5 mục)
| Cử chỉ | Chức năng |
|---------|-----------|
| Vuốt trái | Mục trước |
| Vuốt phải | Mục tiếp |
| Vuốt lên | Chọn |

### Trong Race
| Cử chỉ | Chức năng |
|---------|-----------|
| Nghiêng điện thoại trái/phải | Rẽ trái/phải |
| Vuốt lên | Tăng tốc |
| Vuốt xuống | Phanh |
| Chạm đúp bên trái | Đề máy / Tắt máy |

### Toàn cục
| Cử chỉ | Chức năng |
|---------|-----------|
| Chạm 2 ngón | Tạm dừng |
| Vuốt 3 ngón lên | Mở menu |
| Giữ lâu | Đọc lại thông tin |

## Yêu cầu

- Android 7.0 (API 24) trở lên
- Accelerometer sensor
- Google TTS installed

## Build

### 1. Copy âm thanh từ game gốc

```bash
# Chạy script copy
copy_sounds.bat
```

### 2. Build APK

```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease
```

### 3. Cài đặt

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

## Multiplayer Server

Server đang chạy: `https://topspeedapi-production.up.railway.app`

### API Endpoints
| Method | Endpoint | Mô tả |
|--------|----------|--------|
| GET | `/api/topspeed/ping` | Kiểm tra server |
| POST | `/api/topspeed/room/create` | Tạo phòng mới |
| POST | `/api/topspeed/room/join` | Vào phòng |
| POST | `/api/topspeed/room/leave` | Rời phòng |
| GET | `/api/topspeed/room/list` | Danh sách phòng |
| POST | `/api/topspeed/race/start` | Bắt đầu đua |

## GitHub Actions (Auto Build)

Mỗi khi push code lên `main`, APK sẽ được build tự động:

1. **Debug APK** - Build và upload (giữ 7 ngày)
2. **Release APK** - Build và tạo Release mới (giữ 30 ngày)

### Cách sử dụng
1. Fork repository này
2. Thêm keystore secrets trong Settings > Secrets:
   - `KEYSTORE_FILE` - Base64 encoded keystore
   - `KEYSTORE_PASSWORD` - Password của keystore
   - `KEY_ALIAS` - Alias của key
   - `KEY_PASSWORD` - Password của key
3. Push code lên main branch
4. APK sẽ tự động được build

## Multiplayer Server

Server đang chạy: `https://topspeedapi-production.up.railway.app`

### API Endpoints
| Method | Endpoint | Mô tả |
|--------|----------|--------|
| GET | `/api/topspeed/ping` | Kiểm tra server |
| POST | `/api/topspeed/room/create` | Tạo phòng mới |
| POST | `/api/topspeed/room/join` | Vào phòng |
| POST | `/api/topspeed/room/leave` | Rời phòng |
| GET | `/api/topspeed/room/list` | Danh sách phòng |
| POST | `/api/topspeed/race/start` | Bắt đầu đua |

## GitHub Actions (Auto Build)

Mỗi khi push code lên `main`, APK sẽ được build tự động:

1. **Debug APK** - Build và upload (giữ 7 ngày)
2. **Release APK** - Build và tạo Release mới (giữ 30 ngày)

### Cách sử dụng
1. Fork repository này
2. Thêm keystore secrets trong Settings > Secrets:
   - `KEYSTORE_FILE` - Base64 encoded keystore
   - `KEYSTORE_PASSWORD` - Password của keystore
   - `KEY_ALIAS` - Alias của key
   - `KEY_PASSWORD` - Password của key
3. Push code lên main branch
4. APK sẽ tự động được build

## Cấu trúc project

```
TopSpeed-Android/
├── app/
│   ├── src/main/
│   │   ├── java/com/topspeed/audio/
│   │   │   ├── TopSpeedActivity.kt     # Activity chính
│   │   │   ├── TopSpeedApplication.kt  # Application class
│   │   │   ├── game/                   # Game logic
│   │   │   ├── audio/                  # Audio system (TTS, SoundPool)
│   │   │   ├── input/                  # Gesture & tilt controls
│   │   │   ├── car/                    # Car model
│   │   │   ├── track/                  # Track model
│   │   │   └── accessibility/          # Accessibility service
│   │   ├── cpp/                        # Native C++ code (NDK)
│   │   │   ├── game_logic/             # Car, Track, RaceLogic
│   │   │   └── android_audio/         # Native audio engine
│   │   └── res/
│   │       └── raw/                    # Audio files
│   └── build.gradle
└── settings.gradle
```

## Kiến trúc

### Audio System
- **TTSManager**: Google TTS cho menu và thông tin
- **AudioEngine**: SoundPool cho SFX
- **VoiceAnnouncer**: Đọc race info

### Input System
- **GestureController**: Xử lý cử chỉ touch
- **TiltController**: Xử lý accelerometer

### Game Logic
- **GameLoop**: Game loop 60 FPS
- **CarModel**: Vật lý xe
- **TrackModel**: Đường đua và features

## Nguồn game gốc

- **Top Speed 3** by Playing in the Dark
- **License**: GNU GPL v3
- **Source**: http://playinginthedark.net

## License

Game này được phân phối theo GNU GPL v3. Code C++ port được giữ nguyên license từ game gốc.

---

Build with ❤️ for accessibility
